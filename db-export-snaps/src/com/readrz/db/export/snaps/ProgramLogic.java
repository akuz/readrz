package com.readrz.db.export.snaps;

import java.io.IOException;

import me.akuz.core.FileUtils;
import me.akuz.core.StringUtils;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.readrz.data.Snap;
import com.readrz.data.mongo.MongoColls;
import com.readrz.search.QueryKeyIds;
import com.readrz.search.SnapSearch;
import com.readrz.search.SnapSearchResult;
import com.readrz.search.SnapSearcher;

public final class ProgramLogic {
	
	public void execute(
			String  mongoServer, 
			Integer mongoPort, 
			String  mongoDb,
			ProgramOptions options) throws IOException {
		
		System.out.println("Preparing output dir...");
		FileUtils.isDirExistsOrCreate(options.getOutputDir());

		System.out.println("Connecting to Mongo DB...");
		MongoClient mongoClient = new MongoClient(mongoServer, mongoPort);
		
		try {
			System.out.println("Getting db: " + mongoDb);
			DB db = mongoClient.getDB(mongoDb);
			DBCollection snapsColl = db.getCollection(MongoColls.snaps);
			DBCollection snapsidxColl = db.getCollection(MongoColls.snapsidx);
			DBCollection feedsColl = db.getCollection(MongoColls.feeds);
			DBCollection sourcesColl = db.getCollection(MongoColls.sources);
			
			SnapSearcher searcher = new SnapSearcher(snapsColl, snapsidxColl, feedsColl, sourcesColl);
			
			SnapSearch search = searcher.startSearch(
					false,
					new QueryKeyIds(),
					options.getFromDate(),
					null,
					options.getMaxSnaps() * 2);
			
			try {
				
				int counter = 0;
				while (true) {
					
					SnapSearchResult result = search.findNext();
					if (result == null) {
						break;
					}
					
					Snap snap = searcher.findSnap(result.getSnapId());
					if (snap == null) {
						continue;
					}
					
					StringBuilder sb = new StringBuilder();
					if (options.isSaveTitles()) {
						sb.append(snap.getTitle());
						sb.append("\n");
						sb.append("\n");
					}
					if (options.isSaveBodies()) {
						String body = snap.getText();
						body = StringUtils.trimBySpace(body, options.getMaxBodyChars());
						sb.append(body);
					}
					if (sb.length() > 0) {
						FileUtils.writeEntireFile(
								StringUtils.concatPath(options.getOutputDir(), snap.getId().toString() + ".txt"), 
								sb.toString());
					}
					counter += 1;
					if (counter % 100 == 0) {
						System.out.println("Exported " + counter + " snaps.");
					}
					if (counter >= options.getMaxSnaps()) {
						break;
					}
				}
				System.out.println("Exported " + counter + " snaps.");

			} finally {
				
				search.close();
			}
			
		} finally {
			
			System.out.println("Closing db connection...");
			mongoClient.close();
		}
		
		System.out.println("DONE.");
	}
}
