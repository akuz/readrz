package com.readrz.topicalc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.akuz.core.FileUtils;
import me.akuz.core.Pair;
import me.akuz.core.PairComparator;
import me.akuz.core.Rounding;
import me.akuz.core.SortOrder;
import me.akuz.core.StringUtils;
import me.akuz.core.Triple;
import me.akuz.core.gson.GsonSerializers;

import Jama.Matrix;

import com.readrz.data.ontology.Entity;
import com.readrz.data.ontology.EntityList;
import com.readrz.data.ontology.TopicWord;
import com.readrz.lang.corpus.Corpus;
import com.readrz.lang.corpus.TopicsUnstemmer;

public final class Program3Save<TDocId> {
	
	private final static int PROB_DECIMAL_PLACES = 6;

	public Program3Save(
			Program3Options options,
			int outWordCount,
			Matrix mTopic,
			Matrix mTopicDoc, 
			Matrix mWordTopic,
			Corpus<TDocId> corpus,
			TopicsUnstemmer<TDocId> unstemmer) throws Exception {

		// collect topics
		EntityList topicEntityList = new EntityList("e/topics");
		for (int topicIndex=0; topicIndex<mTopic.getRowDimension(); topicIndex++) {
			
			// sort topic words
			List<Pair<Integer, Double>> listWords = new ArrayList<Pair<Integer,Double>>(mWordTopic.getRowDimension());
			for (int i=0; i<mWordTopic.getRowDimension(); i++) {
				listWords.add(new Pair<Integer, Double>(i, mWordTopic.get(i, topicIndex)));
			}
			Collections.sort(listWords, new PairComparator<Integer, Double>(SortOrder.Desc));
			
			// build topic words string
			List<Triple<String,String,Double>> topicWords = new ArrayList<Triple<String,String,Double>>();
			for (int i=0; i<listWords.size() && i<outWordCount; i++) {
				
				Pair<Integer, Double> pair = listWords.get(i);
				Integer stemIndex = pair.v1();
				Double stemProb = pair.v2();

				String word;
				String stem = corpus.getStemsIndex().getValue(stemIndex);
				Integer wordIndex = unstemmer.getWordIndex(topicIndex, stemIndex);
				if (wordIndex != null) {
					word = corpus.getWordsIndex().getValue(wordIndex);
				} else {
					word = stem + "*";
				}
				
				topicWords.add(new Triple<String,String,Double>(word, stem, stemProb));
			}

			// create output topic object
			double topicProb = mTopic.get(topicIndex, 0);
			topicProb = Rounding.round(topicProb, PROB_DECIMAL_PLACES);
			Entity topicEntity = new Entity("topic" + topicIndex, "Topic " + topicIndex, topicProb);
			topicEntityList.add(topicEntity);
								
			// set topic words
			for (int i=0; i<topicWords.size(); i++) {
				Triple<String, String, Double> triple = topicWords.get(i);
				double wordProb = Rounding.round(triple.v3(), PROB_DECIMAL_PLACES);
				TopicWord topicWord = new TopicWord(triple.v1(), triple.v2(), wordProb);
				topicEntity.addTopicWord(topicWord);
			}
		}

		// save topics
		String outJson = GsonSerializers.NoHtmlEscapingPretty.toJson(topicEntityList.getDbo());
		FileUtils.writeEntireFile(StringUtils.concatPath(options.getOutputDir(), "new_topics.txt"), outJson);
	}
}
