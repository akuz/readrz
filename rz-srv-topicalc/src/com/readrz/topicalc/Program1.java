package com.readrz.topicalc;

import java.security.InvalidParameterException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import me.akuz.core.StringUtils;
import me.akuz.core.UtcDate;

public final class Program1 {

	public static void main(String[] args) throws Exception {
		
		String usageString = 
			"ARGUMENTS:\n" + 
			"   -mongoServer string       : Mongo server\n" + 
			"   -mongoPort int            : Mongo server port\n" +
			"   -mongoDb int              : Mongo database name\n" +
			"   -outputDir string         : output directory to save results to\n" + 
			"   -stopWordsFile string     : Path to the file contaning stop words\n" + 
			"   -burnInStepCount int      : How many steps to take to burn from t=1 to sample temperature\n" + 
			"   -burnInStepIterations int : Number of iterations to perform at each burn in step\n" + 
			"   -sampleTemperature double : Temperature to maintain when taking samples\n" + 
			"   -sampleCount int          : Total number of samples to take\n" + 
			"   -sampleGap int            : How many gap iterations to run between samples\n" + 
			"   -outWordCount int         : Number of words to output for each topic\n" + 
			"   -threadCount int          : Number of threads to use in calculations\n" + 
			" [ -daysCount int]           : How many days to look back for articles\n" + 
			" [ -minDateInc YYYYMMDD]     : Minimum date, inclusive (default is none)\n" + 
			" [ -maxDateExc YYYYMMDD]     : Maximum date, exclusive (default is NOW)\n";

		String  mongoServer = null;
		Integer mongoPort = null;
		String  mongoDb = null;
		String outputDir = null;
		String stopWordsFile = null;
		Integer burnInStepCount = null;
		Integer burnInStepIterations = null;
		Double sampleTemperature = null;
		Integer sampleCount = null;
		Integer sampleGap = null;
		Integer outWordCount = null;
		Integer threadCount = null;
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
				} else if ("-stopWordsFile".equals(args[i])) {
					if (i+1 < args.length) {
						stopWordsFile = StringUtils.unquote(args[i+1]);
						i++;
					}
				} if ("-burnInStepCount".equals(args[i])) {
					if (i+1 < args.length) {
						burnInStepCount = Integer.parseInt(StringUtils.unquote(args[i+1]));
						i++;
					}
				} if ("-burnInStepIterations".equals(args[i])) {
					if (i+1 < args.length) {
						burnInStepIterations = Integer.parseInt(StringUtils.unquote(args[i+1]));
						i++;
					}
				} if ("-sampleTemperature".equals(args[i])) {
					if (i+1 < args.length) {
						sampleTemperature = Double.parseDouble(StringUtils.unquote(args[i+1]));
						i++;
					}
				} if ("-sampleCount".equals(args[i])) {
					if (i+1 < args.length) {
						sampleCount = Integer.parseInt(StringUtils.unquote(args[i+1]));
						i++;
					}
				} if ("-sampleGap".equals(args[i])) {
					if (i+1 < args.length) {
						sampleGap = Integer.parseInt(StringUtils.unquote(args[i+1]));
						i++;
					}
				} if ("-outWordCount".equals(args[i])) {
					if (i+1 < args.length) {
						outWordCount = Integer.parseInt(StringUtils.unquote(args[i+1]));
						i++;
					}
				} if ("-threadCount".equals(args[i])) {
					if (i+1 < args.length) {
						threadCount = Integer.parseInt(StringUtils.unquote(args[i+1]));
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
		if (burnInStepCount == null) {
			System.out.println("burnInStepCount not specified");
			System.out.println(usageString);
			throw new InvalidParameterException("Invalid command line arguments");
		}
		if (burnInStepIterations == null) {
			System.out.println("burnInStepIterations not specified");
			System.out.println(usageString);
			throw new InvalidParameterException("Invalid command line arguments");
		}
		if (sampleTemperature == null) {
			System.out.println("sampleTemperature not specified");
			System.out.println(usageString);
			throw new InvalidParameterException("Invalid command line arguments");
		}
		if (sampleCount == null) {
			System.out.println("sampleCount not specified");
			System.out.println(usageString);
			throw new InvalidParameterException("Invalid command line arguments");
		}
		if (outWordCount == null) {
			System.out.println("outWordCount not specified");
			System.out.println(usageString);
			throw new InvalidParameterException("Invalid command line arguments");
		}
		if (threadCount == null) {
			System.out.println("threadCount not specified");
			System.out.println(usageString);
			throw new InvalidParameterException("Invalid command line arguments");
		}
		if (sampleGap == null) {
			System.out.println("sampleGap not specified");
			System.out.println(usageString);
			throw new InvalidParameterException("Invalid command line arguments");
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

		Program1Options options = new Program1Options(
				mongoServer, mongoPort, mongoDb, 
				outputDir, stopWordsFile, 
				burnInStepCount, burnInStepIterations,
				sampleTemperature, sampleCount, sampleGap,
				outWordCount, threadCount,
				minDateInc, maxDateExc);

		System.out.println("OPTIONS:");
		System.out.println(options.toString());
		
		Program1Logic logic = new Program1Logic();
		logic.execute(options);
		System.out.println("DONE.");
	}
	

}
