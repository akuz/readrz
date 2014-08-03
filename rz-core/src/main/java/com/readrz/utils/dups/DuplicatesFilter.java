package com.readrz.utils.dups;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.akuz.core.DateUtils;
import me.akuz.core.Out;

/**
 * Duplicates filter that ignores *new* items, if they are similar 
 * to any of the old items within the specified period of time;
 * the order in which the items are added is *not* required to
 * be chronological.
 *
 */
public final class DuplicatesFilter<T extends ChronoSimilar<T>> {
	
	private final DuplicatesFilterMode _mode;
	private final double _backCheckDaysNumber;
	private final Map<Integer, Deque<T>> _queues;
	
	public DuplicatesFilter(DuplicatesFilterMode mode, double backCheckDays) {
		if (backCheckDays <=  0) {
			throw new InvalidParameterException("Parameter backCheckDays should be positive");
		}
		_mode = mode;
		_backCheckDaysNumber = backCheckDays;
		_queues = new HashMap<Integer, Deque<T>>();
	}
	
	public DuplicatesFilterMode getMode() {
		return _mode;
	}

	/**
	 * Returns true, if new item was added to history; 
	 * populates obsolete items list, if any items made obsolete.
	 * @return
	 */
	public boolean add(Date currDate, int sourceId, T newItem, Out<List<T>> outObsoleteItems) {
		
		// create deque if needed
		Deque<T> queue = _queues.get(sourceId);
		if (queue == null) {
			queue = new LinkedList<T>();
			_queues.put(sourceId, queue);
		}
		
		// get min back check date
		Date minDate = DateUtils.addDays(currDate, -_backCheckDaysNumber);
		
		// result objects
		T itemToAdd = newItem;
		List<T> obsoleteItems = null;
		
		// remove far enough in the past records
		Iterator<T> iter = queue.iterator();
		while (iter.hasNext()) {
			
			// get past record to check
			T oldItem = iter.next();
			
			// check if record is too old already
			if (oldItem.getDate().compareTo(minDate) < 0) {
				iter.remove();
				continue;
			}
			
			// check if we have a new version of the old record
			if (newItem.isSimilarTo(oldItem)) {
				
				if (_mode == DuplicatesFilterMode.DiscardNewDuplicates) {
					
					// don't add new item
					itemToAdd = null;
					
					// register new item as obsolete
					if (outObsoleteItems != null) {
						if (obsoleteItems == null) {
							obsoleteItems = new ArrayList<T>();
						}
						obsoleteItems.add(newItem);
					}
					
					// stop checking
					break;
					
				} else if (_mode == DuplicatesFilterMode.ReplaceOldDuplicates) {
					
					// register old item as obsolete
					if (outObsoleteItems != null) {
						if (obsoleteItems == null) {
							obsoleteItems = new ArrayList<T>();
						}
						obsoleteItems.add(oldItem);
					}
					
					// remove old item
					iter.remove();
					
					// keep checking
					continue;
				}
			}
		}

		// queue new item
		if (itemToAdd != null) {
			queue.addLast(itemToAdd);
		}
		
		// set obsolete items list
		if (outObsoleteItems != null) {
			outObsoleteItems.setValue(obsoleteItems);
		}
		
		return itemToAdd != null;
	}

}
