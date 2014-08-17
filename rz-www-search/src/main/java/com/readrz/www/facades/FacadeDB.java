package com.readrz.www.facades;

import java.net.UnknownHostException;

import me.akuz.core.SystemUtils;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;

public final class FacadeDB {
	
	private static final Object _lock = new Object();
	private static DB _instance;
	
	static {
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    public void run() {
		    	if (_instance != null) {
			    	synchronized (_lock) {
			    		if (_instance != null) {
			    			_instance.getMongo().close();
			    		}
					}
		    	}
		    }
		 });
	}
	
	public static DB get() {
		
		if (_instance == null) {
			synchronized (_lock) {
				if (_instance == null) {

					MongoClientOptions options
						= new MongoClientOptions.Builder()
							.connectTimeout(3000)
							.build();
					
					ServerAddress address;
					try {
						if (SystemUtils.isLocalhost()) {
							address = new ServerAddress("localhost", 27017);
						} else {
							address = new ServerAddress("ec2-54-228-193-94.eu-west-1.compute.amazonaws.com", 27017);
						}
					} catch (UnknownHostException e) {
						throw new IllegalArgumentException("Unknown Mongo DB server host", e);
					}
					
					MongoClient mongoClient = new MongoClient(address, options);
					
					_instance = mongoClient.getDB("readrz");
				}
			}
		}
		return _instance;
	}
	
	

}
