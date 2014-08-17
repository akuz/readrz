package com.readrz.data.index;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import me.akuz.core.Hit;

/**
 * Hits of all detected entities in a document (forward index),
 * arranged by sentence; with lists of hits by kind for each sentence, 
 * and each hit indicating start and end index of the word's occurrence 
 * (using shorts for positions, and so max document length is 32,767).
 * Max sentence count in a doc is 255, as limited by byte data type.
 *
 */
public final class FwdHits {
	
	private int _i; // cursor
	private final byte[] _data;
	
	public FwdHits(byte[] data) {
		if (data == null) {
			throw new NullPointerException("Hits data cannot be null");
		}
		_data = data;
		_i = -1;
	}	
	
	public void reset() {
		_i = -1;
	}
	
	public boolean nextSentence() {
		
		// check end
		if (_i >= _data.length) {
			return false;
		}

		// move cursor
		if (_i < 0) {
			_i = 0;
		} else {
			// we already checked that we can get 
			// the length at the previous move
			_i += getSentenceByteLen();
		}
		
		// check end after move
		if (_i >= _data.length) {
			return false;
		}
		
		// check length
		int sentenceByteLen = getSentenceByteLen();
		if (_i + sentenceByteLen > _data.length) {
			throw new IllegalStateException("FwdHits data is corrupt: next sentence is outside of bounds");
		}
		
		return true;
	}
	
	private int getSentenceByteLen() {
		if (_i < 0 || _i >= _data.length) {
			throw new IllegalStateException("Current sentence not available");
		}
		if (_i + 2 > _data.length) {
			throw new IllegalStateException("FwdHits data is corrupt: cannot get sentence length");
		}
		ShortBuffer shortBuffer = ByteBuffer.wrap(_data, _i, 2).order(ByteOrder.BIG_ENDIAN).asShortBuffer();
		int sentenceByteLen = shortBuffer.get();
		return sentenceByteLen;
	}
	
	public Hit getSentenceHit() {
		if (_i < 0 || _i >= _data.length) {
			throw new IllegalStateException("Current sentence not available");
		}
		if (_i + 6 > _data.length) {
			throw new IllegalStateException("FwdHits data is corrupt: cannot get sentence hit");
		}
		ShortBuffer intBuffer = ByteBuffer.wrap(_data, _i+2, 4).order(ByteOrder.BIG_ENDIAN).asShortBuffer();
		int sentenceStart = intBuffer.get();
		int sentenceEnd = intBuffer.get();
		return new Hit(sentenceStart, sentenceEnd);
	}

	public FwdHitsMap getSentenceHits(EnumSet<FwdHitKind> fwdHitKinds) {
		
		if (_i < 0 || _i >= _data.length) {
			throw new IllegalStateException("Current sentence not available");
		}
		
		int sentenceBytesEnd = _i + getSentenceByteLen();
		
		FwdHitsMap map = new FwdHitsMap();
		
		int loopKindBytesStart = _i + 6;
		
		while (loopKindBytesStart < sentenceBytesEnd) {
			
			int fwdHitKindId = _data[loopKindBytesStart] & 0xFF;
			FwdHitKind kind = FwdHitKind.fromId(fwdHitKindId);
			int fwdHitsCount = _data[loopKindBytesStart+1] & 0xFF;
			
			if (kind != null && fwdHitKinds.contains(kind)) {
				
				List<FwdHit> fwdHits = new ArrayList<>(fwdHitsCount);

				for (int i=0; i<fwdHitsCount; i++) {
					
					int idx = loopKindBytesStart + 2 + i*8;
					
					IntBuffer intBuffer = ByteBuffer.wrap(_data, idx, 4).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
					int keyId = intBuffer.get();
					
					ShortBuffer shortBuffer = ByteBuffer.wrap(_data, idx + 4, 4).order(ByteOrder.BIG_ENDIAN).asShortBuffer();
					int start = shortBuffer.get();
					int end = shortBuffer.get();
					Hit hit = new Hit(start, end);
					
					FwdHit fwdHit = new FwdHit(keyId, hit);
					fwdHits.add(fwdHit);
				}
								
				map.put(kind, fwdHits);
			}
			loopKindBytesStart = loopKindBytesStart + 2 + 8 * fwdHitsCount;
		}

		return map;
	}
	
}
