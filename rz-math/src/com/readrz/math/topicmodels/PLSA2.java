package com.readrz.math.topicmodels;

import me.akuz.core.math.JamaUtils;
import me.akuz.core.math.SparseMatrix;
import me.akuz.core.math.SparseTensor;
import me.akuz.core.math.SparseVector;
import Jama.Matrix;

public class PLSA2 {
	
	private SparseTensor<Integer, double[]> _tTagDocWord;
	private Matrix _mTopicDoc;
	private Matrix _mWordTopic;
	private Matrix _outTagTopic;
	
	public PLSA2(
			SparseTensor<Integer, double[]> tTagDocWord, 
			Matrix mTopicDoc, Matrix mWordTopic, 
			int iterationCount, Matrix outTagTopic) {
		
		System.out.println("Initializing PLSA2...");
		long millis = System.currentTimeMillis();
		_tTagDocWord = tTagDocWord;
		_mTopicDoc = mTopicDoc;
		_mWordTopic = mWordTopic;
		_outTagTopic = outTagTopic;
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
		
		// calculate n*(i,j,d)
		ms = System.currentTimeMillis();
		for (int tagLoc=0; tagLoc<_tTagDocWord.size(); tagLoc++) {
			int tagIndex = _tTagDocWord.getKeyByIndex(tagLoc);
			SparseMatrix<Integer, double[]> mDocWord = _tTagDocWord.getValueByIndex(tagLoc);
			
			for (int docLoc=0; docLoc<mDocWord.size(); docLoc++) {
				int docIndex = mDocWord.getKeyByIndex(docLoc);
				SparseVector<Integer, double[]> vWord = mDocWord.getValueByIndex(docLoc);
				
				for (int wordLoc=0; wordLoc<vWord.size(); wordLoc++) {
					int wordIndex = vWord.getKeyByIndex(wordLoc);
					double[] value = vWord.getValueByIndex(wordLoc);
					
					double denominator = 0.0;
					for (int topicIndex=0; topicIndex<_mTopicDoc.getRowDimension(); topicIndex++) {
						denominator += 
							_mTopicDoc.get(topicIndex, docIndex)*
							_mWordTopic.get(wordIndex, topicIndex)*
							_outTagTopic.get(tagIndex, topicIndex);
					}
					value[1] = (value[0] + Double.MIN_NORMAL) / (denominator + Double.MIN_NORMAL);
				}
			}
		}
		System.out.print(" " + (System.currentTimeMillis() - ms) + " ms");
		
		// update tag|topic probabilities
		ms = System.currentTimeMillis();
		for (int tagIndex=0; tagIndex<_outTagTopic.getRowDimension(); tagIndex++) {			
			SparseMatrix<Integer, double[]> mDocWord = _tTagDocWord.get(tagIndex);

			for (int topicIndex=0; topicIndex<_outTagTopic.getColumnDimension(); topicIndex++) {
				
				double factor = 0.0;
				if (mDocWord != null) {
					for (int docLoc=0; docLoc<mDocWord.size(); docLoc++) {
						int docIndex = mDocWord.getKeyByIndex(docLoc);
						SparseVector<Integer, double[]> vWord = mDocWord.getValueByIndex(docLoc);
						
						for (int wordLoc=0; wordLoc<vWord.size(); wordLoc++) {
							int wordIndex = vWord.getKeyByIndex(wordLoc);
							double[] value = vWord.getValueByIndex(wordLoc);
							
							factor += value[1] * 
								_mTopicDoc.get(topicIndex, docIndex) * 
								_mWordTopic.get(wordIndex, topicIndex);
						}
						
					}
				}
				double oldValueTimesFactor = _outTagTopic.get(tagIndex, topicIndex) * factor;
				_outTagTopic.set(tagIndex, topicIndex, oldValueTimesFactor);
			}
		}
		System.out.print(" " + (System.currentTimeMillis() - ms) + " ms");
		
		// normalize probabilities
		ms = System.currentTimeMillis();
		JamaUtils.normColsToOne(_outTagTopic);
		System.out.print(" " + (System.currentTimeMillis() - ms) + " ms");
	}
	
	public Matrix getTagTopic() {
		return _outTagTopic;
	}
}
