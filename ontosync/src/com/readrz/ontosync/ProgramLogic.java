package com.readrz.ontosync;

import java.io.IOException;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.readrz.data.index.KeysIndex;
import com.readrz.data.mongo.MongoColls;
import com.readrz.data.ontology.Ontology;

public final class ProgramLogic {

	public void execute(
			String  mngServer, 
			Integer mngPort, 
			String  mngDb,
			String ontologyDir) throws IOException {
		
		System.out.println("Connecting to Mongo DB...");
		MongoClient mongoClient = new MongoClient(mngServer, mngPort);
		DB db = mongoClient.getDB(mngDb);
		
		System.out.println("Loading keys index...");
		KeysIndex keysIndex = new KeysIndex(db.getCollection(MongoColls.keys));
		keysIndex.loadFromDB();
		
		System.out.println("Loading new ontology to put into db...");
		Ontology ontology = new Ontology(keysIndex);
		ontology.loadFromDir(ontologyDir);

		System.out.println("Updating the ontology in DB...");
		ontology.saveToDB(db);
		
		System.out.println("DONE.");
	}
}
