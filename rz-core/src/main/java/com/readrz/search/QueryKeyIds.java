package com.readrz.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import me.akuz.core.CollectionUtils;

import com.readrz.data.index.KeysIndex;

/**
 * Contains key ids for query execution (can be created 
 * directly, or taken from Query parsed by QueryParser).
 *
 */
public final class QueryKeyIds implements Cloneable {

	private List<Integer> _documentKeyIds;
	private List<Integer> _senCheckKeyIds;
	private List<Integer> _sentenceKeyIds;
	
	public QueryKeyIds() {
		_documentKeyIds = new ArrayList<>();
		_senCheckKeyIds = new ArrayList<>();
		_sentenceKeyIds = new ArrayList<>();
	}
	
	public boolean isEmpty() {
		return 
			_documentKeyIds.size() == 0 &&
			_senCheckKeyIds.size() == 0 &&
			_sentenceKeyIds.size() == 0;
	}
	
	/**
	 * Key ids that must be present in the same *document*.
	 * Using list, not set, to enable searching for
	 * double occurrences (for example, "go go").
	 * 
	 */
	public List<Integer> getDocumentKeyIds() {
		return _documentKeyIds;
	}
	public QueryKeyIds addDocumentKeyId(Integer keyId) {
		_documentKeyIds.add(keyId);
		return this;
	}
	public QueryKeyIds addDocumentKeyIds(Collection<Integer> keyIds) {
		_documentKeyIds.addAll(keyIds);
		return this;
	}
	public QueryKeyIds removeDocumentKeyId(Integer keyId) {
		_documentKeyIds.remove(keyId);
		return this;
	}
	public QueryKeyIds clearDocumentKeyIds() {
		_documentKeyIds.clear();
		return this;
	}

	/**
	 * Key ids that must be present in the same *sentence*;
	 * These are the extra "check" key ids, that will not
	 * be highlighted as part of the search results.
	 * 
	 */
	public List<Integer> getSenCheckKeyIds() {
		return _senCheckKeyIds;
	}
	public QueryKeyIds addSenCheckKeyId(Integer keyId) {
		_senCheckKeyIds.add(keyId);
		return this;
	}
	public QueryKeyIds addSenCheckKeyIds(Collection<Integer> keyIds) {
		_senCheckKeyIds.addAll(keyIds);
		return this;
	}
	public QueryKeyIds removeSenCheckKeyId(Integer keyId) {
		_senCheckKeyIds.remove(keyId);
		return this;
	}
	public QueryKeyIds clearSenCheckKeyIds() {
		_senCheckKeyIds.clear();
		return this;
	}

	/**
	 * Key ids that must be present in the same *sentence*;
	 * Using list, not set, to enable searching for
	 * double occurrences (for example, "go go").
	 * 
	 */
	public List<Integer> getSentenceKeyIds() {
		return _sentenceKeyIds;
	}
	public QueryKeyIds addSentenceKeyId(Integer keyId) {
		_sentenceKeyIds.add(keyId);
		return this;
	}
	public QueryKeyIds addSentenceKeyIds(Collection<Integer> keyIds) {
		_sentenceKeyIds.addAll(keyIds);
		return this;
	}
	public QueryKeyIds removeSentenceKeyId(Integer keyId) {
		_sentenceKeyIds.remove(keyId);
		return this;
	}
	public QueryKeyIds clearSentenceKeyIds() {
		_sentenceKeyIds.clear();
		return this;
	}
	
	@Override
	public QueryKeyIds clone() {
		try {
			QueryKeyIds copy = (QueryKeyIds)super.clone();
			copy._documentKeyIds = new ArrayList<>(_documentKeyIds);
			copy._senCheckKeyIds = new ArrayList<>(_senCheckKeyIds);
			copy._sentenceKeyIds = new ArrayList<>(_sentenceKeyIds);
			return copy;
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("Clone not supported", e);
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		QueryKeyIds other = (QueryKeyIds)obj;
		
		return 
				CollectionUtils.Integer.isExactMatch(_documentKeyIds, other._documentKeyIds) &&
				CollectionUtils.Integer.isExactMatch(_senCheckKeyIds, other._senCheckKeyIds) &&
				CollectionUtils.Integer.isExactMatch(_sentenceKeyIds, other._sentenceKeyIds);
	}

	public String toString(KeysIndex keysIndex) {
		StringBuilder sb = new StringBuilder();
		sb.append("{document: ");
		sb.append(keysToString(_documentKeyIds, keysIndex));
		sb.append("}, ");
		sb.append("{senCheck: ");
		sb.append(keysToString(_senCheckKeyIds, keysIndex));
		sb.append("}, ");
		sb.append("{sentence: ");
		sb.append(keysToString(_sentenceKeyIds, keysIndex));
		sb.append("}");
		return sb.toString();
	}

	private static final String keysToString(List<Integer> keyIds, KeysIndex keysIndex) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (Integer keyId : keyIds) {
			if (sb.length() > 0) {
				sb.append(" ");
			}
			String keyStr = keysIndex.getStrCached(keyId);
			sb.append(keyStr);
			sb.append(" (");
			sb.append(keyId);
			sb.append(")");
		}
		sb.append("]");
		return sb.toString();
	}

	public void add(QueryKeyIds other) {
		_documentKeyIds.addAll(other._documentKeyIds);
		_senCheckKeyIds.addAll(other._senCheckKeyIds);
		_sentenceKeyIds.addAll(other._sentenceKeyIds);
	}
}
