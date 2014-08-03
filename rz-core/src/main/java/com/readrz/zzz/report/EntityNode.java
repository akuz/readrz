package com.readrz.zzz.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class EntityNode implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private transient EntityNode _parent;
	private int _level;
	private String _stem;
	private String _word;
	private double _volume;
	private Sentiment _sentiment;
	private Quote _bestQuote;
	private List<EntityNode> _children;
	
	/**
	 * Needed for deserialization.
	 */
	public EntityNode() {
	}

	public EntityNode(EntityNode parent, int level, String stem, String word, double volume, Sentiment sentiment, Quote bestQuote) {
		_parent = parent;
		_level = level;
		_stem = stem;
		_word = word;
		_volume = volume;
		_sentiment = sentiment;
		_bestQuote = bestQuote;
		_children = new ArrayList<EntityNode>();
	}
	
	public EntityNode getParent() {
		return _parent;
	}
	
	public void setParent(EntityNode parent) {
		_parent = parent;
	}
	
	public int getLevel() {
		return _level;
	}
	
	public String getStem() {
		return _stem;
	}
	
	public List<String> getStemPathReverse() {
		List<String> list = new ArrayList<String>();
		list.add(_stem);
		EntityNode loopNode = this;
		while (loopNode._parent != null) {
			loopNode = loopNode._parent;
			list.add(loopNode._stem);
		}
		return list;
	}
	
	public String getWord() {
		return _word;
	}
	
	public double getVolume() {
		return _volume;
	}
	
	public Sentiment getSentiment() {
		return _sentiment;
	}
	
	public boolean getIsPos() {
		return _sentiment != null && Sentiment.POS.equals(_sentiment);
	}
	
	public boolean getIsNeg() {
		return _sentiment != null && Sentiment.NEG.equals(_sentiment);
	}
	
	public Quote getBestQuote() {
		return _bestQuote;
	}
	
	public List<EntityNode> getChildren() {
		return _children;
	}
	
	public boolean isHasManyChildren() {
		return _children.size() > 1;
	}
	
	public void addChild(EntityNode child) {
		_children.add(child);
	}

}
