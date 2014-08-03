package com.readrz.db.unescape.html;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;

import me.akuz.core.StringUtils;
import me.akuz.core.UtcDate;

public class Program {

	public static void main(String[] args) throws Exception {
		
		String usageString = 
			"ARGUMENTS:\n" + 
			"   -mongoServer string : Mongo server\n" + 
			"   -mongoPort int      : Mongo server port\n" +
			"   -mongoDb int        : Mongo database name\n" +
			"   -fromDate YYYYMMDD  : Date to start unescaping from\n";

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
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("WARNING: This will delete all SEARCH INDEX data for snaps.");
		System.out.println("WARNING: Re-indexing will be required for all these snaps.");
		System.out.println("Enter the starting date to process the snaps from (yyyyMMdd): ");
		String input = br.readLine();

		Date fromDate = null;
		try {
			UtcDate utcDate = new UtcDate("yyyyMMdd");
			utcDate.parse(input);
			fromDate = utcDate.getDate();
		} catch (Exception e) {
			System.out.println("Could not parse date: " + input);
		}
		
		if (fromDate != null) {

			System.out.println("Enter 'UNINDEX' if you want to force removal of all");
			System.out.println("these snaps from index, *regardless* of unescaping ");
			System.out.println("(otherwise only unescaped snaps will be unindexed)");
			String input2 = br.readLine();
			boolean forceUnindex;
			if ("UNINDEX".equals(input2)) {
				System.out.println("Will unindex *all* snaps regarless of unescaping!");
				forceUnindex = true;
			} else {
				System.out.println("Will only unindex unescaped snaps.");
				forceUnindex = false;
			}

			ProgramLogic logic = new ProgramLogic();
			logic.execute(
					mongoServer, 
					mongoPort, 
					mongoDb,
					fromDate,
					forceUnindex);
			
		} else {
			System.out.println("Not doing anything.");
		}
	}

}
