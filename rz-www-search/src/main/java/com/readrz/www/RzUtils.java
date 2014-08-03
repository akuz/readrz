package com.readrz.www;

import java.util.ArrayList;
import java.util.List;

import com.readrz.search.QueryTerm;

public final class RzUtils {
	
	public static final String slashEncode(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		return str.replaceAll("\\/", " ");
	}
	
	public static final String slashDecode(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		return str.replaceAll("\\s", "/");
	}

	public static final List<Integer> keyIdsFromQueryTerms(List<QueryTerm> queryTerms) {
		
		if (queryTerms != null && queryTerms.size() > 0) {
			
			List<Integer> keyIds = new ArrayList<Integer>();
			for (int i=0; i<queryTerms.size(); i++) {
				keyIds.add(queryTerms.get(i).getKeyId());
			}
			return keyIds;

		} else {
			return null;
		}
	}
}
