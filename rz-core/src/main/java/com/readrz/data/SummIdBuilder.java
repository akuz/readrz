package com.readrz.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Summary binary id builder.
 *
 */
public final class SummIdBuilder {
	
	private final int _kindId;
	private final int _periodId;
	private int[] _searchKeyIds;
	private int[] _groupKeyIds;
	
	public SummIdBuilder(final int kindId, final int periodId) {
		if (kindId < 0 || kindId > 255) {
			throw new IllegalArgumentException("Kind id should be in [0,255]");
		}
		if (periodId < 0 || periodId > 255) {
			throw new IllegalArgumentException("Period id should be in [0,255]");
		}
		_kindId = kindId;
		_periodId = periodId;
	}
	
	public void setSearchKeyIds(List<Integer> searchKeyIds) {
		if (searchKeyIds != null) {
			List<Integer> sorted = new ArrayList<>(searchKeyIds);
			if (sorted.size() > 1) {
				Collections.sort(sorted);
			}
			_searchKeyIds = new int[sorted.size()];
			for (int i=0; i<sorted.size(); i++) {
				_searchKeyIds[i] = sorted.get(i);
			}
		} else {
			_searchKeyIds = null;
		}
	}
	
	public void setGroupKeyIds(List<Integer> groupKeyIds) {
		if (groupKeyIds != null) {
			_groupKeyIds = new int[groupKeyIds.size()];
			for (int i=0; i<groupKeyIds.size(); i++) {
				_groupKeyIds[i] = groupKeyIds.get(i);
			}
		} else {
			_groupKeyIds = null;
		}
	}
	
	public byte[] getData() {
		
		int searchKeysCount = _searchKeyIds != null ? _searchKeyIds.length : 0;
		int groupKeysCount  = _groupKeyIds  != null ? _groupKeyIds.length  : 0;
		
		if (searchKeysCount < 0 || searchKeysCount > 255) {
			throw new IllegalStateException("Search keys count must be >= 0 and <= 255");
		}
		if (groupKeysCount < 0 || groupKeysCount > 255) {
			throw new IllegalStateException("Group keys count must be >= 0 and <= 255");
		}
		
		int byteCount 
			= searchKeysCount * 4 
			+ groupKeysCount * 4 
			+ 4;
		
		byte[] data = new byte[byteCount];
		data[data.length-4] = (byte)_kindId;
		data[data.length-3] = (byte)_periodId;
		data[data.length-2] = (byte)searchKeysCount;
		data[data.length-1] = (byte)groupKeysCount;
		
		IntBuffer intBuffer
			= ByteBuffer
				.wrap(data, 0, data.length-4)
				.order(ByteOrder.BIG_ENDIAN)
				.asIntBuffer();
		
		if (searchKeysCount > 0) {
			for (int i=0; i<_searchKeyIds.length; i++) {
				intBuffer.put(_searchKeyIds[i]);
			}
		}
		
		if (groupKeysCount > 0) {
			for (int i=0; i<_groupKeyIds.length; i++) {
				intBuffer.put(_groupKeyIds[i]);
			}
		}

		return data;
	}
}
