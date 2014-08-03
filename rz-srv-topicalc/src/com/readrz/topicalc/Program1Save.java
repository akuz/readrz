package com.readrz.topicalc;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.akuz.core.FileUtils;
import me.akuz.core.Pair;
import me.akuz.core.Rounding;
import me.akuz.core.SortOrder;
import me.akuz.core.StringUtils;
import me.akuz.core.gson.GsonSerializers;
import me.akuz.core.sort.SelectK;
import Jama.Matrix;

import com.readrz.data.ontology.Entity;
import com.readrz.data.ontology.EntityList;
import com.readrz.data.ontology.Group;
import com.readrz.data.ontology.TopicWord;
import com.readrz.lang.corpus.Corpus;
import com.readrz.lang.corpus.TopicsUnstemmer;
import com.readrz.math.topicmodels.LDABuildTopic;

public final class Program1Save<TDocId> {
	
	private final static int PROB_DECIMAL_PLACES = 8;

	public Program1Save(
			Corpus<TDocId> corpus,
			List<Group> groups,
			List<LDABuildTopic> topics,
			Matrix mTopic, 
			Matrix mStemTopic,
			TopicsUnstemmer<TDocId> unstemmer,
			int topicOutputStemsCount,
			String outputDir) throws Exception {

		// ensure output directories exist
		if (!FileUtils.isDirExistsOrCreate(outputDir)) {
			throw new IllegalStateException("Could not open or create output dir: " + outputDir);
		}
		
		System.out.println("Save: saving groups... " + new Date(System.currentTimeMillis()));
		for (Group group : groups) {
			
			String shortFileName = group.getId().replaceAll("\\/", "_") + ".txt";
			String fullFileName = StringUtils.concatPath(outputDir, shortFileName);
			
			String groupJson = GsonSerializers.NoHtmlEscapingPretty.toJson(group.getDbo());
			FileUtils.writeEntireFile(fullFileName, groupJson);
		}
		
		System.out.println("Save: sorting stem by topic probs... " + new Date(System.currentTimeMillis()));
		List<List<Pair<Integer, Double>>> topicsSortedStemsLists = new ArrayList<>();
		{
			// reusable SelectK algo for selecting top stems
			SelectK<Integer, Double> selectK = new SelectK<>(SortOrder.Desc, topicOutputStemsCount);
			for (int topicIndex=0; topicIndex<topics.size(); topicIndex++) {
	
				// select top stems
				for (int stemIndex=0; stemIndex<mStemTopic.getRowDimension(); stemIndex++) {
					double prob = mStemTopic.get(stemIndex, topicIndex);
					selectK.add(new Pair<Integer, Double>(stemIndex, prob));
				}
				List<Pair<Integer, Double>> sortedTopicStemsList = selectK.get();

				// add to results lists
				topicsSortedStemsLists.add(sortedTopicStemsList);
			}
		}

		System.out.println("Save: init topic output string buffers... " + new Date(System.currentTimeMillis()));

		// total chars in one line:
		// 5 - probability, 1 - space, 10 - word = 16
		// 3 - spaces (between 4 columns)
		final int textWidth = 4 * 16 + 3;
		DecimalFormat fmtProb = new DecimalFormat("#.0000");
		DecimalFormat fmtInt = new DecimalFormat("0000");
		String separator1;
		{
			StringBuilder sb = new StringBuilder();
			for (int i=0; i<textWidth; i++) {
				sb.append("*");
			}
			separator1 = sb.toString();
		}
		String separator2;
		{
			StringBuilder sb = new StringBuilder();
			for (int i=0; i<textWidth; i++) {
				sb.append("-");
			}
			separator2 = sb.toString();
		}
		StringBuilder sb = new StringBuilder();
		
		sb.append(separator1 + "\n");
		sb.append("                       TOTAL TOPICS COUNT: " + topics.size() + "\n");
		sb.append(separator1 + "\n");

		System.out.println("Save: topics... " + new Date(System.currentTimeMillis()));

		// collect topics and groups
		final String rootEntityId = "e/topics";
		final String rootGroupId  = "g/topics";
		EntityList topicEntityList = new EntityList(rootEntityId);
		for (int topicIndex=0; topicIndex<topics.size(); topicIndex++) {
			
			System.out.println("Save: topic #" + (topicIndex+1) + "... " + new Date(System.currentTimeMillis()));

			// get topic data and stems
			LDABuildTopic topic = topics.get(topicIndex);
			List<Pair<Integer, Double>> topicSortedStemsList = topicsSortedStemsLists.get(topicIndex);
			
			// create output topic object
			double topicProb = mTopic.get(topicIndex, 0);
			topicProb = Rounding.round(topicProb, PROB_DECIMAL_PLACES);
			
			sb.append("\n");
			sb.append("\n");
			sb.append(separator2 + "\n");
			sb.append("#: " + fmtInt.format(topicIndex) + "  P: " + fmtProb.format(topicProb));
			sb.append("    " + topic.getTopicId() + "\n");
			sb.append(separator2 + "\n");
			sb.append("\n");
			
			// output text
			int rowCount = Math.min(topicSortedStemsList.size(), topicOutputStemsCount) / 4 + 1;
			for (int row=0; row<rowCount; row++) {
				
				for (int column=0; column<4; column++) {
					
					int index = row * 4 + column;
					if (index < topicSortedStemsList.size()) {
						
						Pair<Integer, Double> pair = topicSortedStemsList.get(index);
						Integer stemIndex = pair.v1();
						
						double stemProb = pair.v2();
						stemProb = Rounding.round(stemProb, PROB_DECIMAL_PLACES);
		
						Pair<String, String> pair2 = getStemAndWord(stemIndex, topicIndex, corpus, unstemmer);
						String word = pair2.v2();
						
						sb.append(fmtProb.format(stemProb));
						sb.append(" ");
						sb.append(StringUtils.trimOrFillSpaces(word.toLowerCase(), 10));
						
						if (column < 3) {
							sb.append(" ");
						}
					}
				}
				sb.append("\n");
			}
			
			// output entities
			if (!topic.isTransient()) {
				
				// create topic entity
				Entity topicEntity = new Entity(topic.getTopicId(), topic.getTopicId(), topicProb);
				topicEntityList.add(topicEntity);
				
				// add group id
				if (topic.isGroup()) {
					String groupId = rootGroupId + "/" + topic.getTopicId();
					topicEntity.addGroupId(groupId);
				}

				for (int i=0; i<topicSortedStemsList.size() && i < topicOutputStemsCount; i++) {
					
					Pair<Integer, Double> pair = topicSortedStemsList.get(i);
					Integer stemIndex = pair.v1();
					
					double stemProb = pair.v2();
					stemProb = Rounding.round(stemProb, PROB_DECIMAL_PLACES);
	
					Pair<String, String> pair2 = getStemAndWord(stemIndex, topicIndex, corpus, unstemmer);
					String stem = pair2.v1();
					String word = pair2.v2();
					
					// create entities for
					// non-auxiliary topics
					TopicWord topicWord = new TopicWord(word, stem, stemProb);
					topicEntity.addTopicWord(topicWord);
				}
			}
		}
		sb.append("\n");

		// save text
		System.out.println("Save: writing text file... " + new Date(System.currentTimeMillis()));
		String outText = sb.toString();
		String textFileName = StringUtils.concatPath(outputDir, "e_topics_text.txt");
		FileUtils.writeEntireFile(textFileName, outText);

		// save json
		System.out.println("Save: writing json file... " + new Date(System.currentTimeMillis()));
		String outJson = GsonSerializers.NoHtmlEscapingPretty.toJson(topicEntityList.getDbo());
		String jsonFileName = StringUtils.concatPath(outputDir, "e_topics_json.txt");
		FileUtils.writeEntireFile(jsonFileName, outJson);
		
		System.out.println("Saved. " + new Date(System.currentTimeMillis()));
	}
	
	private Pair<String, String> getStemAndWord(
			Integer stemIndex,
			Integer topicIndex,
			Corpus<TDocId> corpus,
			TopicsUnstemmer<TDocId> unstemmer) {
		
		String word;
		String stem = corpus.getStemsIndex().getValue(stemIndex);
		Integer wordIndex = unstemmer.getWordIndex(topicIndex, stemIndex);
		if (wordIndex != null) {
			word = corpus.getWordsIndex().getValue(wordIndex);
		} else {
			word = stem;
		}
		return new Pair<String, String>(stem, word);
	}
}
