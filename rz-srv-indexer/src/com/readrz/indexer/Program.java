package com.readrz.indexer;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.akuz.core.DateUtils;
import me.akuz.core.StringUtils;
import me.akuz.core.UtcDate;
import me.akuz.core.gson.GsonSerializers;
import me.akuz.core.logs.LogUtils;

public class Program {

	public static void main(String[] args) throws Exception {
		
		String usageString = 
			"ARGUMENTS:\n" + 
			"   -mongoServer string         : Mongo server\n" + 
			"   -mongoPort int              : Mongo server port\n" +
			"   -mongoDb int                : Mongo database name\n" +
			" [ -startupFromDate YYYYMMDD ] : Date to index from on startup (UTC DATE, default none)\n" + 
			" [ -startupPeriodMins dbl    ] : Period to index on startup (minutes, default 0)\n" + 
			" [ -startupRedoAll           ] : Reindex all, including indexed before (default false)\n" + 
			" [ -liveFreqMins dbl         ] : Frequency of indexing in live (minutes, default 1)\n" + 
			" [ -livePeriodMins dbl       ] : Period to index in live (minutes, default 60 mins)\n" + 
			" [ -stopWordsFile string     ] : Path to file containing stop words (for topics)\n" +
			" [ -logLevel                 ] : Java logging level (default INFO)\n";

		String  mongoServer = null;
		Integer mongoPort = null;
		String  mongoDb = null;
		Date startupFromDate = null;
		boolean startupRedoAll = false;
		double liveFreqMins = 1;
		double livePeriodMins = 60;
		String stopWordsFile = null;
		String logLevelStr = "INFO";
		
		try {
			
			if (args != null) {
				for (int i=0; i < args.length; i++) {
					
					if ("-mongoServer".equals(args[i])) {
						if (i+1 < args.length) {
							mongoServer = StringUtils.unquote(args[i+1]);
							i++;
						}
					} else if ("-mongoPort".equals(args[i])) {
						if (i+1 < args.length) {
							mongoPort = Integer.parseInt(StringUtils.unquote(args[i+1]));
							i++;
						}
					} else if ("-mongoDb".equals(args[i])) {
						if (i+1 < args.length) {
							mongoDb = StringUtils.unquote(args[i+1]);
							i++;
						}
					} else if ("-startupFromDate".equals(args[i])) {
						if (i+1 < args.length) {
							
							String s = StringUtils.unquote(args[i+1]);
							UtcDate utcDate = new UtcDate("yyyyMMdd");
							utcDate.parse(s);
							startupFromDate = utcDate.getDate();
	
							i++;
						}
					} else if ("-startupPeriodMins".equals(args[i])) {
						if (i+1 < args.length) {
							
							Double mins = Double.parseDouble(StringUtils.unquote(args[i+1]));
							if (mins < 0) {
								throw new IllegalArgumentException("startupPeriodMins should be >= 0");
							}
							UtcDate utcDate = new UtcDate(DateUtils.addMins(new Date(), - mins));
							startupFromDate = utcDate.getDate();
	
							i++;
						}
					} else if ("-startupRedoAll".equals(args[i])) {
						
						startupRedoAll = true;
						
					} else if ("-liveFreqMins".equals(args[i])) {
						if (i+1 < args.length) {
							liveFreqMins = Double.parseDouble(StringUtils.unquote(args[i+1]));
							i++;
						}
					} else if ("-livePeriodMins".equals(args[i])) {
						if (i+1 < args.length) {
							livePeriodMins = Double.parseDouble(StringUtils.unquote(args[i+1]));
							i++;
						}
					} else if ("-stopWordsFile".equals(args[i])) {
						if (i+1 < args.length) {
							stopWordsFile = StringUtils.unquote(args[i+1]);
							i++;
						}
					} else if ("-logLevel".equals(args[i])) {
						if (i+1 < args.length) {
							logLevelStr = StringUtils.unquote(args[i+1]);
							i++;
						}
					}
				}
			}
	
			if (mongoServer == null) {
				throw new IllegalArgumentException("Mongo server not specified");
			}
			if (mongoPort == null) {
				throw new IllegalArgumentException("Mongo port not specified");
			}
			if (mongoDb == null) {
				throw new IllegalArgumentException("Mongo db not specified");
			}
			if (liveFreqMins < 0) {
				throw new IllegalArgumentException("Live freq should be >= 0");
			}
			if (livePeriodMins < 0) {
				throw new IllegalArgumentException("Live period mins should be >= 0");
			}

		} catch (Exception e) {
			
			System.out.println("******** Arguments Error ********");
			System.out.println(e.toString());
			System.out.println("******** Correct Usage ********");
			System.out.println(usageString);
			return;
		}
		
	    // configure logging
		Level logLevel = Level.parse(logLevelStr);
		LogUtils.configure(logLevel);
		Logger log = LogUtils.getLogger(Program.class.getName());

		// create program options
		IdxOptions opts = new IdxOptions(
				startupFromDate,
				startupRedoAll,
				liveFreqMins,
				livePeriodMins,
				stopWordsFile);
		
		log.info("OPTIONS: \n" + GsonSerializers.NoHtmlEscapingPretty.toJson(opts));
		
		log.info("STARTING...");
		ProgramLogic logic = new ProgramLogic();
		logic.execute(
				mongoServer, 
				mongoPort, 
				mongoDb,
				opts);
		log.info("DONE.");
	}

}
