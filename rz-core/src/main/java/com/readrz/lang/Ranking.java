package com.readrz.lang;

import java.util.Date;
import java.util.List;
import java.util.Map;

import me.akuz.core.Hit;
import me.akuz.core.math.StatsUtils;

import com.readrz.data.index.FwdHit;

public final class Ranking {

	/**
	 * When ranking recency of a document, in relation to some period,
	 * paths algorithm will use exponential decay, with half life measured
	 * as the fraction of period expressed by this setting.
	 * 
	 */
	public static final double DEFAULT_RECENCY_PERIOD_FRACTION_HALF_LIFE = 0.5;

	/**
	 * When ranking a hit, the algorithm uses this half life 
	 * (in chars) from other key hits (or sentence start), 
	 * for exponential decay weight.
	 * 
	 */
	private final static double DISTANCE_WEIGHT_DECAY_CHAR_HALF_LIFE = 50.0;

	/**
	 * When ranking a sentence, the algorithm uses this half life 
	 * (in chars) from article start, for exponential decay weight.
	 * 
	 */
	public static final double DEFAULT_SENTENCE_START_CHAR_HALF_LIFE = 5000.0;

	/**
	 * Calculates hit rank based on location relative to other hits (or sentence start).
	 * 
	 */
	public final static double calcDistanceRank(Map<Integer, List<FwdHit>> foundFwdHits, FwdHit fwdHit, Hit sentenceBounds) {

		Hit hit = fwdHit.getHit();
		int minDistanceChars = Integer.MAX_VALUE;
		if (foundFwdHits.size() > 0) {
			for (List<FwdHit> foundFwdHitsList : foundFwdHits.values()) {
				for (int i=0; i<foundFwdHitsList.size(); i++) {
					Hit foundHit = foundFwdHitsList.get(i).getHit();
					int dist = hit.distanceTo(foundHit);
					if (minDistanceChars > dist) {
						minDistanceChars = dist;
					}
					if (minDistanceChars < 0) {
						throw new InternalError("This hit should not be added because it overlaps with a found hit");
					}
					if (minDistanceChars <= 1) {
						break; // already closest
					}
				}
				if (minDistanceChars <= 1) {
					break; // already closest
				}
			}
			if (minDistanceChars == Integer.MAX_VALUE) {
				throw new InternalError("Found fwd hits must not contain empty lists");
			}
		} else {
			// measure from sentence start
			minDistanceChars = Math.abs(hit.start() - sentenceBounds.start());
		}
		
		// calculate weight for the location of extra hit
		double distanceRank = StatsUtils.calcDistanceWeightExponential(minDistanceChars, DISTANCE_WEIGHT_DECAY_CHAR_HALF_LIFE);
		
		return distanceRank;
	}

	/**
	 * Calculates sentence rank based on location in the document.
	 * 
	 */
	public static final double calcSentenceLocationRank(Hit sentenceHit) {
		return calcSentenceLocationRank(sentenceHit, DEFAULT_SENTENCE_START_CHAR_HALF_LIFE);
	}

	/**
	 * Calculates sentence rank based on location in the document.
	 * 
	 */
	public static final double calcSentenceLocationRank(Hit sentenceHit, double sentenceStartCharHalfLife) {
		if (sentenceHit.start() < 0) {
			return 1.0;
		} else {
			return StatsUtils.calcDistanceWeightExponential(sentenceHit.start(), sentenceStartCharHalfLife);
		}
	}

	/**
	 * Calculates date recency rank for a date within a period.
	 * 
	 */
	public static final double calcDateRecencyRank(Date date, Date minDateInc, Date maxDateExc) {
		
		return calcDateRecencyRank(date, minDateInc, maxDateExc, DEFAULT_RECENCY_PERIOD_FRACTION_HALF_LIFE);
	}

	/**
	 * Calculates date recency rank for a date within a period.
	 * 
	 */
	public static final double calcDateRecencyRank(Date date, Date minDateInc, Date maxDateExc, double periodFractionHalfLife) {
	
		double halfLife = (maxDateExc.getTime() - minDateInc.getTime()) * periodFractionHalfLife;
		double distance = maxDateExc.getTime() - date.getTime();
		double recencyRank = StatsUtils.calcDistanceWeightExponential(distance, halfLife);
		return recencyRank;
	}

}
