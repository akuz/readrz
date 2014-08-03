package com.readrz.sentiment.test;

import java.util.Date;

import me.akuz.core.Frequency;

import com.mongodb.BasicDBObject;

public final class ProgramOptions {

	private final BasicDBObject _dbo;
	private String _outputFile         = "outputFile";
	private String _queryString        = "queryString";
	private String _minDateInc         = "minDateInc";
	private String _maxDateExc         = "maxDateExc";
	private String _frequency          = "frequency";
	private String _wordsSentimentFile = "wordsSentimentFile";
	
	public ProgramOptions(
			String outputFile,
			String queryString, 
			Date minDateInc, 
			Date maxDateExc,
			Frequency frequency,
			String wordsSentimentFile) {
		
		_dbo = new BasicDBObject();
		_dbo.put(_outputFile, outputFile);
		_dbo.put(_queryString, queryString);
		_dbo.put(_minDateInc, minDateInc);
		_dbo.put(_maxDateExc, maxDateExc);
		_dbo.put(_frequency, frequency);
		_dbo.put(_wordsSentimentFile, wordsSentimentFile);
	}
	
	public String getOutputFile() {
		return _dbo.getString(_outputFile);
	}
	
	public String getQueryString() {
		return _dbo.getString(_queryString);
	}
	
	public Date getMinDateInc() {
		return _dbo.getDate(_minDateInc);
	}
	
	public Date getMaxDateExc() {
		return _dbo.getDate(_maxDateExc);
	}
	
	public Frequency getFrequency() {
		return (Frequency)_dbo.get(_frequency);
	}
	
	public String getWordsSentimentFile() {
		return _dbo.getString(_wordsSentimentFile);
	}
	
	@Override
	public String toString() {
		return _dbo.toString();
	}
}
