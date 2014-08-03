package com.readrz.zzz.lang.corpus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.akuz.core.Hit;

import com.readrz.lang.corpus.Corpus;
import com.readrz.lang.corpus.CorpusDoc;
import com.readrz.lang.corpus.CorpusPlace;
import com.readrz.lang.corpus.CorpusSentence;
import com.readrz.zzz.FieldName;
import com.readrz.zzz.Location;
import com.readrz.zzz.ParsedPost;
import com.readrz.zzz.data.Post;
import com.readrz.zzz.parse.PostsParserListener;
import com.readrz.zzz.parse.matches.AnyMatch;
import com.readrz.zzz.parse.matches.BreakMatch;
import com.readrz.zzz.parse.matches.WordMatch;

public final class PostsCorpus extends Corpus<Integer> implements PostsParserListener {

	@Override
	public void onPostParsed(Date currDate, ParsedPost parsedPost) {

		Post post = parsedPost.getPost();
		
		CorpusDoc doc = new CorpusDoc();
		
		List<AnyMatch> allMatches = parsedPost.getAllMatches();
		
		int nextSentenceStart = 0;
		
		List<CorpusPlace> places = new ArrayList<>();
		for (int i=0; i<allMatches.size(); i++) {
			AnyMatch match = allMatches.get(i);
			
			if (match instanceof WordMatch) {
				
				WordMatch wordMatch = (WordMatch)match;
				
				String stem = wordMatch.getStem();
				
				Location loc = wordMatch.getLocation();
				
				String word;
				if (loc.getFieldName() == FieldName.TITLE) {
					word = post.getTitle().substring(loc.getStart(), loc.getEnd());
				} else if (loc.getFieldName() == FieldName.TEXT) {
					word = post.getText().substring(loc.getStart(), loc.getEnd());
				} else {
					throw new IllegalArgumentException("Unknown text location field: " + loc.getFieldName());
				}
				
				Integer stemIndex = getStemsIndex().ensure(stem);
				Integer wordIndex = getWordsIndex().ensure(word);
				
				CorpusPlace place = new CorpusPlace(stemIndex, wordIndex, null);
				places.add(place);
			}
			
			if (match instanceof BreakMatch) {
				
				if (places.size() > 0) {
					Hit sentenceHit = new Hit(nextSentenceStart, match.getLocation().getEnd());
					CorpusSentence sentence = new CorpusSentence(sentenceHit);
					for (int p=0; p<places.size(); p++) {
						sentence.addPlace(places.get(p));
					}
					places.clear();
					doc.addSentence(sentence);
				}
				
				nextSentenceStart = match.getLocation().getEnd();
			}
		}
		
		if (places.size() > 0) {
			Hit sentenceHit = new Hit(nextSentenceStart, parsedPost.getPost().getText().length());
			CorpusSentence sentence = new CorpusSentence(sentenceHit);
			for (int p=0; p<places.size(); p++) {
				sentence.addPlace(places.get(p));
			}
			places.clear();
			doc.addSentence(sentence);
		}
	}
	
}
