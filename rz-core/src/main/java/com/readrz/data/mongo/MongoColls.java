package com.readrz.data.mongo;

public final class MongoColls {

	/**
	 * Sources of content (collections of multiple 
	 * feeds with possibly duplicated content).
	 */
	public final static String sources  = "sources";
	
	/**
	 * Individual content feeds.
	 */
	public final static String feeds = "feeds";
	
	/**
	 * Articles from feeds.
	 */
	public final static String snaps = "snaps";
	
	/**
	 * Original html of the articles.
	 */
	public final static String snapshtml = "snapshtml";
	
	/**
	 * Images from the articles.
	 */
	public final static String snapsimag = "snapsimag";
	
	/**
	 * Thumbnails of images from the articles.
	 */
	public final static String snapsthumb = "snapsthumb";
	
	/**
	 * Inverse index of snaps by entity/stem keys.
	 */
	public final static String snapsidx = "snapsidx";
	
	/**
	 * Keys index (unique integer hash by string key).
	 */
	public final static String keys = "keys";

	/**
	 * All entities (pattern-detected and probabilistic).
	 */
	public final static String entities = "entities";
	
	/**
	 * Groups of pattern-detected entities.
	 */
	public final static String groupsPattern = "groups";
	
	/**
	 * Groups of topic entities.
	 */
	public final static String groupsTopic = "groups2";
	
	/**
	 * Calculated summary of snaps, given a query.
	 */
	public final static String paths = "paths";
	
	/**
	 * Paths requests submitted from user interface.
	 */
	public final static String pathsreq = "pathsreq";
	
	/**
	 * Calculated summaries.
	 */
	public final static String summ = "summ";
	
	/**
	 * Summary requests submitted from user interface.
	 */
	public final static String summreq = "summreq";

	/**
	 * Users table.
	 */
	public final static String user = "user";
	
	/**
	 * Temporary codes for user password reset.
	 */
	public final static String userCode = "userCode";
	
	/**
	 * Old user names (to allow changing back).
	 */
	public final static String userOldName = "userOldName";
	
	/**
	 * Server-side session ids for scaling.
	 */
	public final static String userSession = "userSession";
	
	/**
	 * Persisted user-defined tabs.
	 */
	public final static String userTab = "userTab";
}
