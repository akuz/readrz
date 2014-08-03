package com.readrz.math.topicevents;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

import me.akuz.core.ArrayUtils;
import me.akuz.core.logs.LogUtils;
import me.akuz.core.math.MatrixUtils;
import me.akuz.core.math.StatsUtils;
import me.akuz.core.math.VertDirMatrix;
import Jama.Matrix;

import com.readrz.lang.corpus.CorpusPlace;
import com.readrz.lang.corpus.CorpusSentence;

public final class DirEM {
	
	private static final double INIT_EVENT_VARIATION = 0.1;
	private static final double STOP_LOG_LIKE_CHANGE = 0.01;
	private static final int    MAX_ITERATIONS = 200;
	private static final double STEMS_ALPHA  = 0.0001;
	
	private final Logger _log;
	private final int _eventCount;
	private VertDirMatrix _stemEventProbs;
	private double[] _probEvent;
	
	public DirEM(List<CorpusSentence> sentences, int stemCount, int eventCount) {
		
		_log = LogUtils.getLogger(this.getClass().getName());
		_eventCount = eventCount;
		
		Random rnd = ThreadLocalRandom.current();
		
		_log.finest("Initialising...");
		Matrix mStemEventPrior = new Matrix(stemCount, eventCount, STEMS_ALPHA);
		Matrix mSumStemEventPrior = MatrixUtils.sumRows(mStemEventPrior);
		VertDirMatrix currStemEventProbs = new VertDirMatrix(mStemEventPrior, mSumStemEventPrior);
		VertDirMatrix prevStemEventProbs = new VertDirMatrix(mStemEventPrior, mSumStemEventPrior);
		double[][] currProbsSentenceEvent = new double[sentences.size()][];
		double[][] prevProbsSentenceEvent = new double[sentences.size()][];
		double[] currProbsEvent = new double[eventCount];
		double[] prevProbsEvent = new double[eventCount];
		for (int sentenceIndex=0; sentenceIndex<sentences.size(); sentenceIndex++) {
			
			currProbsSentenceEvent[sentenceIndex] = new double[eventCount];
			prevProbsSentenceEvent[sentenceIndex] = new double[eventCount];
			
			for (int eventIndex=0; eventIndex<eventCount; eventIndex++) {
				double prob = 1.0 / eventCount * (1.0 + INIT_EVENT_VARIATION * rnd.nextDouble());
				currProbsSentenceEvent[sentenceIndex][eventIndex] = prob;
			}
			StatsUtils.normalize(currProbsSentenceEvent[sentenceIndex]);
			
			CorpusSentence sentence = sentences.get(sentenceIndex);
			List<CorpusPlace> places = sentence.getPlaces();
			
			for (int eventIndex=0; eventIndex<eventCount; eventIndex++) {
				
				final double probSentenceEvent = currProbsSentenceEvent[sentenceIndex][eventIndex];

				currProbsEvent[eventIndex] += probSentenceEvent;

				for (int placeIndex=0; placeIndex<places.size(); placeIndex++) {
					
					CorpusPlace place = places.get(placeIndex);
					int stemIndex = place.getStemIndex();
					
					if (stemIndex >= 0) {
						currStemEventProbs.addObservation(stemIndex, eventIndex, probSentenceEvent);
					}
				}
			}
		}
		StatsUtils.normalize(currProbsEvent);
		
		int iter = 1;
		double prevLogLike = Double.NaN;
		double[] eventLogLikes = new double[eventCount];
		while (true) {
			
			_log.finest("Iteration " + iter + "...");
		
			{ // swap
				
				double[] tmp = prevProbsEvent;
				prevProbsEvent = currProbsEvent;
				currProbsEvent = tmp;
			}
			{ // swap
				
				double[][] tmp = prevProbsSentenceEvent;
				prevProbsSentenceEvent = currProbsSentenceEvent;
				currProbsSentenceEvent = tmp;
			}
			{ // swap
				
				VertDirMatrix tmp = prevStemEventProbs;
				prevStemEventProbs = currStemEventProbs;
				currStemEventProbs = tmp;
			}
			
			_log.finest("Expectation...");
			double currLogLike = 0;
			currStemEventProbs.resetMemory();
			for (int sentenceIndex=0; sentenceIndex<sentences.size(); sentenceIndex++) {

				CorpusSentence sentence = sentences.get(sentenceIndex);
				List<CorpusPlace> places = sentence.getPlaces();
				
				for (int eventIndex=0; eventIndex<eventCount; eventIndex++) {
					
					final double probSentenceEvent = prevProbsSentenceEvent[sentenceIndex][eventIndex];
					
					eventLogLikes[eventIndex] = Math.log(probSentenceEvent);
					
					for (int placeIndex=0; placeIndex<places.size(); placeIndex++) {
						
						CorpusPlace place = places.get(placeIndex);
						int stemIndex = place.getStemIndex();
						
						if (stemIndex >= 0) {
							currStemEventProbs.addObservation(stemIndex, eventIndex, probSentenceEvent);
							eventLogLikes[eventIndex] 
								+= Math.log(prevStemEventProbs.getProb(stemIndex, eventIndex));
						}
					}
				}
				
				double sentenceLogLike = StatsUtils.logSumExp(eventLogLikes);
				currLogLike += sentenceLogLike;
			}
			_log.finest("Log likelihood: " + currLogLike);
			
			_log.finest("Maximisation...");
			// calculate prior event loglikes
			for (int eventIndex=0; eventIndex<eventCount; eventIndex++) {
				eventLogLikes[eventIndex] = 0;
			}
			Arrays.fill(currProbsEvent, 0);
			for (int sentenceIndex=0; sentenceIndex<sentences.size(); sentenceIndex++) {
				
				CorpusSentence sentence = sentences.get(sentenceIndex);
				List<CorpusPlace> places = sentence.getPlaces();
				
				for (int eventIndex=0; eventIndex<eventCount; eventIndex++) {
					
					// start with event prior loglike
					currProbsSentenceEvent[sentenceIndex][eventIndex] 
							= eventLogLikes[eventIndex];
					
					for (int placeIndex=0; placeIndex<places.size(); placeIndex++) {
						
						CorpusPlace place = places.get(placeIndex);
						int stemIndex = place.getStemIndex();
						
						if (stemIndex >= 0) {
							currProbsSentenceEvent[sentenceIndex][eventIndex]
								+= Math.log(currStemEventProbs.getProb(stemIndex, eventIndex));
						}
					}
				}
				
				StatsUtils.logLikesToProbsReplace(currProbsSentenceEvent[sentenceIndex]);
				ArrayUtils.addToFirst(currProbsEvent, currProbsSentenceEvent[sentenceIndex]);
			}
			StatsUtils.normalize(currProbsEvent);
			
			_stemEventProbs = currStemEventProbs;
			_probEvent = currProbsEvent;

			if (iter >= MAX_ITERATIONS) {
				
				_log.finest("Max iterations reached, stopping...");
				break;
			}
			
			if (!Double.isNaN(prevLogLike) && (
				currLogLike < prevLogLike ||
				currLogLike - prevLogLike <= STOP_LOG_LIKE_CHANGE)) {

				_log.finest("Log likelihood converged, stopping...");
				break;
			}
			
			prevLogLike = currLogLike;
			iter++;
		}
	}
	
	public int getEventCount() {
		return _eventCount;
	}
	
	public double[] getProbsEvent() {
		return _probEvent;
	}
	
	public VertDirMatrix getStemEventProbs() {
		return _stemEventProbs;
	}

}
