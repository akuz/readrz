package com.readrz.sentiment.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import me.akuz.core.DateUtils;
import me.akuz.core.FileUtils;
import me.akuz.core.Hit;
import me.akuz.core.Pair;
import me.akuz.core.PairComparator;
import me.akuz.core.SortOrder;
import me.akuz.core.logs.LogUtils;
import me.akuz.core.math.NIGDist;
import me.akuz.core.math.SampleAverage;
import me.akuz.nlp.porter.PorterStemmer;
import me.akuz.nlp.porter.PorterWordsSentiment;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.readrz.data.Snap;
import com.readrz.data.SnapInfo;
import com.readrz.data.index.FwdHit;
import com.readrz.data.index.FwdHitKind;
import com.readrz.data.index.FwdHits;
import com.readrz.data.index.FwdHitsMap;
import com.readrz.data.index.KeysIndex;
import com.readrz.data.mongo.MongoColls;
import com.readrz.data.ontology.Ontology;
import com.readrz.search.Query;
import com.readrz.search.QueryKeyIds;
import com.readrz.search.QueryParser;
import com.readrz.search.SnapSearch;
import com.readrz.search.SnapSearchResult;
import com.readrz.search.SnapSearcher;

public final class ProgramLogic {

	private final Logger _log = LogUtils.getLogger(Program.class.getName());

	public void execute(
			String  mongoServer, 
			Integer mongoPort, 
			String  mongoDb,
			ProgramOptions options) throws IOException {

		_log.info("Connecting to db...");
		MongoClient mongoClient = new MongoClient(mongoServer, mongoPort);
		DB db = mongoClient.getDB(mongoDb);
		
		_log.info("Loading keys index...");
		KeysIndex keysIndex = new KeysIndex(
				db.getCollection(MongoColls.keys));
		keysIndex.loadFromDB();
		
		System.out.println("Loading ontology...");
		Ontology ontology = new Ontology(keysIndex);
		ontology.loadFromDB(db);

		PorterStemmer porterStemmer = new PorterStemmer("_");
		
//		_log.info("Loading stop words...");
//		Set<String> stopStems = StopWordUtils.loadStopStemsFromStopWordsFile("/Users/andrey/Src/Readrz/Deploy/Server/bin/stop_words.txt");
//		Set<Integer> stopKeyIds = keysIndex.getKeyIds(stopStems);
		
		_log.info("Loading words sentiment...");
		PorterWordsSentiment wordsSentiment = new PorterWordsSentiment();
		wordsSentiment.load(options.getWordsSentimentFile());
		Map<String, SampleAverage> stemSentimentMap = wordsSentiment.getStemSentimentMap();
		Map<Integer, Double> keyIdSentimentMap = new HashMap<>();
		for (String stem : stemSentimentMap.keySet()) {
			String stemId = String.format("%s%s", stem, "_");
			Integer keyId = keysIndex.getId(stemId);
			keyIdSentimentMap.put(keyId, stemSentimentMap.get(stem).getMean());
		}

		_log.info("Parsing query...");
		QueryParser queryParser = new QueryParser(porterStemmer, keysIndex, ontology);
		Query query = queryParser.parse(options.getQueryString());
		QueryKeyIds queryKeyIds = query.getQueryKeyIds();

		_log.info("Loading data...");
		SnapSearcher snapSearcher = new SnapSearcher(
				db.getCollection(MongoColls.snaps), 
				db.getCollection(MongoColls.snapsidx), 
				db.getCollection(MongoColls.feeds), 
				db.getCollection(MongoColls.sources));
		SnapSearch search = snapSearcher.startSearch(false, queryKeyIds, options.getMinDateInc(), options.getMaxDateExc(), 100000);
		
		_log.info("Collecting sentiment...");
		Map<Date, NIGDist> dateSentimentMap = new HashMap<>();
		int resultsCounter = 0;
		while (true) {
			
			SnapSearchResult result = search.findNext();
			if (result == null) {
				break;
			}
			Set<Integer> sentenceHitStarts = result.getSentenceHitStarts();
			
			SnapInfo snapInfo = snapSearcher.findSnapInfo(result.getSnapId());
			if (snapInfo == null) {
				continue;
			}
			Snap snap = snapInfo.getSnap();
			
			Date date = DateUtils.removeMinutesUTC(snap.getSrcDate());
			NIGDist dateSentimentDist = dateSentimentMap.get(date);
			if (dateSentimentDist == null) {
				// FIXME: prior for the sentiment
				dateSentimentDist = new NIGDist(0, 10, 1, 10);
				dateSentimentMap.put(date, dateSentimentDist);
			}

			FwdHits fwdHits = new FwdHits(snap.getFwdHitsData());
			while (fwdHits.nextSentence()) {
				
				Hit sentenceHit = fwdHits.getSentenceHit();
				if (sentenceHitStarts == null || sentenceHitStarts.contains(sentenceHit)) {
					
					FwdHitsMap fwdHitsMap = fwdHits.getSentenceHits(EnumSet.of(FwdHitKind.WORD));
					List<FwdHit> wordFwdHits = fwdHitsMap.get(FwdHitKind.WORD);
					
					if (wordFwdHits != null) {
						for (int i=0; i<wordFwdHits.size(); i++) {
							
							FwdHit fwdHit = wordFwdHits.get(i);
							Double keySentiment = keyIdSentimentMap.get(fwdHit.getKeyId());
							
							if (keySentiment != null) {
								dateSentimentDist.addObservation(keySentiment);
							}
						}
					}
				}
			}
			
			resultsCounter++;
			if (resultsCounter % 100 == 0) {
				_log.info("Processed " + resultsCounter + " articles");
			}
		}
		_log.info("Processed " + resultsCounter + " articles");
		
		_log.info("Normalizing sentiment...");
		List<Pair<Double, Date>> sortedSentiment = new ArrayList<>();
		for (Entry<Date, NIGDist> entry : dateSentimentMap.entrySet()) {
			Date date = entry.getKey();
			NIGDist sentimentDist = entry.getValue();
			double sentiment = sentimentDist.getMeanMode(); // / Math.sqrt(sentimentDist.getVarianceMode());
			sortedSentiment.add(new Pair<Double, Date>(sentiment, date));
		}
		Collections.sort(sortedSentiment, new PairComparator<Double, Date>(SortOrder.Asc));
		
		_log.info("Writing output file...");
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<sortedSentiment.size(); i++) {
			Pair<Double, Date> pair = sortedSentiment.get(i);
			Double sentiment = pair.v1();
			Date date = pair.v2();
			sb.append(date);
			sb.append(",");
			sb.append(sentiment);
			sb.append("\n");
		}
		FileUtils.writeEntireFile(options.getOutputFile(), sb.toString());
	
		_log.info("DONE.");		
	}
}
