package com.readrz.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SnapPhraseSet {
	
	private final Snap _snap;
	private final List<SnapPhrase> _list;
	private final Set<Integer> _allKeyIds;
	
	public SnapPhraseSet(Snap snap) {
		_snap = snap;
		_list = new ArrayList<>();
		_allKeyIds = new HashSet<>();
	}
	
	public Snap getSnap() {
		return _snap;
	}
	
	public void add(SnapPhrase phrase) {
		_list.add(phrase);
		_allKeyIds.addAll(phrase.getFwdHitsMap().getAllKeyIds());
	}
	
	public List<SnapPhrase> getList() {
		return _list;
	}
	
	public Set<Integer> getAllKeyIds() {
		return _allKeyIds;
	}

}
