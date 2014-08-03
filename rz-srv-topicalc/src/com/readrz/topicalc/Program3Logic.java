package com.readrz.topicalc;

import java.io.File;
import java.util.Date;
import java.util.Random;
import java.util.Set;

import me.akuz.core.Pair;
import me.akuz.core.RepeatedValue;
import me.akuz.core.math.DataSplitUtils;
import me.akuz.nlp.porter.PorterStemmer;
import me.akuz.nlp.porter.PorterStopWords;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.readrz.data.Snap;
import com.readrz.data.SnapsListener;
import com.readrz.data.SnapsLoader;
import com.readrz.data.index.KeysIndex;
import com.readrz.data.mongo.MongoColls;
import com.readrz.lang.corpus.TopicsUnstemmer;
import com.readrz.lang.corpus.TopicsCorpus;
import com.readrz.math.topicmodels.LDAVarEM;
import com.readrz.math.topicmodels.LDAVarEMParams;

public final class Program3Logic {

	public final void execute(Program3Options options) throws Exception {

		Random rnd = new Random((new Date()).getTime());
		
		// Ensure Output Folder
		File dir = new File(options.getOutputDir());
		if (!dir.exists()) {
			dir.mkdir();
		}
		
		System.out.println("Loading stop words...");
		PorterStemmer ps = new PorterStemmer("_");
		Set<String> stopStems = PorterStopWords.loadStopWordsAndStemThem(ps, options.getStopWordsFile());

		// create corpus objects
		KeysIndex keysIndex = null;
		TopicsCorpus corpus = null;

		System.out.println("Connecting to Mongo DB...");
		final MongoClient mongoClient = new MongoClient(options.getMongoServer(), options.getMongoPort());
		try {

			System.out.println("Getting database: " + options.getMongoDb());
			final DB db = mongoClient.getDB(options.getMongoDb());
			
			System.out.println("Loading keys...");
			keysIndex = new KeysIndex(db.getCollection(MongoColls.keys));
			keysIndex.loadFromDB();
			Set<Integer> stopKeyIds = keysIndex.getKeyIds(stopStems);
			
			System.out.println("Loading corpus...");
			final int MIN_DOC_LENGTH = 10;
			corpus = new TopicsCorpus(keysIndex, stopKeyIds, MIN_DOC_LENGTH);
			SnapsLoader loader = new SnapsLoader(new SnapsListener[]{corpus});
			BasicDBList extraConditions = new BasicDBList();
			DBObject extraCondition = new BasicDBObject().append(Snap._isIndexed, true);
			extraConditions.add(extraCondition);
			loader.load(db.getCollection(MongoColls.snaps), options.getMinDateInc(), options.getMaxDateExc(), extraConditions);
			
		} finally {
			
			System.out.println("Closing Mongo DB connection...");
			mongoClient.close();
		}
		
		// Unstemmer
		System.out.println();
		System.out.println(new Date(System.currentTimeMillis()));
		System.out.println("Preparing unstemmer ----------------------------------");
		TopicsUnstemmer<ObjectId> unstemmer = new TopicsUnstemmer<ObjectId>(corpus);

		// LDA Gibbs
		System.out.println();
		System.out.println(new Date(System.currentTimeMillis()));
		System.out.println("Preparing LDA Variational EM -------------------------");
		
		LDAVarEMParams params = new LDAVarEMParams();

		double alpha = 2.0 / options.getTopicCount();
		params.addAlphas(new RepeatedValue<Double>(
				options.getAuxTopicCount() + options.getTopicCount(), alpha));
		
		if (options.getAuxTopicCount() > 0) {
			params.addTopicsBeta(new RepeatedValue<Double>(options.getAuxTopicCount(), options.getAuxTopicBeta()));
		}
		
		if (options.getTopicCount() > 0) {
			params.addTopicsBeta(new RepeatedValue<Double>(options.getTopicCount(), options.getTopicBeta()));
		}
		
		final double TRAIN_DATA_PERCENTAGE = 0.8;
		Pair<int[], int[]> split = DataSplitUtils.splitAtRandom(corpus.getDocs().size(), TRAIN_DATA_PERCENTAGE, rnd);
		int[] trainLocs = split.v1();
		//int[] validLocs = split.v2();

		System.out.println();
		System.out.println(new Date(System.currentTimeMillis()));
		System.out.println("Running LDA Var EM -----------------------------------");

		LDAVarEM<ObjectId> lda = new LDAVarEM<>(corpus, params);
		lda.iterate(trainLocs, 10);
		
		System.out.println();
		System.out.println(new Date(System.currentTimeMillis()));
		System.out.println("Optimizing Unstemmer ---------------------------------");
		unstemmer.optimize();
		
		System.out.println();
		System.out.println(new Date(System.currentTimeMillis()));
		System.out.println("Saving Results ---------------------------------------");
		final int OUT_WORD_COUNT = 20;
		new Program3Save<ObjectId>(options, OUT_WORD_COUNT, lda.getAlphaTopic(), lda.getGammaTopicDoc(), lda.getStemByTopicProbs(), corpus, unstemmer);
		
		System.out.println();
		System.out.println(new Date(System.currentTimeMillis()));
		System.out.println("Finished Program Logic -------------------------------");
	}

}
