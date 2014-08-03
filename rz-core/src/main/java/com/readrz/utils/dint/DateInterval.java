package com.readrz.utils.dint;

import java.util.Date;

public final class DateInterval {

	private final String _tag;
	private final Date _minDateInc;
	private final Date _maxDateExc;
	
	public DateInterval(String tag, Date minDateInc, Date maxDateExc) {
		_tag = tag;
		_minDateInc = minDateInc;
		_maxDateExc = maxDateExc;
	}
	
	public String getTag() {
		return _tag;
	}
	
	public Date getMinDateInc() {
		return _minDateInc;
	}
	
	public Date getMaxDateExc() {
		return _maxDateExc;
	}
	
	public boolean containsDate(Date date) {
		return _minDateInc.compareTo(date) <= 0 && date.compareTo(_maxDateExc) < 0;
	}
}
