package com.readrz.pathcalc;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;

import me.akuz.core.logs.LogUtils;
import me.akuz.core.logs.ManualResetLogManager;
import me.akuz.nlp.porter.PorterStemmer;
import me.akuz.nlp.porter.PorterStopWords;

import com.mongodb.DB;
import com.mongodb.MongoClient;

public final class ProgramLogic {
	
	private static final Logger _log = LogUtils.getLogger(ProgramLogic.class.getName());
	
	public ProgramLogic() {
	}

	public void execute(
			String  mongoServer, 
			Integer mongoPort, 
			String  mongoDb,
			ProgramOptions options) throws IOException {
		
		_log.info("Loading stop words...");
		PorterStemmer porterStemmer = new PorterStemmer("_");
		Set<String> stopStems = PorterStopWords.loadStopWordsAndStemThem(porterStemmer, options.getStopWordsFile());

		_log.info("Connecting to Mongo DB...");
		MongoClient mongoClient = new MongoClient(mongoServer, mongoPort);
		
		try {

			_log.info("Getting database: " + mongoDb);
			DB db = mongoClient.getDB(mongoDb);
			
			_log.info("Creating calculation engine...");
			final PathsEngine engine = new PathsEngine(db, porterStemmer, stopStems, options);

			System.out.println("Adding shutdown handler...");
			Runtime.getRuntime().addShutdownHook(new Thread() {
			    public void run() {
			    	try {
						_log.info("Shutting down engine...");
						engine.stop();
						_log.info("Shut down complete.");
			    	} finally {
			    		ManualResetLogManager.resetFinally();
			    	}
			    }
			 });
			
			System.out.println("Starting calculation engine...");
			Thread engineThread = new Thread(engine);
			engineThread.run();
			
		} finally {
			
			_log.info("Closing Mongo DB connection...");
			mongoClient.close();
		}
		
		_log.fine("DONE.");
	}
}
