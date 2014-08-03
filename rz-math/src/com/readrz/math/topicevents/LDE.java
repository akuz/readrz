package com.readrz.math.topicevents;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

import me.akuz.core.DoubleArraysCache;
import me.akuz.core.Pair;
import me.akuz.core.logs.LogUtils;
import me.akuz.core.math.JamaUtils;
import me.akuz.core.math.MatrixUtils;
import me.akuz.core.math.StatsUtils;
import me.akuz.core.math.VertDirMatrix;
import Jama.Matrix;

import com.readrz.data.ontology.TopicModel;
import com.readrz.lang.corpus.CorpusDoc;
import com.readrz.lang.corpus.CorpusPlace;
import com.readrz.lang.corpus.CorpusSentence;

/**
 * Latent Dirichlet Events via Expectation Maximization -
 * an EM inference algorithm detecting events in a corpus
 * by leveraging word frequencies from an existing topic model.
 * 
 */
public final class LDE {
	
	private static final double EVENT_DOC_INITIAL_VARIATION = 0.1;
	private static final double LOG_LIKE_CHANGE_TO_STOP = 0.001;
	private static final double MAX_ITERATIONS = 100;
	
	private final Logger _log;
	private final DoubleArraysCache _doubleArraysCache;
	private final int _eventCount;
	
	private VertDirMatrix _outEventProbs;
	private VertDirMatrix[] _outEventDocProbs;
	private VertDirMatrix[] _outTopicEventProbs;
	private VertDirMatrix[] _outStemTopicEventProbs;
		
	public LDE(
			TopicModel topicModel,
			final List<CorpusDoc> docs,
			final double eventConcentration,
			final double eventDocConcentration,
			final double topicEventConcentration,
			final double stemTopicEventConcentration,
			final int eventCount) {
		
		if (eventCount < 2) {
			throw new IllegalArgumentException("Event count must be >= 2");
		}
		
		_log = LogUtils.getLogger(this.getClass().getName());
		_doubleArraysCache = new DoubleArraysCache(2);
		_eventCount = eventCount;

		_log.finest("Initializing data structures...");

		double[] eventDocLogLikes = new double[eventCount];
		
		// init event priors
		Matrix mEventPrior = new Matrix(eventCount, 1, eventConcentration);
		Matrix mSumEventPrior = MatrixUtils.sumRows(mEventPrior);

		// init event prob arrays
		VertDirMatrix mCurrEventProbs = new VertDirMatrix(mEventPrior, mSumEventPrior);
		VertDirMatrix mPrevEventProbs = new VertDirMatrix(mEventPrior, mSumEventPrior);
		
		// init event|doc priors
		Matrix mEventDocPrior = new Matrix(eventCount, 1, eventDocConcentration);
		Matrix mSumEventDocPrior = MatrixUtils.sumRows(mEventDocPrior);

		// init event|doc prob arrays
		VertDirMatrix[] mCurrEventDocProbs = new VertDirMatrix[docs.size()];
		VertDirMatrix[] mPrevEventDocProbs = new VertDirMatrix[docs.size()];
		for (int docIndex=0; docIndex<docs.size(); docIndex++) {
			mCurrEventDocProbs[docIndex] = new VertDirMatrix(mEventDocPrior, mSumEventDocPrior);
			mPrevEventDocProbs[docIndex] = new VertDirMatrix(mEventDocPrior, mSumEventDocPrior);
		}
		
		// calculate events priors
		Matrix mTopicEventPrior = (Matrix)topicModel.getTopicProb().clone();
		JamaUtils.timesReplace(mTopicEventPrior, topicEventConcentration);
		Matrix mSumTopicEventPrior = MatrixUtils.sumRows(mTopicEventPrior);
		Matrix mStemTopicEventPrior = (Matrix)topicModel.getStemTopicProb().clone();
		JamaUtils.timesReplace(mStemTopicEventPrior, stemTopicEventConcentration);
		Matrix mSumStemTopicEventPrior = MatrixUtils.sumRows(mStemTopicEventPrior);
		
		// init event-specific prob arrays
		VertDirMatrix[] mCurrTopicEventProbs = new VertDirMatrix[eventCount];
		VertDirMatrix[] mPrevTopicEventProbs = new VertDirMatrix[eventCount];
		VertDirMatrix[] mCurrStemTopicEventProbs = new VertDirMatrix[eventCount];
		VertDirMatrix[] mPrevStemTopicEventProbs = new VertDirMatrix[eventCount];
		for (int eventIndex=0; eventIndex<eventCount; eventIndex++) {
			mCurrTopicEventProbs[eventIndex] = new VertDirMatrix(mTopicEventPrior, mSumTopicEventPrior);
			mPrevTopicEventProbs[eventIndex] = new VertDirMatrix(mTopicEventPrior, mSumTopicEventPrior);
			mCurrStemTopicEventProbs[eventIndex] = new VertDirMatrix(mStemTopicEventPrior, mSumStemTopicEventPrior);
			mPrevStemTopicEventProbs[eventIndex] = new VertDirMatrix(mStemTopicEventPrior, mSumStemTopicEventPrior);
		}
		
		_log.finest("Initialising events...");

		// initial allocation of documents to event
		// using a simple online allocation technique
		{
			Random rnd = ThreadLocalRandom.current();
			for (int docIndex=0; docIndex<docs.size(); docIndex++) {

				for (int eventIndex=0; eventIndex<eventCount; eventIndex++) {
					eventDocLogLikes[eventIndex] += 1.0 / eventCount * (1.0 + EVENT_DOC_INITIAL_VARIATION * rnd.nextDouble());
				}
				StatsUtils.normalize(eventDocLogLikes);
				
				for (int eventIndex=0; eventIndex<eventCount; eventIndex++) {
					mCurrEventDocProbs[docIndex].addObservation(eventIndex, 0, eventDocLogLikes[eventIndex]);
					mCurrEventProbs.addObservation(eventIndex, 0, eventDocLogLikes[eventIndex]);
				}
			}
		}
		
		// EM loop
		int iter = 0;
		double prevLogLike = Double.NaN;
		while (true) {
			
			_log.finest("Iteration " + (iter+1) + "...");

			// exchange parameters
			{
				VertDirMatrix _PrevEventProbs = mPrevEventProbs;
				mPrevEventProbs = mCurrEventProbs;
				mCurrEventProbs = _PrevEventProbs;
			}
			{
				VertDirMatrix[] _PrevEventDocProbs = mPrevEventDocProbs;
				mPrevEventDocProbs = mCurrEventDocProbs;
				mCurrEventDocProbs = _PrevEventDocProbs;
			}
			{
				VertDirMatrix[] _PrevTopicEventProbs = mPrevTopicEventProbs;
				mPrevTopicEventProbs = mCurrTopicEventProbs;
				mCurrTopicEventProbs = _PrevTopicEventProbs;
				for (int eventIndex=0; eventIndex<eventCount; eventIndex++) {
					mCurrTopicEventProbs[eventIndex].resetMemory();
				}
			}
			{
				VertDirMatrix[] _PrevStemTopicEventProbs = mPrevStemTopicEventProbs;
				mPrevStemTopicEventProbs = mCurrStemTopicEventProbs;
				mCurrStemTopicEventProbs = _PrevStemTopicEventProbs;
				for (int eventIndex=0; eventIndex<eventCount; eventIndex++) {
					mCurrStemTopicEventProbs[eventIndex].resetMemory();
				}
			}
			
			_log.finest("Expectation...");

			double currLogLike = 0;
			for (int docIndex=0; docIndex<docs.size(); docIndex++) {

				CorpusDoc doc = docs.get(docIndex);
				
				final double docLogLike 
					= addDocToExpectation(
						doc,
						mPrevEventProbs,
						mPrevEventDocProbs[docIndex], 
						mCurrTopicEventProbs, 
						mCurrStemTopicEventProbs);
				
				currLogLike += docLogLike;
			}
			
			_log.finest("Loglike: " + currLogLike);

			_log.finest("Maximization...");

			mCurrEventProbs.resetMemory();
			for (int docIndex=0; docIndex<docs.size(); docIndex++) {

				CorpusDoc doc = docs.get(docIndex);
				
				calcEventDocLogLikes(
						doc,
						mCurrTopicEventProbs,
						mCurrStemTopicEventProbs, 
						eventDocLogLikes);
				
				StatsUtils.logLikesToProbsReplace(eventDocLogLikes);
				
				mCurrEventDocProbs[docIndex].resetMemory();
				
				for (int eventIndex=0; eventIndex<eventCount; eventIndex++) {
					
					mCurrEventDocProbs[docIndex].addObservation(eventIndex, 0, eventDocLogLikes[eventIndex]);
					mCurrEventProbs.addObservation(eventIndex, 0, eventDocLogLikes[eventIndex]);
				}
			}
			
			_outEventProbs = mCurrEventProbs;
			_outEventDocProbs = mCurrEventDocProbs;
			_outTopicEventProbs = mCurrTopicEventProbs;
			_outStemTopicEventProbs = mCurrStemTopicEventProbs;

			// check iterations
			iter += 1;
			if (iter >= MAX_ITERATIONS) {
				_log.finest("Max iterations threshold reached, stopping");
				break;
			}

			// check for convergence
			if (!Double.isNaN(prevLogLike) && 
				(currLogLike < prevLogLike ||
				 Math.abs(currLogLike - prevLogLike) < LOG_LIKE_CHANGE_TO_STOP)) {
				_log.finest("Log likelihood converged, stopping");
				break;
			}
			
			prevLogLike = currLogLike;
		}
	}

	@SuppressWarnings("unchecked")
	public final void calcEventDocLogLikes(
			CorpusDoc doc,
			VertDirMatrix[] topicEventProbs,
			VertDirMatrix[] stemTopicEventProbs,
			double[] outEventDocLogLikes) {
		
		for (int eventIndex=0; eventIndex<_eventCount; eventIndex++) {

			// init event|doc loglike
			outEventDocLogLikes[eventIndex] = 0;
			
			// loop through doc sentences
			List<CorpusSentence> sentences = doc.getSentences();
			for (int sentenceIndex=0; sentenceIndex<sentences.size(); sentenceIndex++) {
				
				CorpusSentence sentence = sentences.get(sentenceIndex);
				List<CorpusPlace> places = sentence.getPlaces();
				
				// loop through sentence places
				for (int placeIndex=0; placeIndex<places.size(); placeIndex++) {
					
					CorpusPlace place = places.get(placeIndex);
					int placeStemIndex = place.getStemIndex();
					List<Pair<Integer, Double>> placeTopicProbs = (List<Pair<Integer, Double>>)place.getTag();
					
					// if place has topics
					if (placeTopicProbs != null) {
						
						double[] placeTopicLogLikes = _doubleArraysCache.get(placeTopicProbs.size());
						
						for (int i=0; i<placeTopicProbs.size(); i++) {
							
							// get place topic prob
							Pair<Integer, Double> pair = placeTopicProbs.get(i);
							Integer placeTopicIndex = pair.v1();
							Double placeTopicProb = pair.v2();
							
							// calc place log like
							double placeTopicLogLike 
								= Math.log(placeTopicProb)
								+ Math.log(topicEventProbs[eventIndex].getProb(placeTopicIndex, 0))
								+ Math.log(stemTopicEventProbs[eventIndex].getProb(placeStemIndex, placeTopicIndex));
							
							// remember place topic log like
							placeTopicLogLikes[i] = placeTopicLogLike;
						}
						
						// calc and add 
						double placeLogLike = StatsUtils.logSumExp(placeTopicLogLikes);
						outEventDocLogLikes[eventIndex] += placeLogLike;
						
						_doubleArraysCache.ret(placeTopicLogLikes);
					}
				}
			}			
		}
	}

	@SuppressWarnings("unchecked")
	public final double addDocToExpectation(
			CorpusDoc doc,
			VertDirMatrix eventProbs,
			VertDirMatrix eventDocProbs,
			VertDirMatrix[] outEventTopicProb,
			VertDirMatrix[] outEventStemTopicProb) {
		
		double[] eventLogLikes = _doubleArraysCache.get(_eventCount);
		
		for (int eventIndex=0; eventIndex<_eventCount; eventIndex++) {
			
			eventLogLikes[eventIndex] = 0;
			eventLogLikes[eventIndex] += Math.log(eventProbs.getProb(eventIndex, 0));
			eventLogLikes[eventIndex] += Math.log(eventDocProbs.getProb(eventIndex, 0));

			// loop through doc sentences
			List<CorpusSentence> sentences = doc.getSentences();
			for (int sentenceIndex=0; sentenceIndex<sentences.size(); sentenceIndex++) {
				
				CorpusSentence sentence = sentences.get(sentenceIndex);
				List<CorpusPlace> places = sentence.getPlaces();
				
				// loop through sentence places
				for (int placeIndex=0; placeIndex<places.size(); placeIndex++) {
					
					CorpusPlace place = places.get(placeIndex);
					int placeStemIndex = place.getStemIndex();
					List<Pair<Integer, Double>> placeTopicProbs = (List<Pair<Integer, Double>>)place.getTag();
					
					// if place has topics
					if (placeTopicProbs != null) {
						
						double[] topicLogLikes = _doubleArraysCache.get(placeTopicProbs.size());
						
						for (int i=0; i<placeTopicProbs.size(); i++) {
							
							// get place topic prob
							Pair<Integer, Double> pair = placeTopicProbs.get(i);
							Integer placeTopicIndex = pair.v1();
							Double placeTopicProb = pair.v2();

							// observed value
							double value = eventProbs.getProb(eventIndex, 0) * eventDocProbs.getProb(eventIndex, 0) * placeTopicProb;
							topicLogLikes[i] = Math.log(value);

							// add observations
							outEventTopicProb[eventIndex].addObservation(placeTopicIndex, 0, value);
							outEventStemTopicProb[eventIndex].addObservation(placeStemIndex, placeTopicIndex, value);
						}
						
						final double placeLogLike = StatsUtils.logSumExp(topicLogLikes);
						eventLogLikes[eventIndex] += placeLogLike;
						_doubleArraysCache.ret(topicLogLikes);
					}
				}
			}			
		}
		
		final double docLogLike = StatsUtils.logSumExp(eventLogLikes);
		_doubleArraysCache.ret(eventLogLikes);
		return docLogLike;
	}
	
	public int getEventCount() {
		return _eventCount;
	}
	
	public VertDirMatrix getEventProbs() {
		return _outEventProbs;
	}
	
	public VertDirMatrix[] getEventDocProbs() {
		return _outEventDocProbs;
	}
	
	public VertDirMatrix[] getTopicEventProbs() {
		return _outTopicEventProbs;
	}
	
	public VertDirMatrix[] getStemTopicEventProbs() {
		return _outStemTopicEventProbs;
	}
	
}
