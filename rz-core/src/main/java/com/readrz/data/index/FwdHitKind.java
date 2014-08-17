package com.readrz.data.index;

import java.util.EnumSet;

/**
 * Enumerates the kinds of forward hits: regular words, 
 * patterns & pattern groups, and topics and topic groups.
 *
 */
public enum FwdHitKind {
	
	/**
	 * Regular words (all).
	 */
	WORD (0),
	
	/**
	 * Pattern entities occurrences.
	 */
	PATTERN (10),

	/**
	 * Pattern groups occurrences.
	 */
	PATTERN_GROUP (11),

	/**
	 * Topic entities occurrences.
	 */
	TOPIC (20),

	/**
	 * Topic groups occurrences.
	 */
	TOPIC_GROUP (21);
	
	/**
	 * All forward hit types.
	 */
	public static final EnumSet<FwdHitKind> ALL = EnumSet.allOf(FwdHitKind.class);
	
	/**
	 * Group forward hit types.
	 */
	public static final EnumSet<FwdHitKind> ALL_GROUPS = EnumSet.of(FwdHitKind.TOPIC_GROUP, FwdHitKind.PATTERN_GROUP);
	
	/**
	 * Entity forward hit types.
	 */
	public static final EnumSet<FwdHitKind> ALL_ENTITIES = EnumSet.of(FwdHitKind.TOPIC, FwdHitKind.PATTERN);
	
	/**
	 * Basic forward hit types (maybe add numbers later).
	 */
	public static final EnumSet<FwdHitKind> ALL_BASICS = EnumSet.of(FwdHitKind.WORD);

	/**
	 * Create forward hit kind from id (or return null).
	 */
	public static FwdHitKind fromId(int id) {
		switch (id) {
			case 0: return WORD;
			case 10 : return PATTERN;
			case 11 : return PATTERN_GROUP;
			case 20: return TOPIC;
			case 21: return TOPIC_GROUP;
			default: return null;
		}
	}
	
	// keeping id inside 
	// for serialization
	private int _id;

	private FwdHitKind(int id) {
		if (id < 0 || id > 255) {
			throw new IllegalArgumentException("id must be within [0, 255]");
		}
		_id = id;
	}
	public int getId() {
		return _id;
	}
}
