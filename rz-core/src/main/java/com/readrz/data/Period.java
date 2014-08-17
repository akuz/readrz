package com.readrz.data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import me.akuz.core.DateUtils;

public final class Period {
	
	public final static int Id1h =  1;
	public final static int Id2h =  2;
	public final static int Id4h =  4;
	public final static int Id8h =  8;
	public final static int Id1d = 11;
	public final static int Id2d = 12;
	public final static int Id3d = 13;
	public final static int Id1w = 21;
	public final static int Id2w = 22;
	public final static int Id1m = 31;
	
	private final static Map<Integer, Period> _byId;
	private final static Map<String, Period> _byAbbr;
	
	static {
		_byId = new HashMap<Integer, Period>();
		_byAbbr = new HashMap<String, Period>();
		add(new Period(Id1h, "1hr",    "1 hr",    60,            5,  30));
		add(new Period(Id2h, "2hrs",   "2 hrs",   60 *  2,       5,  60));
		add(new Period(Id4h, "4hrs",   "4 hrs",   60 *  4,      10, 120));
		add(new Period(Id8h, "8hrs",   "8 hrs",   60 *  8,      10, 120));
		add(new Period(Id1d, "1day",   "1 day",   60 * 24,      10, 120));
		add(new Period(Id2d, "2days",  "2 days",  60 * 24 *  2, 20, 120));
		add(new Period(Id3d, "3days",  "3 days",  60 * 24 *  3, 20, 120));
		add(new Period(Id1w, "1week",  "1 week",  60 * 24 *  7, 60, 120));
		add(new Period(Id2w, "2weeks", "2 weeks", 60 * 24 * 14, 60, 120));
		add(new Period(Id1m, "1month", "1 month", 60 * 24 * 30, 60, 120));
	}
	
	private static final void add(Period period) {
		_byId.put(period.getId(), period);
		_byAbbr.put(period.getAbbr(), period);
	}
	
	public static final Period get(Integer id) {
		return _byId.get(id);
	}
	
	public static final Period getOrThrow(Integer id) {
		Period period = _byId.get(id);
		if (period == null) {
			throw new IllegalArgumentException("Unknown " + Period.class.getSimpleName() + " id: " + id);
		}
		return period;
	}
	
	public static final Period get(String abbr) {
		return _byAbbr.get(abbr);
	}
	
	public static final Period getOrThrow(String abbr) {
		Period period = _byAbbr.get(abbr);
		if (period == null) {
			throw new IllegalArgumentException("Unknown " + Period.class.getSimpleName() + " abbr: " + abbr);
		}
		return period;
	}
	
	private final int _id;
	private final String _abbr;
	private final String _name;
	private final int _lengthMins;
	private final int _dyingMins;
	private final int _deadMins;
	
	private Period(
			final int id, 
			final String abbr, 
			final String name, 
			final int lengthMins, 
			final int dyingMins, 
			final int deadMins) {
		_id = id;
		_abbr = abbr;
		_name = name;
		_lengthMins = lengthMins;
		_dyingMins = dyingMins;
		_deadMins = deadMins;
	}
	
	public int getId() {
		return _id;
	}
	
	public String getAbbr() {
		return _abbr;
	}
	
	public String getName() {
		return _name;
	}
	
	public int getLengthMins() {
		return _lengthMins;
	}
	
	public int getDyingMins() {
		return _dyingMins;
	}
	
	public int getDeadMins() {
		return _deadMins;
	}
	
	public AliveStatus getAliveStatus(Date now, Date checkDate) {
		if (checkDate == null) {
			return AliveStatus.Dead;
		}
		Date deadDate = DateUtils.addMinutes(now, - _deadMins);
		if (checkDate.compareTo(deadDate) < 0) {
			return AliveStatus.Dead;
		}
		Date dyingDate = DateUtils.addMinutes(now, - _dyingMins); 
		if (checkDate.compareTo(dyingDate) < 0) {
			return AliveStatus.Dying;
		}
		return AliveStatus.Alive;
	}
	
	public Date getMinDate(Date maxDate) {
		return DateUtils.addMinutes(maxDate, - _lengthMins);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		return _id == ((Period)obj)._id;
	}
}
