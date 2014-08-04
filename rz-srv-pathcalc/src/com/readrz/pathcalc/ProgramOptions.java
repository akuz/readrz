package com.readrz.pathcalc;

import me.akuz.core.gson.GsonSerializers;

import com.mongodb.BasicDBObject;

public final class ProgramOptions {

	private final BasicDBObject _dbo;
	private static final String _stopWordsFile = "stopWordsFile";
	private static final String _liveFreqMs    = "liveFreqMs";
	private static final String _threadCount   = "threadCount";
	private static final String _logLevel      = "logLevel";
	
	public ProgramOptions(String stopWordsFile, int liveFreqMs, int threadCount, String logLevel) {
		
		_dbo = new BasicDBObject();
		setStopWordsFile(stopWordsFile);
		setLiveFreqMs(liveFreqMs);
		setThreadCount(threadCount);
		setLogLevel(logLevel);
	}
	
	public int getThreadCount() {
		return _dbo.getInt(_threadCount);
	}
	public void setThreadCount(int threadCount) {
		_dbo.put(_threadCount, threadCount);
	}
	
	public String getStopWordsFile() {
		return _dbo.getString(_stopWordsFile);
	}
	public void setStopWordsFile(String fileName) {
		_dbo.put(_stopWordsFile, fileName);
	}

	public int getLiveFreqMs() {
		return _dbo.getInt(_liveFreqMs);
	}
	public void setLiveFreqMs(int ms) {
		_dbo.put(_liveFreqMs, ms);
	}
	
	public String getLogLevel() {
		return (String)_dbo.getString(_logLevel);
	}
	public void setLogLevel(String logLevel) {
		_dbo.put(_logLevel, logLevel);
	}
	
	@Override
	public String toString() {
		return GsonSerializers.NoHtmlEscapingPretty.toJson(_dbo);
	}
}
