package com.readrz.clusters.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.akuz.core.DateUtils;
import me.akuz.core.HashIndex;
import me.akuz.core.Index;
import me.akuz.core.StringUtils;
import me.akuz.core.logs.LogUtils;
import me.akuz.nlp.porter.PorterStemmer;
import me.akuz.nlp.porter.PorterStopWords;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.readrz.data.Snap;
import com.readrz.data.SnapInfo;
import com.readrz.data.index.FwdHits;
import com.readrz.data.index.KeysIndex;
import com.readrz.data.mongo.MongoColls;
import com.readrz.lang.corpus.CorpusDoc;
import com.readrz.lang.corpus.CorpusSentence;
import com.readrz.lang.corpus.TopicsCorpus;
import com.readrz.math.merging.snaps.Cluster1;
import com.readrz.math.merging.snaps.Cluster1Algo;
import com.readrz.math.merging.snaps.ClusterDocument;
import com.readrz.math.merging.snaps.ClusterSentence;
import com.readrz.search.QueryKeyIds;
import com.readrz.search.SnapSearch;
import com.readrz.search.SnapSearchResult;
import com.readrz.search.SnapSearcher;

public class Program {

	public static void main(String[] args) throws IOException {
		
		LogUtils.configure(Level.FINEST);
		Logger _log = LogUtils.getLogger(Program.class.getName());

		_log.info("Connecting to db...");
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		DB db = mongoClient.getDB("readrz");
		
		_log.info("Loading keys index...");
		KeysIndex keysIndex = new KeysIndex(
				db.getCollection(MongoColls.keys));
		keysIndex.loadFromDB();
		
		_log.info("Loading stop words...");
		PorterStemmer ps = new PorterStemmer("_");
		Set<String> stopStems = PorterStopWords.loadStopWordsAndStemThem(ps, "/Users/andrey/Src/Readrz/Deploy/Server/bin/stop_words.txt");
		Set<Integer> stopKeyIds = keysIndex.getKeyIds(stopStems);

		_log.info("Loading docs...");
		SnapSearcher snapSearcher = new SnapSearcher(
				db.getCollection(MongoColls.snaps), 
				db.getCollection(MongoColls.snapsidx), 
				db.getCollection(MongoColls.feeds), 
				db.getCollection(MongoColls.sources));
		
		QueryKeyIds queryKeyIds = new QueryKeyIds();
//		Integer keyId = keysIndex.getIdCached("g/topics/finance");
//		Integer keyId = keysIndex.getIdCached("g/topics/business/sectors/technology");
//		Integer keyId = keysIndex.getIdCached("g/topics/business/sectors/energy");
//		Integer keyId = keysIndex.getIdCached("e/lists/countries/russia");
//		Integer keyId = keysIndex.getIdCached("g/countries/un_region/asia");
//		queryKeyIds.addDocumentKeyId(keyId);
		
		final double MINUTES = 1 * 24 * 60;
		final double MIN_MINUTES_DIFF_TO_MERGE = 120;
		final int ITERATION_COUNT = 5;
		
		SnapSearch search = snapSearcher.startSearch(false, queryKeyIds, DateUtils.addMinutes(new Date(), -MINUTES), new Date(), 10000);
		
		Index<String> stemsIndex = new HashIndex<>();
		Map<Integer, Integer> keyIdByStemIndex = new HashMap<>();
		List<ClusterSentence> sentences = new ArrayList<>();
		while (true) {
			
			SnapSearchResult result = search.findNext();
			if (result == null) {
				break;
			}
			
			SnapInfo snapInfo = snapSearcher.findSnapInfo(result.getSnapId());
			if (snapInfo == null) {
				continue;
			}
			
			Snap snap = snapInfo.getSnap();
			FwdHits fwdHits = new FwdHits(snap.getFwdHitsData());
			
			CorpusDoc corpusDoc = TopicsCorpus.createDocForTopics(snap, fwdHits, true, keysIndex, stopKeyIds, stemsIndex, null, true);
			if (corpusDoc.getLength() < 3) {
				continue;
			}
			
			ClusterDocument document = new ClusterDocument(snapInfo);

			List<CorpusSentence> corpusSentences = corpusDoc.getSentences();
			for (int sentenceIndex=0; sentenceIndex<corpusSentences.size(); sentenceIndex++) {
				
				CorpusSentence corpusSentence = corpusSentences.get(sentenceIndex);
				ClusterSentence sentence = new ClusterSentence(document, sentenceIndex, corpusSentence, keyIdByStemIndex);
				
				if (sentence.getStemCount() > 0) {
					
					sentences.add(sentence);
					document.addSentence(sentence);
				}
			}
		}
		_log.info("Found " + sentences.size() + " sentences");

		_log.info("Clustering documents by sentences...");
		
		Cluster1Algo clusterAlgo = new Cluster1Algo(sentences, MIN_MINUTES_DIFF_TO_MERGE, ITERATION_COUNT);
		List<Cluster1> clusters1 = clusterAlgo.getClusters1();
		
		System.out.println("Clusters:");
		for (int i=0; i<clusters1.size() && i<100; i++) {
			Cluster1 cluster1 = clusters1.get(i);
			
			System.out.println("#" + StringUtils.trimOrFillSpaces("" + (i+1), 3) + " ***** " + cluster1.getDocumentCount() + " docs");

			StringBuilder sb = new StringBuilder();
			cluster1.getBaseCluster().print(sb, stemsIndex);
			System.out.print(sb.toString());
		}
		
		_log.info("Preparing summary items...");
		
		// TODO

		_log.info("DONE.");
	}
	
}
