package com.readrz.parse.test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import me.akuz.core.FileUtils;
import me.akuz.core.Pair;
import me.akuz.core.Rounding;
import me.akuz.core.StringUtils;
import me.akuz.nlp.detect.SentencesDetector;
import me.akuz.nlp.detect.WordsDetector;
import me.akuz.nlp.porter.PorterStemmer;
import me.akuz.nlp.porter.PorterStopWords;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.readrz.data.Snap;
import com.readrz.data.index.FwdHit;
import com.readrz.data.index.FwdHits;
import com.readrz.data.index.KeysIndex;
import com.readrz.data.mongo.MongoColls;
import com.readrz.data.ontology.Ontology;
import com.readrz.data.ontology.TopicModel;
import com.readrz.lang.corpus.CorpusDoc;
import com.readrz.lang.corpus.CorpusPlace;
import com.readrz.lang.corpus.CorpusSentence;
import com.readrz.lang.parse.Const;
import com.readrz.lang.parse.PatternsDetector;
import com.readrz.lang.parse.SnapsParser;
import com.readrz.math.topicdetect.TopicsDetector;

public class Program {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {

		String fileName = null;
		String stopWordsFile = null;
		int loopSize  = 1;
		int loopCount = 1;
		{
			if (args != null) {
				for (int i=0; i<args.length; i++) {
					if (args[i].equals("-file")) {
						i++;
						if (i < args.length) {
							fileName = StringUtils.unquote(args[i]);
						}
					} else if (args[i].equals("-stopWordsFile")) {
						i++;
						if (i < args.length) {
							stopWordsFile = StringUtils.unquote(args[i]);
						}
					} else if (args[i].equals("-loopSize")) {
						i++;
						if (i < args.length) {
							loopSize = Integer.parseInt(args[i]);
						}
					} else if (args[i].equals("-loopCount")) {
						i++;
						if (i < args.length) {
							loopCount = Integer.parseInt(args[i]);
						}
					}
				}
			}
			if (fileName == null) {
				System.out.println("ERROR: Argument -file not provided");
				return;
			}
			if (stopWordsFile == null) {
				System.out.println("WARNING: Stop words file not specified");
			}
			if (loopSize < 1) {
				System.out.println("ERROR: Argument -loopSize must be >= 1");
			}
			if (loopCount < 1) {
				System.out.println("ERROR: Argument -loopCount must be >= 1");
			}
		}
		
		PorterStemmer porterStemmer = new PorterStemmer("_");

		Set<String> stopStems = null;
		if (stopWordsFile != null) {
			System.out.println("Loading stop words...");
			stopStems = PorterStopWords.loadStopWordsAndStemThem(porterStemmer, stopWordsFile);
		}
		
		System.out.println("Loading file...");
		File file = new File(fileName);
		String text = FileUtils.readEntireFile(file);
		Snap snap = new Snap(null, null, "", null, text);
		
		System.out.println("Opening db...");
		MongoClient mc = new MongoClient();
		DB db = mc.getDB("readrz");
		try {

			System.out.println("Loading keys...");
			KeysIndex keysIndex = new KeysIndex(db.getCollection(MongoColls.keys));
			keysIndex.loadFromDB();
			Set<Integer> stopKeyIds = keysIndex.getKeyIds(stopStems);
			
			System.out.println("Loading ontology...");
			Ontology ontology = new Ontology(keysIndex);
			ontology.loadFromDB(db);
			
			System.out.println("Creating topic model...");
			TopicModel topicModel = new TopicModel(ontology.getEntityListCatalog().getLists(), keysIndex);
			
			System.out.println("Validating topic model...");
			topicModel.validate();
			
//			System.out.println("Saving topic model matrices...");
//			MatrixUtils.writeMatrix("mTopicProb.log", topicModel.getTopicProb());
//			MatrixUtils.writeMatrix("mStemTopicProb.log", topicModel.getStemTopicProb());

			System.out.println("Creating parsers stack...");
			SentencesDetector sentencesParser = new SentencesDetector();
			PatternsDetector patternEntitiesParser = new PatternsDetector(ontology.getEntityListCatalog().getLists());
			WordsDetector wordsParser = new WordsDetector(porterStemmer);
			SnapsParser snapsParser = new SnapsParser(keysIndex, sentencesParser, patternEntitiesParser, wordsParser);
			TopicsDetector topicsDetector = new TopicsDetector(
					topicModel, 
					keysIndex, 
					stopKeyIds, 
					Const.TOPICS_DETECTOR_CONSIDER_TOPIC_COUNT_PER_PLACE,
					Const.TOPICS_DETECTOR_IGNORE_TOPIC_INDEX, 
					Const.TOPICS_DETECTOR_EXPECTED_DOC_WORDS_THRESHOLD, 
					Const.TOPICS_DETECTOR_EXPECTED_SENTENCE_WORDS_THRESHOLD);
			
			CorpusDoc doc = null;
			double msAvg = 0;
			for (int i=0; i<loopCount; i++) {
				System.out.println("Loop " + (i+1) +" of size " + loopSize + "...");
				double ms = System.currentTimeMillis();
				for (int j=0; j<loopSize; j++) {

					FwdHits fwdHits = snapsParser.parse(snap);
					doc = topicsDetector.step0_createCorpusDoc(snap, fwdHits);
					topicsDetector.step1_calcTopicProbs(doc);
					topicsDetector.step2_confirmTopics(doc);
					fwdHits = topicsDetector.step3_updateFwdHits(doc, fwdHits);

//					int estimatedTopicCount = (int)TopicCountEstimate.estimateTopicCountInDocument(doc.getPlaceCount(), 2, 200);
//					TopicOccurDetector topicOccurDetector = new TopicOccurDetector(0, estimatedTopicCount, 1.5);
//					topicOccurDetector.detectTopicOccurrences(doc);
				}
				ms = (System.currentTimeMillis() - ms) / loopSize;
				System.out.println("Loop ms per doc: " + ms);
				msAvg = msAvg / (i+1) * i + ms / (i+1);
			}
			
			System.out.println("Saving...");
			List<CorpusSentence> sentences = doc.getSentences();
			for (int sentenceLoc=0; sentenceLoc<sentences.size(); sentenceLoc++) {
				
				CorpusSentence sentence = sentences.get(sentenceLoc);
				List<CorpusPlace> places = sentence.getPlaces();
				
				for (int placeLoc=0; placeLoc<places.size(); placeLoc++) {
					
					CorpusPlace place = places.get(placeLoc);
					if (place.getStemIndex() > 0) {
					
						List<Pair<Integer, Double>> placeTopicProbs = (List<Pair<Integer, Double>>)place.getTag();
						FwdHit fwdHit = place.getFwdHit();
	
						String word = text.substring(fwdHit.getHit().start(), fwdHit.getHit().end());
						System.out.println();
						System.out.println(word);
						
						for (int j=0; j<placeTopicProbs.size(); j++) {
							
							Pair<Integer, Double> pair2 = placeTopicProbs.get(j);
							int topicIndex = pair2.v1();
							double topicProb = pair2.v2();
							topicProb = Rounding.round(topicProb, 6);
							String entityId = topicModel.getTopicEntity(topicIndex).getId();
							System.out.println(" - " + topicProb + "\t" + entityId);
						}
					}
				}
			}

			System.out.println();
			System.out.println("************************");
			System.out.println("    Text Topic Probs");
			System.out.println("************************");
			System.out.println();
			List<Pair<Integer, Double>> docTopicProbs = (List<Pair<Integer, Double>>)doc.getTag();
			for (int i=0; i<docTopicProbs.size(); i++) {
				Pair<Integer, Double> pair2 = docTopicProbs.get(i);
				Integer topicIndex = pair2.v1();
				double topicProb = Rounding.round(pair2.v2(), 6);
				String entityId = topicModel.getTopicEntity(topicIndex).getId();
				System.out.println(" - " + topicProb + "\t" + entityId);
			}
			System.out.println();
			
			System.out.println("Building HTML...");
			HtmlBuilder htmlBuilder = new HtmlBuilder(text);
			String html = htmlBuilder.buildHtml(topicModel, doc);
			FileUtils.writeEntireFile("output.html", html);

			System.out.println();
			System.out.println("Average ms per doc: " + msAvg);
			System.out.println("DONE.");
			
		} finally {
			mc.close();
		}
	}
}
