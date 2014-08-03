package com.readrz.zzz;

public final class Location implements Comparable<Location> {
	
	private final FieldName _fieldName;
	private final int _start;
	private final int _end;
	
	public Location(FieldName fieldName, int start, int end) {
		if (start > end) {
			throw new IllegalArgumentException("Argument start must be <= end");
		}
		_fieldName = fieldName;
		_start = start;
		_end = end;
	}
	
	public FieldName getFieldName() {
		return _fieldName;
	}
	
	public int getStart() {
		return _start;
	}
	
	public int getEnd() {
		return _end;
	}
	
	public boolean isNotEmpty() {
		return _start < _end;
	}

	public boolean overlaps(Location other) {
		if (_fieldName != other._fieldName) {
			return false;
		}
		if (_end < other._start) {
			return false;
		} else if (_start > other._end) {
			return false;
		} else {
			return true;
		}
	}
	
	public String toString() {
		return "(" + _fieldName + ":" + _start + ":" + _end + ")";
	}

	public int compareTo(Location other) {
		
		if (_fieldName != other._fieldName) {
			if (_fieldName == FieldName.TITLE) {
				return -1;
			} else if (_fieldName == FieldName.TEXT) {
				return 1;
			} else {
				throw new IllegalArgumentException("Unknown field: " + _fieldName);
			}
		} else {
			if (_start < other._start) {
				return -1;
			} else if (_start > other._start) {
				return 1;
			} else {
				if (_end < other._end) {
					return -1;
				} else if (_end > other._end) {
					return 1;
				} else {
					return 0;
				}
			}
		}
	}
}
