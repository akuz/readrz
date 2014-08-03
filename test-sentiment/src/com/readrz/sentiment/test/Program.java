package com.readrz.sentiment.test;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.akuz.core.Frequency;
import me.akuz.core.StringUtils;
import me.akuz.core.UtcDate;
import me.akuz.core.gson.GsonSerializers;
import me.akuz.core.logs.LogUtils;

public class Program {

	public static void main(String[] args) throws IOException {
		
		String usageString = 
			"ARGUMENTS:\n" + 
			"   -mongoServer string          : Mongo server\n" + 
			"   -mongoPort int               : Mongo server port\n" +
			"   -mongoDb string              : Mongo database name\n" +
			"   -outputFile string           : Output sentiment file\n" + 
			"   -minDateInc YYYYMMDD         : Date to load the data from (inclusive)\n" + 
			"   -maxDateExc YYYYMMDD         : Date to load the data to (exclusive)\n" + 
			" [ -frequency enum            ] : Frequency of sentiment calculation (TenMins, Hourly (default), Daily)\n" + 
			"   -wordsSentimentFile string   : Words sentiment file\n" + 
			" [ -queryString               ] : Query string (use * at the end of terms for document search, default null)\n";

		String  mongoServer = null;
		Integer mongoPort = null;
		String  mongoDb = null;
		String outputFile = null;
		Date minDateInc = null;
		Date maxDateExc = null;
		Frequency frequency = Frequency.Hourly;
		String wordsSentimentFile = null;
		String queryString = null;
		
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
					} if ("-outputFile".equals(args[i])) {
						if (i+1 < args.length) {
							outputFile = StringUtils.unquote(args[i+1]);
							i++;
						}
					} else if ("-minDateInc".equals(args[i])) {
						if (i+1 < args.length) {
							
							String s = StringUtils.unquote(args[i+1]);
							UtcDate utcDate = new UtcDate("yyyyMMdd");
							utcDate.parse(s);
							minDateInc = utcDate.getDate();
	
							i++;
						}
					} else if ("-maxDateExc".equals(args[i])) {
						if (i+1 < args.length) {
							
							String s = StringUtils.unquote(args[i+1]);
							UtcDate utcDate = new UtcDate("yyyyMMdd");
							utcDate.parse(s);
							maxDateExc = utcDate.getDate();
	
							i++;
						}
					} else if ("-frequency".equals(args[i])) {
						if (i+1 < args.length) {
							frequency = Frequency.valueOf(StringUtils.unquote(args[i+1]));
							i++;
						}
					} else if ("-wordsSentimentFile".equals(args[i])) {
						if (i+1 < args.length) {
							wordsSentimentFile = StringUtils.unquote(args[i+1]);
							i++;
						}
					} else if ("-queryString".equals(args[i])) {
						if (i+1 < args.length) {
							queryString = StringUtils.unquote(args[i+1]);
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
			if (outputFile == null) {
				throw new IllegalArgumentException("Output file not specified");
			}
			if (minDateInc == null) {
				throw new IllegalArgumentException("Min date not specified");
			}
			if (maxDateExc == null) {
				throw new IllegalArgumentException("Max date not specified");
			}
			if (minDateInc.compareTo(maxDateExc) >= 0) {
				throw new IllegalArgumentException("Min date must be < max date");
			}
			if (wordsSentimentFile == null) {
				throw new IllegalArgumentException("Words sentiment file not specified");
			}

		} catch (Exception e) {
			
			System.out.println("******** Arguments Error ********");
			System.out.println(e.toString());
			System.out.println("********  Correct Usage  ********");
			System.out.println(usageString);
			return;
		}
		
	    // configure logging
		Level logLevel = Level.FINEST;
		LogUtils.configure(logLevel);
		Logger log = LogUtils.getLogger(Program.class.getName());

		// create program options
		ProgramOptions options = new ProgramOptions(
				outputFile,
				queryString,
				minDateInc,
				maxDateExc,
				frequency,
				wordsSentimentFile);
		
		log.info("OPTIONS: \n" + GsonSerializers.NoHtmlEscapingPretty.toJson(options));
		
		log.info("STARTING...");
		ProgramLogic logic = new ProgramLogic();
		logic.execute(
				mongoServer, 
				mongoPort, 
				mongoDb,
				options);
		log.info("DONE.");
	}
}
