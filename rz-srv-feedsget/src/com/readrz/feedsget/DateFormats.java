package com.readrz.feedsget;

import me.akuz.core.MultiDateFormat;

public final class DateFormats {
	
	public static MultiDateFormat instance;
	
	static {
		instance = new MultiDateFormat();
		instance.isThreadSafe(true);
		instance.addFormat("yyyy-MM-dd");
		instance.addFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		instance.addFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		instance.addFormat("yyyy-MM-dd'T'HH:mm:ssz");
		instance.addFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		instance.addFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		instance.addFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz");
		instance.addFormat("EEE, dd MMM yyyy HH:mm:ss 'UT'");
		instance.addFormat("EEE, dd MMM yyyy HH:mm:ss 'Z'");
		instance.addFormat("EEE, dd MMM yyyy HH:mm:ss Z");
		instance.addFormat("EEE, dd MMM yyyy HH:mm:ss z");
		instance.addFormat("EEE, dd MMM yyyy HH:mm 'UT'");
		instance.addFormat("EEE, dd MMM yyyy HH:mm 'Z'");
		instance.addFormat("EEE, dd MMM yyyy HH:mm Z");
		instance.addFormat("EEE, dd MMM yyyy HH:mm z");
	}

}
