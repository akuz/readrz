package com.readrz.db.export.snaps;

import java.util.Date;

import me.akuz.core.StringUtils;
import me.akuz.core.UtcDate;
import me.akuz.core.gson.GsonSerializers;

public class Program {

	public static void main(String[] args) throws Exception {
		
		String usageString = 
			"ARGUMENTS:\n" + 
			"   -mongoServer string : Mongo server\n" + 
			"   -mongoPort int      : Mongo server port\n" +
			"   -mongoDb int        : Mongo database name\n" +
			"   -fromDate YYYYMMDD  : Date to start exporting from\n" +
			"   -outputDir string   : Directory to export snaps to\n" +
			"   -maxSnaps int       : Max number of snaps to export\n" +
			" [ -saveTitles bool  ] : Whether to save titles (default true)\n" +
			" [ -saveBodies bool  ] : Whether to save bodies (default true)\n" +
			" [ -maxBodyChars int ] : Max body chars to save (default 1000)\n";

		String  mongoServer = null;
		Integer mongoPort = null;
		String  mongoDb = null;
		Date fromDate = null;
		String outputDir = null;
		Integer maxSnaps = null;
		boolean saveTitles = true;
		boolean saveBodies = true;
		int maxBodyChars = 1000;
		
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
					} else if ("-fromDate".equals(args[i])) {
						if (i+1 < args.length) {
							fromDate = new UtcDate(UtcDate.NumbersDateOnlyFormatString).parse(StringUtils.unquote(args[i+1])).getTime();
							i++;
						}
					} else if ("-outputDir".equals(args[i])) {
						if (i+1 < args.length) {
							outputDir = StringUtils.unquote(args[i+1]);
							i++;
						}
					} else if ("-maxSnaps".equals(args[i])) {
						if (i+1 < args.length) {
							maxSnaps = Integer.parseInt(StringUtils.unquote(args[i+1]));
							i++;
						}
					} else if ("-saveTitles".equals(args[i])) {
						if (i+1 < args.length) {
							saveTitles = Boolean.parseBoolean(StringUtils.unquote(args[i+1]));
							i++;
						}
					} else if ("-saveBodies".equals(args[i])) {
						if (i+1 < args.length) {
							saveBodies = Boolean.parseBoolean(StringUtils.unquote(args[i+1]));
							i++;
						}
					} else if ("-maxBodyChars".equals(args[i])) {
						if (i+1 < args.length) {
							maxBodyChars = Integer.parseInt(StringUtils.unquote(args[i+1]));
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
			if (fromDate == null) {
				throw new IllegalArgumentException("From date not specified");
			}
			if (outputDir == null) {
				throw new IllegalArgumentException("Output dir not specified");
			}

		} catch (Exception e) {
			
			System.out.println("******** Arguments Error ********");
			System.out.println(e.toString());
			System.out.println("******** Correct Usage ********");
			System.out.println(usageString);
			return;
		}
		
		ProgramOptions options = new ProgramOptions(fromDate, outputDir, maxSnaps, saveTitles, saveBodies, maxBodyChars);
		System.out.println("OPTIONS:");
		System.out.println(GsonSerializers.NoHtmlEscapingPretty.toJson(options.getMap()));

		System.out.println("STARTING...");
		ProgramLogic logic = new ProgramLogic();
		logic.execute(
				mongoServer, 
				mongoPort, 
				mongoDb,
				options);

		System.out.println("DONE.");
	}

}
