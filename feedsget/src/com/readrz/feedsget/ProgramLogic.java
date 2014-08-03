package com.readrz.feedsget;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.readrz.data.Feed;
import com.readrz.data.mongo.MongoColls;

public final class ProgramLogic {

	public void execute(
			String  mongoServer, 
			Integer mongoPort, 
			String  mongoDb,
			ProgramOptions options) throws UnknownHostException {
		
		System.out.println("Connecting to Mongo DB...");
		final MongoClient mongoClient = new MongoClient(mongoServer, mongoPort);
		try {

			System.out.println("Getting database: " + mongoDb);
			final DB db = mongoClient.getDB(mongoDb);
	
			System.out.println("Loading feeds...");
			List<Feed> feeds = new ArrayList<Feed>();
			DBCursor feedsCursor = db.getCollection(MongoColls.feeds).find();
			while (feedsCursor.hasNext()) {
				Feed feed = new Feed(feedsCursor.next());
				feeds.add(feed);
			}
			
			System.out.println("Creating scanning engine...");
			final FeedGetEngine engine = new FeedGetEngine(
					null, feeds, db, options);
			
			System.out.println("Adding shutdown handler...");
			Runtime.getRuntime().addShutdownHook(new Thread() {
			    public void run() {
					System.out.println("Shutting down scanning engine...");
			    	engine.stop();
			    	System.out.println("Clean shut down complete!");
			    }
			 });
			
			System.out.println("Starting scanning engine...");
			Thread engineThread = new Thread(engine);
			engineThread.run();
			
		} finally {
			
			System.out.println("Closing Mongo DB connection...");
			mongoClient.close();
		}
		
		System.out.println("DONE.");
	}
}
