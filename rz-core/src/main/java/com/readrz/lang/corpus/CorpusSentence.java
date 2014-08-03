package com.readrz.lang.corpus;

import java.util.ArrayList;
import java.util.List;

import me.akuz.core.Hit;


public final class CorpusSentence {
	
	private final Hit _sentenceHit;
	private List<CorpusPlace> _places;
	private int _corpusSentenceIndex;
	private int _sourceSentenceIndex;
	private Object _tag;

	public CorpusSentence(Hit sentenceHit) {
		_sentenceHit = sentenceHit;
		_places = new ArrayList<CorpusPlace>();
		_corpusSentenceIndex = -1;
		_sourceSentenceIndex = -1;
	}
	
	public Hit getSentenceHit() {
		return _sentenceHit;
	}
	
	public void addPlace(CorpusPlace place) {
		_places.add(place);
	}
	
	public int getPlaceCount() {
		return _places.size();
	}
	public CorpusPlace getPlace(int index) {
		return _places.get(index);
	}
	public List<CorpusPlace> getPlaces() {
		return _places;
	}
	
	public int getCorpusSentenceIndex() {
		return _corpusSentenceIndex;
	}
	public void setCorpusSentenceIndex(int index) {
		_corpusSentenceIndex = index;
	}
	
	public int getSourceSentenceIndex() {
		return _sourceSentenceIndex;
	}
	public void setSourceSentenceIndex(int index) {
		_sourceSentenceIndex = index;
	}
	
	public Object getTag() {
		return _tag;
	}
	public void setTag(Object tag) {
		_tag = tag;
	}
}
