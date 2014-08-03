package com.readrz.utils.dups;

import java.util.Date;

/**
 * Chronological objects, comparable in terms of similarity.
 *
 * @param <T> "Object" type
 */
public interface ChronoSimilar<T> {
	
	/**
	 * Returns the date of this object.
	 * @return
	 */
	Date getDate();
	
	/**
	 * Checks if this object similar to another object.
	 * @param other
	 * @return
	 */
	boolean isSimilarTo(T other);
}
