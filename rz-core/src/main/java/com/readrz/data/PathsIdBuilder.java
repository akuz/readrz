package com.readrz.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.readrz.search.QueryKeyIds;

/**
 * Builds binary id for paths entry.
 *
 */
public final class PathsIdBuilder {
	
	private final int _periodId;
	private QueryKeyIds _queryKeyIds;
	
	public PathsIdBuilder(final int periodId) {
		if (periodId < 0 || periodId > 255) {
			throw new IllegalArgumentException("Period id should be in [0,255]");
		}
		_periodId = periodId;
	}
	
	public void setQueryKeyIds(QueryKeyIds queryKeyIds) {
		_queryKeyIds = queryKeyIds;
	}
	
	public byte[] getData() {
		
		int documentKeysCount = _queryKeyIds != null ? _queryKeyIds.getDocumentKeyIds().size() : 0;
		int senCheckKeysCount = _queryKeyIds != null ? _queryKeyIds.getSenCheckKeyIds().size() : 0;
		int sentenceKeysCount = _queryKeyIds != null ? _queryKeyIds.getSentenceKeyIds().size() : 0;
		
		if (documentKeysCount < 0 || documentKeysCount > 255) {
			throw new IllegalStateException("Document keys count should be >= 0 and <= 255");
		}
		if (senCheckKeysCount < 0 || senCheckKeysCount > 255) {
			throw new IllegalStateException("SenCheck keys count should be >= 0 and <= 255");
		}
		if (sentenceKeysCount < 0 || sentenceKeysCount > 255) {
			throw new IllegalStateException("Sentence keys count should be >= 0 and <= 255");
		}
		
		int byteCount 
			= documentKeysCount * 4 
			+ senCheckKeysCount * 4 
			+ sentenceKeysCount * 4 
			+ 4;
		
		byte[] data = new byte[byteCount];
		data[data.length-4] = (byte)_periodId;
		data[data.length-3] = (byte)documentKeysCount;
		data[data.length-2] = (byte)senCheckKeysCount;
		data[data.length-1] = (byte)sentenceKeysCount;
		
		IntBuffer intBuffer = ByteBuffer.wrap(data, 0, data.length-3).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
		
		if (documentKeysCount > 0) {
			
			List<Integer> keyIdsList = new ArrayList<Integer>(_queryKeyIds.getDocumentKeyIds());
			Collections.sort(keyIdsList);
	
			for (int i=0; i<keyIdsList.size(); i++) {
				Integer keyId = keyIdsList.get(i);
				intBuffer.put(keyId);
			}
		}
		
		if (senCheckKeysCount > 0) {
			
			List<Integer> keyIdsList = new ArrayList<Integer>(_queryKeyIds.getSenCheckKeyIds());
			Collections.sort(keyIdsList);
	
			for (int i=0; i<keyIdsList.size(); i++) {
				Integer keyId = keyIdsList.get(i);
				intBuffer.put(keyId);
			}
		}
		
		if (sentenceKeysCount > 0) {
			
			List<Integer> keyIdsList = new ArrayList<Integer>(_queryKeyIds.getSentenceKeyIds());
			Collections.sort(keyIdsList);
	
			for (int i=0; i<keyIdsList.size(); i++) {
				Integer keyId = keyIdsList.get(i);
				intBuffer.put(keyId);
			}
		}

		return data;
	}
}
