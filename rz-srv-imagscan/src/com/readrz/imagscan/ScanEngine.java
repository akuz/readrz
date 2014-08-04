package com.readrz.imagscan;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import me.akuz.core.logs.LogUtils;
import me.akuz.nlp.porter.PorterStemmer;

import org.bson.types.ObjectId;

import com.mongodb.DBCollection;

public final class ScanEngine implements Runnable {
	
	private static final Logger _log = LogUtils.getLogger(ScanEngine.class.getName());

	private final Object _lock = new Object();
	
	private final ExtractImages _extractImages;
	private final DBCollection _snapsColl;
	private final DBCollection _snapsHtmlColl;
	private final DBCollection _snapsImagColl;
	private final DBCollection _snapsThumbColl;
	private final int _threadCount;
	private final Pattern _blockedUrlsPattern;
	
	private final Set<ObjectId> _queuedAndWorkingPostIds;
	private final PriorityQueue<ScanSnapCall> _queuedCalls;
	private final Map<ObjectId, ScanSnapCall> _workingCalls;
	private final List<CountDownLatch> _idleLatches;
	private volatile boolean _stopRequested;
	private final CountDownLatch _stopped;
	
	public ScanEngine(
			PorterStemmer porterStemmer,
			Set<String> stopStems,
			Pattern blockedUrlsPattern,
			DBCollection snapsColl,
			DBCollection snapsHtmlColl,
			DBCollection snapsImagColl,
			DBCollection snapsThumbColl,
			int threadCount) {
		
		_extractImages = new ExtractImages(porterStemmer, stopStems, blockedUrlsPattern);
		
		_snapsColl = snapsColl;
		_snapsHtmlColl = snapsHtmlColl;
		_snapsImagColl = snapsImagColl;
		_snapsThumbColl = snapsThumbColl;
		_threadCount = threadCount;
		_blockedUrlsPattern = blockedUrlsPattern;
		
		_queuedAndWorkingPostIds = new HashSet<>();
		_queuedCalls = new PriorityQueue<ScanSnapCall>(100);
		_workingCalls = new HashMap<>();
		_idleLatches = new ArrayList<>();
		
		_stopped = new CountDownLatch(1);
		
		// start the main engine thread
		Thread engineThread = new Thread(this);
		engineThread.start();
	}
	
	public ExtractImages getExtractImages() {
		return _extractImages;
	}
	public DBCollection getSnapsColl() {
		return _snapsColl;
	}
	public DBCollection getSnapsHtmlColl() {
		return _snapsHtmlColl;
	}
	public DBCollection getSnapsImagColl() {
		return _snapsImagColl;
	}
	public DBCollection getSnapsThumbColl() {
		return _snapsThumbColl;
	}
	public Pattern getBlockedUrlsPattern() {
		return _blockedUrlsPattern;
	}

	@Override
	public void run() {
		
		_log.info("Creating thread pool with " + _threadCount + " threads...");
		ExecutorService es = Executors.newFixedThreadPool(_threadCount);

		try {

			int counter = 0;
			while (true) {
				
				if (_stopRequested) {
					break;
				}
				
				synchronized (_lock) {
					
					Date now = new Date();
					
					// submit next calls, if any, *only* if threads 
					// are available (to prevent from slow stopping)
					while (_queuedCalls.size() > 0) {
						
						if (_workingCalls.size() < _threadCount) {
							
							// check queued call is due
							ScanSnapCall call = _queuedCalls.peek();
							if (call.getDueDate() == null || 
								call.getDueDate().compareTo(now) < 0) {

								// get the call
								call = _queuedCalls.poll();
								
								// submit the call for execution
								_workingCalls.put(call.getSnap().getId(), call);
								
								_log.fine("Submitting " + call);
								es.submit(call);

							} else {
								
								// not time yet for the next call
								break;
							}
							
						} else {
							
							// wait for available threads
							break;
						}
					}
					
					// check if idle now
					if (_idleLatches.size() > 0) {
						
						if (_workingCalls.size() == 0) {
						
							boolean isIdle = false;
							if (_queuedCalls.size() == 0) {
								isIdle = true;
							} else {
								ScanSnapCall nextCall = _queuedCalls.peek();
								if (nextCall.getDueDate() != null && 
									nextCall.getDueDate().compareTo(now) > 0) {
									
									isIdle = true;
								}
							}
							
							if (isIdle) {
								
								_log.fine("Idle");

								// notify latches waiting for idle 
								for (int i=0; i<_idleLatches.size(); i++) {
									_idleLatches.get(i).countDown();
								}
								_idleLatches.clear();
							}
						}
					}
					
					counter += 1;
					counter %= 100;
					if (counter == 0 && _queuedAndWorkingPostIds.size() > 0) {
						_log.fine("Queued and working posts: " + _queuedAndWorkingPostIds.size());
					}
				}
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					_log.warning("Interrupted while waiting on engine main thread, will stop...");
					_stopRequested = true;
				}
			}
			
		} finally {
			
			if (es != null) {
				
				// request shutdown
				es.shutdown();
				
				// wait for shutdown completion
				final int munutesToWait = 1;
				try {
					es.awaitTermination(munutesToWait, TimeUnit.MINUTES);
				} catch (InterruptedException e) {
					_log.warning("Could not shutdown thread pool in " 
							+ munutesToWait + " min, stopped waiting.");
				}
			}
			
			_stopped.countDown();
		}
	}
	
	public void addIdleLatch(CountDownLatch latch) {
		synchronized (_lock) {
			_idleLatches.add(latch);
		}
	}

	public void submit(ScanSnapCall call) {
		synchronized (_lock) {
			
			ObjectId snapId = call.getSnap().getId();

			// only submit, if not yet submitted
			if (_queuedAndWorkingPostIds.contains(snapId) == false) {
				
				// add to queue
				_queuedAndWorkingPostIds.add(snapId);
				_queuedCalls.add(call);
			}
		}
	}

	public void reattempt(ScanSnapCall call) {
		synchronized (_lock) {
			
			ObjectId snapId = call.getSnap().getId();

			// check call has been submitted before
			if (_workingCalls.containsKey(snapId) == false) {
				throw new IllegalStateException("Can't resubmit a non-working call");
			}
			
			// remove from working calls
			_workingCalls.remove(snapId);
			
			// add to queue
			_queuedCalls.add(call);
		}
	}
	
	public void remove(ScanSnapCall call) {
		synchronized (_lock) {
			
			ObjectId snapId = call.getSnap().getId();
			
			// check call has been submitted before
			if (_workingCalls.containsKey(snapId) == false) {
				throw new IllegalStateException("Can't remove call a non-working call");
			}

			// remove from working calls
			_queuedAndWorkingPostIds.remove(snapId);
			_workingCalls.remove(snapId);
		}
	}
	
	public void stop() {
		_stopRequested = true;
		try {
			_stopped.await();
		} catch (InterruptedException e) {
			_log.warning("Interrupted while waiting for engine to stop");
		}
	}
	
	public void stopAsync() {
		_stopRequested = true;
	}
	
	public boolean isStopped() {
		return _stopped.getCount() == 0L;
	}
	
}
