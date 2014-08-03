package com.readrz.utils.db;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public final class UtcSqlDateFormat {

	public static final DateFormat create() {
		DateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sqlDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sqlDateFormat;
	}

}
