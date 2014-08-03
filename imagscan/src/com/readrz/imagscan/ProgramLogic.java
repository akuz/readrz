package com.readrz.imagscan;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import me.akuz.core.DateUtils;
import me.akuz.core.FileUtils;
import me.akuz.core.Out;
import me.akuz.core.UtcDate;
import me.akuz.core.logs.LogUtils;
import me.akuz.core.logs.ManualResetLogManager;
import me.akuz.nlp.porter.PorterStemmer;
import me.akuz.nlp.porter.PorterStopWords;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.readrz.data.mongo.MongoColls;

public final class ProgramLogic {
	
	private static final Logger _log = LogUtils.getLogger(ProgramLogic.class.getName());
	private volatile boolean _stopRequested;
	
	public ProgramLogic() {
	}

	public void execute(
			String  mngServer, 
			Integer mngPort, 
			String  mngDb,
			ProgramOptions options) throws IOException {

		PorterStemmer porterStemmer = new PorterStemmer("_");
		
		Pattern blockedUrlsPattern = null;
		if (options.getBlockedUrlsFile() != null) {
			_log.info("Loading blocked urls...");
			List<String> patterns = FileUtils.readLinesNoComments(options.getBlockedUrlsFile());
			if (patterns.size() > 0) {
				StringBuilder sb = new StringBuilder();
				for (int i=0; i<patterns.size(); i++) {
					String pattern = patterns.get(i);
					if (sb.length() > 0) {
						sb.append("|");
					}
					sb.append("(?:");
					sb.append(pattern);
					sb.append(")");
				}
				blockedUrlsPattern = Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE);
			}
		} else {
			_log.warning("Blocked urls file not specified");
		}
		
		Set<String> stopStems = null;
		if (options.getStopWordsFile() != null) {
			_log.info("Loading stop words...");
			stopStems = PorterStopWords.loadStopWordsAndStemThem(porterStemmer, options.getStopWordsFile());
		} else {
			_log.warning("Stop words file not specified");
		}

		_log.info("Connecting to db...");
		MongoClient mongoClient = new MongoClient(mngServer, mngPort);
		DB db = mongoClient.getDB(mngDb);
		
		try {
			
			_log.info("Getting db collections...");
			DBCollection snapsColl = db.getCollection(MongoColls.snaps);
			DBCollection snapsHtmlColl = db.getCollection(MongoColls.snapshtml);
			DBCollection snapsImagColl = db.getCollection(MongoColls.snapsimag);
			DBCollection snapsThumbColl = db.getCollection(MongoColls.snapsthumb);

			final Out<ScanSubmitCall> startupCallHandle = new Out<ScanSubmitCall>(null);
			final Out<ScanSubmitCall> liveCallHandle = new Out<ScanSubmitCall>(null);
			final ScanEngine scanEngine = new ScanEngine(
					porterStemmer,
					stopStems,
					blockedUrlsPattern,
					snapsColl, 
					snapsHtmlColl, 
					snapsImagColl, 
					snapsThumbColl,
					options.getThreadCount());
			
			Runtime.getRuntime().addShutdownHook(new Thread() {
			    public void run() {
			    	
			    	try {
			    		_log.info("Shutting down...");
						_stopRequested = true;
						
						ScanSubmitCall startupCall = startupCallHandle.getValue();
						if (startupCall != null) {
							_log.info("Stopping startup call...");
							startupCall.stop();
						}
						
						ScanSubmitCall lastCall = liveCallHandle.getValue();
						if (lastCall != null) {
							_log.info("Stopping live call...");
							lastCall.stop();
						}
						
						if (scanEngine != null) {
							_log.info("Stopping scan engine...");
							scanEngine.stop();
						}
						
						_log.info("Shut down complete.");
						
			    	} finally {
			    		ManualResetLogManager.resetFinally();
			    	}
			    }
			});
			
			// perform scan on startup, if asked for
			if (options.getStartupFromDate() != null) {

				Date endDate = new Date();
				Date startDate = options.getStartupFromDate();
				
				_log.info("Checking for scans on startup, from " + new UtcDate(startDate));
				ScanSubmitCall startupCall = new ScanSubmitCall(
						scanEngine, 
						snapsColl,
						options.getStartupRedoAll(), 
						startDate, 
						endDate);
				
				startupCallHandle.setValue(startupCall);
				startupCall.call();
				startupCallHandle.setValue(null);
				
				// wait for startup call to complete
				CountDownLatch startupCallCompleted = new CountDownLatch(1);
				scanEngine.addIdleLatch(startupCallCompleted);
				try { 
					_log.info("Waiting for startup call to complete...");
					startupCallCompleted.await();
					_log.info("Startup call completed.");
				} catch (InterruptedException e) {
					_log.warning("Interrupted while waiting for startup to complete");
				}
			}
			
			Date lastCheckDate = null;
			while (_stopRequested == false) {
				
				if (options.getLiveFreqMins() <= 0) {
					_log.info("Not starting live scans checking, bacause live freq argument is zero.");
					break;
				}
				
				if (lastCheckDate != null) {
					
					try { // wait
						
						while (_stopRequested == false) {
							
							Date now = new Date();
							if (DateUtils.minutesBetween(lastCheckDate, now) > options.getLiveFreqMins()) {
								break;
							}
							
							Thread.sleep(1000);
						}
						
					} catch (InterruptedException e) {
						_log.warning("Main thread interrupted while waiting for next check time, will stop...");
						_stopRequested = true;
						break;
					}
				}
				
				if (_stopRequested) {
					break;
				}

				try {
					_log.info("Checking for live scans (with frequency of " + options.getLiveFreqMins() + " mins)...");

					lastCheckDate = new Date();

					Date endDate = new Date();
					Date startDate = DateUtils.addMins(endDate, - options.getLivePeriodMins());
					
					ScanSubmitCall liveCall = new ScanSubmitCall(
							scanEngine,
							snapsColl,
							false, 
							startDate, 
							endDate);
					
					liveCallHandle.setValue(liveCall);
					liveCall.call();
					liveCallHandle.setValue(null);

					// wait for live call to complete
					CountDownLatch liveCallCompleted = new CountDownLatch(1);
					scanEngine.addIdleLatch(liveCallCompleted);
					try { 
						_log.info("Waiting for live call to complete...");
						liveCallCompleted.await();
						_log.info("Live call completed.");
						
					} catch (InterruptedException e) {
						_log.info("Interrupted while waiting for live call to complete");
					}
					
				} catch (Exception e) {
					
					_log.log(Level.SEVERE, "Exception while checking for live scans, will retry in " + options.getLiveFreqMins() + " min", e);
				}
			}
			
		} finally {
			
			mongoClient.close();
		}
		_log.info("Exit main thread.");
	}
}
