package com.readrz.math.topicdetect;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import me.akuz.core.Pair;
import me.akuz.core.SortOrder;
import me.akuz.core.logs.LogUtils;
import me.akuz.core.math.MatrixUtils;
import me.akuz.core.math.StatsUtils;
import me.akuz.core.sort.SelectK;
import Jama.Matrix;

import com.readrz.data.Snap;
import com.readrz.data.index.FwdHits;
import com.readrz.data.index.KeysIndex;
import com.readrz.data.ontology.TopicModel;
import com.readrz.lang.corpus.CorpusDoc;
import com.readrz.lang.corpus.CorpusPlace;
import com.readrz.lang.corpus.CorpusSentence;
import com.readrz.lang.corpus.TopicsCorpus;

/**
 * Provides functionality to calculate topic probabilities
 * for a document (and each word-places within a document).
 * 
 */
public final class TopicsAnalyser {

	private final Logger _log;
	private final TopicModel _topicModel;
	private final KeysIndex _keysIndex;
	private final Set<Integer> _stopKeyIds;
	private final int _collectTopTopicsCount;

	public TopicsAnalyser(
			TopicModel topicModel, 
			KeysIndex keysIndex, 
			Set<Integer> stopKeyIds, 
			int collectTopTopicsCount) {
		
		_log = LogUtils.getLogger(this.getClass().getName());
		_topicModel = topicModel;
		_keysIndex = keysIndex;
		_stopKeyIds = stopKeyIds;
		_collectTopTopicsCount = collectTopTopicsCount;
	}
	
	public TopicModel getTopicModel() {
		return _topicModel;
	}
	
	/**
	 * Creates corpus doc used for further topic detection.
	 */
	public CorpusDoc step0_createCorpusDoc(Snap snap, FwdHits fwdHits) {
		
		CorpusDoc doc = TopicsCorpus.createDocForTopics(
				snap, 
				fwdHits,
				false,
				_keysIndex, 
				_stopKeyIds, 
				_topicModel.getStemsIndex(),
				null, 
				false);
		
		return doc;
	}
	
	/**
	 * Calculate topic probs for each word-place, as well as for the whole document; 
	 * returns a corpus doc object which contains the output, with probs in tags:
	 * tags contain List<Pair<Integer, Double>> of top topic indices with probs.
	 */
	public void step1_calcTopicProbs(final CorpusDoc doc, final double alpha) {
		
		// init expectation optimization
		final double alphaMinus1 = alpha - 1.0;
		final int maxIterations = 100;
		final double stopDeltaLogLike = 0.01;
		double prevLogLike = Double.NaN;
		final Matrix mStemTopicProb = _topicModel.getStemTopicProb();
//		final double logLikeConst = 0;
//			= _topicModel.getTopicCount() * GammaFunction.lnGamma(alpha) 
//			- GammaFunction.lnGamma(alpha * _topicModel.getTopicCount());
//		_log.finest("LogLikeConst: " + logLikeConst);
		
		// initialize log likelihood
		double currLogLike = Double.NaN;
		
		// initialize document topic probs
		double[] mTopicProb = MatrixUtils.columnToArray(_topicModel.getTopicProb(), 0);
		doc.setTag(mTopicProb);

		// get doc sentences to iterate over
		List<CorpusSentence> sentences = doc.getSentences();
		
		int iter = 0;
		while (true) {

			// expectation
			currLogLike = 0;
			for (int topicIndex=0; topicIndex<mTopicProb.length; topicIndex++) {
				currLogLike += alphaMinus1 * Math.log(mTopicProb[topicIndex]);
			}
			for (int sentenceLoc=0; sentenceLoc<sentences.size(); sentenceLoc++) {
				
				CorpusSentence sentence = sentences.get(sentenceLoc);
				List<CorpusPlace> places = sentence.getPlaces();
				
				for (int j=0; j<places.size(); j++) {
					
					CorpusPlace place = places.get(j);
					if (place.getStemIndex() >= 0) {
					
						double[] placeTopicProb = (double[])place.getTag();
						if (placeTopicProb == null) {
							placeTopicProb = new double[_topicModel.getTopicCount()];
							place.setTag(placeTopicProb);
						}
						for (int topicIndex=0; topicIndex<placeTopicProb.length; topicIndex++) {
							double topicProb = mTopicProb[topicIndex];
							double stemTopicProb = mStemTopicProb.get(place.getStemIndex(), topicIndex);
							placeTopicProb[topicIndex] = Math.log(topicProb) + Math.log(stemTopicProb);
						}
						currLogLike += StatsUtils.logSumExp(placeTopicProb);
						StatsUtils.logLikesToProbsReplace(placeTopicProb);
					}
				}
			}
			_log.finest("LogLike: " + currLogLike);

			if (Double.isInfinite(currLogLike)) {
				_log.finest("Stop: infinite log likelihood");
				break;
			}
			
			if (!Double.isNaN(prevLogLike) && 
				(currLogLike < prevLogLike ||
				 Math.abs(currLogLike - prevLogLike) < stopDeltaLogLike)) {
				_log.finest("Stop: log like converged");
				break;
			}
			
			iter += 1;
			if (iter >= maxIterations) {
				_log.finest("Stop: max iterations (" + maxIterations + ")");
				break;
			}
			
			prevLogLike = currLogLike;
			
			// maximization
			Arrays.fill(mTopicProb, alpha);
			for (int sentenceLoc=0; sentenceLoc<sentences.size(); sentenceLoc++) {
				
				CorpusSentence sentence = sentences.get(sentenceLoc);
				List<CorpusPlace> places = sentence.getPlaces();
				
				for (int i=0; i<places.size(); i++) {
					
					CorpusPlace place = places.get(i);
					if (place.getStemIndex() >= 0) {

						double[] placeTopicProb = (double[])place.getTag();
						for (int topicIndex=0; topicIndex<placeTopicProb.length; topicIndex++) {
							mTopicProb[topicIndex] += placeTopicProb[topicIndex];
						}
					}
				}
			}
			StatsUtils.normalize(mTopicProb);
		}
		
		// collect doc top topic probs
		SelectK<Integer, Double> selectDocTopTopics = new SelectK<>(SortOrder.Desc, _collectTopTopicsCount);
		for (int topicIndex=0; topicIndex<mTopicProb.length; topicIndex++) {
			selectDocTopTopics.add(new Pair<Integer, Double>(topicIndex, mTopicProb[topicIndex]));
		}
		List<Pair<Integer, Double>> topicProbs = selectDocTopTopics.get();
		doc.setTag(topicProbs);
		
		// collect places top topic probs
		for (int sentenceLoc=0; sentenceLoc<sentences.size(); sentenceLoc++) {
			
			CorpusSentence sentence = sentences.get(sentenceLoc);
			List<CorpusPlace> places = sentence.getPlaces();
			
			for (int i=0; i<places.size(); i++) {
				
				CorpusPlace place = places.get(i);
				if (place.getStemIndex() >= 0) {
					
					double[] placeTopicProb = (double[])place.getTag();
	
					// collect place top topic probs
					SelectK<Integer, Double> selectPlaceTopTopics = new SelectK<>(SortOrder.Desc, _collectTopTopicsCount);
					for (int topicIndex=0; topicIndex<placeTopicProb.length; topicIndex++) {
						selectPlaceTopTopics.add(new Pair<Integer, Double>(topicIndex, placeTopicProb[topicIndex]));
					}
					List<Pair<Integer, Double>> placeTopicProbs = selectPlaceTopTopics.get();
					place.setTag(placeTopicProbs);
				}
			}
		}
	}
	
}
