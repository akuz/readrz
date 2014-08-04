package com.readrz.dupfinder;

import java.util.Date;

import com.mongodb.BasicDBObject;

public final class ProgramOptions {

	private final BasicDBObject _dbo;
	private String _startupFromDate    = "startupFromDate";
	private String _startupRedoAll     = "startupRedoAll";
	private String _dupsPeriodMinsName = "dupsPeriodMins";
	private String _liveFreqMinsName   = "liveFreqMins";
	private String _livePeriodMinsName = "livePeriodMins";
	private String _isVerboseName      = "isVerbose";
	
	public ProgramOptions(Date startupFromDate, boolean startupRedoAll, double dupsPeriodMins, double liveFreqMins, double livePeriodMins, boolean isVerbose) {
		
		_dbo = new BasicDBObject();
		setStartupFromDate(startupFromDate);
		setStartupReduAll(startupRedoAll);
		setDupsPeriodMins(dupsPeriodMins);
		setLiveFreqMins(liveFreqMins);
		setLivePeriodMins(livePeriodMins);
		isVerbose(isVerbose);
	}
	
	/**
	 * Historical date from which to check on startup.
	 */
	public Date getStartupFromDate() {
		return _dbo.getDate(_startupFromDate);
	}
	public void setStartupFromDate(Date date) {
		_dbo.put(_startupFromDate, date);
	}
	
	public boolean getStartupRedoAll() {
		Boolean is = (Boolean)_dbo.get(_startupRedoAll);
		return is == null ? false : is.booleanValue();
	}
	public void setStartupReduAll(boolean on) {
		_dbo.put(_startupRedoAll, on);
	}

	/**
	 * Period within which to consider matching items as dups.
	 */
	public double getDupsPeriodMins() {
		return _dbo.getDouble(_dupsPeriodMinsName);
	}
	public void setDupsPeriodMins(double mins) {
		_dbo.put(_dupsPeriodMinsName, mins);
	}
	
	/**
	 * Frequency with which to check in live (after startup).
	 * @return
	 */
	public double getLiveFreqMins() {
		return _dbo.getDouble(_liveFreqMinsName);
	}
	public void setLiveFreqMins(double mins) {
		_dbo.put(_liveFreqMinsName, mins);
	}
	
	/**
	 * Period of time to check in live (after startup).
	 * @return
	 */
	public double getLivePeriodMins() {
		return _dbo.getDouble(_livePeriodMinsName);
	}
	public void setLivePeriodMins(double mins) {
		_dbo.put(_livePeriodMinsName, mins);
	}
	
	public boolean isVerbose() {
		Boolean is = (Boolean)_dbo.get(_isVerboseName);
		return is == null ? false : is.booleanValue();
	}
	public void isVerbose(boolean is) {
		_dbo.put(_isVerboseName, is);
	}
	
	@Override
	public String toString() {
		return _dbo.toString();
	}
}
