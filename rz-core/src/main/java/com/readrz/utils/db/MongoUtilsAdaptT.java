package com.readrz.utils.db;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mongodb.BasicDBList;

public final class MongoUtilsAdaptT<T> {
	
	public static final MongoUtilsAdaptT<Integer> Integer = new MongoUtilsAdaptT<>();
	public static final MongoUtilsAdaptT<Double>  Double = new MongoUtilsAdaptT<>();
	public static final MongoUtilsAdaptT<String>  String = new MongoUtilsAdaptT<>();
	
	@SuppressWarnings("unchecked")
	public final List<T> toList(BasicDBList dbList) {
		List<T> list = new ArrayList<>();
		if (dbList != null) {
			for (int i=0; i<dbList.size(); i++) {
				list.add((T)dbList.get(i));
			}
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public final Set<T> toSet(BasicDBList dbList) {
		Set<T> set = new HashSet<>();
		if (dbList != null) {
			for (int i=0; i<dbList.size(); i++) {
				set.add((T)dbList.get(i));
			}
		}
		return set;
	}
	
}
