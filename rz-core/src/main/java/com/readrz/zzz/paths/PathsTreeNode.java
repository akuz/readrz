package com.readrz.zzz.paths;

import java.util.ArrayList;
import java.util.List;

import com.readrz.zzz.sentiment.SentimentAggregator;

public final class PathsTreeNode {
	
	private final int _level;
	private final String _stem;
	private final String _word;
	private final PathStats _pathStats;
	private final SentimentAggregator _sentimentAggregator;
	private final PathQuote _bestQuote;
	private final List<PathsTreeNode> _children;
	
	public PathsTreeNode(
			int level, 
			String stem, 
			String word, 
			PathStats pathStats,
			SentimentAggregator sentimentAggregator,
			PathQuote bestQuote) {
		
		_level = level;
		_stem = stem;
		_word = word;
		_pathStats = pathStats;
		_sentimentAggregator = sentimentAggregator;
		_bestQuote = bestQuote;
		_children = new ArrayList<PathsTreeNode>();
	}
	
	public int getLevel() {
		return _level;
	}
	
	public String getStem() {
		return _stem;
	}
	
	public String getWord() {
		return _word;
	}
	
	public PathQuote getBestQuote() {
		return _bestQuote;
	}
	
	public PathStats getPathStats() {
		return _pathStats;
	}
	
	public SentimentAggregator getSentimentAggregator() {
		return _sentimentAggregator;
	}
	
	public List<PathsTreeNode> getChildren() {
		return _children;
	}
	
	public void addChild(PathsTreeNode child) {
		_children.add(child);
	}

}
