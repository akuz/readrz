package com.readrz.indexer;

import java.util.Date;

import com.mongodb.BasicDBObject;

public final class IdxOptions {

	private final BasicDBObject _dbo;
	private String _startupFromDate    = "startupFromDate";
	private String _startupRedoAll     = "startupRedoAll";
	private String _liveFreqMinsName   = "liveFreqMins";
	private String _livePeriodMinsName = "livePeriodMins";
	private String _stopWordFile       = "stopWordsFile";
	
	public IdxOptions(Date startupFromDate, boolean startupRedoAll, double liveFreqMins, double livePeriodMins, String stopWordsFile) {
		
		_dbo = new BasicDBObject();
		_dbo.put(_startupFromDate, startupFromDate);
		_dbo.put(_startupRedoAll, startupRedoAll);
		_dbo.put(_liveFreqMinsName, liveFreqMins);
		_dbo.put(_livePeriodMinsName, livePeriodMins);
		_dbo.put(_stopWordFile, stopWordsFile);
	}
	
	/**
	 * Historical date from which to check on startup.
	 */
	public Date getStartupFromDate() {
		return _dbo.getDate(_startupFromDate);
	}
	
	/**
	 * True if should re-index all snaps on startup, even if indexed already.
	 */
	public boolean getStartupRedoAll() {
		Boolean is = (Boolean)_dbo.get(_startupRedoAll);
		return is == null ? false : is.booleanValue();
	}
	
	/**
	 * Frequency with which to check in live (after startup).
	 * @return
	 */
	public double getLiveFreqMins() {
		return _dbo.getDouble(_liveFreqMinsName);
	}
	
	/**
	 * Period of time to check in live (after startup).
	 * @return
	 */
	public double getLivePeriodMins() {
		return _dbo.getDouble(_livePeriodMinsName);
	}
	
	public String getStopWordsFile() {
		return _dbo.getString(_stopWordFile);
	}
	
	@Override
	public String toString() {
		return _dbo.toString();
	}
}
