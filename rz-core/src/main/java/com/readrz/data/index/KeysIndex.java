package com.readrz.data.index;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.akuz.core.UtcDate;
import me.akuz.core.crypt.MurmurHash3;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.readrz.utils.db.MongoUtils;

/**
 * Provides functionality to obtain unique integer numbers
 * for any string, in a synchronized way backed by hashing 
 * and using database for resolving hashing conflicts 
 * (infrequent if hashing algorithm is good).
 *
 */
public final class KeysIndex {
	
	public static final String _idField = MongoUtils._id;
	public static final String _originalHashField = "o"; // if collided with other hash
	public static final String _strField = "s";
	
	private final boolean _verbose;
	private final DBCollection _coll;
	private Map<String, Integer> _cache;
	private Map<Integer, String> _cacheInverse;
	
	public KeysIndex(DBCollection coll) {
		this(coll, false);
	}
	
	public KeysIndex(DBCollection coll, boolean verbose) {
		if (coll == null) {
			throw new NullPointerException("coll");
		}
		_verbose = verbose;
		_coll = coll;
		_cache = new HashMap<String, Integer>();
		_cacheInverse = new HashMap<Integer, String>();
	}
	
	private final static void log(String str) {
		UtcDate now = new UtcDate();
		String cname = KeysIndex.class.getSimpleName();
		System.out.println(now.toString() + " - " + cname + " - " + str);
	}
	
	/**
	 * Loads the latest string ids cache from the database
	 * and entirely replaces the in-memory cache.
	 * 
	 */
	public void loadFromDB() {
		
		if (_verbose) {
			log("Loading from db...");
		}
		Map<String, Integer> cache = new HashMap<String, Integer>();
		Map<Integer, String> cacheInverse = new HashMap<Integer, String>();

		DBCursor dbCursor = _coll.find();
		try {
			while (dbCursor.hasNext()) {
				
				DBObject dbo = dbCursor.next();
				
				String str = (String)dbo.get(_strField);
				if (str == null) {
					throw new NullPointerException("Db key contains null string: " + dbo);
				}
				Integer id = (Integer)dbo.get(_idField);
				if (id == null) {
					throw new NullPointerException("Db key contains null id for string " + str);
				}
				cache.put(str, id);
				cacheInverse.put(id, str);
				if (_verbose) {
					log("Loaded: " + id + " " + str);
				}
			}
		} finally {
			dbCursor.close();
		}
		if (_verbose) {
			log("Finished loading from db...");
		}
		
		_cache = cache;
		_cacheInverse = cacheInverse;
	}

	public String getStrCached(Integer id) {
		if (id == null) {
			throw new NullPointerException("Cannot use null id");
		}
		return _cacheInverse.get(id);
	}

	public Integer getIdCached(String str) {
		if (str == null || str.length() == 0) {
			throw new NullPointerException("Cannot get key null or empty strings");
		}
		Integer id = _cache.get(str);
		if (id != null) {
			if (_verbose) {
				log("Id from cache: " + id + " " + str);
			}
			return id;
		}
		return null;
	}
	
	/**
	 * Returns the cached id for string, if exists, otherwise creates 
	 * a new id based on the hashing algorithm, and makes sure it's 
	 * unique by synchronizing with the database, and modifying 
	 * hash if collisions are detected.
	 * 
	 */
	public Integer getId(String str) {
		
		if (str == null || str.length() == 0) {
			throw new NullPointerException("Cannot get key null or empty strings");
		}
		
		// check if cached already
		Integer id = _cache.get(str);
		if (id != null) {
			if (_verbose) {
				log("Id from cache: " + id + " " + str);
			}
			return id;
		}
		
		// calculate *starting* hash to try to use as id
		int hash = MurmurHash3.murmurhash3_x86_32(str);
		int originalHash = hash;
		
		// keep trying
		while (true) {

			// check if possibly already inserted by others
			{
				BasicDBObject q = new BasicDBObject().append(_strField, str);
				DBObject dbo = _coll.findOne(q);
				if (dbo != null) {
					id = (Integer)dbo.get(_idField);
					if (id == null) {
						throw new NullPointerException("Db key contains null id for string " + str);
					}
					_cache.put(str, id);
					_cacheInverse.put(id, str);

					if (_verbose) {
						log("Id from database: " + id + " " + str);
					}
					break;
				}
			}

			// check if proposed hash id is available (not used by other word)
			BasicDBObject q = new BasicDBObject().append(_idField, hash);
			DBObject dbo = _coll.findOne(q);
			if (dbo != null) {
				String strThere = (String)dbo.get(_strField);
				if (strThere == null) {
					throw new NullPointerException("Db key contains null string for id " + hash);
				}
				if (str.equals(strThere)) {
					
					// someone already inserted with the proposed hash
					_cache.put(str, hash);
					_cacheInverse.put(hash, str);
					id = hash;
					
					if (_verbose) {
						log("Id from database (got before trying to insert): " + id + " " + str);
					}
					break;
					
				} else {
					// another string is already using this hash as id,
					// so try to inc hash and use it as id
					hash += 1;

					if (_verbose) {
						log("Id " + id + " in use by " + strThere + ", will try another: " + hash + " " + str);
					}
					continue;
				}
				
			} else { // try to insert
				
				dbo = new BasicDBObject()
					.append(_idField, hash)
					.append(_strField, str);
				
				if (hash != originalHash) {
					dbo.put(_originalHashField, originalHash);
				}

				try {
					
					_coll.insert(dbo, WriteConcern.ACKNOWLEDGED);
					_cache.put(str, hash);
					_cacheInverse.put(hash, str);
					id = hash;

					if (_verbose) {
						log("Inserted new id: " + id + " " + str);
					}
					break;
					
				} catch (Exception ex) {
					
					// could not insert, possible reasons:
					// 1) network error
					// 2) database key error (someone already written the same)
					// >>
					// so we will try again: 
					// 1) if it was a network error, the find() will throw again
					// 2) if it was an key collision, we will find existing entry

					if (_verbose) {
						log("Error inserting new id (" + ex.getClass().getSimpleName() 
								+ "), will retry: " + id + " " + str);
					}
					continue;
				}
			}
		}
		
		if (id == null) {
			throw new InternalError("Error in the keys algorithm, trying to return null value");
		}
		return id;
	}
	
	/**
	 * Get key ids for all specified keys.
	 * 
	 */
	public Set<Integer> getKeyIds(Set<String> keys) {
		if (keys == null) {
			return null;
		}
		Set<Integer> keyIds = new HashSet<Integer>();
		for (String key : keys) {
			Integer stopKeyId = getId(key);
			keyIds.add(stopKeyId);
		}
		return keyIds;
	}

	public final static void ensureIndices(DBCollection coll) {
		coll.createIndex(
				new BasicDBObject()
					.append(_strField, 1), 
				new BasicDBObject()
					.append("name", "idxStr")
					.append("unique", true));
	}
}
