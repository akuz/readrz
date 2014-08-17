package com.readrz.data.index;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.List;

import me.akuz.core.Hit;

/**
 * Builds binary inverse hits data (describing occurrences 
 * of a specific word within sentences in a document). 
 * Max document length is Short.MAX_VALUE.
 * Max number of sentences is 256.
 *
 */
public final class HitsBuilder {
	
	private int _lastSentenceEnd;
	private final ByteArrayOutputStream _baos;
	private boolean _isAtLeastOneAdded;
	
	public HitsBuilder() {
		_baos = new ByteArrayOutputStream();
	}
	
	/**
	 * Add inverse hits for one sentence.
	 * @param sentenceStart - sentence start position (-1 means title)
	 * @param hitsList - list of inverse hits within this sentence
	 */
	public void addSentenceHits(Hit sentenceHit, List<Hit> hitsList) {
		if (_isAtLeastOneAdded && (_lastSentenceEnd > sentenceHit.start())) {
			throw new IllegalStateException(
					"Sentence hits must be increasing as you add them (last end: " 
					+ _lastSentenceEnd + ", added start: " + sentenceHit.start() + ")");
		}
		if (hitsList.size() == 0) {
			throw new IllegalArgumentException("Sentence hits count must be positive (otherwise don't add it)");
		}
		int hitsCount = Math.min(hitsList.size(), 255);
		int sentenceByteLen 
			= 2 // for sentence byte length 
			+ 4 // for sentence hit
			+ 1 // for hits count
			+ hitsCount * 4; // per hit
		
		if (sentenceByteLen > Short.MAX_VALUE) {
			throw new IllegalArgumentException("Serialized sentence is too long (" + sentenceByteLen + " > max " + Short.MAX_VALUE + ")");
		}
		
		byte[] bytes = new byte[sentenceByteLen];

		// write sentence byte len
		ShortBuffer sentenceByteLenShortBuffer = ByteBuffer.wrap(bytes, 0, 2).order(ByteOrder.BIG_ENDIAN).asShortBuffer();
		sentenceByteLenShortBuffer.put((short)sentenceByteLen);

		// write sentence hit
		if (sentenceHit.start() < Short.MIN_VALUE) {
			throw new IllegalArgumentException("Hit start cannot be less than " + Short.MIN_VALUE + "; specified " + sentenceHit.start());
		}
		if (sentenceHit.end() > Short.MAX_VALUE) {
			throw new IllegalArgumentException("Hit end cannot be more than " + Short.MAX_VALUE + "; specified " + sentenceHit.end());
		}
		ShortBuffer sentenceHitShortBuffer = ByteBuffer.wrap(bytes, 2, 4).order(ByteOrder.BIG_ENDIAN).asShortBuffer();
		sentenceHitShortBuffer.put((short)sentenceHit.start());
		sentenceHitShortBuffer.put((short)sentenceHit.end());
		
		// write hits count
		bytes[6] = (byte)hitsCount;
		
		// write hits list
		ShortBuffer hitsShortBuffer = ByteBuffer.wrap(bytes, 7, hitsCount*4).order(ByteOrder.BIG_ENDIAN).asShortBuffer();
		for (int i=0; i<hitsCount; i++) {
			Hit hit = hitsList.get(i);
			if (hit.start() < Short.MIN_VALUE) {
				throw new IllegalArgumentException("Hit start cannot be less than " + Short.MIN_VALUE + "; specified " + hit.start());
			}
			if (hit.end() > Short.MAX_VALUE) {
				throw new IllegalArgumentException("Hit end cannot be more than " + Short.MAX_VALUE + "; specified " + hit.end());
			}
			hitsShortBuffer.put((short)hit.start());
			hitsShortBuffer.put((short)hit.end());
		}

		// output all bytes
		_baos.write(bytes, 0, bytes.length);
		
		_isAtLeastOneAdded = true;
		_lastSentenceEnd = sentenceHit.end();
	}
	
	public byte[] getData() {
		if (_isAtLeastOneAdded == false) {
			throw new IllegalStateException("At least one hit must be added before converting to bytes");
		}
		return _baos.toByteArray();
	}

}
