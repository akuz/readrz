package com.readrz.lang.corpus;

import java.util.ArrayList;
import java.util.List;

public final class CorpusDoc {
	
	private final List<CorpusSentence> _sentences;
	private int _placeCount;
	private Object _tag;

	public CorpusDoc() {
		_sentences = new ArrayList<CorpusSentence>();
	}
	
	public void addSentence(CorpusSentence sentence) {
		_placeCount += sentence.getPlaceCount();
		_sentences.add(sentence);
	}
	
	public int getLength() {
		return _placeCount;
	}
	
	public List<CorpusSentence> getSentences() {
		return _sentences;
	}
	public CorpusSentence getSentence(int index) {
		return _sentences.get(index);
	}
	public int getSentenceCount() {
		return _sentences.size();
	}
	
	public Object getTag() {
		return _tag;
	}
	
	public void setTag(Object tag) {
		_tag = tag;
	}
}
