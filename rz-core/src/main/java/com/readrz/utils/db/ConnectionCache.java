package com.readrz.utils.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class ConnectionCache {

	private final String _connString;
	private final int _expiryMinutes;
	
	private final Object _lock = new Object();
	private final Map<Connection, Integer> _counters;
	private Connection _currentConn;
	private Date _lastDate;
	
	public ConnectionCache(String connString, int expiryMinutes) {
		
		_connString = connString;
		_expiryMinutes = expiryMinutes;
		_counters = new HashMap<Connection, Integer>();
	}
	
	public Connection getConn() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {

		synchronized (_lock) {

			if (checkNeedsRefresh()) {

				System.out.println("Reconnecting to the database...");
				_currentConn = MySqlUtils.createConnectionUtf8(_connString);
				Calendar now = me.akuz.core.DateUtils.getUTCCalendar();
				_lastDate = now.getTime();
			}
			
			// increase count for this connection
			Integer count = _counters.get(_currentConn);
			if (count == null) {
				count = 0;
			}
			_counters.put(_currentConn, count + 1);
			
			closeObsoleteConnections();
			
			return _currentConn;
		}
	}
	
	public void returnConn(Connection conn) {
		synchronized (_lock) {
			Integer count = _counters.get(conn);
			if (count == null || count == 0) {
				throw new IllegalStateException("Cannot return this conneciton");
			}
			_counters.put(conn, count-1);
		}
		closeObsoleteConnections();
	}

	public void closeObsoleteConnections() {
		synchronized (_lock) {
			ArrayList<Connection> toRemove = null;
			for (Connection conn : _counters.keySet()) {
				if (_currentConn != conn) {
					Integer count = _counters.get(conn);
					if (count == 0) {
						if (toRemove == null) {
							toRemove = new ArrayList<Connection>();
						}
						toRemove.add(conn);
					}
				}
			}
			if (toRemove != null) {
				for (int i=0; i<toRemove.size(); i++) {
					Connection conn = toRemove.get(i);
					_counters.remove(conn);
					try {
						conn.close();
					} catch (SQLException e) {
						System.out.println("WARNING: Could not close cached DB connection: " + e.getMessage());
					}
				}
			}
		}
	}
	
	public void closeAllConnections() {
		synchronized (_lock) {
			for (Connection conn : _counters.keySet()) {
				try {
					conn.close();
				} catch (SQLException e) {
					System.out.println("WARNING: Could not close cached DB connection: " + e.getMessage());
				}
			}
			_counters.clear();
			_currentConn = null;
			_lastDate = null;
		}
	}
	
	private boolean checkNeedsRefresh() {

		if (_currentConn == null) {
			return true;
		} else if (_lastDate == null) {
			return true;
		} else {
			Calendar expiryCal = me.akuz.core.DateUtils.getUTCCalendar();
			expiryCal.add(Calendar.MINUTE, -_expiryMinutes);
			Date expiryDate = expiryCal.getTime();
			return _lastDate.compareTo(expiryDate) < 0;
		}
	}
	
}
