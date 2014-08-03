package com.readrz.math.merging.snaps;

import java.util.ArrayList;
import java.util.List;

import com.readrz.data.SnapInfo;

public final class ClusterDocument {

	private final SnapInfo _snapInfo;
	private final List<ClusterSentence> _sentences;
	
	public ClusterDocument(SnapInfo snapInfo) {
		_snapInfo = snapInfo;
		_sentences = new ArrayList<>();
	}
	
	public SnapInfo getSnapInfo() {
		return _snapInfo;
	}
	
	public void addSentence(ClusterSentence sentence) {
		_sentences.add(sentence);
	}
	
	public List<ClusterSentence> getSentences() {
		return _sentences;
	}
}
