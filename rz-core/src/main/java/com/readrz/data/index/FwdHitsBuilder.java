package com.readrz.data.index;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.List;
import java.util.Map.Entry;

import me.akuz.core.Hit;

/**
 * Builds binary forward index hits data, describing occurrences 
 * of all detected entities in sentences of a specific a document. 
 * Max fwd hits count of a particular kind in one sentence is 255
 * (the rest will be ignored and will not be serialized).
 *
 */
public final class FwdHitsBuilder {
	
	private int _sentenceCount;
	private int _lastSentenceEnd;
	private final ByteArrayOutputStream _baos;
	
	public FwdHitsBuilder() {
		_baos = new ByteArrayOutputStream();
	}
	
	public byte[] getData() {
		return _sentenceCount > 0 ? _baos.toByteArray() : new byte[0];
	}
	
	/**
	 * Add forward hits for one sentence.
	 * @param sentenceHit - sentence start position (-1 means title)
	 * @param sentenceFwdHitsMap - a map of sentence forward hits by kind
	 */
	public void addSentenceHits(Hit sentenceHit, FwdHitsMap sentenceFwdHitsMap) {
		if (_sentenceCount > 0 && (_lastSentenceEnd > sentenceHit.start())) {
			throw new IllegalStateException(
					"Sentence hits must be increasing as you add them (last end: " 
					+ _lastSentenceEnd + ", added start: " + sentenceHit.start() + ")");
		}
		int fwdHitBytesLen = 0;
		for (Entry<FwdHitKind, List<FwdHit>> entry : sentenceFwdHitsMap.entrySet()) {
			fwdHitBytesLen += 2; // 1 byte for fwd hit kind, 1 byte for fwd hit count
			fwdHitBytesLen += Math.min(entry.getValue().size(), 255) * 8; // 8 bytes per fwd hit
		}
		if (fwdHitBytesLen == 0) {
			throw new IllegalArgumentException("There must be some fwd hits in sentence (otherwise don't add it)");
		}
		final int sentenceByteLen 
			= 2 // for sentence byte len
			+ 4 // for sentence hit
			+ fwdHitBytesLen; 

		if (sentenceHit.start() < Short.MIN_VALUE) {
			throw new IllegalArgumentException("Hit start cannot be less than " + Short.MIN_VALUE + "; specified " + sentenceHit.start());
		}
		if (sentenceHit.end() > Short.MAX_VALUE) {
			throw new IllegalArgumentException("Hit end cannot be more than " + Short.MAX_VALUE + "; specified " + sentenceHit.end());
		}
		
		// write sentence bytes len
		// and sentence start position
		byte[] startBytes = new byte[6];
		ShortBuffer startShortBuffer = ByteBuffer.wrap(startBytes).order(ByteOrder.BIG_ENDIAN).asShortBuffer();
		startShortBuffer.put((short)sentenceByteLen);
		startShortBuffer.put((short)sentenceHit.start());
		startShortBuffer.put((short)sentenceHit.end());
		_baos.write(startBytes, 0, startBytes.length);

		// write forward hits by kind
		for (Entry<FwdHitKind, List<FwdHit>> entry : sentenceFwdHitsMap.entrySet()) {

			FwdHitKind fwdHitKind = entry.getKey();
			List<FwdHit> fwdHitList = entry.getValue();

			// write fwd hit kind and hits count
			_baos.write((byte)fwdHitKind.getId());
			_baos.write((byte)Math.min(fwdHitList.size(), 255));
			
			// write all forward hits
			for (int i=0; i<fwdHitList.size() && i<255; i++) {
				
				FwdHit fwdHit = fwdHitList.get(i);
				int keyId = fwdHit.getKeyId();
				Hit hit = fwdHit.getHit();
				
				byte[] keyIdBytes = new byte[4];
				IntBuffer keyIdIntBuffer = ByteBuffer.wrap(keyIdBytes).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
				keyIdIntBuffer.put(keyId);
				_baos.write(keyIdBytes, 0, keyIdBytes.length);

				if (hit.start() < Short.MIN_VALUE) {
					throw new IllegalArgumentException("Hit start cannot be less than " + Short.MIN_VALUE + "; specified " + hit.start());
				}
				if (hit.end() > Short.MAX_VALUE) {
					throw new IllegalArgumentException("Hit end cannot be more than " + Short.MAX_VALUE + "; specified " + hit.end());
				}
				
				byte[] hitBytes = new byte[4];
				ShortBuffer hitShortBuffer = ByteBuffer.wrap(hitBytes).order(ByteOrder.BIG_ENDIAN).asShortBuffer();
				hitShortBuffer.put((short)hit.start());
				hitShortBuffer.put((short)hit.end());
				_baos.write(hitBytes, 0, hitBytes.length);
			}
		}
		
		_sentenceCount += 1;
		_lastSentenceEnd = sentenceHit.end();
	}
	
	public int getSentenceCount() {
		return _sentenceCount;
	}

}
