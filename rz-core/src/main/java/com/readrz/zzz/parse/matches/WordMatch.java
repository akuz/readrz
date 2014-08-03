package com.readrz.zzz.parse.matches;

import com.readrz.zzz.Location;

public final class WordMatch implements AnyMatch {
	
	private int _matchIndex;
	private final WordKind _kind;
	private final Location _location;
	private final String _stem;
	private final String _text;
	private final boolean _isStopWord;
	
	public WordMatch(WordKind kind, Location location, String stem, String text, boolean isStopWord) {
		_matchIndex = -1;
		_kind = kind;
		_location = location;
		_stem = stem;
		_text = text;
		_isStopWord = isStopWord;
	}
	
	public WordKind getKind() {
		return _kind;
	}
	
	public boolean isStopWord() {
		return _isStopWord;
	}

	public boolean isBreakMatch() {
		return false;
	}
	
	public boolean isEntityOrNonStopWordMatch() {
		return _isStopWord == false;
	}
	
	public String getStem() {
		return _stem;
	}
	
	public String getText() {
		return _text;
	}
	
	public Location getLocation() {
		return _location;
	}
	
	public int getMatchIndex() {
		return _matchIndex;
	}
	
	public void setMatchIndex(int index) {
		_matchIndex = index;
	}
}
