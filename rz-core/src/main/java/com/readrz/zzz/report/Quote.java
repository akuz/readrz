package com.readrz.zzz.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.akuz.core.StringUtils;
import me.akuz.nlp.porter.PorterWordsSentiment;

import com.readrz.zzz.FieldName;
import com.readrz.zzz.Phrase;
import com.readrz.zzz.PhrasePlace;
import com.readrz.zzz.parse.matches.AnyMatch;
import com.readrz.zzz.paths.PathQuote;
import com.readrz.zzz.paths.PathStats;

public final class Quote implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// keeping member names short 
	// for small serialized size:
	private List<Word> t; // title
	private List<Word> s; // source
	private List<Word> q; // quote
	private List<Word> b; // best words
	
	/**
	 * Needed for deserialization.
	 */
	public Quote() {
	}
	
	public Quote(PathQuote pathQuote, PathStats pathStats, PorterWordsSentiment wordsSenSet) {
		
		Phrase phrase = pathQuote.getPhrase();
		
		if (phrase.getFieldName() != FieldName.TITLE) {
			q = phrasePlacesToWords(phrase.getPlaces(), pathStats, wordsSenSet);
		} else {
			q = null;
		}
		
		t = titleMatchesToWords(phrase.getTitleMatches(), pathStats, wordsSenSet);
		
		s = new ArrayList<Word>();
		s.add(new Word("Source " + phrase.getSrcId()));
		
		b = phrasePlacesToBestWords(phrase.getPlaces(), pathStats, wordsSenSet);
	}
	
	public List<Word> getQuoteWords() {
		return q;
	}
	
	public List<Word> getTitleWords() {
		return t;
	}
	
	public List<Word> getSrcWords() {
		return s;
	}
	
	public List<Word> getBestWords() {
		return b;
	}

	private final static List<Word> phrasePlacesToBestWords(
			List<PhrasePlace> places, 
			PathStats pathStats, 
			PorterWordsSentiment wordsSenSet) {
		
		String mainStem = pathStats.getStemsSortedByExpectedWeight().get(0).v1();
		Map<String, Double> stemsExpectedWeights = pathStats.getStemsExpectedWeights();
		
		List<Word> words = new ArrayList<Word>();

		for (int i=0; i<places.size(); i++) {
			
			PhrasePlace place = places.get(i);
			AnyMatch match = place.getMatch();
			
			// get word stem
			String stem = match.getStem();
			Double expectedWeight = stemsExpectedWeights.get(stem);
			if (expectedWeight != null && expectedWeight >= 0.5) {
				
				// create word
				String text = match.getText();
				Word word = new Word(text);
				words.add(word);
	
				// set sentiment
				String lowercaseWord = StringUtils.toLowercaseFirstLetter(text);
				Integer sentiment = wordsSenSet.getByWord(lowercaseWord);
				if (sentiment != null) {
					if (sentiment > 1) {
						word.setSentiment(Sentiment.POS);
					} else if (sentiment < -1) {
						word.setSentiment(Sentiment.NEG);
					}
				}

				// set highlight
				if (mainStem.equals(stem)) {
					word.setHighlight(Highlight.ON);
				}
			}
		}
		
		return words;
	}

	private final static List<Word> phrasePlacesToWords(
			List<PhrasePlace> places, 
			PathStats pathStats, 
			PorterWordsSentiment wordsSenSet) {
		
		String mainStem = pathStats.getStemsSortedByExpectedWeight().get(0).v1();
		Map<String, Double> stemsExpectedWeights = pathStats.getStemsExpectedWeights();
		
		List<Word> words = new ArrayList<Word>();

		for (int i=0; i<places.size(); i++) {
			
			PhrasePlace place = places.get(i);
			AnyMatch match = place.getMatch();
			
			// create word
			String text = match.getText();
			Word word = new Word(text);
			words.add(word);

			// set sentiment
			String lowercaseWord = StringUtils.toLowercaseFirstLetter(text);
			Integer sentiment = wordsSenSet.getByWord(lowercaseWord);
			if (sentiment != null) {
				if (sentiment > 1) {
					word.setSentiment(Sentiment.POS);
				} else if (sentiment < -1) {
					word.setSentiment(Sentiment.NEG);
				}
			}

			// get word stem
			String stem = match.getStem();

			// set highlight
			if (mainStem.equals(stem)) {
				word.setHighlight(Highlight.ON);
			}

			// set emphasis
			Double expectedWeight = stemsExpectedWeights.get(stem);
			if (expectedWeight != null && expectedWeight >= 0.5) {
				word.setEmphasis(Emphasis.ON);
			}
		}
		
		return words;
	}
	
	private final static List<Word> titleMatchesToWords(
			List<AnyMatch> matches, 
			PathStats pathStats, 
			PorterWordsSentiment wordsSenSet) {

		String mainStem = pathStats.getStemsSortedByExpectedWeight().get(0).v1();
		Map<String, Double> stemsExpectedWeights = pathStats.getStemsExpectedWeights();
		
		List<Word> words = new ArrayList<Word>();

		int lastIndex = matches.size()-1;
		for (int i=0; i<matches.size(); i++) {
			
			AnyMatch match = matches.get(i);
			String text = match.getText();

			if (i == 0) {
				
				// capitalize first word
				text = StringUtils.toUppercaseFirstLetter(text);
				
			} else if (i == lastIndex) {
				
				// don't add last break match
				if (match.isBreakMatch()) {
					break;
				}
			}

			// create word
			Word word = new Word(text);
			words.add(word);
			
			// set sentiment
			String lowercaseWord = StringUtils.toLowercaseFirstLetter(text);
			Integer sentiment = wordsSenSet.getByWord(lowercaseWord);
			if (sentiment != null) {
				if (sentiment > 1) {
					word.setSentiment(Sentiment.POS);
				} else if (sentiment < -1) {
					word.setSentiment(Sentiment.NEG);
				}
			}
			
			// get word stem
			String stem = match.getStem();

			// set highlight
			if (mainStem.equals(stem)) {
				word.setHighlight(Highlight.ON);
			}
			
			// set emphasis
			Double expectedWeight = stemsExpectedWeights.get(stem);
			if (expectedWeight != null && expectedWeight >= 0.5) {
				word.setEmphasis(Emphasis.ON);
			}
		}
		
		return words;
	}
}
