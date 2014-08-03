package com.readrz.zzz.parse.matches;

import com.readrz.zzz.Location;

public final class UnrecMatch implements AnyMatch {

	private int _matchIndex;
	private final Location _location;
	private final String _stem;
	private final String _text;
	
	public UnrecMatch(Location location, String stem, String text) {
		_matchIndex = -1;
		_location = location;
		_stem = stem;
		_text = text;
	}

	public boolean isBreakMatch() {
		return false;
	}

	public boolean isEntityOrNonStopWordMatch() {
		return false;
	}
	
	public Location getLocation() {
		return _location;
	}

	public String getStem() {
		return _stem;
	}

	public String getText() {
		return _text;
	}
	
	public int getMatchIndex() {
		return _matchIndex;
	}
	
	public void setMatchIndex(int index) {
		_matchIndex = index;
	}

}
