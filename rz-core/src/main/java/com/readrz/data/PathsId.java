package com.readrz.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import com.readrz.data.index.KeysIndex;
import com.readrz.search.QueryKeyIds;

/**
 * Paths *identifier* object used in the database collection.
 *
 */
public final class PathsId {

	private final byte[] _data;
	private final int _periodId;
	private final QueryKeyIds _queryKeyIds;
	
	public PathsId(byte[] data) {
		if (data == null) {
			throw new IllegalArgumentException("Paths id data is corrupt: code 0");
		}
		if (data.length % 4 != 0) {
			throw new IllegalArgumentException("Paths id data is corrupt: code 1");
		}
		_periodId = data[data.length-4] & 0xFF;
		int documentKeysCount = data[data.length-3] & 0xFF;
		int senCheckKeysCount = data[data.length-2] & 0xFF;
		int sentenceKeysCount = data[data.length-1] & 0xFF;

		_queryKeyIds = new QueryKeyIds();
		
		final int keysCount 
			= documentKeysCount 
			+ senCheckKeysCount 
			+ sentenceKeysCount;
		
		IntBuffer intBuffer = ByteBuffer.wrap(data, 0, 4 * keysCount).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
		for (int i=0; i<documentKeysCount; i++) {
			_queryKeyIds.addDocumentKeyId(intBuffer.get());
		}
		for (int i=0; i<senCheckKeysCount; i++) {
			_queryKeyIds.addSenCheckKeyId(intBuffer.get());
		}
		for (int i=0; i<sentenceKeysCount; i++) {
			_queryKeyIds.addSentenceKeyId(intBuffer.get());
		}

		_data = data;
	}
	
	public byte[] getData() {
		return _data;
	}
	
	public int getPeriodId() {
		return _periodId;
	}
	
	public Period getPeriod() {
		return Period.getOrThrow(_periodId);
	}
	
	public QueryKeyIds getQueryKeyIds() {
		return _queryKeyIds;
	}

	public String toString(KeysIndex keysIndex) {
		StringBuilder sb = new StringBuilder();
		sb.append(getPeriod().getName());
		sb.append(", query keys: ");
		sb.append(_queryKeyIds.toString(keysIndex));
		return sb.toString();
	}

}
