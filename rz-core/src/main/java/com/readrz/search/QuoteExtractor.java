package com.readrz.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.akuz.core.Hit;
import me.akuz.core.Pair;

import com.readrz.data.Snap;
import com.readrz.data.index.FwdHit;
import com.readrz.data.index.FwdHitKind;
import com.readrz.data.index.FwdHits;
import com.readrz.data.index.FwdHitsMap;
import com.readrz.data.index.FwdHitsUtils;

public final class QuoteExtractor {

	private final List<Integer> _documentKeyIds;
	private final List<Integer> _senCheckKeyIds;
	private final List<Integer> _sentenceKeyIds;

	public QuoteExtractor(QueryKeyIds queryKeyIds) {
		
		_documentKeyIds = queryKeyIds.getDocumentKeyIds();
		_senCheckKeyIds = queryKeyIds.getSenCheckKeyIds();
		_sentenceKeyIds = queryKeyIds.getSentenceKeyIds();
	}

	/**
	 * Extracts a quote from the provided snap,
	 * returning a pair with a title quote (v1)
	 * and a text quote (v2).
	 * 
	 */
	public Pair<String, String> extractQuote(Snap snap) {
		
		// get title and text
		String title = snap.getTitle();
		String text = snap.getText();

		// init default quote
		String titleQuote = title;
		String textQuote = null;
		int textQuoteRank = -1;

		// analyze forward hits
		byte[] fwdHitsData = snap.getFwdHitsData();
		if (fwdHitsData != null && fwdHitsData.length > 0) {
			
			// start iterating forward hits
			FwdHits fwdHits = new FwdHits(fwdHitsData);
			while (fwdHits.nextSentence()) {

				// get sentence data
				Hit sentenceHit = fwdHits.getSentenceHit();
				FwdHitsMap fwdHitsMap = fwdHits.getSentenceHits(FwdHitKind.ALL);
				
				// prepare lists for found fwd hits
				Map<Integer, List<FwdHit>> foundDocumentFwdHitsMap = new HashMap<>();
				Map<Integer, List<FwdHit>> foundSenCheckFwdHitsMap = new HashMap<>();
				Map<Integer, List<FwdHit>> foundSentenceFwdHitsMap = new HashMap<>();
				
				// find matching fwd hits
				fwdHitsMap.matchesSearch(
						_documentKeyIds, 
						foundDocumentFwdHitsMap,
						_senCheckKeyIds, 
						foundSenCheckFwdHitsMap,
						_sentenceKeyIds, 
						foundSentenceFwdHitsMap);
				
				// select fwd hits to highlight
				List<FwdHit> highlightFwdHits 
					= FwdHitsUtils.getUniqueFwdHits(
						FwdHitsUtils.flattenFwdHitsMaps(
							foundSentenceFwdHitsMap));
				
				// update quote
				if (sentenceHit.start() < 0) {
					
					titleQuote = quoteFwdHits(title, sentenceHit.shift(title.length()), highlightFwdHits);
					
				} else {
					
					// calculate quote rank
					int sentenceQuoteRank 
						= foundSentenceFwdHitsMap.size() * 10
						+ foundSenCheckFwdHitsMap.size() 
						+ foundDocumentFwdHitsMap.size();
					
					if (textQuoteRank < sentenceQuoteRank) {
						textQuoteRank = sentenceQuoteRank;
						textQuote = quoteFwdHits(text, sentenceHit, highlightFwdHits);
					}
				}

			} // sentences
		} // fwd hits
		
		return new Pair<String, String>(titleQuote, textQuote);
	}
	
	public static final String quoteFwdHits(String str, Hit quoteHit, List<FwdHit> fwdHits) {
	
		Hit furtherHit = null;
		List<String> quoteBlocks = new ArrayList<>();
		for (int i=fwdHits.size()-1; i>=0; i--) {
			
			Hit hit = fwdHits.get(i).getHit();
			
			// add text after hit
			if (furtherHit != null) {
				String block = str.substring(hit.end(), furtherHit.start());
				quoteBlocks.add(block);
			} else {
				String block = str.substring(hit.end(), quoteHit.end());
				quoteBlocks.add(block);
			}
			quoteBlocks.add("</em>");
			String block = str.substring(hit.start(), hit.end());
			quoteBlocks.add(block);
			quoteBlocks.add("<em>");
			
			furtherHit = hit;
		}
		
		// add beginning of the string
		if (furtherHit != null) {
			String block = str.substring(quoteHit.start(), furtherHit.start());
			quoteBlocks.add(block);
		} else {
			String block = str.substring(quoteHit.start(), quoteHit.end());
			quoteBlocks.add(block);
		}
		
		StringBuilder sb = new StringBuilder();
		for (int i=quoteBlocks.size()-1; i>=0; i--) {
			sb.append(quoteBlocks.get(i));
		}
		String highlighted = sb.toString();
		return highlighted;
	}
	
}
