package com.readrz.feedsget;

import com.mongodb.BasicDBObject;

public final class ProgramOptions {

	private final BasicDBObject _dbo;
	private static final String _isVerbose   = "isVerbose";
	private static final String _threadCount = "threadCount";
	
	public ProgramOptions(boolean isVerbose, int threadCount) {
		
		_dbo = new BasicDBObject();
		
		isVerbose(isVerbose);
		setThreadCount(threadCount);
	}
	
	public boolean isVerbose() {
		return _dbo.getBoolean(_isVerbose);
	}
	public void isVerbose(boolean is) {
		_dbo.put(_isVerbose, is);
	}
	
	public int getThreadCount() {
		return _dbo.getInt(_threadCount);
	}
	public void setThreadCount(int threadCount) {
		_dbo.put(_threadCount, threadCount);
	}
	
	@Override
	public String toString() {
		return _dbo.toString();
	}
}
