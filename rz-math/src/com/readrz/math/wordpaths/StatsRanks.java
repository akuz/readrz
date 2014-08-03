package com.readrz.math.wordpaths;

import java.util.Date;

import com.readrz.data.SnapPhrase;
import com.readrz.lang.Ranking;

public final class StatsRanks {

	public static final double calcPhraseRank(SnapPhrase phrase, Date minDateInc, Date maxDateExc) {

		// calc sentence rank
		double sentenceRank = Ranking.calcSentenceLocationRank(phrase.getSentenceHit());
		
		// calc recency rank
		double recencyRank = Ranking.calcDateRecencyRank(phrase.getSnap().getSrcDate(), minDateInc, maxDateExc);
		
		// calc overall rank
		return sentenceRank * recencyRank;
	}
	
}
