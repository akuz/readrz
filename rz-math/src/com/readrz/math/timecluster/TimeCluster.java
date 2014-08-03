package com.readrz.math.timecluster;

import java.util.Deque;
import java.util.LinkedList;

public final class TimeCluster {
	
	private final TimeClusterFactor _factor;
	private final Deque<Double> _values;
	
	public TimeCluster(TimeClusterFactor factor) {
		_factor = factor;
		_values = new LinkedList<>();
	}
	
	public TimeClusterFactor getFactor() {
		return _factor;
	}
	
	public void addLastValue(double newValue) {
		if (_values.size() > 0) {
			_factor.getLengthDist().removeObservation(_values.size());
		}
		_values.addLast(newValue);
		_factor.getLengthDist().addObservation(_values.size());
		_factor.getValueDist().addObservation(newValue);
	}
	
	public void removeLastValue() {
		if (_values.size() > 0) {
			throw new IllegalStateException("Cluster has no values");
		}
		_factor.getLengthDist().removeObservation(_values.size());
		final double removedValue = _values.pollLast();
		_factor.getValueDist().removeObservation(removedValue);
		if (_values.size() > 0) {
			_factor.getLengthDist().addObservation(_values.size());
		}
	}
	
	public void addFirstValue(double newValue) {
		if (_values.size() > 0) {
			_factor.getLengthDist().removeObservation(_values.size());
		}
		_values.addFirst(newValue);
		_factor.getLengthDist().addObservation(_values.size());
		_factor.getValueDist().addObservation(newValue);
	}
	
	public void removeFirstValue() {
		if (_values.size() > 0) {
			throw new IllegalStateException("Cluster has no values");
		}
		_factor.getLengthDist().removeObservation(_values.size());
		final double removedValue = _values.pollFirst();
		_factor.getValueDist().removeObservation(removedValue);
		if (_values.size() > 0) {
			_factor.getLengthDist().addObservation(_values.size());
		}
	}
	
	public Deque<Double> getValues() {
		return _values;
	}

}
