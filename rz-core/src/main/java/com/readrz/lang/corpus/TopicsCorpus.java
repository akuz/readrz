package com.readrz.lang.corpus;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import me.akuz.core.Hit;
import me.akuz.core.Index;

import org.bson.types.ObjectId;

import com.readrz.data.Snap;
import com.readrz.data.SnapsListener;
import com.readrz.data.index.FwdHit;
import com.readrz.data.index.FwdHitKind;
import com.readrz.data.index.FwdHits;
import com.readrz.data.index.FwdHitsMap;
import com.readrz.data.index.FwdHitsUtils;
import com.readrz.data.index.KeysIndex;

public final class TopicsCorpus extends Corpus<ObjectId> implements SnapsListener {

	private final KeysIndex _keysIndex;
	private final Set<Integer> _stopKeyIds;
	private final int _minDocLength;
	
	public TopicsCorpus(KeysIndex keysIndex, Set<Integer> stopKeyIds, int minDocLength) {
		_keysIndex = keysIndex;
		_stopKeyIds = stopKeyIds;
		_minDocLength = minDocLength;
	}
	
	@Override
	public void onSnap(Date currDate, Snap snap) {
		
		// get saved fwd hits
		FwdHits fwdHits = new FwdHits(snap.getFwdHitsData());
		
		// create corpus doc for topics
		CorpusDoc doc = createDocForTopics(snap, fwdHits, false, _keysIndex, _stopKeyIds, getStemsIndex(), getWordsIndex(), true);

		if (doc.getLength() >= _minDocLength) {
			addDoc(snap.getId(), doc);
		}
	}
	
	public static CorpusDoc createDocForTopics(
			Snap snap,
			FwdHits fwdHits,
			boolean populateListPatterns,
			KeysIndex keysIndex,
			Set<Integer> stopKeyIds,
			Index<String> stemsIndex,
			Index<String> wordsIndex,
			boolean fillIndices) {
		
		CorpusDoc doc = new CorpusDoc();

		fwdHits.reset();
		while (fwdHits.nextSentence()) {
			
			Hit sentenceHit = fwdHits.getSentenceHit();
			FwdHitsMap fwdHitsMap = fwdHits.getSentenceHits(EnumSet.of(FwdHitKind.WORD, FwdHitKind.PATTERN));
			
			// obtain word and pattern hits for this sentence
			List<FwdHit> wordFwdHits = fwdHitsMap.get(FwdHitKind.WORD);
			List<FwdHit> patternFwdHits = fwdHitsMap.get(FwdHitKind.PATTERN);

			// arrange pattern and word hits into sentence
			List<FwdHit> sentenceFwdHits = FwdHitsUtils.getUniqueHitsForTopics(
					populateListPatterns, patternFwdHits, wordFwdHits, keysIndex, stopKeyIds);

			// create sentence entry
			CorpusSentence corpusSentence = new CorpusSentence(sentenceHit);
			for (int i=0; i<sentenceFwdHits.size(); i++) {
				
				FwdHit fwdHit = sentenceFwdHits.get(i);
				
				String stem = keysIndex.getStrCached(fwdHit.getKeyId());
				if (stem == null) {
					throw new IllegalStateException(
							"This fwd hit should not have been extracted " +
							"because we should have already seen that we " +
							"can't identify its stem by its key id");
				}
				
				Integer stemIndex;
				if (fillIndices) {
					stemIndex = stemsIndex.ensure(stem);
				} else {
					stemIndex = stemsIndex.getIndex(stem);
				}
				
				Integer wordIndex = null;
				if (wordsIndex != null) {
					String word = snap.extractHitStr(sentenceHit, fwdHit.getHit());
					if (fillIndices) {
						wordIndex = wordsIndex.ensure(word);
					} else {
						wordIndex = wordsIndex.getIndex(word);
					}
				}
				
				if (stemIndex != null) {
					
					CorpusPlace place = new CorpusPlace(
							stemIndex.intValue(),
							wordIndex == null ? -1 : wordIndex.intValue(),
							fwdHit);
					
					corpusSentence.addPlace(place);
				}
				
			}
			doc.addSentence(corpusSentence);
		}		
		
		return doc;
	}

}
