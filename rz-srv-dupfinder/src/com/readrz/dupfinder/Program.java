package com.readrz.dupfinder;

import java.util.Date;

import me.akuz.core.DateUtils;
import me.akuz.core.StringUtils;
import me.akuz.core.UtcDate;

public class Program {

	public static void main(String[] args) throws Exception {
		
		String usageString = 
			"ARGUMENTS:\n" + 
			"   -mongoServer string         : Mongo server\n" + 
			"   -mongoPort int              : Mongo server port\n" +
			"   -mongoDb int                : Mongo database name\n" +
			" [ -startupFromDate YYYYMMDD ] : Date to check for dups from on startup (UTC, default none)\n" + 
			" [ -startupPeriodMins dbl    ] : Period to check for dups on startup (minutes, default 0)\n" + 
			" [ -startupRedoAll           ] : Check all for dups, including checked before (default false)\n" + 
			" [ -dupsPeriodMins dbl       ] : Period within which to check for dups (minutes, default 60)\n" + 
			" [ -liveFreqMins dbl         ] : Frequency to check for dups in live (minutes, default 1)\n" + 
			" [ -livePeriodMins dbl       ] : Period to check for dups in live (minutes, default 0)\n" + 
			" [ -verbose                  ] : Verbose output about duplicates checking (default false)\n";

		String  mongoServer = null;
		Integer mongoPort = null;
		String  mongoDb = null;
		Date startupFromDate = null;
		boolean startupRedoAll = false;
		double dupsPeriodMins = 60;
		double liveFreqMins = 1;
		double livePeriodMins = 0;
		boolean isVerbose = false;
		
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
							UtcDate utcDate = new UtcDate(DateUtils.addMinutes(new Date(), - mins));
							startupFromDate = utcDate.getDate();
	
							i++;
						}
					} else if ("-startupRedoAll".equals(args[i])) {
						
						startupRedoAll = true;
						
					} else if ("-dupsPeriodMins".equals(args[i])) {
						if (i+1 < args.length) {
							dupsPeriodMins = Double.parseDouble(StringUtils.unquote(args[i+1]));
							i++;
						}
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
					} else if ("-verbose".equals(args[i])) {
						isVerbose = true;
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
			if (dupsPeriodMins <= 0) {
				throw new IllegalArgumentException("Dups period should be positive");
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
		
		ProgramOptions dupsOptions = new ProgramOptions(
				startupFromDate,
				startupRedoAll,
				dupsPeriodMins,
				liveFreqMins,
				livePeriodMins,
				isVerbose);
		
		System.out.println("OPTIONS:");
		System.out.println(dupsOptions.toString());
		
		ProgramLogic logic = new ProgramLogic();
		logic.execute(
				mongoServer, 
				mongoPort, 
				mongoDb,
				dupsOptions);
	}

}
