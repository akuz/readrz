package com.readrz.summr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import me.akuz.core.ArrayUtils;
import me.akuz.core.DateUtils;
import me.akuz.core.Pair;
import me.akuz.core.Triple;
import me.akuz.core.logs.LogUtils;
import me.akuz.nlp.porter.PorterStemmer;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.readrz.data.AliveStatus;
import com.readrz.data.Period;
import com.readrz.data.SummId;
import com.readrz.data.SummIdBuilder;
import com.readrz.data.SummKind;
import com.readrz.data.SummRequest;
import com.readrz.data.SummResult;
import com.readrz.data.index.KeysIndex;
import com.readrz.data.mongo.MongoColls;
import com.readrz.data.ontology.Ontology;
import com.readrz.data.ontology.TopicModel;
import com.readrz.data.ontology.TopicModelKeyIds;
import com.readrz.search.QueryParser;
import com.readrz.search.SnapCache;
import com.readrz.search.SnapSearcher;

/**
 * Feed scanner that performs periodic scanning of recent posts,
 * saves new posts as they are downloaded, and notifies the 
 * listeners when this happens (new posts appear).
 *
 */
public final class SummEngine implements Runnable {
	
	private static final Logger _log = LogUtils.getLogger(SummEngine.class.getName());

	private static final int CACHE_CLEAN_FREQ_MINS = 10;
	private static final int CACHE_KEEP_DAYS_COUNT = 3;
	
	private static final List<Integer> _keepAliveKindIds;
	private static final List<Integer> _keepAlivePeriodIds;
	private static final List<String>  _keepAliveGroupIds;

	static {
		
		_keepAliveKindIds = new ArrayList<>();
		_keepAliveKindIds.add(SummKind.Menu.getId());
		_keepAliveKindIds.add(SummKind.List.getId());
		
		_keepAlivePeriodIds = new ArrayList<>();
		_keepAlivePeriodIds.add(Period.Id4h);
		_keepAlivePeriodIds.add(Period.Id1d);
		//_keepAlivePeriodIds.add(Period.Id3d);
		
		_keepAliveGroupIds = new ArrayList<>();
		
		_keepAliveGroupIds.add("g/topics/business");
//		_keepAliveGroupIds.add("g/topics/business/sectors");
//		_keepAliveGroupIds.add("g/topics/business/sectors/technology");
//		_keepAliveGroupIds.add("g/topics/business/sectors/technology/areas");
//		_keepAliveGroupIds.add("g/topics/business/events");
		_keepAliveGroupIds.add("g/topics/disasters");
//		_keepAliveGroupIds.add("g/topics/disasters/natural");
//		_keepAliveGroupIds.add("g/topics/disasters/technological");
//		_keepAliveGroupIds.add("g/topics/disasters/transportation");
		_keepAliveGroupIds.add("g/topics/entertainment");
//		_keepAliveGroupIds.add("g/topics/entertainment/celebrities");
//		_keepAliveGroupIds.add("g/topics/entertainment/lifestyle");
//		_keepAliveGroupIds.add("g/topics/entertainment/media");
		_keepAliveGroupIds.add("g/topics/finance");
//		_keepAliveGroupIds.add("g/topics/finance/economy");
//		_keepAliveGroupIds.add("g/topics/finance/events");
//		_keepAliveGroupIds.add("g/topics/finance/sectors");
//		_keepAliveGroupIds.add("g/topics/finance/stocks");
		_keepAliveGroupIds.add("g/topics/government");
//		_keepAliveGroupIds.add("g/topics/government/sectors");
//		_keepAliveGroupIds.add("g/topics/government/politics");
		_keepAliveGroupIds.add("g/topics/science");
//		_keepAliveGroupIds.add("g/topics/science/sectors");
		_keepAliveGroupIds.add("g/topics/society");
//		_keepAliveGroupIds.add("g/topics/society/conflicts");
//		_keepAliveGroupIds.add("g/topics/society/crime");
//		_keepAliveGroupIds.add("g/topics/society/issues");
		_keepAliveGroupIds.add("g/topics/sports");
		
		_keepAliveGroupIds.add("g/companies");
//		_keepAliveGroupIds.add("g/companies/stock_index");
//		_keepAliveGroupIds.add("g/companies/stock_index/ftse100");
//		_keepAliveGroupIds.add("g/companies/stock_index/ftse100/sector");
//		_keepAliveGroupIds.add("g/companies/stock_index/nasdaq100");
//		_keepAliveGroupIds.add("g/companies/stock_index/nasdaq100/sector");
		_keepAliveGroupIds.add("g/countries");
//		_keepAliveGroupIds.add("g/countries/economic_group");
//		_keepAliveGroupIds.add("g/countries/economic_group/brics");
//		_keepAliveGroupIds.add("g/countries/economic_group/g20");
//		_keepAliveGroupIds.add("g/countries/economic_group/g8");
//		_keepAliveGroupIds.add("g/countries/geopolitics");
//		_keepAliveGroupIds.add("g/countries/geopolitics/middle_east");
//		_keepAliveGroupIds.add("g/countries/geopolitics/greater_middle_east");
//		_keepAliveGroupIds.add("g/countries/un_region");
//		_keepAliveGroupIds.add("g/countries/un_region/africa");
//		_keepAliveGroupIds.add("g/countries/un_region/africa/un_subregion");
//		_keepAliveGroupIds.add("g/countries/un_region/americas");
//		_keepAliveGroupIds.add("g/countries/un_region/americas/un_subregion");
//		_keepAliveGroupIds.add("g/countries/un_region/antarctica");
//		_keepAliveGroupIds.add("g/countries/un_region/asia");
//		_keepAliveGroupIds.add("g/countries/un_region/asia/un_subregion");
//		_keepAliveGroupIds.add("g/countries/un_region/europe");
//		_keepAliveGroupIds.add("g/countries/un_region/europe/geopolitics");
//		_keepAliveGroupIds.add("g/countries/un_region/europe/un_subregion");
//		_keepAliveGroupIds.add("g/countries/un_region/oceania");
//		_keepAliveGroupIds.add("g/countries/un_region/oceania/un_subregion");
	}
	
	private final String _thisClassName;
	private volatile boolean _stopRequested;
	private final CountDownLatch _stopped;
	
	private final DB _db;
	private final DBCollection _keysColl;
	private final DBCollection _summColl;
	private final DBCollection _summreqColl;
	private final KeysIndex _keysIndex;
	private final Set<Integer> _stopKeyIds;
	private final Ontology _ontology;
	private final TopicModel _topicModel;
	private final TopicModelKeyIds _topicModelKeyIds;
	private final QueryParser _queryParser;
	private final SnapSearcher _snapSearcher;
	private final SnapCache _snapCache;

	private final ProgramOptions _options;
	private final Queue<SummEngineCall> _workingQueue;
	private final List<SummId> _keepAliveSummIds;
	
	@SuppressWarnings("serial")
	public SummEngine(
			DB db,
			PorterStemmer porterStemmer,
			Set<String> stopStems,
			ProgramOptions options) throws IOException {
		
		_thisClassName = getClass().getSimpleName();
		_db = db;
		
		_log.info("Getting DB collections...");
		_keysColl = _db.getCollection(MongoColls.keys);
		_summColl = _db.getCollection(MongoColls.summ);
		_summreqColl = _db.getCollection(MongoColls.summreq);
		
		_snapSearcher 
			= new SnapSearcher(
				_db.getCollection(MongoColls.snaps), 
				_db.getCollection(MongoColls.snapsidx), 
				_db.getCollection(MongoColls.feeds), 
				_db.getCollection(MongoColls.sources));
		
		_snapCache = new SnapCache(_snapSearcher, CACHE_CLEAN_FREQ_MINS);

		_log.info("Loading keys index...");
		_keysIndex = new KeysIndex(_keysColl);
		_keysIndex.loadFromDB();
		_stopKeyIds = _keysIndex.getKeyIds(stopStems);

		_log.info("Loading ontology...");
		_ontology = new Ontology(_keysIndex);
		_ontology.loadFromDB(db);

		_log.info("Creating topic model...");
		_topicModel = new TopicModel(_ontology.getEntityListCatalog().getLists(), _keysIndex);
		_topicModelKeyIds = new TopicModelKeyIds(_topicModel, 1000, _keysIndex);

		_log.info("Creating query parser...");
		_queryParser = new QueryParser(porterStemmer, _keysIndex, _ontology);

		_stopRequested = false;
		_stopped = new CountDownLatch(1);
		_workingQueue = new LinkedList<SummEngineCall>();
		_options = options;
		
		_keepAliveSummIds = new ArrayList<>();
		for (Integer kindId : _keepAliveKindIds) {
			for (Integer periodId : _keepAlivePeriodIds) {
	
				{ // everything for this kind and period
					
					SummIdBuilder b = new SummIdBuilder(kindId, periodId);
					_keepAliveSummIds.add(new SummId(b.getData()));
				}
				
				// specific groups for this kind and period
				for (String groupId : _keepAliveGroupIds) {
					
					final Integer groupKeyId = _keysIndex.getId(groupId);
					SummIdBuilder b = new SummIdBuilder(kindId, periodId);
					b.setGroupKeyIds(new ArrayList<Integer>() {{ add(groupKeyId); }});
					_keepAliveSummIds.add(new SummId(b.getData()));
				}
			}
		}
	}
	
	public DBCollection getKeysColl() {
		return _keysColl;
	}
	public DBCollection getSummColl() {
		return _summColl;
	}
	public DBCollection getSummreqColl() {
		return _summreqColl;
	}
	public Set<Integer> getStopKeyIds() {
		return _stopKeyIds;
	}
	public KeysIndex getKeysIndex() {
		return _keysIndex;
	}
	public Ontology getOntology() {
		return _ontology;
	}
	public TopicModel getTopicModel() {
		return _topicModel;
	}
	public TopicModelKeyIds getTopicModelKeyIds() {
		return _topicModelKeyIds;
	}
	public QueryParser getQueryParser() {
		return _queryParser;
	}
	public SnapSearcher getSnapSearcher() {
		return _snapSearcher;
	}
	public SnapCache getSnapCache() {
		return _snapCache;
	}
	
	public void stop() {
		
		_stopRequested = true;
		_log.info(_thisClassName + " stop requested.");
		
		try {
			
			_log.info("Waiting for " + _thisClassName + " to stop...");
			_stopped.await();
			_log.info(_thisClassName + " stopped.");
			
		} catch (InterruptedException e) {
			
			_log.warning("Waiting for " + _thisClassName + " to stop was interrupted");
		}
	}
	
	public void run() {

		_log.info("Creating thread pool with " + _options.getThreadCount() + " threads...");
		ExecutorService es = Executors.newFixedThreadPool(_options.getThreadCount());
		final int autoThreadCount = _options.getThreadCount() / 2;
		final int userThreadCount = _options.getThreadCount() - autoThreadCount;
		
		final Map<Boolean, Integer> threadCountsMap = new HashMap<>();
		threadCountsMap.put(false, autoThreadCount);
		threadCountsMap.put(true,  userThreadCount);
		
		try {
			
			while (_stopRequested == false) {
				
				// clean cache if necessary
				if (_snapCache.isTimeToClean()) {
					
					// min date to keep
					Date cacheMinDateInc = DateUtils.addDays(new Date(), -CACHE_KEEP_DAYS_COUNT);
					
					// clean the cache
					_snapCache.clean(cacheMinDateInc);
				}
				
				// handle user-requested and automatic summaries separately
				for (Boolean isUserRequested : threadCountsMap.keySet()) {
					
					// get thread count allocated for this type of summary
					Integer threadCount = threadCountsMap.get(isUserRequested);
					
					// count working calls
					List<SummId> workingSummIds = new ArrayList<>();
					Iterator<SummEngineCall> callsIterator = _workingQueue.iterator();
					while (callsIterator.hasNext()) {
						
						SummEngineCall call = callsIterator.next();
						
						// only count calls of the current type
						if (call.isUserRequested() == isUserRequested) {
							if (call.isFinished()) {
								
								// print errors, if any
								if (call.hasException()) {
								
									Exception ex = call.getException();
									ex.printStackTrace(System.out);
									_log.severe("Could not calculate " + call);
								}
								
								// forget working call
								callsIterator.remove();
			
							} else {
								
								// count working call
								workingSummIds.add(call.getSummId());
							}
						}
					}
					
					// check calculation threads available
					int availableThreadsCount = threadCount - workingSummIds.size();
					if (availableThreadsCount > 0) {
						
						// find requested ids
						List<Pair<Boolean, SummId>> requestedSummIds = new ArrayList<>();
						
						// add user requested ids
						if (isUserRequested) {
							List<SummRequest> summRequests = SummRequest.findRequestedAsList(_summreqColl);
							if (summRequests != null) {
								for (int i=0; i<summRequests.size(); i++) {
									SummRequest summRequest = summRequests.get(i);
									SummId summId = new SummId(summRequest.getId());
									requestedSummIds.add(new Pair<Boolean, SummId>(isUserRequested, summId));
								}
							}
						} else { // add keep alive ids
							
							for (int i=0; i<_keepAliveSummIds.size(); i++) {
								requestedSummIds.add(new Pair<Boolean, SummId>(isUserRequested, _keepAliveSummIds.get(i)));
							}
						}
						
						// filter requests
						List<Triple<Boolean, AliveStatus, SummId>> todoSummIds = new ArrayList<>();
						for (int i=0; i<requestedSummIds.size(); i++) {
							
							// get requested id
							Pair<Boolean, SummId> pair = requestedSummIds.get(i);
							Boolean isUser = pair.v1();
							SummId summId = pair.v2();
							
							// check not working
							boolean isWorkingAlready = false;
							for (SummEngineCall workingCall : _workingQueue) {
								if (workingCall.getSummId().equals(summId)) {
									isWorkingAlready = true;
									break;
								}
							}
							if (isWorkingAlready) {
								continue;
							}
							
							// check alive status
							AliveStatus aliveStatus = SummResult.findAliveStatus(_summColl, summId.getData(), new Date());
							if (aliveStatus.equals(AliveStatus.Alive)) {
								continue;
							}
							
							todoSummIds.add(new Triple<Boolean, AliveStatus, SummId>(isUser, aliveStatus, summId));
						}
						
						// sort new requests
						Collections.sort(todoSummIds, new SummIdSorter());
						
						// submit new requests
						int submittedCount = 0;
						for (int i=0; i<todoSummIds.size() && 
							submittedCount<availableThreadsCount; i++) {
							
							if (_stopRequested) {
								break;
							}
							
							// get next request
							Triple<Boolean, AliveStatus, SummId> triple = todoSummIds.get(i);;
							SummId summId = triple.v3();
							
							// double-check check 
							// request is not working
							boolean alreadyWorking = false;
							for (int j=0; j<workingSummIds.size(); j++) {
								SummId workingSummId = workingSummIds.get(j);
								if (ArrayUtils.equals(workingSummId.getData(), summId.getData())) {
									alreadyWorking = true;
									break;
								}
							}
							if (alreadyWorking) {
								continue;
							}
							
							// submit the summary calculation call to thread pool
							Date maxDateExc = new Date();
							SummEngineCall call = new SummEngineCall(this, isUserRequested, summId, maxDateExc, _options);
							_workingQueue.add(call);
							workingSummIds.add(summId);
							es.submit(call);
							submittedCount++;
						}
						if (submittedCount > 0) {
							_log.info("Submitted " + submittedCount + " requests for calculation...");
						}
					}
							
				}

				if (_options.getLiveFreqMs() <= 0) {
				
					_log.info("Not repeating, bacause live freq argument is <= zero, will exit...");
					break;
	
				} else {
				
					try { // wait for next check time
	
						long sleepMs = _options.getLiveFreqMs();
						_log.finest("Waiting " + sleepMs + " ms for the next check time...");
						Thread.sleep(sleepMs);
						
					} catch (InterruptedException e) {
						
						_log.warning("Interrupted while waiting for the next check time, exiting...");
						break;
					}
				}
			}		
		
		} catch (Exception ex) {
			
			ex.printStackTrace(System.out);
			_log.severe("Exception is thrown on main " + _thisClassName + " thread, stopping...");
			
		} finally {
			
			_log.info("Shutting down thread pull...");
			es.shutdown();
		}
		
		_stopped.countDown();
	}

}
