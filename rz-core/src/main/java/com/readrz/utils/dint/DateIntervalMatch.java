package com.readrz.utils.dint;

public final class DateIntervalMatch {
	
	private final DateInterval _dateInterval;
	private final int _firstIndex;
	private final int _afterDocIndex;
	
	public DateIntervalMatch(DateInterval dateInterval, int firstIndex, int afterLastIndex) {
		_dateInterval = dateInterval;
		_firstIndex = firstIndex;
		_afterDocIndex = afterLastIndex;
	}
	
	public DateInterval getDateInterval() {
		return _dateInterval;
	}
	
	public int getFirstIndex() {
		return _firstIndex;
	}
	
	public int getAfterLastIndex() {
		return _afterDocIndex;
	}
	
	public String toString() {
		return "[" + _firstIndex + ", " + _afterDocIndex + ")";
	}

}
