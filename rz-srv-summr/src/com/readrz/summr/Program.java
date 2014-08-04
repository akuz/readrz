package com.readrz.summr;

import java.util.logging.Level;
import java.util.logging.Logger;

import me.akuz.core.StringUtils;
import me.akuz.core.gson.GsonSerializers;
import me.akuz.core.logs.LogUtils;


public class Program {
    
	public static void main(String[] args) throws Exception {
		
		String usageString = 
			"ARGUMENTS:\n" + 
			"   -mongoServer string   : Mongo server\n" + 
			"   -mongoPort int        : Mongo server port\n" +
			"   -mongoDb int          : Mongo database name\n" +
			"   -stopWordsFile string : File with stop words not to use\n" +
			"   -liveFreqMs int       : Frequency of checking for live requests\n" + 
			" [ -threadCount int    ] : Number of threads to use (default 3)\n" +
			" [ -logLevel           ] : Java logging level (default INFO)\n";

		String  mongoServer = null;
		Integer mongoPort = null;
		String  mongoDb = null;
		String  stopWordsFile = null;
		Integer liveFreqMs = null;
		Integer threadCount = 3;
		String  logLevelStr = "INFO";
		
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
					} else if ("-stopWordsFile".equals(args[i])) {
						if (i+1 < args.length) {
							stopWordsFile = StringUtils.unquote(args[i+1]);
							i++;
						}
					} else if ("-liveFreqMs".equals(args[i])) {
						if (i+1 < args.length) {
							liveFreqMs = Integer.parseInt(StringUtils.unquote(args[i+1]));
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
			if (stopWordsFile == null) {
				throw new IllegalArgumentException("Stop words file not specified");
			}
			if (liveFreqMs == null) {
				throw new IllegalArgumentException("Live freq not specified");
			}
			if (liveFreqMs < 0) {
				throw new IllegalArgumentException("Live freq should be >= 0");
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
		ProgramOptions options = new ProgramOptions(
				stopWordsFile,
				liveFreqMs,
				threadCount,
				logLevelStr);
		
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
