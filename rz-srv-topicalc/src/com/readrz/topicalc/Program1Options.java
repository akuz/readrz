package com.readrz.topicalc;

import java.util.Date;

import me.akuz.core.gson.GsonSerializers;

import com.mongodb.BasicDBObject;

public final class Program1Options {

	private final BasicDBObject _dbo;
	private static final String _mongoServer          = "mongoServer";
	private static final String _mongoPort            = "mongoPort";
	private static final String _mongoDb              = "mongoDb";
	private static final String _outputDir            = "outputDir";
	private static final String _stopWordsFile        = "stopWordsFile";
	private static final String _burnInStepCount      = "burnInStepCount";
	private static final String _burnInStepIterations = "burnInStepIterations";
	private static final String _sampleTemperature    = "sampleTemperature";
	private static final String _sampleCount          = "sampleCount";
	private static final String _sampleGap            = "sampleGap";
	private static final String _outWordCount         = "outWordCount";
	private static final String _threadCount          = "threadCount";
	private static final String _minDateInc           = "minDateInc";
	private static final String _maxDateExc           = "maxDateExc";
	
	public Program1Options(
			String mongoServer, 
			int mongoPort, 
			String mongoDb,
			String outputDir, 
			String stopWordsFile, 
			int burnInStepCount,
			int burnInStepIterations,
			double sampleTemperature,
			int sampleCount,
			int sampleGap,
			int outWordCount,
			int threadCount,
			Date minDateInc, 
			Date maxDateExc) {
		
		_dbo = new BasicDBObject();
		_dbo.put(_mongoServer, mongoServer);
		_dbo.put(_mongoPort, mongoPort);
		_dbo.put(_mongoDb, mongoDb);
		_dbo.put(_outputDir, outputDir);
		_dbo.put(_stopWordsFile, stopWordsFile);
		_dbo.put(_burnInStepCount, burnInStepCount);
		_dbo.put(_burnInStepIterations, burnInStepIterations);
		_dbo.put(_sampleTemperature, sampleTemperature);
		_dbo.put(_sampleCount, sampleCount);
		_dbo.put(_sampleGap, sampleGap);
		_dbo.put(_outWordCount, outWordCount);
		_dbo.put(_threadCount, threadCount);
		_dbo.put(_minDateInc, minDateInc);
		_dbo.put(_maxDateExc, maxDateExc);
	}
	
	public String getMongoServer() {
		return (String)_dbo.get(_mongoServer);
	}
	
	public int getMongoPort() {
		return (Integer)_dbo.get(_mongoPort);
	}
	
	public String getMongoDb() {
		return (String)_dbo.get(_mongoDb);
	}
	
	public String getOutputDir() {
		return (String)_dbo.get(_outputDir);
	}
	
	public String getStopWordsFile() {
		return (String)_dbo.get(_stopWordsFile);
	}
	
	public Integer getBurnInStepCount() {
		return (Integer)_dbo.get(_burnInStepCount);
	}
	
	public Integer getBurnInStepIterations() {
		return (Integer)_dbo.get(_burnInStepIterations);
	}
	
	public Double getSampleTemperature() {
		return (Double)_dbo.get(_sampleTemperature);
	}
	
	public Integer getSampleCount() {
		return (Integer)_dbo.get(_sampleCount);
	}
	
	public Integer getSampleGap() {
		return (Integer)_dbo.get(_sampleGap);
	}
	
	public Integer getOutWordCount() {
		return (Integer)_dbo.get(_outWordCount);
	}
	
	public Integer getThreadCount() {
		return (Integer)_dbo.get(_threadCount);
	}
	
	public Date getMinDateInc() {
		return (Date)_dbo.get(_minDateInc);
	}
	
	public Date getMaxDateExc() {
		return (Date)_dbo.get(_maxDateExc);
	}

	@Override
	public String toString() {
		return GsonSerializers.NoHtmlEscapingPretty.toJson(_dbo);
	}
}
