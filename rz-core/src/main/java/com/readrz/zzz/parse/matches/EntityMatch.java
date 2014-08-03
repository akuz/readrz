package com.readrz.zzz.parse.matches;

import com.readrz.zzz.Location;
import com.readrz.zzz.categ.EntityFeature;

public final class EntityMatch implements AnyMatch {
	
	private int _matchIndex;
	private final Location _location;
	private final EntityFeature _def;
	private final String _text;
	
	public EntityMatch(Location location, EntityFeature def, String text) {
		_matchIndex = -1;
		_location = location;
		_def = def;
		_text = text;
	}
	
	public EntityFeature getDef() {
		return _def;
	}
	
	public boolean isBreakMatch() {
		return false;
	}

	public boolean isEntityOrNonStopWordMatch() {
		return true;
	}
	
	public Location getLocation() {
		return _location;
	}
	
	public String getStem() {
		return _def.getKey();
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
