package com.readrz.dupfinder;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import me.akuz.core.DateUtils;
import me.akuz.core.Out;
import me.akuz.core.UtcDate;

import org.bson.types.ObjectId;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.readrz.data.mongo.MongoColls;

public final class ProgramLogic {
	
	private volatile boolean _stopRequested;
	private final Map<ObjectId, ObjectId> _sourceIdByFeedId;
	
	public ProgramLogic() {
		_sourceIdByFeedId = new HashMap<ObjectId, ObjectId>();
	}

	public void execute(
			String  mngServer, 
			Integer mngPort, 
			String  mngDb,
			ProgramOptions opts) throws UnknownHostException {
		
		System.out.println("Connecting to Mongo DB...");
		MongoClient mongoClient = new MongoClient(mngServer, mngPort);
		DB db = mongoClient.getDB(mngDb);
		
		try {

			final Out<DupsCall> startupCallHandle = new Out<DupsCall>(null);
			final Out<DupsCall> lastCallHandle = new Out<DupsCall>(null);
			
			Runtime.getRuntime().addShutdownHook(new Thread() {
			    public void run() {
					System.out.println("Shutting down...");
					_stopRequested = true;
					
					DupsCall startupCall = startupCallHandle.getValue();
					if (startupCall != null) {
						if (startupCall.isStopped() == false) {
							System.out.println("Asking startup call to stop...");
							startupCall.stop();
						}
					}
					
					DupsCall lastCall = lastCallHandle.getValue();
					if (lastCall != null) {
						if (lastCall.isStopped() == false) {
							System.out.println("Asking last call to stop...");
							lastCall.stop();
						}
					}
					
					System.out.println("Shut down complete.");
			    }
			 });
			
			System.out.println("Getting DB collections needed for dups checking...");
			DBCollection feedsColl = db.getCollection(MongoColls.feeds);
			DBCollection snapsColl = db.getCollection(MongoColls.snaps);
			
			// perform scan on startup, if asked for
			if (opts.getStartupFromDate() != null) {

				Date endDate = new Date();
				Date startDate = opts.getStartupFromDate();
				
				System.out.println((new UtcDate()).toString() + ": Checking for dups on startup, from " + new UtcDate(startDate));
				DupsCall startupCall = new DupsCall(_sourceIdByFeedId, feedsColl, snapsColl,
						opts.getDupsPeriodMins(), opts.getStartupRedoAll(), startDate, endDate, opts.isVerbose());
				
				startupCallHandle.setValue(startupCall);
				startupCall.call();
			}
			
			Date lastCheckDate = null;
			while (_stopRequested == false) {
				
				if (opts.getLiveFreqMins() <= 0) {
					System.out.println("Not starting live dups checking, bacause live freq argument is zero.");
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
						System.out.println("WARNING: Dups finder thread interrupted while waiting for next check time");
						break;
					}
				}
				
				if (_stopRequested) {
					break;
				}

				try {
					System.out.println(new UtcDate() + ": Checking for dups live (with frequency of " + opts.getLiveFreqMins() + " mins)... ");

					lastCheckDate = new Date();

					Date endDate = new Date();
					Date startDate = DateUtils.addMinutes(endDate, - opts.getLivePeriodMins());
					
					DupsCall liveCall = new DupsCall(_sourceIdByFeedId, feedsColl, snapsColl, 
							opts.getDupsPeriodMins(), false, startDate, endDate, opts.isVerbose());
					lastCallHandle.setValue(liveCall);
					liveCall.call();
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("ERROR: Exception while checking for dups, will retry in " + opts.getLiveFreqMins() + " mins");
				}
			}
			
		} finally {
			
			mongoClient.close();
		}
		System.out.println("Finished dups checking.");
	}
}
