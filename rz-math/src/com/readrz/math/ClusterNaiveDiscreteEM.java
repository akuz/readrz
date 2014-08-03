 package com.readrz.math;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import me.akuz.core.math.StatsUtils;

import com.readrz.lang.corpus.Corpus;
import com.readrz.lang.corpus.CorpusDoc;
import com.readrz.lang.corpus.CorpusPlace;
import com.readrz.lang.corpus.CorpusSentence;
import me.akuz.core.math.SparseVector;
import me.akuz.core.ArrayUtils;

/**
 * EM (Expectation Maximization) Inference for clustering of 
 * "documents" with "words" assumed to be drawn independently 
 * (naive assumption) from a discrete distribution specific 
 * to a (single) cluster that the document belongs to.
 *
 */
public final class ClusterNaiveDiscreteEM<TDocId> {

	// numbers formatting
	private static final DecimalFormat _fmt_0_00 = new DecimalFormat("0.00");

	// input parameters and data
	private final int _clusterCount;
	private final double[] _clusterReusedArr;
	private final double _clusterStemsBeta;
	private final SparseVector<TDocId, CorpusDoc> _docs;
	private final int _stemCount;

	// Expectation parameters
	private double[][] _probDocCluster;

	// Maximization parameters
	private double[][] _probClusterStem;
	private double[]   _probCluster;

	public ClusterNaiveDiscreteEM(
			int clusterCount,
			double clusterStemsBeta,
			Corpus<TDocId> corpus,
			int[] trainDocLocs,
			int[] validDocLocs,
			double maxIter) {
		
		if (clusterCount < 2) {
			throw new IllegalArgumentException("Parameter clusterCount must be >= 2");
		}
		if (clusterStemsBeta < 0) {
			throw new IllegalArgumentException("Parameter clusterStemsBeta must be >= 0");
		}
		if (corpus == null || corpus.getDocCount() < 2) {
			throw new IllegalArgumentException("Parameter corpus must contain at least two documents");
		}
		if (corpus.getStemsIndex().size() < 2) {
			throw new IllegalArgumentException("Parameter corpus must contain at least two stems");
		}
		if (maxIter < 1) {
			throw new IllegalArgumentException("Parameter maxIter must be >= 1");
		}
		
		_clusterCount = clusterCount;
		_clusterReusedArr = new double[clusterCount];
		_clusterStemsBeta = clusterStemsBeta;
		_docs = corpus.getDocs();
		_stemCount = corpus.getStemsIndex().size();
		
		Set<Integer> setTrainDocLocs = null;
		if (trainDocLocs != null) {
			
			System.out.println("CND: Checking training doc locations...");
			
			if (trainDocLocs.length == 0) {
				throw new IllegalArgumentException("Training doc locations must contain at least one entry");
			}
			
			if (trainDocLocs.length > _docs.size()) {
				throw new IllegalArgumentException("Training doc locations entries count must be <= the number of documents");
			}
			
			setTrainDocLocs = new HashSet<Integer>();
			for (int i=0; i<trainDocLocs.length; i++) {
				
				int docLoc = trainDocLocs[i];
				
				if (docLoc < 0 || docLoc >= _docs.size()) {
					throw new IllegalArgumentException("Training doc location " + docLoc + " is out of bounds");
				}
				
				if (setTrainDocLocs.contains(docLoc)) {
					throw new IllegalArgumentException("Duplicate training doc location " + docLoc);
				}
				
				setTrainDocLocs.add(docLoc);
			}
		}

		Set<Integer> setValidDocLocs = null;
		if (validDocLocs != null) {

			System.out.println("CND: Checking validation doc locations...");

			if (validDocLocs.length == 0) {
				throw new IllegalArgumentException("Validation doc locations must contain at least one entry");
			}
			
			if (validDocLocs.length >= _docs.size()) {
				throw new IllegalArgumentException("Validation doc locations entries count must be < the number of documents");
			}

			setValidDocLocs = new HashSet<Integer>();
			for (int i=0; i<validDocLocs.length; i++) {
				
				int docLoc = validDocLocs[i];
				
				if (docLoc < 0 || docLoc >= _docs.size()) {
					throw new IllegalArgumentException("Validation doc location " + docLoc + " is out of bounds");
				}

				if (setTrainDocLocs != null && setTrainDocLocs.contains(docLoc)) {
					throw new IllegalArgumentException("Validation doc location " + docLoc + " is already marked as training doc location");
				}
				
				if (setValidDocLocs.contains(docLoc)) {
					throw new IllegalArgumentException("Duplicate validation doc location " + docLoc);
				}
				
				setValidDocLocs.add(docLoc);
			}
		}
		
		if (trainDocLocs == null) {
			
			if (setValidDocLocs == null) {
				
				trainDocLocs = new int[_docs.size()];
				for (int docLoc=0; docLoc<_docs.size(); docLoc++) {
					trainDocLocs[docLoc] = docLoc;
				}
				
				System.out.println("CND: Training doc locations not provided - allocated all docs for training.");
				
			} else {
				
				trainDocLocs = new int[_docs.size() - setValidDocLocs.size()];
				
				int i=0;
				for (int docLoc=0; docLoc<_docs.size(); docLoc++) {
					trainDocLocs[docLoc] = docLoc;
					if (setValidDocLocs.contains(docLoc) == false) {
						trainDocLocs[i] = docLoc;
						i++;
					}
				}

				System.out.println("CND: Training doc locations not provided - allocated based on diff with validation doc locations.");
			}
		}
		
		double[][] prev_probDocCluster  = ArrayUtils.initArray2D(_docs.size(), _clusterCount);
		double[][] prev_probClusterStem = ArrayUtils.initArray2D(_clusterCount, _stemCount);
		double[]   prev_probCluster     = ArrayUtils.initArray1D(_clusterCount);
		
		double[][] curr_probDocCluster  = ArrayUtils.initArray2D(_docs.size(), _clusterCount);
		double[][] curr_probClusterStem = ArrayUtils.initArray2D(_clusterCount, _stemCount);
		double[]   curr_probCluster     = ArrayUtils.initArray1D(_clusterCount);
		
		// initialize Expectation parameters
		Random rnd = new Random(System.currentTimeMillis());
		for (int docLoc=0; docLoc<_docs.size(); docLoc++) {
			double norm = 0;
			for (int clusterLoc=0; clusterLoc<_clusterCount; clusterLoc++) {
				double unnormProb = 0.25 + 0.5 * rnd.nextDouble();
				curr_probDocCluster[docLoc][clusterLoc] = unnormProb;
				norm += unnormProb;
			}
			for (int clusterLoc=0; clusterLoc<_clusterCount; clusterLoc++) {
				curr_probDocCluster[docLoc][clusterLoc] = curr_probDocCluster[docLoc][clusterLoc] / norm;
			}
		}

		// perform EM iterations, starting from Maximization
		List<Double> trainLogLikeList = new ArrayList<Double>();
		List<Double> validLogLikeList = new ArrayList<Double>();
		boolean stoppedBecauseValidationLogLikeDecreased = false;
		int iter;
		for (iter=1; iter<=maxIter; iter++) {
			
			System.out.println("CND: Running iteration " + iter + "...");

			// remember parameters for exchange
			double[][] exchange_probDocCluster  = prev_probDocCluster;
			double[][] exchange_probClusterStem = prev_probClusterStem;
			double[]   exchange_probCluster     = prev_probCluster;
			
			// previous parameters = current parameters
			prev_probDocCluster  = curr_probDocCluster;
			prev_probClusterStem = curr_probClusterStem;
			prev_probCluster     = curr_probCluster;
			
			// reuse old parameters
			curr_probDocCluster  = exchange_probDocCluster;
			curr_probClusterStem = exchange_probClusterStem;
			curr_probCluster     = exchange_probCluster;

			// -------------- //
			// *** M-STEP *** //
			// -------------- //

			System.out.println("CND: M-Step...");

			// *** estimate cluster probs
			for (int clusterLoc=0; clusterLoc<_clusterCount; clusterLoc++) {
				double sum_probDocCluster = 0;
				for (int i=0; i<trainDocLocs.length; i++) {
					int docLoc = trainDocLocs[i];
					sum_probDocCluster += prev_probDocCluster[docLoc][clusterLoc];
				}
				curr_probCluster[clusterLoc] = sum_probDocCluster / trainDocLocs.length;
			}
			
			// *** estimate cluster-stem probs
			
			// initialize with cluster-stem beta
			ArrayUtils.fillArray2D(curr_probClusterStem, _clusterStemsBeta);
			
			// add values from all documents
			for (int i=0; i<trainDocLocs.length; i++) {
				
				int docLoc = trainDocLocs[i];
				CorpusDoc doc = _docs.getValueByIndex(docLoc);
				List<CorpusSentence> sentences = doc.getSentences();
				
				for (int sentenceLoc=0; sentenceLoc<sentences.size(); sentenceLoc++) {
					
					CorpusSentence sentence = sentences.get(sentenceLoc);
					List<CorpusPlace> places = sentence.getPlaces();
					
					// for all places in doc
					for (int placeLoc=0; placeLoc<places.size(); placeLoc++) {

						// get place weight
						CorpusPlace place = places.get(placeLoc);
						int stemIndex = place.getStemIndex();
						
						// update soft counts based on cluster soft assignments
						for (int clusterLoc=0; clusterLoc<_clusterCount; clusterLoc++) {
							double clusterResponsibility = prev_probDocCluster[docLoc][clusterLoc];
							curr_probClusterStem[clusterLoc][stemIndex] += clusterResponsibility;
						}
					}					
				}
			}
			// normalize cluster-stem probs
			for (int clusterLoc=0; clusterLoc<_clusterCount; clusterLoc++) {
				double norm = 0;
				for (int stemLoc=0; stemLoc<_stemCount; stemLoc++) {
					norm += curr_probClusterStem[clusterLoc][stemLoc];
				}
				for (int stemLoc=0; stemLoc<_stemCount; stemLoc++) {
					curr_probClusterStem[clusterLoc][stemLoc] /= norm;
				}
			}
			
			// -------------- //
			// *** E-STEP *** //
			// -------------- //
			
			System.out.println("CND: E-Step...");

			double trainLogLike = 0;
			for (int i=0; i<trainDocLocs.length; i++) {
				int docLoc = trainDocLocs[i];
				double docLogLike = calcDocClusterProbsAndLogLike(curr_probDocCluster, docLoc, curr_probCluster, curr_probClusterStem);
				trainLogLike += docLogLike;
			}
			double validLogLike = 0;
			if (validDocLocs != null && validDocLocs.length > 0) {
				for (int i=0; i<validDocLocs.length; i++) {
					int docLoc = validDocLocs[i];
					double docLogLike = calcDocClusterProbsAndLogLike(curr_probDocCluster, docLoc, curr_probCluster, curr_probClusterStem);
					validLogLike += docLogLike;
				}
			}
			trainLogLikeList.add(trainLogLike);
			validLogLikeList.add(validLogLike);
			System.out.println("TrainLogLike: " + _fmt_0_00.format(trainLogLike));
			System.out.println("ValidLogLike: " + _fmt_0_00.format(validLogLike));
			
			if (validLogLikeList.size() > 1) {
				if (validLogLikeList.get(validLogLikeList.size() - 1) <
					validLogLikeList.get(validLogLikeList.size() - 2)) {
					
					stoppedBecauseValidationLogLikeDecreased = true;
					break;
				}
			}
		}
		
		if (stoppedBecauseValidationLogLikeDecreased) {

			_probDocCluster  = prev_probDocCluster;
			_probClusterStem = prev_probClusterStem;
			_probCluster     = prev_probCluster;
			
			System.out.println("CND: Validation log likelihood decreased, stopped before " + iter + " iteration.");
		
		} else {
			
			_probDocCluster  = curr_probDocCluster;
			_probClusterStem = curr_probClusterStem;
			_probCluster     = curr_probCluster;
		
			System.out.println("CND: Performed max number of iterations (" + maxIter + "), stoped.");
		}
	}

	private final double calcDocClusterProbsAndLogLike(
			double[][] out_probDocCluster,
			int docIndex,
			double[] probCluster,
			double[][] probClusterStem) {

		CorpusDoc doc = _docs.getValueByIndex(docIndex);
		List<CorpusSentence> sentences = doc.getSentences();
		
		// calculate cluster log likelihoods
		for (int clusterLoc=0; clusterLoc<_clusterCount; clusterLoc++) {

			// init cluster log like
			double clusterLogLike = 0;

			// add cluster likelihood
			clusterLogLike +=  Math.log(probCluster[clusterLoc]);
			
			// add doc places likelihood
			for (int sentenceLoc=0; sentenceLoc<sentences.size(); sentenceLoc++) {
				
				CorpusSentence sentence = sentences.get(sentenceLoc);
				List<CorpusPlace> places = sentence.getPlaces();

				for (int placeLoc=0; placeLoc<places.size(); placeLoc++) {
					
					// get place weight
					CorpusPlace place = places.get(placeLoc);
					int stemLoc = place.getStemIndex();
					
					double stemProb = probClusterStem[clusterLoc][stemLoc];
					double placeLogLike = Math.log(stemProb);
					
					clusterLogLike += placeLogLike;
				}
			}
			
			// remember cluster log like
			_clusterReusedArr[clusterLoc] = clusterLogLike;
		}
		
		// sum up cluster log likes
		double docLogLike = StatsUtils.logSumExp(_clusterReusedArr);
		
		// normalize and output topic probs
		StatsUtils.logLikesToProbsReplace(_clusterReusedArr);
		for (int clusterLoc=0; clusterLoc<_clusterCount; clusterLoc++) {
			double clusterProb = _clusterReusedArr[clusterLoc];
			out_probDocCluster[docIndex][clusterLoc] = clusterProb;
		}

		return docLogLike;
	}

	public double[] getProbCluster() {
		return _probCluster;
	}
	
	public double[][] getProbClusterStem() {
		return _probClusterStem;
	}
	
	public double[][] getProbDocCluster() {
		return _probDocCluster;
	}
	
	public double[][] calcsRankDocCluster() {
		
		double[][] rankDocCluster = ArrayUtils.initArray2D(_docs.size(), _clusterCount);
		
		for (int docLoc=0; docLoc<_docs.size(); docLoc++) {
			
			CorpusDoc doc = _docs.getValueByIndex(docLoc);
			List<CorpusSentence> sentences = doc.getSentences();
			
			for (int sentenceLoc=0; sentenceLoc<sentences.size(); sentenceLoc++) {
				
				CorpusSentence sentence = sentences.get(sentenceLoc);
				List<CorpusPlace> places = sentence.getPlaces();
				
				for (int placeLoc=0; placeLoc<places.size(); placeLoc++) {
					
					CorpusPlace place = places.get(placeLoc);
					int stemLoc = place.getStemIndex();

					for (int clusterLoc=0; clusterLoc<_clusterCount; clusterLoc++) {
						
						double stemProb = _probClusterStem[clusterLoc][stemLoc];
						rankDocCluster[docLoc][clusterLoc] += stemProb;
					}
				}				
			}
		}
		
		return rankDocCluster;
	}

}
