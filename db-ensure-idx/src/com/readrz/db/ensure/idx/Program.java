package com.readrz.db.ensure.idx;

import me.akuz.core.StringUtils;

public class Program {

	public static void main(String[] args) throws Exception {
		
		String usageString = 
			"ARGUMENTS:\n" + 
			"   -mongoServer string      : Mongo server\n" + 
			"   -mongoPort int           : Mongo server port\n" +
			"   -mongoDb int             : Mongo database name\n";

		String  mongoServer = null;
		Integer mongoPort = null;
		String  mongoDb = null;
		
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
			
			System.out.println("******** Arguments Error ********");
			System.out.println(e.toString());
			System.out.println("******** Correct Usage ********");
			System.out.println(usageString);
			return;
		}
		
		ProgramLogic logic = new ProgramLogic();
		logic.execute(
				mongoServer, 
				mongoPort, 
				mongoDb);
	}

}
