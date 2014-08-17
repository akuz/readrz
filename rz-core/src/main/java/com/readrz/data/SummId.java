package com.readrz.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import me.akuz.core.ArrayUtils;
import me.akuz.core.StringUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Summary binary identifier.
 *
 */
public final class SummId {

	private final byte[] _data;
	private final int _kindId;
	private final int _periodId;
	private final List<Integer> _searchKeyIds;
	private final List<Integer> _groupKeyIds;
	
	public SummId(byte[] data) {
		if (data == null) {
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " data is corrupt: code 0");
		}
		if (data.length % 4 != 0) {
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " id data is corrupt: code 1");
		}
		if (data.length < 4) {
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " data is corrupt: code 2");
		}
		_kindId   = data[data.length-4] & 0xFF;
		_periodId = data[data.length-3] & 0xFF;
		int searchKeysCount = data[data.length-2] & 0xFF;
		int groupKeysCount  = data[data.length-1] & 0xFF;
		
		_searchKeyIds = new ArrayList<>(searchKeysCount);
		_groupKeyIds  = new ArrayList<>(groupKeysCount);
		
		final int keysCount 
			= searchKeysCount 
			+ groupKeysCount;

		int correctByteLength = keysCount * 4 + 4;
		if (correctByteLength != data.length) {
			throw new IllegalArgumentException(this.getClass().getSimpleName() + " data is corrupt: code 3");
		}
		
		IntBuffer intBuffer 
			= ByteBuffer
				.wrap(data, 0, 4 * keysCount)
				.order(ByteOrder.BIG_ENDIAN)
				.asIntBuffer();
		
		for (int i=0; i<searchKeysCount; i++) {
			_searchKeyIds.add(intBuffer.get());
		}
		for (int i=0; i<groupKeysCount; i++) {
			_groupKeyIds.add(intBuffer.get());
		}

		_data = data;
	}
	
	public byte[] getData() {
		return _data;
	}
	
	public int getKindId() {
		return _kindId;
	}
	
	public int getPeriodId() {
		return _periodId;
	}
	
	public List<Integer> getSearchKeyIds() {
		return _searchKeyIds;
	}
	
	public List<Integer> getGroupKeyIds() {
		return _groupKeyIds;
	}
	
	public DBObject toDbo() {
		DBObject dbo = new BasicDBObject();
		dbo.put("kindId", _kindId);
		dbo.put("periodId", _periodId);
		if (_searchKeyIds != null && _searchKeyIds.size() > 0) {
			dbo.put("searchKeyIds", _searchKeyIds);
		}
		if (_groupKeyIds != null && _groupKeyIds.size() > 0) {
			dbo.put("groupKeyIds", _groupKeyIds);
		}
		return dbo;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof SummId)) {
			return false;
		}
		SummId o = (SummId)obj;
		return ArrayUtils.equals(_data, o._data);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getSimpleName());
		sb.append(" (kind: [");
		sb.append(_kindId);
		sb.append("], period: [");
		sb.append(_periodId);
		sb.append("], search: [");
		sb.append(StringUtils.collectionToString(_searchKeyIds, ","));
		sb.append("], groups: [");
		sb.append(StringUtils.collectionToString(_groupKeyIds, ","));
		sb.append("])");
		return sb.toString();
	}

}
