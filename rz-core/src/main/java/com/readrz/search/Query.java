package com.readrz.search;

import java.util.ArrayList;
import java.util.List;

/**
 * Query object representing a parsed query string.
 *
 */
public final class Query {
	
	public static final Query empty = new Query(null);
	
	private final String _queryString;
	private final List<QueryTerm> _terms;
	private final QueryKeyIds _queryKeyIds;
	private boolean _isSomeTermsNotFound;
	
	public Query(String queryString) {
		_queryString = queryString;
		_terms = new ArrayList<>();
		_queryKeyIds = new QueryKeyIds();
	}
	
	public String getQueryString() {
		return _queryString;
	}
	
	public List<QueryTerm> getTerms() {
		return _terms;
	}
	public void addTerm(QueryTerm term) {
		_terms.add(term);
		if (term.getIsDocumentLevel()) {
			_queryKeyIds.addDocumentKeyId(term.getKeyId());
		} else {
			_queryKeyIds.addSentenceKeyId(term.getKeyId());
		}
	}
	
	public QueryKeyIds getQueryKeyIds() {
		return _queryKeyIds;
	}

	public boolean getIsSomeTermsNotFound() {
		return _isSomeTermsNotFound;
	}
		public void setIsSomeTermsNotFound(boolean is) {
		_isSomeTermsNotFound = is;
	}
	
	public boolean getIsEmpty() {
		return _terms.size() == 0;
	}
	
	public static final String formatSearchSentenceTerm(String str) {
		return str;
	}
	public static final String formatSearchDocumentTerm(String str) {
		return String.format("%s*", str);
	}
	
}
