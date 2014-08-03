package com.readrz.zzz.paths;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import me.akuz.core.Pair;
import me.akuz.core.StringUtils;
import me.akuz.nlp.porter.PorterWordsSentiment;

import com.readrz.zzz.sentiment.SentimentAggregator;

public final class PathsTree {
	
	public final DecimalFormat FMT_0 = new DecimalFormat("0");
	public final DecimalFormat FMT_0_00 = new DecimalFormat("0.00");
	
	private final Set<String> _ignoreStems;
	private final Path _rootPath;
	
	public PathsTree(Set<String> ignoreStems, Path rootPath) {
		_ignoreStems = ignoreStems;
		_rootPath = rootPath;
	}
	
	public Set<String> getIgnoreStems() {
		return _ignoreStems;
	}
	
	public Path getRootPath() {
		return _rootPath;
	}

	public String print() {

		StringBuilder sb = new StringBuilder();
		
		Deque<Path> queue = new LinkedList<Path>();
		queue.add(_rootPath);
		
		while (queue.size() > 0) {
			
			Path path = queue.pollFirst();
			
			boolean writingThisPath = false;
			
			if (path.getLeafCondition() == null) {

				writingThisPath = true;
				
//				sb.append(path.getLevel() + "-");
				sb.append("<all> ");
//				sb.append("-" + path.getLevelPositiveSiblingsCount());
				
			} else if (path.getLeafCondition().isPositive()) {
				
				writingThisPath = true;
				
				int trueDepth = path.getLevel();
				for (int i=0; i<trueDepth; i++) {
					sb.append("  | ");
				}
//				sb.append(path.getLevel() + "-");
				sb.append(path.getLeafCondition().getStem());
				sb.append(" ");
//				sb.append("-" + path.getLevelPositiveSiblingsCount());
				
			} else if (path.getChildren().size() == 0) {
				
				writingThisPath = true;
				
				int trueDepth = path.getLevel();
				for (int i=0; i<trueDepth; i++) {
					sb.append("  | ");
				}
				sb.append("");
//				sb.append(path.getLevel() + "-" + "<");
//				sb.append(path.getLeafCondition().getStem());
//				sb.append(">" + "-" + path.getLevelPositiveSiblingsCount());
			}
			
			if (writingThisPath) {
			
				PathStats pathStats = path.getStats();
				double pathSumDocProb = pathStats.getSumAdjustedPhraseProb();
//				Map<String, Double> pathStatsStemProbs = pathStats.getStemProbs();
				
				sb.append("(");
				sb.append(FMT_0.format(pathSumDocProb));
				sb.append(")");
				
				if (path.getChildren().size() == 0)  {
					
					int MAX_TOTAL_WORDS = 8;
					int displayedWords = path.getLevel();
					
					List<Pair<String, Double>> pathStemsByExpectedWeight = pathStats.getStemsSortedByExpectedWeight();
					
					for (int i=0; i<pathStemsByExpectedWeight.size() && displayedWords<MAX_TOTAL_WORDS; i++) {
						
						Pair<String, Double> pair = pathStemsByExpectedWeight.get(i);
						String stem = pair.v1();
//						Double expectedWeight = pair.v2();
//						Double stemProb = pathStatsStemProbs.get(stem);
						
						if ((_ignoreStems == null || _ignoreStems.contains(stem) == false) 
							&& path.getAllPositiveConditionsStems().contains(stem) == false) {
							
							sb.append(" ");
							sb.append(stem);
//							sb.append("/");
//							sb.append(_fmt_0_00.format(expectedWeight));
//							sb.append(" *");
//							sb.append(_fmt_0_00.format(stemProb));
//							sb.append("=");
//							sb.append(_fmt_0_00.format(stemProb * pathSumDocProb));
							
							displayedWords += 1;
						}
					}
				}
				
				sb.append("\n");
			}

			for (int i=path.getChildren().size()-1; i>=0; i--) {
				queue.addFirst(path.getChildren().get(i));
			}
		}
		
		return sb.toString();	
	}

	public PathsTreeNode exportAsRegularTree(PorterWordsSentiment wordsSentSet, int maxQuotesPerNode) {

		List<PathsTreeNode> stack = new ArrayList<PathsTreeNode>();

		Deque<Path> queue = new LinkedList<Path>();
		queue.add(_rootPath);
		
		while (queue.size() > 0) {
			
			Path path = queue.pollFirst();
			PathCondition pathCond = path.getLeafCondition();
			
			int pathLevel = path.getLevel();
			
			if (pathCond == null || pathCond.isPositive()) {

				PathStats pathStats = path.getStats();
				List<Pair<String, Double>> pathStemsByExpectedWeight = pathStats.getStemsSortedByExpectedWeight();

				// initialize path sentiment
				SentimentAggregator pathSentimentAggr = new SentimentAggregator();
				pathSentimentAggr.add(pathStats.getSumAdjustedPhraseProb() / 10.0, 0.0);

				// number of words to check
				int MAX_SENTIMENT_CALC_WORDS = 8;

				// update path sentiment with words
				for (int i=0; i<pathStemsByExpectedWeight.size() && i<MAX_SENTIMENT_CALC_WORDS; i++) {
					
					Pair<String, Double> pair = pathStemsByExpectedWeight.get(i);
					String stem = pair.v1();
					Double expectedWeight = pair.v2();
					
					// update sentiment
					String word = pathStats.getWordsByStem().get(stem);
					String lovercaseWord = StringUtils.toLowercaseFirstLetter(word);
					Integer wordSentiment = wordsSentSet.getByWord(lovercaseWord);
					if (wordSentiment != null) {
						if (_ignoreStems == null || _ignoreStems.contains(stem) == false) {
							double weight = pathStats.getSumAdjustedPhraseProb() * expectedWeight;
							pathSentimentAggr.add(weight, wordSentiment);
						}
					}
				}

				// select best quote
				PathQuoteSelectorBest pathQuoteSelectorBest = new PathQuoteSelectorBest(path);
				PathQuote pathBestQuote = pathQuoteSelectorBest.getBestQuote();

				// fill list item
				String pathStem = null;
				String pathWord = null;
				if (pathCond != null) {
					pathStem = pathCond.getStem();
					pathWord = pathStats.getWordsByStem().get(pathStem);
				}
				PathsTreeNode node = new PathsTreeNode(
						pathLevel, 
						pathStem, 
						pathWord, 
						pathStats, 
						pathSentimentAggr, 
						pathBestQuote);

//				// add extra nodes
//				if (path.getChildren().size() == 0) {
//					
//					int addedLastLevelWords = 0;
//					int MAX_LAST_LEVEL_WORDS = 3;
//					for (int i=0; i<pathStemsByExpectedWeight.size() && addedLastLevelWords<MAX_LAST_LEVEL_WORDS; i++) {
//						
//						Pair<String, Double> pair = pathStemsByExpectedWeight.get(i);
//						String stem = pair.v1();
//						Double expectedWeight = pair.v2();
//						
//						if (_ignoreStems == null || _ignoreStems.contains(stem) == false) {
//							
//							if (path.getAllPositiveConditionsStems().contains(stem) == false) {
//								
//								SentimentAggregator extraSentiment = new SentimentAggregator();
//
//								String word = pathStats.getWordsByStem().get(stem);
//								String lovercaseWord = StringUtils.toLowercaseFirstLetter(word);
//								Integer wordSentiment = wordsSentSet.getByWord(lovercaseWord);
//								if (wordSentiment != null) {
//									double weight = pathStats.getSumAdjustedPhraseProb() * expectedWeight;
//									extraSentiment.add(weight, wordSentiment);
//								}
//								
//								PathsTreeNode extraNode = new PathsTreeNode(
//										pathLevel + 1, 
//										stem, 
//										word, 
//										pathStats, 
//										extraSentiment, 
//										null,
//										null);
//								
//								node.addChild(extraNode);
//
//								addedLastLevelWords += 1;
//							}
//						}
//					}
//				}
				
				// ********************
				// stack update - BEGIN
				// --------------------
				
				// check if jumped too far
				if (stack.size() < pathLevel) {
					throw new IllegalStateException();
				}

				// shorten the stack
				while (stack.size() > pathLevel) {
					stack.remove(stack.size()-1);
				}

				// update higher levels sentiment
				for (int i=0; i<stack.size(); i++) {
					stack.get(i).getSentimentAggregator().add(pathSentimentAggr);
				}
				
				// deepen the stack
				if (stack.size() > 0) {
					stack.get(stack.size()-1).addChild(node);
				}
				stack.add(node);
				
				// ------------------
				// stack update - END
				// ******************
			}

			for (int i=path.getChildren().size()-1; i>=0; i--) {
				queue.addFirst(path.getChildren().get(i));
			}
		}
		
		return stack.get(0);
	}


}
