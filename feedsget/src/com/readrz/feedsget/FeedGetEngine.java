package com.readrz.feedsget;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.akuz.core.DateUtils;
import me.akuz.core.UtcDate;
import me.akuz.core.http.Non200HttpCodeExeption;

import com.mongodb.DB;
import com.readrz.data.Feed;

/**
 * Feed scanner that performs periodic scanning of recent posts,
 * saves new posts as they are downloaded, and notifies the 
 * listeners when this happens (new posts appear).
 *
 */
public final class FeedGetEngine implements Runnable {
	
	private final CountDownLatch _globalStopRequested;
	private volatile boolean _stopRequested;
	private final CountDownLatch _stopped;
	private final Object _lock = new Object();
	
	private final List<Feed> _feeds;
	private final DB _db;
	private final ProgramOptions _options;
	private final Queue<FeedGetCall> _scanningQueue;
	
	public FeedGetEngine(
			CountDownLatch globalStopRequested,
			List<Feed> feeds,
			DB db,
			ProgramOptions options) {
		
		_globalStopRequested = globalStopRequested;

		_stopRequested = false;
		_stopped = new CountDownLatch(1);
		
		_scanningQueue = new LinkedList<FeedGetCall>();

		_feeds = feeds;
		_db = db;
		_options = options;
	}
	
	public void stop() {
		
		synchronized (_lock) {
			
			_stopRequested = true;
			
			Iterator<FeedGetCall> callsIter = _scanningQueue.iterator();
			while (callsIter.hasNext()) {
				
				FeedGetCall call = callsIter.next();
				System.out.println("Asking call to stop: " + call);
				call.stopAsync();
			}
		}
		
		try {
			_stopped.await();
			
		} catch (InterruptedException e) {
			
			System.out.println("WARNING: Waiting for " + this.getClass().getSimpleName() + " to stop was interrupted");
		}
	}
	
	public void run() {
		
		System.out.println("Creating scanning thread pool with " + _options.getThreadCount() + " threads...");
		ExecutorService es = Executors.newFixedThreadPool(_options.getThreadCount());

		try {
		
			// populate scanning queues
			Queue<FeedGet> toScanQueue = new PriorityQueue<>();
			for (int i=0; i<_feeds.size(); i++) {
				
				Feed feed = _feeds.get(i);
				FeedGet feedScan = new FeedGet(feed);
				toScanQueue.add(feedScan);
			}
			
			// add scan calls in batches, to that we can exit gracefully
			UtcDate lastScanDisplayDate = null;
			while (_stopRequested == false) {

				synchronized (_lock) {
					
					// double-checking pattern
					if (_stopRequested) {
						break;
					}
					
					// count completed calls
					int workingCallsCount = 0;
					Iterator<FeedGetCall> callsIter = _scanningQueue.iterator();
					while (callsIter.hasNext()) {
						
						FeedGetCall call = callsIter.next();
						if (call.isComplete()) {
							
							// get call's feed scan
							FeedGet feedGet = call.getFeedGet();
	
							// print errors, if any
							if (call.hasException()) {
							
								int errorDelayMins = 20;
								Exception ex = call.getException();
								
								System.out.println("ERROR: Could not get feed " + feedGet);
								if (ex instanceof FileNotFoundException) {
									System.out.println("FILE NOT FOUND: " + ex.getMessage());
								} else if (ex instanceof Non200HttpCodeExeption) {
									System.out.println("NON 200 CODE: " + ex.getMessage());
								} else if (ex instanceof IOException) {
									System.out.println("IO EXCEPTION: " + ex.getMessage());
								} else if (ex instanceof SocketException) {
									System.out.println("SOCKET EXCEPTION: " + ex.getMessage());
								} else {
									System.out.println("EXCEPTION: " + ex.getMessage());
									ex.printStackTrace(System.out);
								}
								System.out.println("WILL: Try again after " + errorDelayMins + " minutes...");

								Date nextScanDate = DateUtils.addMins(new Date(), errorDelayMins);
								feedGet.setNextScanDate(nextScanDate);
							}
							
							// queue for next scan
							toScanQueue.add(feedGet);
							
							// forget working call
							callsIter.remove();
	
						} else {
							
							// count working call
							workingCallsCount += 1;
						}
					}
					
					// add new scan calls
					int freeThreadsCount = _options.getThreadCount() - workingCallsCount;
					for (int i=0; i<freeThreadsCount; i++) {
						
						// take next from to scan queue
						FeedGet feedGet = toScanQueue.peek();
						if (feedGet == null) {
							
							// if nothing more to scan, stop
							if (_scanningQueue.size() == 0) {
								_stopRequested = true;
							}	
							break;
						}
	
						// check if time to scan next
						if (feedGet.getNextScanDate() != null) {
							
							// check if too early to scan the next feed
							if (feedGet.getNextScanDate().compareTo(new Date()) > 0) {
								break;
							}
						}
						
						// remove the first one,
						// we peeked it already
						toScanQueue.poll();
						
						// submit asynchronous scan call
						UtcDate now = new UtcDate();
						if (_options.isVerbose() ||
							lastScanDisplayDate == null || 
							DateUtils.minutesBetween(lastScanDisplayDate.getDate(), now.getDate()) > 1.0) {
							
							StringBuilder sb = new StringBuilder();
							sb.append(now.toString());
							sb.append(" ");
							
							if (_options.isVerbose()) {
								sb.append("(Verbose): ");
							} else {
								sb.append("(Not verbose, showing *some* scans): ");
							}
							sb.append("Scheduling: ");
							sb.append(feedGet.toString());
							
							System.out.println(sb.toString());
							lastScanDisplayDate = now;
						}
						FeedGetCall call = new FeedGetCall(_db, feedGet, _options);
						_scanningQueue.add(call);
						es.submit(call);
					}
				}
				
				// wait until the next check
				Thread.sleep(200);
			}
			
		} catch (Exception ex) {
			
			System.out.println("ERROR: Exception is thrown on " + getClass().getSimpleName() + " thread, stopped.");
			ex.printStackTrace();
			
			if (_globalStopRequested != null) {
				_globalStopRequested.countDown();
			}
			
		} finally {
			
			System.out.println("Shutting down scanning thread pull, waiting for current calls to complete...");
			es.shutdown();
		}
		
		_stopped.countDown();
	}

}
