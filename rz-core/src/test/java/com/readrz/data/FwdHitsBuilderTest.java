package com.readrz.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.akuz.core.Hit;

import org.junit.Test;

import com.readrz.data.index.FwdHit;
import com.readrz.data.index.FwdHitKind;
import com.readrz.data.index.FwdHits;
import com.readrz.data.index.FwdHitsBuilder;
import com.readrz.data.index.FwdHitsMap;

public final class FwdHitsBuilderTest {

	@Test
	public void testFwdHitsSerialization() {
		
		System.out.println("Building fwd hits...");
		FwdHitsBuilder hb = new FwdHitsBuilder();
		
		FwdHitsMap hitsM20 = new FwdHitsMap();
		{
			List<FwdHit> patternHits = new ArrayList<FwdHit>();
			patternHits.add(new FwdHit(0, new Hit(0, 8)));
			patternHits.add(new FwdHit(23, new Hit(12, 18)));
			hitsM20.put(FwdHitKind.PATTERN, patternHits);
		}
		{
			List<FwdHit> wordHits = new ArrayList<FwdHit>();
			wordHits.add(new FwdHit(123, new Hit(12, 22)));
			wordHits.add(new FwdHit(456, new Hit(128, 150)));
			wordHits.add(new FwdHit(289282, new Hit(21, 34)));
			hitsM20.put(FwdHitKind.WORD, wordHits);
		}
		hb.addSentenceHits(new Hit(-20, 18), hitsM20);
		
		FwdHitsMap hits20 = new FwdHitsMap();
		{
			List<FwdHit> patternHits = new ArrayList<FwdHit>();
			patternHits.add(new FwdHit(44, new Hit(20, 25)));
			patternHits.add(new FwdHit(-333, new Hit(61, 66)));
			hits20.put(FwdHitKind.PATTERN, patternHits);
		}
		{
			List<FwdHit> wordHits = new ArrayList<FwdHit>();
			wordHits.add(new FwdHit(-1223, new Hit(22, 33)));
			wordHits.add(new FwdHit(-222222, new Hit(888, 889)));
			wordHits.add(new FwdHit(1187187, new Hit(123, 456)));
			hits20.put(FwdHitKind.WORD, wordHits);
		}
		hb.addSentenceHits(new Hit(20, 50), hits20); 
		
		FwdHitsMap hits80 = new FwdHitsMap();
		{
			List<FwdHit> patternHits = new ArrayList<FwdHit>();
			patternHits.add(new FwdHit(123, new Hit(80, 85)));
			hits80.put(FwdHitKind.PATTERN, patternHits);
		}
		{
			List<FwdHit> wordHits = new ArrayList<FwdHit>();
			wordHits.add(new FwdHit(22222, new Hit(12, 14)));
			wordHits.add(new FwdHit(-287287, new Hit(222, 444)));
			wordHits.add(new FwdHit(2552222, new Hit(321, 400)));
			hits80.put(FwdHitKind.WORD, wordHits);
		}
		hb.addSentenceHits(new Hit(80, 100), hits80);
		
		System.out.println("Serializing hits...");
		byte[] hitsData = hb.getData();
		System.out.println("Bytes: " + hitsData.length);
		
		System.out.println("Deserializing hits...");
		FwdHits fwdHits = new FwdHits(hitsData);
		
		{
			if (fwdHits.nextSentence() == false) {
				throw new IllegalStateException("Should have had more sentences");
			}
			Hit sentenceHit = fwdHits.getSentenceHit();
			if (sentenceHit.start() != -20 || sentenceHit.end() != 18) {
				throw new IllegalStateException("Invalid sentence hit: " + sentenceHit);
			}
			checkSameHits(fwdHits.getSentenceHits(FwdHitKind.ALL), hitsM20);
		}
		{
			if (fwdHits.nextSentence() == false) {
				throw new IllegalStateException("Should have had more sentences");
			}
			Hit sentenceHit = fwdHits.getSentenceHit();
			if (sentenceHit.start() != 20 || sentenceHit.end() != 50) {
				throw new IllegalStateException("Invalid sentence hit: " + sentenceHit);
			}
			checkSameHits(fwdHits.getSentenceHits(FwdHitKind.ALL), hits20);
		}
		{
			if (fwdHits.nextSentence() == false) {
				throw new IllegalStateException("Should have had more sentences");
			}
			Hit sentenceHit = fwdHits.getSentenceHit();
			if (sentenceHit.start() != 80 || sentenceHit.end() != 100) {
				throw new IllegalStateException("Invalid sentence hit: " + sentenceHit);
			}
			checkSameHits(fwdHits.getSentenceHits(FwdHitKind.ALL), hits80);
		}
		
		if (fwdHits.nextSentence()) {
			throw new IllegalStateException("Should have had fewer sentences");
		}
		
		System.out.println("Success.");
	}

	private void checkSameHits(FwdHitsMap map1, FwdHitsMap map2) {

		if (map1 == map2) {
			throw new IllegalStateException("Same objects");
		}
		if (map1.size() != map2.size()) {
			throw new IllegalStateException("Hits are different");
		}
		Set<FwdHitKind> allKinds = new HashSet<>();
		allKinds.addAll(map1.keySet());
		allKinds.addAll(map2.keySet());
		
		if (!map1.keySet().containsAll(allKinds)) {
			throw new IllegalStateException("Hits are different");
		}
		if (!map2.keySet().containsAll(allKinds)) {
			throw new IllegalStateException("Hits are different");
		}
		
		for (FwdHitKind kind : allKinds) {
			checkSameHits(map1.get(kind), map2.get(kind));
		}
	}

	private void checkSameHits(List<FwdHit> hits1, List<FwdHit> hits2) {
		
		if (hits1 == hits2) {
			throw new IllegalStateException("Same objects");
		}
		if (hits1.size() != hits2.size()) {
			throw new IllegalStateException("Hits are different");
		}
		for (int i=0; i<hits1.size(); i++) {
			if (hits1.get(i).equals(hits2.get(i)) == false) {
				throw new IllegalStateException("Hits are different");
			}
		}
	}
}
