package com.readrz.zzz.parse.matches;

import com.readrz.zzz.Location;

public interface AnyMatch {
	
	boolean isBreakMatch();
	
	boolean isEntityOrNonStopWordMatch();
	
	Location getLocation();
	
	String getStem();
	
	String getText();

	int getMatchIndex();
	
	void setMatchIndex(int index);
}
