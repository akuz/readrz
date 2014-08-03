package com.readrz.feedsget;

import me.akuz.core.StringUtils;
import me.akuz.core.gson.GsonSerializers;


public class Program {

	public static void main(String[] args) throws Exception {
		
		String usageString = 
			"ARGUMENTS:\n" + 
			"    -mongoServer string : Target Mongo server\n" + 
			"    -mongoPort   int    : Target Mongo port\n" +
			"    -mongoDb     string : Target Mongo db\n" +
			" [ -threadCount int   ] : Number of threads to use (default 10)\n" +
			" [ -verbose           ] : Enable verbose output about each scan\n";

		String  mongoServer = null;
		Integer mongoPort   = null;
		String  mongoDb     = null;
		boolean verbose = false;
		Integer threadCount = 10;
		
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
					} else if ("-threadCount".equals(args[i])) {
						if (i+1 < args.length) {
							threadCount = Integer.parseInt(StringUtils.unquote(args[i+1]));
							i++;
						}
					} else if ("-verbose".equals(args[i])) {
						verbose = true;

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

		} catch (Exception e) {

			e.printStackTrace(System.out);
			System.out.println("******** Arguments Error ********");
			System.out.println(e.getMessage());
			System.out.println("******** Correct Usage ********");
			System.out.println(usageString);
			return;
		}
		
		ProgramOptions options = new ProgramOptions(
				verbose,
				threadCount);

		System.out.println("OPTIONS:");
		System.out.println(GsonSerializers.NoHtmlEscapingPretty.toJson(options));
		
		System.out.println("STARTING...");
		ProgramLogic logic = new ProgramLogic();
		logic.execute(
			mongoServer,
			mongoPort,
			mongoDb,
			options);

	}
}
