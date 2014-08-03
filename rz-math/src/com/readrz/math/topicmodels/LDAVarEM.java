package com.readrz.math.topicmodels;

import java.util.Arrays;
import java.util.List;

import me.akuz.core.RepeatedValue;
import me.akuz.core.math.MatrixUtils;
import me.akuz.core.math.Randoms;
import me.akuz.core.math.SparseVector;

import org.apache.commons.math3.special.Gamma;

import Jama.Matrix;

import com.readrz.lang.corpus.Corpus;
import com.readrz.lang.corpus.CorpusDoc;
import com.readrz.lang.corpus.CorpusPlace;
import com.readrz.lang.corpus.CorpusSentence;

/**
 * LDA (Latent Dirichlet Allocation) inference using 
 * variational EM (Expectation Maximization).
 *
 */
public final class LDAVarEM<TDocId> {
	
	private static final int ALPHA_MAX_ITERATIONS = 100;
	private static final double ALPHA_PRECISION_BASE = 0.001;
	
	private final Randoms _rnds;
	private int _currentIter;
	private final SparseVector<TDocId, CorpusDoc> _docs;
	private final int _stemCount;
	private final int _topicCount;
	private final double[] _alphaDirParams;
	private final double[] _betaDirParams;
	private final Matrix _gammaTopicDoc;
	private final double[] _gammaTopicByDocSumOutTopics;
	private final Matrix _betaStemTopic;
	private final Matrix _alphaTopic;
	private final double[] _alphaTopic_g;
	private final double[] _alphaTopic_h;
	
	public LDAVarEM(
			Corpus<TDocId> corpus,
			LDAVarEMParams params) {

		_rnds = new Randoms((int)System.currentTimeMillis());
		
		_docs = corpus.getDocs();
		_stemCount = corpus.getStemsIndex().size();
		
		int topicCount = params.getTopicCount();
		_topicCount = topicCount;
		
		// initialize per-topic Dirichlet priors
		_alphaDirParams = RepeatedValue.expandDouble(params.getAlphas());
		_betaDirParams = RepeatedValue.expandDouble(params.getTopicBetas());

		System.out.println("LDA: Preparing internal data structures...");
		for (int docLoc=0; docLoc<_docs.size(); docLoc++) {
			
			CorpusDoc doc = _docs.getValueByIndex(docLoc);
			List<CorpusSentence> sentences = doc.getSentences();
			
			for (int sentenceLoc=0; sentenceLoc<sentences.size(); sentenceLoc++) {
				
				CorpusSentence sentence = sentences.get(sentenceLoc);
				List<CorpusPlace> places = sentence.getPlaces();
				
				for (int placeLoc=0; placeLoc<places.size(); placeLoc++) {
					
					CorpusPlace place = places.get(placeLoc);
					double[] placePhi = new double[topicCount];
					place.setTag(placePhi);
				}
			}
		}
		
		// model variational parameters
		_gammaTopicDoc = new Matrix(topicCount, _docs.size());
		_gammaTopicByDocSumOutTopics = new double[_docs.size()];
		_betaStemTopic = new Matrix(_stemCount, topicCount);

		// topics Dirichlet posterior
		_alphaTopic = new Matrix(topicCount, 1);
		_alphaTopic_g = new double[topicCount];
		_alphaTopic_h = new double[topicCount];
		
		initialize();
	}
	
	public void initialize() {
		
		System.out.println("LDA: Initializing model parameters...");
		
		for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
			_alphaTopic.set(topicIndex, 0, _alphaDirParams[topicIndex]);
		}
		
		// calculate stem frequencies for single topic model
		System.out.println("LDA: Counting stem statistics...");
		int[] totalStemCounts = new int[_stemCount];
		for (int docLoc=0; docLoc<_docs.size(); docLoc++) {
			
			CorpusDoc doc = _docs.getValueByIndex(docLoc);
			List<CorpusSentence> sentences = doc.getSentences();
			
			for (int sentenceLoc=0; sentenceLoc<sentences.size(); sentenceLoc++) {
				
				CorpusSentence sentence = sentences.get(sentenceLoc);
				List<CorpusPlace> places = sentence.getPlaces();
				
				for (int placeLoc=0; placeLoc<places.size(); placeLoc++) {
					CorpusPlace place = places.get(placeLoc);
					int stemIndex = place.getStemIndex();
					totalStemCounts[stemIndex] += 1;
				}
			}
		}
		
		// initialize topic-stem probabilities by sampling
		System.out.println("LDA: Sampling initial topics...");
		for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
			double betaDirParam = _betaDirParams[topicIndex];
			double sum = 0;
			// sample from single topic Dirichlet prior
			for (int stemIndex=0; stemIndex<_stemCount; stemIndex++) {
				double fraction = _rnds.nextGamma(betaDirParam + totalStemCounts[stemIndex], 1.0);
				_betaStemTopic.set(stemIndex, topicIndex, fraction);
				sum += fraction;
			}
			// normalize topic-stem probabilities
			for (int stemIndex=0; stemIndex<_stemCount; stemIndex++) {
				_betaStemTopic.set(stemIndex, topicIndex, 
						_betaStemTopic.get(stemIndex, topicIndex) / sum);
			}
		}
	}
	
	public void iterate(int[] trainingDocLocs, int maxIter) {
		
		for (int localIter=0; localIter<maxIter; localIter++) {
			_currentIter += 1;
			
			System.out.println("LDA: Running iteration " + _currentIter + "...");
			
			// **************
			// *** E-STEP ***
			// **************
			
			System.out.print("LDA: E-step...");
			int pct10 = Math.max(_docs.size() / 10, 1);
			for (int docLoc=0; docLoc<_docs.size(); docLoc++) {

				if (docLoc > 0 && docLoc % pct10 == 0) {
					System.out.print(" " + (docLoc / pct10) + "0%");
				}
				
				CorpusDoc doc = _docs.getValueByIndex(docLoc);
				int docPlaceCount = doc.getLength();
				List<CorpusSentence> sentences = doc.getSentences();
				
				// initialize gamma
				double addPerTopic = (double)docPlaceCount/(double)_topicCount;
				for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
					_gammaTopicDoc.set(topicIndex, docLoc, 
							_alphaTopic.get(topicIndex, 0) + addPerTopic);
				}
				
				// initialize phi
				double initPhi = 1.0/_topicCount;
				for (int sentenceLoc=0; sentenceLoc<sentences.size(); sentenceLoc++) {
					CorpusSentence sentence = sentences.get(sentenceLoc);
					List<CorpusPlace> places = sentence.getPlaces();
					for (int placeLoc=0; placeLoc<places.size(); placeLoc++) {
						CorpusPlace place = places.get(placeLoc);
						double[] placePhi = (double[])place.getTag();
						Arrays.fill(placePhi, initPhi);
					}
				}
				
				// make places.size() iterations
				for (int iter2=0; iter2<Math.min(docPlaceCount, 10); iter2++) {

					// update phi
					for (int sentenceLoc=0; sentenceLoc<sentences.size(); sentenceLoc++) {
						
						CorpusSentence sentence = sentences.get(sentenceLoc);
						List<CorpusPlace> places = sentence.getPlaces();
							
						for (int placeLoc=0; placeLoc<places.size(); placeLoc++) {
							
							CorpusPlace place = places.get(placeLoc);
							int stemIndex = place.getStemIndex();
							double[] placePhi = (double[])place.getTag();

							double sum = 0;
							for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
								double stemTopicPhi 
									= _betaStemTopic.get(stemIndex, topicIndex)
									* Math.exp(Gamma.digamma(_gammaTopicDoc.get(topicIndex, docLoc)));
								placePhi[topicIndex] = stemTopicPhi;
								sum += stemTopicPhi;
							}
							for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
								placePhi[topicIndex] /= sum;
							}
						}
					}

					// update gamma
					for (int sentenceLoc=0; sentenceLoc<sentences.size(); sentenceLoc++) {
						
						CorpusSentence sentence = sentences.get(sentenceLoc);
						List<CorpusPlace> places = sentence.getPlaces();

						MatrixUtils.columnSet(_gammaTopicDoc, docLoc, _alphaTopic, 0);
						for (int placeLoc=0; placeLoc<places.size(); placeLoc++) {
							
							CorpusPlace place = places.get(placeLoc);
							double[] placePhi = (double[])place.getTag();
							
							MatrixUtils.columnAdd(_gammaTopicDoc, docLoc, placePhi);
						}
					}
					
					// sum gammas for doc for future use
					_gammaTopicByDocSumOutTopics[docLoc] = 0;
					for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
						_gammaTopicByDocSumOutTopics[docLoc] += _gammaTopicDoc.get(topicIndex, docLoc);
					}					
				}
			}
			System.out.println("");
			
			// **************
			// *** M-STEP ***
			// **************
			
			System.out.println("LDA: M-step...");

			// ---------------------
			// --- optimize beta ---
			// ---------------------

			// initialize beta before summing
			for (int stemIndex=0; stemIndex<_stemCount; stemIndex++) {
				for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
					_betaStemTopic.set(stemIndex, topicIndex, _betaDirParams[topicIndex]);
				}
			}
			// add stem counts times phi to beta
			for (int i=0; i<trainingDocLocs.length; i++) {
				int docLoc = trainingDocLocs[i];
				
				CorpusDoc doc = _docs.getValueByIndex(docLoc);
				List<CorpusSentence> sentences = doc.getSentences();
				
				for (int sentenceLoc=0; sentenceLoc<sentences.size(); sentenceLoc++) {
					
					CorpusSentence sentence = sentences.get(sentenceLoc);
					List<CorpusPlace> places = sentence.getPlaces();
					
					for (int placeLoc=0; placeLoc<places.size(); placeLoc++) {
						
						CorpusPlace place = places.get(placeLoc);
						int stemIndex = place.getStemIndex();
						double[] placePhi = (double[])place.getTag();
						
						for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
							double stemTopicPhi = placePhi[topicIndex];
							_betaStemTopic.set(stemIndex, topicIndex,
									_betaStemTopic.get(stemIndex, topicIndex)
									+ stemTopicPhi);
						}
					}
				}
			}

			// normalize beta
			MatrixUtils.normalizeColumns(_betaStemTopic);
			System.out.println("LDA: Calculated beta.");
			
			// ----------------------
			// --- optimize alpha ---
			// ----------------------
			
			// apply digamma function to sums of gamma
			for (int docIndex=0; docIndex<_docs.size(); docIndex++) {
				_gammaTopicByDocSumOutTopics[docIndex] = Gamma.digamma(_gammaTopicByDocSumOutTopics[docIndex]);
			}
			
			// perform iterations to find alpha
			final double ALPHA_PRECISION = ALPHA_PRECISION_BASE / _topicCount;
			// initialize
			for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
				_alphaTopic.set(topicIndex, 0, _alphaDirParams[topicIndex]);
			}
			// iterate
			int iterAlpha;
			for (iterAlpha=0; iterAlpha<ALPHA_MAX_ITERATIONS; iterAlpha++) {
				
//				System.out.println("LDA: Alpha iteration " + (iterAlpha+1) + "...");
//				System.out.println(MatrixUtils.toString(_alphaTopic));
				
				// calculate alpha iteration parameters
				double sumAlphaTopic = 0;
				for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
					sumAlphaTopic += _alphaTopic.get(topicIndex, 0);
				}

				// precalculate digamma of sum of alphas
				double ksiSumAlphaTopic = Gamma.digamma(sumAlphaTopic);
				
				// calculate g and h
				for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
					_alphaTopic_g[topicIndex] = _docs.size() * (ksiSumAlphaTopic - Gamma.digamma(_alphaTopic.get(topicIndex, 0)));
					for (int i=0; i<trainingDocLocs.length; i++) {
						int docIndex = trainingDocLocs[i];
						_alphaTopic_g[topicIndex] 
						              += Gamma.digamma(_gammaTopicDoc.get(topicIndex, docIndex))
						              - _gammaTopicByDocSumOutTopics[docIndex];
					}
					_alphaTopic_h[topicIndex] = - _docs.size() * Gamma.trigamma(_alphaTopic.get(topicIndex, 0));
				}
				
				// calculate z
				double alphaTopic_z = _docs.size() * Gamma.trigamma(sumAlphaTopic);
								
				double c1 = 0;
				double c2 = 0;
				for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
					c1 += _alphaTopic_g[topicIndex] / _alphaTopic_h[topicIndex];
					c2 += 1.0 / _alphaTopic_h[topicIndex];
				}
				double c = c1 / (1.0 / alphaTopic_z + c2);
				
				double alphaTotalChange = 0;
				for (int topicIndex=0; topicIndex<_topicCount; topicIndex++) {
					double alphaLogChange = (_alphaTopic_g[topicIndex] - c) / _alphaTopic_h[topicIndex];
					double newAlpha = Math.exp(Math.log(_alphaTopic.get(topicIndex, 0)) - alphaLogChange);
					alphaTotalChange += Math.abs(newAlpha - _alphaTopic.get(topicIndex, 0));
					_alphaTopic.set(topicIndex, 0, newAlpha);
				}
				
				if (alphaTotalChange < ALPHA_PRECISION) {
					break;
				}
			}
			
			System.out.println("LDA: Calculated alpha in " + (iterAlpha+1) + " iterations.");
			System.out.println(MatrixUtils.toString(_alphaTopic));
		}
	}
	
	public Matrix getAlphaTopic() {
		return _alphaTopic;
	}
	
	public Matrix getStemByTopicProbs() {
		return _betaStemTopic;
	}
	
	public Matrix getGammaTopicDoc() {
		return _gammaTopicDoc;
	}

}
