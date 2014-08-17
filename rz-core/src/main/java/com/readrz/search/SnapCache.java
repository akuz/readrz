package com.readrz.search;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import me.akuz.core.DateUtils;

import org.bson.types.ObjectId;

import com.readrz.data.SnapInfo;

/**
 * Caches snaps in memory to avoid reloading.
 *
 */
public final class SnapCache {
	
	private final Object _lock = new Object();
	private final SnapSearcher _snapSearcher;
	private final Map<ObjectId, SnapInfo> _cache;
	private final int _cleanFreqMins;
	private Date _nextCleanTime;
	
	public SnapCache(SnapSearcher snapSearcher, int cleanFreqMins) {
		_snapSearcher = snapSearcher;
		_cache = new HashMap<>();
		_cleanFreqMins = cleanFreqMins;
		_nextCleanTime = DateUtils.addMinutes(new Date(), cleanFreqMins);
	}
	
	public SnapInfo get(final ObjectId snapId) {
		
		SnapInfo snapInfo;
		
		// get from cache
		synchronized (_lock) {
			snapInfo = _cache.get(snapId);
		}

		// return if cached
		if (snapInfo != null) {
			return snapInfo;
		}
		
		// load from database (not synchronized)
		snapInfo = _snapSearcher.findSnapInfo(snapId);

		synchronized (_lock) {
			// store null values too
			// until next clean is done
			_cache.put(snapId, snapInfo);
		}
		
		return snapInfo;
	}
	
	public boolean isTimeToClean() {

		return _nextCleanTime.compareTo(new Date()) < 0;
	}
	
	public void clean(final Date minDateInc) {
		
		synchronized (_lock) {

			for (Iterator<Entry<ObjectId, SnapInfo>> it = _cache.entrySet().iterator(); it.hasNext(); ) {
				
				Entry<ObjectId, SnapInfo> entry = it.next();
				
				// remove failed to load snaps
				if (entry.getValue() == null) {
					it.remove();
					continue;
				}
				
				// remove snaps that are not scanned
				if (entry.getValue().getSnap().isScanned() == false) {
					it.remove();
					continue;
				}
				
				// remove snaps that are beyond the min date
				if (minDateInc.compareTo(entry.getValue().getSnap().getSrcDate()) > 0) {
					it.remove();
					continue;
				}
			}
		}
		
		_nextCleanTime = DateUtils.addMinutes(new Date(), _cleanFreqMins);
	}

}
