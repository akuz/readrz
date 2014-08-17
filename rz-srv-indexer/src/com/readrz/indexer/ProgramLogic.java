package com.readrz.indexer;

import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.akuz.core.DateUtils;
import me.akuz.core.Out;
import me.akuz.core.UtcDate;
import me.akuz.core.logs.LogUtils;
import me.akuz.core.logs.ManualResetLogManager;
import me.akuz.nlp.detect.SentencesDetector;
import me.akuz.nlp.detect.WordsDetector;
import me.akuz.nlp.porter.PorterStemmer;
import me.akuz.nlp.porter.PorterStopWords;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.readrz.data.index.KeysIndex;
import com.readrz.data.mongo.MongoColls;
import com.readrz.data.ontology.Ontology;
import com.readrz.data.ontology.TopicModel;
import com.readrz.lang.parse.Const;
import com.readrz.lang.parse.PatternsDetector;
import com.readrz.lang.parse.SnapsParser;
import com.readrz.math.topicdetect.TopicsDetector;

public final class ProgramLogic {
	
	private final Logger _log;
	private volatile boolean _stopRequested;
	
	public ProgramLogic() {
		_log = LogUtils.getLogger(this.getClass().getName());
	}

	public void execute(
			String  mngServer, 
			Integer mngPort, 
			String  mngDb,
			IdxOptions opts) throws UnknownHostException, FileNotFoundException {
		
		PorterStemmer porterStemmer = new PorterStemmer("_");

		_log.info("Connecting to Mongo DB...");
		MongoClient mongoClient = new MongoClient(mngServer, mngPort);
		DB db = mongoClient.getDB(mngDb);
		
		try {

			final Out<IdxCall> startupCallHandle = new Out<IdxCall>(null);
			final Out<IdxCall> lastCallHandle = new Out<IdxCall>(null);
			
			Runtime.getRuntime().addShutdownHook(new Thread() {
			    public void run() {
			    	
			    	try {
						_log.info("Shutting down...");
						_stopRequested = true;
						
						IdxCall startupCall = startupCallHandle.getValue();
						if (startupCall != null) {
							if (startupCall.isStopped() == false) {
								_log.info("Asking startup call to stop...");
								startupCall.stop();
							}
						}
						
						IdxCall lastCall = lastCallHandle.getValue();
						if (lastCall != null) {
							if (lastCall.isStopped() == false) {
								_log.info("Asking last call to stop...");
								lastCall.stop();
							}
						}
						
						_log.info("Shut down complete.");
						
			    	} finally {
			    		
			    		ManualResetLogManager.resetFinally();
			    	}
			    }
			 });
			
			Set<String> stopStems = null;
			if (opts.getStopWordsFile() != null) {
				_log.info("Loading stop words...");
				stopStems = PorterStopWords.loadStopWordsAndStemThem(porterStemmer, opts.getStopWordsFile());
			} else {
				_log.warning("Stop words file not specified.");
			}
			
			_log.info("Loading keys index...");
			DBCollection keysColl = db.getCollection(MongoColls.keys);
			KeysIndex keysIndex = new KeysIndex(keysColl);
			keysIndex.loadFromDB();
			Set<Integer> stopKeyIds = keysIndex.getKeyIds(stopStems);
			
			_log.info("Loading ontology...");
			Ontology ontology = new Ontology(keysIndex);
			ontology.loadFromDB(db);
			
			_log.info("Creating sentences detector...");
			SentencesDetector sentencesDetector = new SentencesDetector();
			
			_log.info("Creating pattern entities detector...");
			PatternsDetector patternEntitiesDetector = new PatternsDetector(ontology.getEntityListCatalog().getLists());
			
			_log.info("Creating words parser...");
			WordsDetector wordsParser = new WordsDetector(porterStemmer);
			
			_log.info("Creating topic model...");
			TopicModel topicModel = new TopicModel(ontology.getEntityListCatalog().getLists(), keysIndex);

			_log.info("Creating topics detector...");
			TopicsDetector topicsDetector = new TopicsDetector(
					topicModel, 
					keysIndex, 
					stopKeyIds, 
					Const.TOPICS_DETECTOR_CONSIDER_TOPIC_COUNT_PER_PLACE,
					Const.TOPICS_DETECTOR_IGNORE_TOPIC_INDEX, 
					Const.TOPICS_DETECTOR_EXPECTED_DOC_WORDS_THRESHOLD, 
					Const.TOPICS_DETECTOR_EXPECTED_SENTENCE_WORDS_THRESHOLD);
			
			_log.info("Creating snaps parser...");
			SnapsParser snapsParser = new SnapsParser(keysIndex, sentencesDetector, patternEntitiesDetector, wordsParser);
			
			_log.info("Getting db collections for indexing...");
			DBCollection snapsColl = db.getCollection(MongoColls.snaps);
			DBCollection snapsidxColl = db.getCollection(MongoColls.snapsidx);
			
			// perform scan on startup, if asked for
			if (opts.getStartupFromDate() != null) {

				Date endDate = new Date();
				Date startDate = opts.getStartupFromDate();
				
				_log.info("Indexing on startup from " + new UtcDate(startDate));
				
				IdxCall startupCall = new IdxCall(
						snapsParser,
						topicsDetector,
						snapsColl, 
						snapsidxColl,
						opts.getStartupRedoAll(), 
						startDate, 
						endDate);
				
				startupCallHandle.setValue(startupCall);
				startupCall.call();
			}
			
			Date lastCheckDate = null;
			while (_stopRequested == false) {
				
				if (opts.getLiveFreqMins() <= 0) {
					_log.info("Not starting live indexing checking, bacause live freq argument is zero.");
					break;
				}
				
				if (lastCheckDate != null) {
					
					try { // wait
						
						while (_stopRequested == false) {
							
							Date now = new Date();
							if (DateUtils.minutesBetween(lastCheckDate, now) > opts.getLiveFreqMins()) {
								break;
							}
							
							Thread.sleep(1000);
						}
						
					} catch (InterruptedException e) {
						e.printStackTrace();
						_log.warning("Indexing thread interrupted while waiting for next check time");
						break;
					}
				}
				
				if (_stopRequested) {
					break;
				}

				try {
					_log.info("Indexing live (with frequency of " + opts.getLiveFreqMins() + " mins)...");

					lastCheckDate = new Date();

					Date endDate = new Date();
					Date startDate = DateUtils.addMinutes(endDate, - opts.getLivePeriodMins());
					
					IdxCall liveCall = new IdxCall(
							snapsParser,
							topicsDetector,
							snapsColl, 
							snapsidxColl,
							false, 
							startDate, 
							endDate);
					
					lastCallHandle.setValue(liveCall);
					liveCall.call();
					
				} catch (Exception e) {
					_log.log(Level.SEVERE, "Exception while indexing, will retry in " + opts.getLiveFreqMins() + " mins", e);
				}
			}
		
		} catch (Exception e) {
			
			_log.log(Level.SEVERE, "Error in indexer, will stop...", e);
			
		} finally {
			
			mongoClient.close();

			_log.info("Exit indexing.");
		}
	}
}
