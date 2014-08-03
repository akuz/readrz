package com.readrz.topicalc;

import java.security.InvalidParameterException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import me.akuz.core.StringUtils;
import me.akuz.core.UtcDate;

public final class Program3 {

	public static void main(String[] args) throws Exception {

		String usageString = 
			"ARGUMENTS:\n" + 
			"   -mongoServer string   : Mongo server\n" + 
			"   -mongoPort int        : Mongo server port\n" +
			"   -mongoDb int          : Mongo database name\n" +
			"   -outputDir string     : Output folder for the topic files\n" + 
			"   -stopWordsFile string : Path to the file contaning stop words\n" + 
			"   -auxTopicCount int    : Auxiliary topics count\n" + 
			"   -auxTopicBeta double  : Auxiliary topics beta\n" + 
			"   -topicCount int       : Regular topics count\n" + 
			"   -topicBeta double     : Regular topics beta\n" + 
			"   -warmupIterations int : How many iterations to warm up\n" + 
			"   -sampleCount int      : How many samples to take after warm up\n" + 
			"   -sampleGap int        : How many samples to skip between samples\n" + 
			" [ -daysCount int]       : Number of days to look back for posts\n" + 
			" [ -minDateInc YYYYMMDD] : Minimum date, inclusive (default is none)\n" + 
			" [ -maxDateExc YYYYMMDD] : Maximum date, exclusive (default is NOW)\n";

		String  mongoServer = null;
		Integer mongoPort = null;
		String  mongoDb = null;
		String outputDir = null;
		String stopWordsFile = null;
		Integer auxTopicCount = null;
		Double auxTopicBeta = null;
		Integer topicCount = null;
		Double topicBeta = null;
		Integer warmupIterations = null;
		Integer sampleCount = null;
		Integer sampleGap = null;
		Date minDateIncByDaysCount = null;
		Date minDateInc = null;
		Date maxDateExc = null;
		
		if (args != null) {
			for (int i=0; i < args.length; i++) {
				
				if ("-mongoServer".equals(args[i])) {
					if (i+1 < args.length) {
						mongoServer = StringUtils.unquote(args[i+1]);
						i++;
					}
				} if ("-mongoPort".equals(args[i])) {
					if (i+1 < args.length) {
						mongoPort = Integer.parseInt(StringUtils.unquote(args[i+1]));
						i++;
					}
				} else if ("-mongoDb".equals(args[i])) {
					if (i+1 < args.length) {
						mongoDb = StringUtils.unquote(args[i+1]);
						i++;
					}
				} else if ("-outputDir".equals(args[i])) {
					if (i+1 < args.length) {
						outputDir = StringUtils.unquote(args[i+1]);
						i++;
					}
				} else if ("-auxTopicCount".equals(args[i])) {
					if (i+1 < args.length) {
						auxTopicCount = Integer.parseInt(StringUtils.unquote(args[i+1]));
						i++;
					}
				} else if ("-auxTopicBeta".equals(args[i])) {
					if (i+1 < args.length) {
						auxTopicBeta = Double.parseDouble(StringUtils.unquote(args[i+1]));
						i++;
					}
				} else if ("-stopWordsFile".equals(args[i])) {
					if (i+1 < args.length) {
						stopWordsFile = StringUtils.unquote(args[i+1]);
						i++;
					}
				} else if ("-topicCount".equals(args[i])) {
					if (i+1 < args.length) {
						topicCount = Integer.parseInt(StringUtils.unquote(args[i+1]));
						i++;
					}
				} else if ("-topicBeta".equals(args[i])) {
					if (i+1 < args.length) {
						topicBeta = Double.parseDouble(StringUtils.unquote(args[i+1]));
						i++;
					}
				} else if ("-warmupIterations".equals(args[i])) {
					if (i+1 < args.length) {
						warmupIterations = Integer.parseInt(StringUtils.unquote(args[i+1]));
						i++;
					}
				} else if ("-sampleCount".equals(args[i])) {
					if (i+1 < args.length) {
						sampleCount = Integer.parseInt(StringUtils.unquote(args[i+1]));
						i++;
					}
				} else if ("-sampleGap".equals(args[i])) {
					if (i+1 < args.length) {
						sampleGap = Integer.parseInt(StringUtils.unquote(args[i+1]));
						i++;
					}
				} else if ("-daysCount".equals(args[i])) {
					if (i+1 < args.length) {
						int daysCount = Integer.parseInt(StringUtils.unquote(args[i+1]));
						Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
						cal.add(Calendar.DAY_OF_MONTH, -daysCount);
						minDateIncByDaysCount = cal.getTime();
						i++;
					}
				} else if ("-minDateInc".equals(args[i])) {
					if (i+1 < args.length) {
						String strDate = StringUtils.unquote(args[i+1]);
						UtcDate utcDate = new UtcDate("yyyyMMdd");
						utcDate.parse(strDate);
						minDateInc = utcDate.getDate();
						i++;
					}
				} else if ("-maxDateExc".equals(args[i])) {
					if (i+1 < args.length) {
						String strDate = StringUtils.unquote(args[i+1]);
						UtcDate utcDate = new UtcDate("yyyyMMdd");
						utcDate.parse(strDate);
						maxDateExc = utcDate.getDate();
						i++;
					}
				}
			}
		}
		// find latest minDate
		if (minDateInc == null) {
			minDateInc = minDateIncByDaysCount;
		} else if (minDateIncByDaysCount != null && minDateIncByDaysCount.compareTo(minDateInc) > 0) {
			minDateInc = minDateIncByDaysCount;
		}
		if (minDateInc == null) {
			System.out.println("Minimum date not specified");
			System.out.println(usageString);
			throw new InvalidParameterException("Invalid command line arguments");
		}
		if (maxDateExc == null) {
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			maxDateExc = cal.getTime();
		}
		
		if (mongoServer == null) {
			System.out.println("Mongo server not specified");
			System.out.println(usageString);
			throw new InvalidParameterException("Invalid command line arguments");
		}
		if (mongoPort == null) {
			System.out.println("Mongo port not specified");
			System.out.println(usageString);
			throw new InvalidParameterException("Invalid command line arguments");
		}
		if (mongoDb == null) {
			System.out.println("Mongo db not specified");
			System.out.println(usageString);
			throw new InvalidParameterException("Invalid command line arguments");
		}
		if (outputDir == null) {
			System.out.println("Output directory not specified");
			System.out.println(usageString);
			throw new InvalidParameterException("Invalid command line arguments");
		}
		if (stopWordsFile == null) {
			System.out.println("Stop words file not specified");
			System.out.println(usageString);
			throw new InvalidParameterException("Invalid command line arguments");
		}
		if (auxTopicCount == null) {
			System.out.println("Parameter auxTopicCount not specified");
			System.out.println(usageString);
			throw new InvalidParameterException("Invalid command line arguments");
		}
		if (auxTopicBeta == null) {
			System.out.println("Parameter auxTopicBeta not specified");
			System.out.println(usageString);
			throw new InvalidParameterException("Invalid command line arguments");
		}
		if (topicCount == null) {
			System.out.println("Parameter topicCount not specified");
			System.out.println(usageString);
			throw new InvalidParameterException("Invalid command line arguments");
		}
		if (topicBeta == null) {
			System.out.println("Parameter topicBeta not specified");
			System.out.println(usageString);
			throw new InvalidParameterException("Invalid command line arguments");
		}
		if (warmupIterations == null) {
			System.out.println("Parameter warmupIterations not specified");
			System.out.println(usageString);
			throw new InvalidParameterException("Invalid command line arguments");
		}
		if (sampleCount == null) {
			System.out.println("Parameter sampleCount not specified");
			System.out.println(usageString);
			throw new InvalidParameterException("Invalid command line arguments");
		}
		
		Program3Options options = new Program3Options(
				mongoServer, mongoPort, mongoDb, 
				outputDir, stopWordsFile, 
				auxTopicCount, auxTopicBeta,
				topicCount, topicBeta,
				warmupIterations, sampleCount, sampleGap,
				minDateInc, maxDateExc);

		System.out.println("OPTIONS:");
		System.out.println(options.toString());
		
		Program3Logic logic = new Program3Logic();
		logic.execute(options);
		System.out.println("DONE.");
	}
}
