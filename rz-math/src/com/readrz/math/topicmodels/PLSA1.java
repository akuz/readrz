package com.readrz.math.topicmodels;

import java.security.InvalidParameterException;

import me.akuz.core.math.JamaUtils;
import me.akuz.core.math.SparseMatrix;
import me.akuz.core.math.SparseVector;
import Jama.Matrix;

/**
 * Probabilistic latent semantic indexing algorithm.
 *
 */
public final class PLSA1 {
	
	private int _topicCount;
	private SparseMatrix<Integer, double[]> _mWordDoc;
	private SparseMatrix<Integer, double[]> _mDocWord;
	private Matrix _outWordTopic;
	private Matrix _outTopicDoc;
	
	/**
	 * Input matrices contain double arrays: 
	 * entry at index 0 should contain input data, 
	 * entry at index 1 will be used for calculations.
	 */
	public PLSA1(
			SparseMatrix<Integer, double[]> mDocWord, 
			SparseMatrix<Integer, double[]> mWordDoc, 
			int topicCount, 
			int iterationCount,
			Matrix outTopicDoc,
			Matrix outWordTopic) {
		
		System.out.println("Initializing PLSA1...");
		long millis = System.currentTimeMillis();
		if (mDocWord == null) {
			throw new InvalidParameterException("Parameter mDocWord must not be null");
		}
		if (mDocWord.size() < 2) {
			throw new InvalidParameterException("Parameter mDocWord must have at least two rows");
		}
		if (mWordDoc == null) {
			throw new InvalidParameterException("Parameter mWordDoc must not be null");
		}
		if (mWordDoc.size() < 2) {
			throw new InvalidParameterException("Parameter mWordDoc must have at least two rows");
		}
		if (topicCount < 1) {
			throw new InvalidParameterException("Parameter topicCount must be >= 1");
		}
		if (iterationCount < 1) {
			throw new InvalidParameterException("Parameter iterationCount must be >= 1");
		}
		_topicCount = topicCount;
		_mWordDoc = mWordDoc;
		_mDocWord = mDocWord;
		_outTopicDoc = outTopicDoc;
		_outWordTopic = outWordTopic;
		System.out.println("Will run " + iterationCount + " iterations.");
		for (int iteration=1; iteration<=iterationCount; iteration++) {
			System.out.print("Running iteration " + iteration + "... ");
			long iterMillis = System.currentTimeMillis();
			iterate(iteration);
			System.out.println(" done in " + (System.currentTimeMillis() - iterMillis) + " ms");
		}
		System.out.println("PLSA complete (" + (System.currentTimeMillis() - millis) + " ms)");
	}
	
	private void iterate(int iteration) {
		
		long ms;
		ms = System.currentTimeMillis();
		
		// calculate n*(i,j) in "doc-word" mode
		for (int docLoc=0; docLoc<_mDocWord.size(); docLoc++) {
			
			int docIndex = _mDocWord.getKeyByIndex(docLoc);
			SparseVector<Integer, double[]> wordsVector = _mDocWord.getValueByIndex(docLoc);
			
			for (int wordLoc=0; wordLoc<wordsVector.size(); wordLoc++) {
				
				int wordIndex = wordsVector.getKeyByIndex(wordLoc);
				double[] values = wordsVector.getValueByIndex(wordLoc);
				
				double denominator = 0.0;
				for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
					denominator += _outWordTopic.get(wordIndex, topicIndex)*_outTopicDoc.get(topicIndex, docIndex);
				}
				values[1] = (values[0] + Double.MIN_NORMAL) / (denominator + Double.MIN_NORMAL);
			}
		}
		
		System.out.print(" " + (System.currentTimeMillis() - ms)+ " ms");
		ms = System.currentTimeMillis();
			
		// update topic|doc probabilities
		for (int docLoc=0; docLoc<_mDocWord.size(); docLoc++) {
			
			int docIndex = _mDocWord.getKeyByIndex(docLoc);
			SparseVector<Integer, double[]> wordsVector = _mDocWord.getValueByIndex(docLoc);
			
			for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
				
				double factor = 0.0;
				for (int wordLoc=0; wordLoc<wordsVector.size(); wordLoc++) {
					
					int wordIndex = wordsVector.getKeyByIndex(wordLoc);
					double[] value = wordsVector.getValueByIndex(wordLoc);
					
					factor += value[1] * 
						_outWordTopic.get(wordIndex, topicIndex);
				}
				
				double oldValueTimesFactor = _outTopicDoc.get(topicIndex, docIndex) * factor;
				_outTopicDoc.set(topicIndex, docIndex, oldValueTimesFactor);
			}
		}
		System.out.print(" " + (System.currentTimeMillis() - ms)+ " ms");
		ms = System.currentTimeMillis();
	
		JamaUtils.normColsToOne(_outTopicDoc);
		
		System.out.print(" " + (System.currentTimeMillis() - ms)+ " ms");
		ms = System.currentTimeMillis();
		
		// calculate n*(i,j) in "word-doc" mode
		for (int wordLoc=0; wordLoc<_mWordDoc.size(); wordLoc++) {
			
			int wordIndex = _mWordDoc.getKeyByIndex(wordLoc);
			SparseVector<Integer, double[]> docsVector = _mWordDoc.getValueByIndex(wordLoc);
			
			for (int docLoc=0; docLoc<docsVector.size(); docLoc++) {
				
				int docIndex = docsVector.getKeyByIndex(docLoc);
				double[] values = docsVector.getValueByIndex(docLoc);
				
				double denominator = 0.0;
				for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
					denominator += _outWordTopic.get(wordIndex, topicIndex)*_outTopicDoc.get(topicIndex, docIndex);
				}
				values[1] = (values[0] + Double.MIN_NORMAL) / (denominator + Double.MIN_NORMAL);
			}
		}
		
		System.out.print(" " + (System.currentTimeMillis() - ms)+ " ms");
		ms = System.currentTimeMillis();
		
		// update word|topic probabilities
		for (int wordLoc=0; wordLoc<_mWordDoc.size(); wordLoc++) {
			
			int wordIndex = _mWordDoc.getKeyByIndex(wordLoc);
			SparseVector<Integer, double[]> docsVector = _mWordDoc.getValueByIndex(wordLoc);
			
			for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
				
				double factor = 0.0;
				for (int docLoc=0; docLoc<docsVector.size(); docLoc++) {
					
					int docIndex = docsVector.getKeyByIndex(docLoc);
					double[] value = docsVector.getValueByIndex(docLoc);
					
					factor += value[1] * 
						_outTopicDoc.get(topicIndex, docIndex);
				}
				
				double oldValueTimesFactor = _outWordTopic.get(wordIndex, topicIndex) * factor;
				_outWordTopic.set(wordIndex, topicIndex, oldValueTimesFactor);
			}
		}
		System.out.print(" " + (System.currentTimeMillis() - ms)+ " ms");
		ms = System.currentTimeMillis();
		
		JamaUtils.normColsToOne(_outWordTopic);

		System.out.print(" " + (System.currentTimeMillis() - ms)+ " ms");
		ms = System.currentTimeMillis();
	}
	
	public Matrix getTopicDoc() {
		return _outTopicDoc;
	}
	
	public Matrix getWordTopic() {
		return _outWordTopic;
	}
}
