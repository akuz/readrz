package com.readrz.db.ensure.idx;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.readrz.data.Feed;
import com.readrz.data.PathsRequest;
import com.readrz.data.Snap;
import com.readrz.data.SnapHtml;
import com.readrz.data.SnapImag;
import com.readrz.data.SnapThumb;
import com.readrz.data.Source;
import com.readrz.data.SummRequest;
import com.readrz.data.index.Idx;
import com.readrz.data.index.KeysIndex;
import com.readrz.data.mongo.MongoColls;
import com.readrz.data.user.User;
import com.readrz.data.user.UserCodeDA;
import com.readrz.data.user.UserOldNameDA;
import com.readrz.data.user.UserSessionDA;

public final class ProgramLogic {

	public void execute(
			String  mongoServer, 
			Integer mongoPort, 
			String  mongoDb) throws UnknownHostException {
		
		System.out.println("Connecting to db server...");
		MongoClient mongoClient = new MongoClient(mongoServer, mongoPort);
		
		try {
			System.out.println("Getting db: " + mongoDb);
			DB db = mongoClient.getDB(mongoDb);

			System.out.println("Ensuring db indices...");

			KeysIndex.ensureIndices(db.getCollection(MongoColls.keys));
			Source.ensureIndices(db.getCollection(MongoColls.sources));
			Feed.ensureIndices(db.getCollection(MongoColls.feeds));
			Snap.ensureIndices(db.getCollection(MongoColls.snaps));
			SnapHtml.ensureIndices(db.getCollection(MongoColls.snapshtml));
			SnapImag.ensureIndices(db.getCollection(MongoColls.snapsimag));
			SnapThumb.ensureIndices(db.getCollection(MongoColls.snapsthumb));
			Idx.ensureIndices(db.getCollection(MongoColls.snapsidx));
			PathsRequest.ensureIndices(db.getCollection(MongoColls.pathsreq));
			SummRequest.ensureIndices(db.getCollection(MongoColls.summreq));

			User.ensureIndices(db.getCollection(MongoColls.user));
			UserCodeDA.ensureIndices(db.getCollection(MongoColls.userCode));
			UserOldNameDA.ensureIndices(db.getCollection(MongoColls.userOldName));
			UserSessionDA.ensureIndices(db.getCollection(MongoColls.userSession));

		} finally {

			System.out.println("Closing db connection...");
			mongoClient.close();
		}
		
		System.out.println("DONE.");
	}
}
