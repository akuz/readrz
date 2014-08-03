package com.readrz.math.topicmodels;

import java.util.ArrayList;
import java.util.List;

import me.akuz.core.RepeatedValue;

public final class LDAVarEMParams {

	private int _topicCountFromAlpha;
	private int _topicCountFromBeta;
	private final List<RepeatedValue<Double>> _topicAlphas;
	private final List<RepeatedValue<Double>> _topicBetas;
	
	public LDAVarEMParams() {
		_topicAlphas = new ArrayList<RepeatedValue<Double>>();
		_topicBetas = new ArrayList<RepeatedValue<Double>>();
	}
	
	public void addAlphas(RepeatedValue<Double> multi) {
		_topicCountFromAlpha += multi.getCount();
		_topicAlphas.add(multi);
	}
	
	public void addTopicsBeta(RepeatedValue<Double> multi) {
		_topicCountFromBeta += multi.getCount();
		_topicBetas.add(multi);
	}
	
	public int getTopicCount() {
		if (_topicCountFromAlpha != _topicCountFromBeta) {
			throw new IllegalStateException("Number of topics is inconsistent, please check that you add Alpha and Beta parameters for the same number of topics in total");
		}
		return _topicCountFromAlpha;
	}
	
	public List<RepeatedValue<Double>> getAlphas() {
		return _topicAlphas;
	}
	
	public List<RepeatedValue<Double>> getTopicBetas() {
		return _topicBetas;
	}
}
