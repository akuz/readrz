package com.readrz.zzz.parse;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.akuz.core.Index;
import me.akuz.core.Pair;
import me.akuz.nlp.porter.PorterStemmerOrig;
import me.akuz.nlp.porter.PorterStemmerOrigUtils;

import com.readrz.zzz.FieldName;
import com.readrz.zzz.Location;
import com.readrz.zzz.parse.matches.AnyMatch;
import com.readrz.zzz.parse.matches.UnrecMatch;
import com.readrz.zzz.parse.matches.WordKind;
import com.readrz.zzz.parse.matches.WordMatch;

public final class WordsParserOld {
	
	private final Index<String> _stemsIndex;
	private final Index<String> _wordsIndex;
	
	// reused parts of regular expressions
	private static final String _blockOpen  = "[-+=({\\[\"'‘’“”]";
	private static final String _blockClose = "[-+=)}\\]\"'‘’“”.!?,:;sld]";
	private static final String _abbrChar = "[-a-zA-Z0-9]";
	private static final String _letter = "[a-zA-Z]";
	
	// used to match next no-space block as word candidate
	private final static Pattern _patternWordCandidate = Pattern.compile("\\S{1,}");
	
	// used to check if block is a simple word
	private final static Pattern _patternIsWordSimple = Pattern.compile(
			"^" + 
			"(" + _blockOpen +  "*)" + 
			"(" + _letter + "{2,})" + 
			"(" + _blockClose +  "*)" +
			"$");
	
	// used to check if block is a composite word
	private final static Pattern _patternIsWordComposite = Pattern.compile(
			"^" + 
			"(" + _blockOpen +  "*)" + 
			"(" + _letter + "{1,})" + 
			"-" + 
			"(" + _letter + "{2,})" + 
			"(" + _blockClose +  "*)" +
			"$");
	
	// used to check if block is an letter/number abbreviation
	private final static Pattern _patternIsAbbrev = Pattern.compile(
			"^" + 
			"(" + _blockOpen +  "*)" + 
			"((" + _abbrChar + "+\\.)+(" + _abbrChar + ")\\.?)" + 
			"(" + _blockClose +  "*)" +
			"$");
	
	// used to check if block contains a letter
	private final static Pattern _patternHasLetter = Pattern.compile(_letter);

	// private fields
	private final PorterStemmerOrig _porterStemmer;
	private final Set<String> _stopStemSet;
	
	public WordsParserOld(Set<String> stopStemSet, Index<String> stemsIndex, Index<String> wordsIndex) {
		_porterStemmer = new PorterStemmerOrig();
		_stopStemSet = stopStemSet;
		_stemsIndex = stemsIndex;
		_wordsIndex = wordsIndex;
	}

	public void addWordMatches(
			FieldName fieldName, 
			String str, 
			List<WordMatch> wordMatchList, 
			List<Pair<AnyMatch, Location>> allMatchesSorted,
			int allMatchesOverlapCheckSize) {
		
		// find words
		Matcher blockMatcher = _patternWordCandidate.matcher(str);
		while (blockMatcher.find()) {
			
			// word match to add
			AnyMatch matchToAdd = null;
			
			// get the block to analyze
			String block = blockMatcher.group();
			Matcher matcher;

			// check if simple word
			matcher = _patternIsWordSimple.matcher(block);
			if (matcher.find()) {
				
				final int GROUP = 2;
				
				String word = _wordsIndex.ensureGetCachedValue(matcher.group(GROUP));
				String stem = _stemsIndex.ensureGetCachedValue(PorterStemmerOrigUtils.stem(_porterStemmer, word));
				
				Location location = new Location(fieldName, 
						blockMatcher.start() + matcher.start(GROUP), 
						blockMatcher.start() + matcher.end(GROUP));
				
				boolean isStopWord = _stopStemSet != null && _stopStemSet.contains(stem);
				matchToAdd = new WordMatch(WordKind.SimpleWord, location, stem, word, isStopWord);
				
			} else {

				// check if composite word
				matcher = _patternIsWordComposite.matcher(block);
				if (matcher.find()) {
					
					final int GROUP1 = 2;
					final int GROUP2 = 3;
					
					String word1 = matcher.group(GROUP1);
					String stem1 = PorterStemmerOrigUtils.stem(_porterStemmer, word1);
					
					String word2 = matcher.group(GROUP2);
					String stem2 = PorterStemmerOrigUtils.stem(_porterStemmer, word2);
					
					String word = _wordsIndex.ensureGetCachedValue(word1 + "-" + word2);
					String stem = _stemsIndex.ensureGetCachedValue(stem1 + "-" + stem2);
					
					Location location = new Location(fieldName, 
							blockMatcher.start() + matcher.start(GROUP1), 
							blockMatcher.start() + matcher.end(GROUP2));

					boolean isStopWord = _stopStemSet != null && _stopStemSet.contains(stem);
					matchToAdd = new WordMatch(WordKind.CompositeWord, location, stem, word, isStopWord);
									
				} else {
					
					matcher = _patternIsAbbrev.matcher(block);
					if (matcher.find()) {
						
						final int GROUP = 2;
						
						String word = matcher.group(GROUP);
						if (_patternHasLetter.matcher(word).find()) {
							
							word = _wordsIndex.ensureGetCachedValue(word);
							String stem = _stemsIndex.ensureGetCachedValue(word.toLowerCase());
							
							Location location = new Location(fieldName, 
									blockMatcher.start() + matcher.start(GROUP), 
									blockMatcher.start() + matcher.end(GROUP));
							
							boolean isStopWord = _stopStemSet != null && _stopStemSet.contains(stem);
							matchToAdd = new WordMatch(WordKind.Abbreviation, location, stem, word, isStopWord);
						}
						
					} else {
						
						String word = _wordsIndex.ensureGetCachedValue(block);
						String stem = _stemsIndex.ensureGetCachedValue(word.toLowerCase());
						
						Location location = new Location(fieldName, 
								blockMatcher.start(), 
								blockMatcher.end());
						
						matchToAdd = new UnrecMatch(location, stem, word);
					}
				}
			}
			
			if (matchToAdd != null) {
				
				boolean overlapsWithBlockedLocation = false;
				if (allMatchesSorted != null) {
					for (int i=0; i<allMatchesOverlapCheckSize; i++) {
						if (allMatchesSorted.get(i).v2().overlaps(matchToAdd.getLocation())) {
							overlapsWithBlockedLocation = true;
							break;
						}
					}
				}
				
				if (overlapsWithBlockedLocation == false) {
					
					// add to all matches
					allMatchesSorted.add(new Pair<AnyMatch, Location>(matchToAdd, matchToAdd.getLocation()));
					
					// add to word matches
					if (matchToAdd instanceof WordMatch) {
						wordMatchList.add((WordMatch)matchToAdd);
					}
				}
				
			} else {
				//System.out.println("Skipping at " + blockMatcher.start() +": " + block);
			}
		}
	}
	
}
