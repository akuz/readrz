package com.readrz.math.topicmodels;

import me.akuz.core.math.JamaUtils;
import me.akuz.core.math.SpaMatrix;
import me.akuz.core.math.SpaMatrixLine;
import Jama.Matrix;

/**
 * Probabilistic latent semantic indexing algorithm.
 *
 */
public final class PLSA1AkaAtaKaban {
	
	private int _topicCount;
	private int _iterationCount;
	private SpaMatrix _X;
	private SpaMatrix _X_div_WT_TD;
	private SpaMatrix _X_div_WT_TD_TransposedView;
	private Matrix _outWordTopic;
	private Matrix _outTopicDoc;
	
	public PLSA1AkaAtaKaban(
			SpaMatrix docTermMatrix, 
			int topicCount, 
			int iterationCount,
			Matrix outTopicDoc,
			Matrix outWordTopic) {
		
		System.out.println("Initializing PLSA...");
		long millis = System.currentTimeMillis();
		_topicCount = topicCount;
		_iterationCount = iterationCount;
		_X = docTermMatrix.transposedView();
		_X_div_WT_TD = new SpaMatrix(_X);
		_X_div_WT_TD_TransposedView = _X_div_WT_TD.transposedView();
		_outWordTopic = outWordTopic;
		_outTopicDoc = outTopicDoc;
		for (int i=0; i<_iterationCount; i++) {
			System.out.print("Running iteration " + (i+1) + "... ");
			long iterMillis = System.currentTimeMillis();
			iterate();
			System.out.println(" done in " + (System.currentTimeMillis() - iterMillis) + " ms");
		}
		System.out.println("PLSA complete (" + (System.currentTimeMillis() - millis) + " ms)");
	}
	
	private void iterate() {
		long ms;
		
		ms = System.currentTimeMillis();
		fastCalc_X_div_WT_TD();
		
		System.out.print(" " + (System.currentTimeMillis() - ms)+ " ms");
		ms = System.currentTimeMillis();

		Matrix STransposedElementWiseMultiplier = _X_div_WT_TD_TransposedView.multOnRightBy(_outWordTopic);
		JamaUtils.updateMultTransposedElementWise(_outTopicDoc, STransposedElementWiseMultiplier);
		
		System.out.print(" " + (System.currentTimeMillis() - ms)+ " ms");
		ms = System.currentTimeMillis();

		JamaUtils.normColsToOne(_outTopicDoc);
		
		System.out.print(" " + (System.currentTimeMillis() - ms)+ " ms");
		ms = System.currentTimeMillis();

		fastCalc_X_div_WT_TD();
		
		System.out.print(" " + (System.currentTimeMillis() - ms)+ " ms");
		ms = System.currentTimeMillis();

		Matrix WTransposedElementWiseMultiplier = _X_div_WT_TD_TransposedView.multOnLeftBy(_outTopicDoc);
		JamaUtils.updateMultTransposedElementWise(_outWordTopic, WTransposedElementWiseMultiplier);
		
		System.out.print(" " + (System.currentTimeMillis() - ms)+ " ms");
		ms = System.currentTimeMillis();

		JamaUtils.normColsToOne(_outWordTopic);

		System.out.print(" " + (System.currentTimeMillis() - ms)+ " ms");
		ms = System.currentTimeMillis();
	}
	
	private void fastCalc_X_div_WT_TD() {
		for (int i=0; i<_X_div_WT_TD.getRowCount(); i++) {
			SpaMatrixLine row = _X_div_WT_TD.getRow(i);
			SpaMatrixLine rowInX = _X.getRow(i);
			for (int idx=0; idx<row.size(); idx++) {
				int j = row.getIndexByIdx(idx);
				double valueInX = rowInX.getValueByIdx(idx);
				double WSij = 0.0;
				for (int k=0; k<_topicCount; k++) {
					WSij += _outWordTopic.get(i, k)*_outTopicDoc.get(k, j);
				}
				_X_div_WT_TD.set(i, j, (valueInX + Double.MIN_NORMAL)/(WSij + Double.MIN_NORMAL));
			}
		}
	}
	
	/**
	 * P(word|topic) conditional probabilities matrix: 
	 * rows correspond to terms, columns correspond to topics.
	 */
	public Matrix getWordTopic() {
		return _outWordTopic;
	}

	/**
	 * P(topic|document) conditional probabilities matrix: 
	 * rows correspond to topics, columns correspond to documents.
	 */
	public Matrix getTopicDoc() {
		return _outTopicDoc;
	}

}
