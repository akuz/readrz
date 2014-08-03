package com.readrz.utils.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

public final class MySqlUtils {

	public static final Connection createConnectionUtf8(String connString) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		
		if (connString == null || connString.length() == 0) {
			throw new IllegalArgumentException("connString");
		}

		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Properties props = new Properties();
		props.put("useUnicode", "true");
		props.put("characterEncoding", "utf8");
		props.put("characterSetResults", "utf8");
		Connection conn = DriverManager.getConnection(connString, props);

		Statement st = conn.createStatement();
		st.execute("SET NAMES 'utf8'");
		st.execute("SET character_set_client=utf8");
		st.execute("SET character_set_connection=utf8");
		st.execute("SET character_set_results=utf8");
		st.close();
		
		return conn;
	}
	
	@SuppressWarnings("deprecation")
	public static Date getDateTime(ResultSet rs, String field, Calendar cal) throws SQLException {
		
		Date date = rs.getDate(field, cal);
		Date time = rs.getTime(field, cal);
		if (date == null || time == null) {
			return null;
		} else {
			cal.clear();
			cal.set(1900 + date.getYear(), date.getMonth(), date.getDate(), time.getHours(), time.getMinutes(), time.getSeconds());
			return cal.getTime();
		}
	}
}
