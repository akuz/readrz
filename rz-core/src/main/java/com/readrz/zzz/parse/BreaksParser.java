package com.readrz.zzz.parse;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.akuz.core.Pair;

import com.readrz.zzz.FieldName;
import com.readrz.zzz.Location;
import com.readrz.zzz.data.Post;
import com.readrz.zzz.parse.matches.AnyMatch;
import com.readrz.zzz.parse.matches.BreakMatch;

public final class BreaksParser {
	
	private final static Pattern _patternUppercaseLetter = Pattern.compile("[A-Z]");
	private final static Pattern _patternTripleSpace = Pattern.compile("\\s{3,}");
	private final static Pattern _patternEndSymbol = Pattern.compile("[.?!]\\s");
	
	public BreaksParser() {
	}
	
	public void addBreakMatches(Post post, List<Pair<AnyMatch, Location>> sortedMatches) {
		
		int originalSize = sortedMatches.size();
		
		for (int i=1; i<originalSize; i++) {
			
			Pair<AnyMatch, Location> prevPair = sortedMatches.get(i-1);
			Location prevLocation = prevPair.v2();
			
			Pair<AnyMatch, Location> currPair = sortedMatches.get(i);
			Location currLocation = currPair.v2();
			
			// check if not in the same field
			if (prevLocation.getFieldName() != currLocation.getFieldName()) {
				
				Location breakLocation = new Location(prevLocation.getFieldName(), prevLocation.getEnd(), prevLocation.getEnd());
				BreakMatch breakMatch = new BreakMatch(breakLocation, "", "");

				sortedMatches.add(new Pair<AnyMatch, Location>(breakMatch, breakLocation));
				continue;
			}
			
			// don't apply these rules for titles
			if (prevLocation.getFieldName() != FieldName.TITLE) {

				// check if prev match ended with sentence break symbol (such as Yahoo! or U.S.)
				String lastPrevChar = post.getExtract(prevLocation.getFieldName(), prevLocation.getEnd()-1, prevLocation.getEnd());
				if (_patternEndSymbol.matcher(lastPrevChar).matches()) {
				
					String firstCurrChar = post.getExtract(currLocation.getFieldName(), currLocation.getStart(), currLocation.getStart()+1);
	
					// check if next match starts with an uppercase letter
					if (_patternUppercaseLetter.matcher(firstCurrChar).matches()) {
						
						Location breakLocation = new Location(prevLocation.getFieldName(), prevLocation.getEnd(), prevLocation.getEnd());
						BreakMatch breakMatch = new BreakMatch(breakLocation, "", "");
	
						sortedMatches.add(new Pair<AnyMatch, Location>(breakMatch, breakLocation));
						continue;
					}
				}
			
				// extract text between matches
				String textBetween = post.getExtract(prevLocation.getFieldName(), prevLocation.getEnd(), currLocation.getStart());
				Matcher m;
				
				// check for double new line
				m = _patternTripleSpace.matcher(textBetween);
				if (m.find()) {
					
					int start = prevLocation.getEnd() + m.start();
					int end = prevLocation.getEnd() + m.end();
					
					Location breakLocation = new Location(prevLocation.getFieldName(), start, end);
					BreakMatch breakMatch = new BreakMatch(breakLocation, "", m.group());
	
					sortedMatches.add(new Pair<AnyMatch, Location>(breakMatch, breakLocation));
					continue;
				}
				
				// check for sentence break symbol
				m = _patternEndSymbol.matcher(textBetween);
				if (m.find()) {
					
					int start = prevLocation.getEnd() + m.start();
					int end = prevLocation.getEnd() + m.end();
					
					Location breakLocation = new Location(prevLocation.getFieldName(), start, end);
					BreakMatch breakMatch = new BreakMatch(breakLocation, m.group(), m.group());
	
					sortedMatches.add(new Pair<AnyMatch, Location>(breakMatch, breakLocation));
					continue;
				}
			}
		}
	}

}
