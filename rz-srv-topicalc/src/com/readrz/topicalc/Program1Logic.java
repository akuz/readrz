package com.readrz.topicalc;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;

import me.akuz.core.logs.ManualResetLogManager;
import me.akuz.nlp.porter.PorterStemmer;
import me.akuz.nlp.porter.PorterStopWords;

import org.bson.types.ObjectId;

import Jama.Matrix;

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
import com.readrz.data.ontology.Group;
import com.readrz.data.ontology.Ontology;
import com.readrz.lang.corpus.TopicsUnstemmer;
import com.readrz.lang.corpus.TopicsCorpus;
import com.readrz.math.topicmodels.LDABuildAlpha;
import com.readrz.math.topicmodels.LDABuildBeta;
import com.readrz.math.topicmodels.LDABuildTopic;
import com.readrz.math.topicmodels.LDABuildTopicsTree;
import com.readrz.math.topicmodels.LDAGibbs;
import com.readrz.search.QueryParser;

public final class Program1Logic {
	
	private static final int    DOC_MIN_TOPIC_COUNT              = 2;
	private static final int    DOC_LENGTH_FOR_FIRST_EXTRA_TOPIC = 100;
	private static final int    MIN_DOC_LENGTH                   = 10;
	private static final double BACKGROUND_TOPIC_PRIOR_PROB      = 0.5;
	private static final int    TRANSIENT_TOPICS_COUNT           = 20;
	private static final double TRANSIENT_TOPIC_PRIOR_PROB       = 0.001;
	private static final double UNNECESSARY_TOPIC_PRIOR_PROB     = 0.001;
	private static final double PRIORITY_TOPIC_PRIOR_PROB        = 0.01;
	private static final double PRIORITY_STEMS_MASS_FRACTION     = 0.75;
	
	private static final DecimalFormat _doubleFormat = new DecimalFormat("0.0#######");;

	public final void execute(Program1Options options) throws Exception {
		
		System.out.println("Loading stop words...");
		PorterStemmer porterStemmer = new PorterStemmer("_");
		Set<String> stopStems = PorterStopWords.loadStopWordsAndStemThem(porterStemmer, options.getStopWordsFile());

		// create corpus objects
		KeysIndex keysIndex = null;
		QueryParser queryParser = null;
		TopicsCorpus corpus = null;

		System.out.println("Connecting to db...");
		final MongoClient mongoClient = new MongoClient(options.getMongoServer(), options.getMongoPort());
		try {

			System.out.println("Getting database: " + options.getMongoDb());
			final DB db = mongoClient.getDB(options.getMongoDb());
			
			System.out.println("Loading keys...");
			keysIndex = new KeysIndex(db.getCollection(MongoColls.keys));
			keysIndex.loadFromDB();
			Set<Integer> stopKeyIds = keysIndex.getKeyIds(stopStems);
			
			System.out.println("Loading ontology...");
			Ontology ontology = new Ontology(keysIndex);
			ontology.loadFromDB(db);
			
			System.out.println("Creating query parser...");
			queryParser = new QueryParser(porterStemmer, keysIndex, ontology);

			System.out.println("Loading corpus...");
			corpus = new TopicsCorpus(keysIndex, stopKeyIds, MIN_DOC_LENGTH);
			SnapsLoader loader = new SnapsLoader(new SnapsListener[]{corpus});
			BasicDBList extraConditions = new BasicDBList();
			DBObject extraCondition = new BasicDBObject()
				.append(Snap._isIndexed, true)
				.append(Snap._isDupChecked, true)
				.append(Snap._isDuplicate, false);
			extraConditions.add(extraCondition);
			loader.load(
					db.getCollection(MongoColls.snaps), 
					options.getMinDateInc(), 
					options.getMaxDateExc(), 
					extraConditions);
			System.out.println("Corpus: " + corpus.getPlaceCount() + " places.");
			System.out.println("Corpus: " + corpus.getStemsIndex().size() + " stems.");
			System.out.println("Corpus: " + corpus.getDocCount() + " docs.");
			
		} finally {
			
			System.out.println("Closing db connection...");
			mongoClient.close();
		}

		System.out.println("Building topics tree...");
		LDABuildTopicsTree buildTopicsTree 
			= BuildTopicsTree.buildReadrzTopicTree(
					BACKGROUND_TOPIC_PRIOR_PROB,
					TRANSIENT_TOPICS_COUNT,
					TRANSIENT_TOPIC_PRIOR_PROB,
					UNNECESSARY_TOPIC_PRIOR_PROB,
					PRIORITY_TOPIC_PRIOR_PROB,
					PRIORITY_STEMS_MASS_FRACTION,
					queryParser, 
					keysIndex, 
					corpus.getStemsIndex());

		System.out.println("Building topic groups...");
		List<Group> groups = buildTopicsTree.buildGroups("g/topics");
		System.out.println("Built " + groups.size() + " root groups.");

		System.out.println("Building topics...");
		List<LDABuildTopic> topics = buildTopicsTree.buildTopics();
		System.out.println();
		System.out.println("TOPICS:");
		System.out.println();
		for (int i=0; i<topics.size(); i++) {
			System.out.println(topics.get(i).getTopicId());
		}
		System.out.println();

		System.out.println("Preparing hyperparameters...");
		LDABuildAlpha<ObjectId> buildAlpha = new LDABuildAlpha<>(corpus, topics, DOC_MIN_TOPIC_COUNT, DOC_LENGTH_FOR_FIRST_EXTRA_TOPIC);
		LDABuildBeta<ObjectId> buildBeta = new LDABuildBeta<>(corpus, topics);

		System.out.println("Creating topics algorithm...");
		final LDAGibbs<ObjectId> ldaGibbs = new LDAGibbs<ObjectId>(
				topics,
				buildAlpha,
				buildBeta,
				corpus,
				options.getThreadCount());

		System.out.println("Adding shutdown handler...");
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
		    	try {
					System.out.println("Shutting down topics algorithm...");
					ldaGibbs.terminate();
					System.out.println("Topics algo shut down complete.");
		    	} finally {
		    		ManualResetLogManager.resetFinally();
		    	}
		    }
		 });
		
		System.out.println("Preparing unstemmer...");
		int stemCount = corpus.getStemsIndex().size();
		TopicsUnstemmer<ObjectId> unstemmer = new TopicsUnstemmer<ObjectId>(corpus);
		
		System.out.println("Preparing average matrices...");
		Matrix mStemTopic_average = new Matrix(stemCount, ldaGibbs.getTopicCount());
		Matrix mTopic_average = new Matrix(ldaGibbs.getTopicCount(), 1);
		int iteration = 1;
		
		System.out.println("Burning in Gibbs sampler (" + options.getBurnInStepCount() + " steps)...");
		for (int burnInStepIndex=0; burnInStepIndex<options.getBurnInStepCount(); burnInStepIndex++) {
			
			double temperature = 1.0 + (options.getSampleTemperature() - 1.0) / options.getBurnInStepCount() * burnInStepIndex;
			ldaGibbs.setTemperature(temperature);

			System.out.println("Running " + options.getBurnInStepIterations() + " iterations at temperature " + _doubleFormat.format(temperature) + "...");
			iteration = ldaGibbs.run(iteration, options.getBurnInStepIterations());
		}
		
		System.out.println("Setting sample temperature " + _doubleFormat.format(options.getSampleTemperature()) + "...");
		ldaGibbs.setTemperature(options.getSampleTemperature());

		final int lastSampleIndex = options.getSampleCount() - 1;
		System.out.println("Taking " + options.getSampleCount() + " samples...");
		for (int sampleIndex=0; sampleIndex<options.getSampleCount(); sampleIndex++) {
			
			System.out.println("Running 1 iteration and taking sample #" + (sampleIndex+1) + "...");
			iteration = ldaGibbs.run(iteration, 1);
			
			// update sample averages
			ldaGibbs.calcStemTopic(mStemTopic_average);
			ldaGibbs.calcTopicProbs(mTopic_average);
			
			// sample unstemmer
			unstemmer.sample();
			
			// output progress
			if (sampleIndex < lastSampleIndex && options.getSampleGap() > 0) {
				
				System.out.println("Running " + options.getSampleGap() + " gap iterations between samples...");
				iteration = ldaGibbs.run(iteration, options.getSampleGap());
			}
		}
		
		System.out.println("Terminating algorithm...");
		ldaGibbs.terminate();
		
		System.out.println("Optimizing unstemmer...");
		unstemmer.optimize();
		
		System.out.println("Saving results...");
		new Program1Save<ObjectId>(
				corpus, 
				groups,
				topics, 
				mTopic_average, 
				mStemTopic_average,
				unstemmer, 
				options.getOutWordCount(), 
				options.getOutputDir());
		
		System.out.println("Finished logic.");
	}

}
