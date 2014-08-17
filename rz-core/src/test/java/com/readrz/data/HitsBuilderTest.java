package com.readrz.data;

import java.util.ArrayList;
import java.util.List;

import me.akuz.core.Hit;

import org.junit.Test;

import com.readrz.data.index.Hits;
import com.readrz.data.index.HitsBuilder;

public final class HitsBuilderTest {

	@Test
	public void testHitsSerialization() {
		
		System.out.println("Building hits...");
		HitsBuilder hb = new HitsBuilder();
		
		List<Hit> hitsM20 = new ArrayList<Hit>();
		hitsM20.add(new Hit(0, 8));
		hitsM20.add(new Hit(12, 18));
		hb.addSentenceHits(new Hit(-20, 18), hitsM20);
		
		List<Hit> hits20 = new ArrayList<Hit>();
		hits20.add(new Hit(20, 25));
		hits20.add(new Hit(45, 50));
		hits20.add(new Hit(61, 66));
		hb.addSentenceHits(new Hit(20, 66), hits20); 
		
		List<Hit> hits80 = new ArrayList<Hit>();
		hits80.add(new Hit(80, 85));
		hb.addSentenceHits(new Hit(80, 85), hits80);
		
		System.out.println("Serializing hits...");
		byte[] hitsData = hb.getData();
		System.out.println("Bytes: " + hitsData.length);
		
		System.out.println("Deserializing hits...");
		Hits hits = new Hits(hitsData);
		
		if (hits.nextSentence() == false) {
			throw new IllegalStateException("Should have had more sentences");
		}
		Hit sentenceHit = hits.getSentenceHit();
		if (sentenceHit.start() != -20 || sentenceHit.end() != 18) {
			throw new IllegalStateException("Invalid sentence hit: " + sentenceHit);
		}
		checkSameHits(hits.getSentenceHitsList(), hitsM20);
		
		if (hits.nextSentence() == false) {
			throw new IllegalStateException("Should have had more sentences");
		}
		sentenceHit = hits.getSentenceHit();
		if (sentenceHit.start() != 20 || sentenceHit.end() != 66) {
			throw new IllegalStateException("Invalid sentence hit: " + sentenceHit);
		}
		checkSameHits(hits.getSentenceHitsList(), hits20);
		
		if (hits.nextSentence() == false) {
			throw new IllegalStateException("Should have had more sentences");
		}
		sentenceHit = hits.getSentenceHit();
		if (sentenceHit.start() != 80 || sentenceHit.end() != 85) {
			throw new IllegalStateException("Invalid sentence hit: " + sentenceHit);
		}
		checkSameHits(hits.getSentenceHitsList(), hits80);
		
		if (hits.nextSentence()) {
			throw new IllegalStateException("Should have had fewer sentences");
		}
		
		System.out.println("Success.");
	}

	private void checkSameHits(List<Hit> hits1, List<Hit> hits2) {
		
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
