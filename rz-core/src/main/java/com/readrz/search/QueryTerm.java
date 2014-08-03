package com.readrz.search;

import me.akuz.core.Hit;
import me.akuz.core.StringUtils;


/**
 * Search query term; by default the search term is "sentence level",
 * which means it must occur in the same sentence with other sentence
 * level terms, if any; "document level" means the term must occur
 * somewhere in a document, but not necessarily in the same
 * sentence with sentence level terms, if any; optional
 * level terms are, obviously, optional, but will be
 * highlighted in the search results, if present.
 *
 */
public final class QueryTerm {
	
	private final Hit _hit;
	private final Integer _keyId;
	private final String _origWord;
	private final String _betterWord;
	private boolean _isDocumentLevel;
	
	public QueryTerm(
			Hit hit, 
			Integer keyId, 
			String origWord) {
		
		_hit = hit;
		_keyId = keyId;
		_origWord = origWord;
		_betterWord = StringUtils.capitalizeIfNoCaps(origWord);
	}
	
	public Hit getHit() {
		return _hit;
	}
	
	public Integer getKeyId() {
		return _keyId;
	}
	
	public String getOrigWord() {
		return _origWord;
	}
	
	public String getBetterWord() {
		return _betterWord;
	}

	public boolean getIsDocumentLevel() {
		return _isDocumentLevel;
	}
	public void setIsDocumentLevel(boolean is) {
		_isDocumentLevel = is;
	}
}
