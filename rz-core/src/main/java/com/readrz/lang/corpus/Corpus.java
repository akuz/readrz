package com.readrz.lang.corpus;

import java.util.List;

import me.akuz.core.HashIndex;
import me.akuz.core.Index;
import me.akuz.core.math.SparseVector;

/**
 * Corpus that contains the documents that store each word position in the original document.
 * (so, if the document contains the same word twice, it will be stored at different locations).
 *
 */
public class Corpus<TDocId> {

	private final Index<String> _wordsIndex;
	private final Index<String> _stemsIndex;
	private final SparseVector<TDocId, CorpusDoc> _docs;
	private int _sentenceCount;
	private int _placeCount;
	
	public Corpus() {
		_wordsIndex = new HashIndex<String>();
		_stemsIndex = new HashIndex<String>();
		_docs = new SparseVector<TDocId, CorpusDoc>(10);
	}
	
	/**
	 * Get original words index (to retrieve original words by wordIndex).
	 * @return
	 */
	public Index<String> getWordsIndex() {
		return _wordsIndex;
	}
	
	/**
	 * Get stem index (to retrieve stems by stem index).
	 * @return
	 */
	public Index<String> getStemsIndex() {
		return _stemsIndex;
	}
	
	/**
	 * Get the documents in the corpus (sparse index is document id).
	 * @return
	 */
	public SparseVector<TDocId, CorpusDoc> getDocs() {
		return _docs;
	}
	
	/**
	 * Total number of documents in corpus.
	 * @return
	 */
	public int getDocCount() {
		return _docs.size();
	}
	
	/**
	 * Total number of places (word occurrences) in the corpus.
	 * @return
	 */
	public int getPlaceCount() {
		return _placeCount;
	}
	
	/**
	 * Total number of sentences in the corpus.
	 * @return
	 */
	public int getSentenceCount() {
		return _sentenceCount;
	}

	/**
	 * Add a new document to corpus.
	 * @param docId - external document id (can be sparse)
	 * @return - document index
	 */
	public int addDoc(TDocId docId, CorpusDoc doc) {
		_placeCount += doc.getLength();
		List<CorpusSentence> sentences = doc.getSentences();
		for (int i=0; i<sentences.size(); i++) {
			sentences.get(i).setCorpusSentenceIndex(_sentenceCount);
			_sentenceCount += 1;
		}
		return _docs.set(docId, doc);
	}

}
