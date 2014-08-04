package com.readrz.pathcalc;

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
import me.akuz.core.Pair;
import me.akuz.core.Triple;
import me.akuz.core.logs.LogUtils;
import me.akuz.nlp.porter.PorterStemmer;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.readrz.data.Paths;
import com.readrz.data.PathsId;
import com.readrz.data.PathsIdBuilder;
import com.readrz.data.PathsRequest;
import com.readrz.data.Period;
import com.readrz.data.AliveStatus;
import com.readrz.data.index.KeysIndex;
import com.readrz.data.mongo.MongoColls;
import com.readrz.data.ontology.Ontology;
import com.readrz.search.QueryKeyIds;
import com.readrz.search.QueryParser;
import com.readrz.search.SnapSearcher;

/**
 * Feed scanner that performs periodic scanning of recent posts,
 * saves new posts as they are downloaded, and notifies the 
 * listeners when this happens (new posts appear).
 *
 */
public final class PathsEngine implements Runnable {
	
	private static final Logger _log = LogUtils.getLogger(PathsEngine.class.getName());

	private static final List<Integer> _keepAlivePeriodIds;
	private static final List<String>  _keepAliveTopicGroupIds;
	private static final List<String>  _keepAliveListGroupIds;
	static {
		
		_keepAlivePeriodIds = new ArrayList<>();
		_keepAlivePeriodIds.add(Period.Id4h);
		_keepAlivePeriodIds.add(Period.Id1d);
		_keepAlivePeriodIds.add(Period.Id3d);
		
		_keepAliveTopicGroupIds = new ArrayList<>();
		_keepAliveTopicGroupIds.add("g/topics/business");
		_keepAliveTopicGroupIds.add("g/topics/business/sectors");
		_keepAliveTopicGroupIds.add("g/topics/business/sectors/technology");
		_keepAliveTopicGroupIds.add("g/topics/business/sectors/technology/areas");
		_keepAliveTopicGroupIds.add("g/topics/business/events");
		_keepAliveTopicGroupIds.add("g/topics/disasters");
		_keepAliveTopicGroupIds.add("g/topics/disasters/natural");
		_keepAliveTopicGroupIds.add("g/topics/disasters/technological");
		_keepAliveTopicGroupIds.add("g/topics/disasters/transportation");
		_keepAliveTopicGroupIds.add("g/topics/entertainment");
		_keepAliveTopicGroupIds.add("g/topics/entertainment/celebrities");
		_keepAliveTopicGroupIds.add("g/topics/entertainment/lifestyle");
		_keepAliveTopicGroupIds.add("g/topics/entertainment/media");
		_keepAliveTopicGroupIds.add("g/topics/finance");
		_keepAliveTopicGroupIds.add("g/topics/finance/economy");
		_keepAliveTopicGroupIds.add("g/topics/finance/events");
		_keepAliveTopicGroupIds.add("g/topics/finance/sectors");
		_keepAliveTopicGroupIds.add("g/topics/finance/stocks");
		_keepAliveTopicGroupIds.add("g/topics/government");
		_keepAliveTopicGroupIds.add("g/topics/government/sectors");
		_keepAliveTopicGroupIds.add("g/topics/government/politics");
		_keepAliveTopicGroupIds.add("g/topics/science");
		_keepAliveTopicGroupIds.add("g/topics/science/sectors");
		_keepAliveTopicGroupIds.add("g/topics/society");
		_keepAliveTopicGroupIds.add("g/topics/society/conflicts");
		_keepAliveTopicGroupIds.add("g/topics/society/crime");
		_keepAliveTopicGroupIds.add("g/topics/society/issues");
		_keepAliveTopicGroupIds.add("g/topics/sports");
		
		_keepAliveListGroupIds = new ArrayList<>();
		_keepAliveListGroupIds.add("g/companies");
		_keepAliveListGroupIds.add("g/companies/stock_index");
		_keepAliveListGroupIds.add("g/companies/stock_index/ftse100");
		_keepAliveListGroupIds.add("g/companies/stock_index/ftse100/sector");
		_keepAliveListGroupIds.add("g/companies/stock_index/nasdaq100");
		_keepAliveListGroupIds.add("g/companies/stock_index/nasdaq100/sector");
		_keepAliveListGroupIds.add("g/countries");
		_keepAliveListGroupIds.add("g/countries/economic_group");
		_keepAliveListGroupIds.add("g/countries/economic_group/brics");
		_keepAliveListGroupIds.add("g/countries/economic_group/g20");
		_keepAliveListGroupIds.add("g/countries/economic_group/g8");
		_keepAliveListGroupIds.add("g/countries/geopolitics");
		_keepAliveListGroupIds.add("g/countries/geopolitics/middle_east");
		_keepAliveListGroupIds.add("g/countries/geopolitics/greater_middle_east");
		_keepAliveListGroupIds.add("g/countries/un_region");
		_keepAliveListGroupIds.add("g/countries/un_region/africa");
		_keepAliveListGroupIds.add("g/countries/un_region/africa/un_subregion");
		_keepAliveListGroupIds.add("g/countries/un_region/americas");
		_keepAliveListGroupIds.add("g/countries/un_region/americas/un_subregion");
		_keepAliveListGroupIds.add("g/countries/un_region/antarctica");
		_keepAliveListGroupIds.add("g/countries/un_region/asia");
		_keepAliveListGroupIds.add("g/countries/un_region/asia/un_subregion");
		_keepAliveListGroupIds.add("g/countries/un_region/europe");
		_keepAliveListGroupIds.add("g/countries/un_region/europe/geopolitics");
		_keepAliveListGroupIds.add("g/countries/un_region/europe/un_subregion");
		_keepAliveListGroupIds.add("g/countries/un_region/oceania");
		_keepAliveListGroupIds.add("g/countries/un_region/oceania/un_subregion");
	}
	
	private final String _thisClassName;
	private volatile boolean _stopRequested;
	private final CountDownLatch _stopped;
	
	private final DB _db;
	private final DBCollection _keysColl;
	private final DBCollection _pathsColl;
	private final DBCollection _pathsreqColl;
	private final KeysIndex _keysIndex;
	private final Set<Integer> _stopKeyIds;
	private final Ontology _ontology;
	private final QueryParser _queryParser;
	private final SnapSearcher _snapSearcher;

	private final ProgramOptions _options;
	private final Queue<PathsCalcCall> _workingQueue;
	private final List<PathsId> _keepAlivePathsIds;
	
	public PathsEngine(
			DB db,
			PorterStemmer porterStemmer,
			Set<String> stopStems,
			ProgramOptions options) throws IOException {
		
		_thisClassName = getClass().getSimpleName();
		_db = db;
		
		_log.info("Getting DB collections...");
		_keysColl = _db.getCollection(MongoColls.keys);
		_pathsColl = _db.getCollection(MongoColls.paths);
		_pathsreqColl = _db.getCollection(MongoColls.pathsreq);

		_snapSearcher = new SnapSearcher(
				_db.getCollection(MongoColls.snaps), 
				_db.getCollection(MongoColls.snapsidx), 
				_db.getCollection(MongoColls.feeds), 
				_db.getCollection(MongoColls.sources));

		_log.info("Loading keys index...");
		_keysIndex = new KeysIndex(_keysColl);
		_keysIndex.loadFromDB();
		_stopKeyIds = _keysIndex.getKeyIds(stopStems);

		_log.info("Loading ontology...");
		_ontology = new Ontology(_keysIndex);
		_ontology.loadFromDB(db);

		_log.info("Creating query parser...");
		_queryParser = new QueryParser(porterStemmer, _keysIndex, _ontology);

		_stopRequested = false;
		_stopped = new CountDownLatch(1);
		_workingQueue = new LinkedList<PathsCalcCall>();
		_options = options;
		
		_keepAlivePathsIds = new ArrayList<>();
		for (Integer periodId : _keepAlivePeriodIds) {

			{ // everything for this period
				
				PathsIdBuilder b = new PathsIdBuilder(periodId);
				_keepAlivePathsIds.add(new PathsId(b.getData()));
			}
				
			for (String groupId : _keepAliveTopicGroupIds) {
				
				QueryKeyIds queryKeyIds = new QueryKeyIds();
				Integer groupKeyId = _keysIndex.getId(groupId);

				// add *senCheck* key id for *topic* group
				queryKeyIds.addSenCheckKeyId(groupKeyId);
				
				PathsIdBuilder b = new PathsIdBuilder(periodId);
				b.setQueryKeyIds(queryKeyIds);
				_keepAlivePathsIds.add(new PathsId(b.getData()));
			}
			
			for (String groupId : _keepAliveListGroupIds) {
				
				QueryKeyIds queryKeyIds = new QueryKeyIds();
				Integer groupKeyId = _keysIndex.getId(groupId);
				
				// add *sentence* key id for *list* group
				queryKeyIds.addSentenceKeyId(groupKeyId);
				
				PathsIdBuilder b = new PathsIdBuilder(periodId);
				b.setQueryKeyIds(queryKeyIds);
				_keepAlivePathsIds.add(new PathsId(b.getData()));
			}
		}
	}
	
	public DBCollection getKeysColl() {
		return _keysColl;
	}
	public DBCollection getPathsColl() {
		return _pathsColl;
	}
	public DBCollection getPathsreqColl() {
		return _pathsreqColl;
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
	public QueryParser getQueryParser() {
		return _queryParser;
	}
	public SnapSearcher getSnapSearcher() {
		return _snapSearcher;
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

		_log.info("Creating paths calc thread pool with " + _options.getThreadCount() + " threads...");
		ExecutorService es = Executors.newFixedThreadPool(_options.getThreadCount());
		final int autoThreadCount = _options.getThreadCount() / 2;
		final int userThreadCount = _options.getThreadCount() - autoThreadCount;
		
		final Map<Boolean, Integer> threadCounts = new HashMap<>();
		threadCounts.put(false, autoThreadCount);
		threadCounts.put(true,  userThreadCount);
		
		final Map<Boolean, List<PathsId>> workingPathsIdsMap = new HashMap<>();
		workingPathsIdsMap.put(false, new ArrayList<PathsId>());
		workingPathsIdsMap.put(true,  new ArrayList<PathsId>());
		
		try {
			
			
			while (_stopRequested == false) {
				
				for (Boolean isUserRequested : threadCounts.keySet()) {
					
					Integer threadCount = threadCounts.get(isUserRequested);
					List<PathsId> workingPathsIds = workingPathsIdsMap.get(isUserRequested);
					
					// count working calls
					int workingCallsCount = 0;
					Iterator<PathsCalcCall> callsIter = _workingQueue.iterator();
					workingPathsIds.clear();
					while (callsIter.hasNext()) {
						
						PathsCalcCall call = callsIter.next();
						if (call.isUserRequested() == isUserRequested) {
							if (call.isFinished()) {
								
								// print errors, if any
								if (call.hasException()) {
								
									Exception ex = call.getException();
									ex.printStackTrace(System.out);
									_log.severe("Could not calculate " + call);
								}
								
								// forget working call
								callsIter.remove();
			
							} else {
								
								// count working call
								workingCallsCount += 1;
								workingPathsIds.add(call.getPathsId());
							}
						}
					}
					
					// check calculation threads available
					int freeThreadsCount = threadCount - workingCallsCount;
					if (freeThreadsCount > 0) {
						
						// find requested paths ids
						List<Pair<Boolean, PathsId>> requestedPathsIds = new ArrayList<>();
						
						// add user requested paths ids
						if (isUserRequested) {
							List<PathsRequest> pathsReqs = PathsRequest.findRequestedAsList(_pathsreqColl);
							if (pathsReqs != null) {
								for (int i=0; i<pathsReqs.size(); i++) {
									PathsRequest pathsReq = pathsReqs.get(i);
									PathsId pathsId = new PathsId(pathsReq.getId());
									requestedPathsIds.add(new Pair<Boolean, PathsId>(isUserRequested, pathsId));
								}
							}
						} else { // add keep alive paths ids
							
							for (int i=0; i<_keepAlivePathsIds.size(); i++) {
								requestedPathsIds.add(new Pair<Boolean, PathsId>(isUserRequested, _keepAlivePathsIds.get(i)));
							}
						}
						
						// filter paths requests
						List<Triple<Boolean, AliveStatus, PathsId>> sortedPathsIds = new ArrayList<>();
						for (int i=0; i<requestedPathsIds.size(); i++) {
							
							// get requested paths id
							Pair<Boolean, PathsId> pair = requestedPathsIds.get(i);
							Boolean isUser = pair.v1();
							PathsId pathsId = pair.v2();
							
							// check alive status
							Date now = new Date();
							AliveStatus aliveStatus = Paths.findAliveStatus(_pathsColl, pathsId.getData(), now, pathsId.getPeriod());
							if (aliveStatus.equals(AliveStatus.Alive) == false) {
								sortedPathsIds.add(new Triple<Boolean, AliveStatus, PathsId>(isUser, aliveStatus, pathsId));
							}
						}
						
						// sort paths requests
						Collections.sort(sortedPathsIds, new PathsIdSorter());
						
						// submit new requests
						int submittedCount = 0;
						for (int i=0; i<sortedPathsIds.size() && 
							submittedCount<freeThreadsCount; i++) {
							
							if (_stopRequested) {
								break;
							}
							
							// get next request
							Triple<Boolean, AliveStatus, PathsId> triple = sortedPathsIds.get(i);;
							PathsId pathsId = triple.v3();
							
							// check request is not working
							boolean alreadyWorking = false;
							for (int j=0; j<workingPathsIds.size(); j++) {
								PathsId workingPathsId = workingPathsIds.get(j);
								if (ArrayUtils.equals(workingPathsId.getData(), pathsId.getData())) {
									alreadyWorking = true;
									break;
								}
							}
							if (alreadyWorking) {
								continue;
							}
							
							PathsCalcCall call = new PathsCalcCall(this, isUserRequested, pathsId, _options);
							_workingQueue.add(call);
							workingPathsIds.add(pathsId);
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
