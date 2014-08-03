package com.readrz.utils.dint;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DateIntervalMatcher<TDocId> {
	
	private Date _minDate;
	private Date _maxDate;
	private final List<DateIntervalMatch> _matches;
	private final Map<DateInterval, DateIntervalMatch> _map;
	
	public DateIntervalMatcher(List<DateInterval> dateIntervals, List<TDocId> idsList, Map<TDocId, Date> dateById) {

		_matches = new ArrayList<DateIntervalMatch>();
		_map = new HashMap<DateInterval, DateIntervalMatch>();
		
		for (int i=0; i<dateIntervals.size(); i++) {
			
			DateInterval dateInterval = dateIntervals.get(i);
			if (_minDate == null || _minDate.compareTo(dateInterval.getMinDateInc()) > 0) {
				_minDate = dateInterval.getMinDateInc();
			}
			if (_maxDate == null || _maxDate.compareTo(dateInterval.getMaxDateExc()) < 0) {
				_maxDate = dateInterval.getMaxDateExc();
			}
			
			int firstDocIndex = idsList.size();
			int afterLastDocIndex = 0;
			for (int d=0; d<idsList.size(); d++) {
				TDocId docId = idsList.get(d);
				Date docDate = dateById.get(docId);
				if (dateInterval.containsDate(docDate)) {
					if (firstDocIndex > d) {
						firstDocIndex = d;
					}
					if (afterLastDocIndex < d+1) {
						afterLastDocIndex = d+1;
					}
				}
			}
			DateIntervalMatch interval = new DateIntervalMatch(dateInterval, firstDocIndex, afterLastDocIndex);
			_matches.add(interval);
			_map.put(dateInterval, interval);
		}
	}
	
	public List<DateIntervalMatch> getMatches() {
		return _matches;
	}
	
	public DateIntervalMatch getInterval(DateInterval def) {
		return _map.get(def);
	}
	
	public Date getMinDate() {
		return _minDate;
	}
	
	public Date getMaxDate() {
		return _maxDate;
	}
}
