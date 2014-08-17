package com.readrz.data.index;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import me.akuz.core.Hit;

/**
 * Hits of a specific detected entity within a document (inverse index),
 * arranged by sentence; with start position and hits list for each sentence, 
 * and each hit indicating start and end index of the word's occurrence 
 * (using shorts for positions, and so max document length is 32,767).
 * Max sentence count in a doc is 255, as limited by byte data type.
 *
 */
public final class Hits {

	private int _i; // cursor
	private final byte[] _data;
	
	public Hits(byte[] data) {
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
			throw new IllegalStateException("Hits data is corrupt: next sentence is outside of bounds");
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
		int sentenceBytesLen = shortBuffer.get();
		return sentenceBytesLen;
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
	
	public List<Hit> getSentenceHitsList() {
		if (_i < 0 || _i >= _data.length) {
			throw new IllegalStateException("Current sentence not available");
		}
		if (_i + 7 > _data.length) {
			throw new IllegalStateException("FwdHits data is corrupt: cannot get sentence hits count");
		}
		int hitsCount = _data[_i+6] & 0xFF;
		List<Hit> hitsList = new ArrayList<Hit>(hitsCount);
		
		ShortBuffer shortBuffer = ByteBuffer.wrap(_data, _i+7, hitsCount*4).order(ByteOrder.BIG_ENDIAN).asShortBuffer();
		for (int i=0; i<hitsCount; i++) {
			int start = shortBuffer.get();
			int end = shortBuffer.get();
			Hit hit = new Hit(start, end);
			hitsList.add(hit);
		}
		
		return hitsList;
	}

}
