package com.readrz.imagscan;

import java.util.Date;

import com.mongodb.BasicDBObject;

public final class ProgramOptions {

	private final BasicDBObject _dbo;
	private String _startupFromDate    = "startupFromDate";
	private String _startupRedoAll     = "startupRedoAll";
	private String _liveFreqMinsName   = "liveFreqMins";
	private String _livePeriodMinsName = "livePeriodMins";
	private String _blockedUrlsFile    = "blockedUrlsFile";
	private String _stopWordsFile      = "stopWordsFile";
	private String _threadCount        = "threadCount";
	
	public ProgramOptions(
			Date startupFromDate, 
			boolean startupRedoAll, 
			double liveFreqMins, 
			double livePeriodMins,
			String blockedUrlsFile,
			String stopWordsFile,
			int threadCount) {
		
		_dbo = new BasicDBObject();
		_dbo.put(_startupFromDate, startupFromDate);
		_dbo.put(_startupRedoAll, startupRedoAll);
		_dbo.put(_liveFreqMinsName, liveFreqMins);
		_dbo.put(_livePeriodMinsName, livePeriodMins);
		_dbo.put(_blockedUrlsFile, blockedUrlsFile);
		_dbo.put(_stopWordsFile, stopWordsFile);
		_dbo.put(_threadCount, threadCount);
	}
	
	public Date getStartupFromDate() {
		return _dbo.getDate(_startupFromDate);
	}
	
	public boolean getStartupRedoAll() {
		Boolean is = (Boolean)_dbo.get(_startupRedoAll);
		return is == null ? false : is.booleanValue();
	}
	
	public double getLiveFreqMins() {
		return _dbo.getDouble(_liveFreqMinsName);
	}

	public double getLivePeriodMins() {
		return _dbo.getDouble(_livePeriodMinsName);
	}

	public int getThreadCount() {
		return _dbo.getInt(_threadCount);
	}
	
	public String getBlockedUrlsFile() {
		return _dbo.getString(_blockedUrlsFile);
	}
	
	public String getStopWordsFile() {
		return _dbo.getString(_stopWordsFile);
	}
	
	@Override
	public String toString() {
		return _dbo.toString();
	}
}
