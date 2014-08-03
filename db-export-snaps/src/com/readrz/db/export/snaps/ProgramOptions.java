package com.readrz.db.export.snaps;

import java.util.Date;

import me.akuz.core.Dbo;

public final class ProgramOptions extends Dbo {
	
	private static final String _fromDate     = "fromDate";
	private static final String _outputDir    = "outputDir";
	private static final String _maxSnaps     = "maxSnaps";
	private static final String _saveTitles   = "saveTitles";
	private static final String _saveBodies   = "saveBodies";
	private static final String _maxBodyChars = "maxBodyChars";
	
	public ProgramOptions(Date fromDate, String outputDir, int maxSnaps, boolean saveTitles, boolean saveBodies, int maxBodyChars) {
		
		set(_fromDate, fromDate);
		set(_outputDir, outputDir);
		set(_maxSnaps, maxSnaps);
		set(_saveTitles, saveTitles);
		set(_saveBodies, saveBodies);
		set(_maxBodyChars, maxBodyChars);
	}
	
	public Date getFromDate() {
		return getDate(_fromDate);
	}
	public String getOutputDir() {
		return getString(_outputDir);
	}
	public Integer getMaxSnaps() {
		return getInteger(_maxSnaps);
	}
	public boolean isSaveTitles() {
		return getBoolean(_saveTitles);
	}
	public boolean isSaveBodies() {
		return getBoolean(_saveBodies);
	}
	public Integer getMaxBodyChars() {
		return getInteger(_maxBodyChars);
	}

}
