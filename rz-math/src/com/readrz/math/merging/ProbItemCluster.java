package com.readrz.math.merging;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.akuz.core.DateUtils;
import me.akuz.core.Index;
import me.akuz.core.Pair;
import me.akuz.core.PairComparator;
import me.akuz.core.SortOrder;
import me.akuz.core.StringUtils;
import me.akuz.core.math.WeightedAverage;
import me.akuz.core.sort.SelectK;

public final class ProbItemCluster<TItem extends ProbItem> implements MergeCluster<TItem>, Comparable<ProbItemCluster<TItem>> {

	private final static double EPSILON = 0.00000001;

	private final ProbItemClusterManager<TItem> _manager;
	private final Map<TItem, Double> _itemWeights;
	private double _itemSumWeight;
	private Date _minDate;
	private Date _maxDate;
	private final Map<Integer, WeightedAverage>  _allProbs;
	private List<Pair<Integer, Double>> _topProbsList;
	private Map<Integer, Double> _topProbsMap;
	private Object _tag;
	
	public ProbItemCluster(ProbItemClusterManager<TItem> manager) {
		_manager = manager;
		_itemWeights = new HashMap<>();
		_allProbs = new HashMap<>();
		_topProbsList = new ArrayList<>();
		_topProbsMap = new HashMap<>();
	}
	
	public int getItemCount() {
		return _itemWeights.size();
	}
	
	public Set<TItem> getItems() {
		return _itemWeights.keySet();
	}
	
	public final double rankItem(final TItem item) {
		
		if (_itemWeights.size() == 0) {
			throw new IllegalStateException("Cannot rank item against empty cluster");
		}
		
		if (DateUtils.minutesBetween(item.getDate(), _minDate) > _manager.getMaxMinutesDiffToMerge()) {
			return 0;
		}
		if (DateUtils.minutesBetween(_maxDate, item.getDate()) > _manager.getMaxMinutesDiffToMerge()) {
			return 0;
		}
		
		// select more efficient way to overlap
		final List<Pair<Integer, Double>> stemIndexList;
		final Map<Integer, Double> stemIndexMap;
		if (_topProbsList.size() < item.getStemIndexMap().size()) {
			stemIndexList = _topProbsList;
			stemIndexMap  = item.getStemIndexMap();
		} else {
			stemIndexList = item.getStemIndexList();
			stemIndexMap  = _topProbsMap;
		}
		
		// overlap stem probs
		double overlapStemWeight = 0;
		for (int i=0; i<stemIndexList.size(); i++) {
			
			final Pair<Integer, Double> pair1 = stemIndexList.get(i);
			final Integer stemIndex1 = pair1.v1();
			final double stemProb1 = pair1.v2().doubleValue();
			
			final Double stemProb2_obj = stemIndexMap.get(stemIndex1);
			if (stemProb2_obj != null) {
				final double stemProb2 = stemProb2_obj.doubleValue();
				overlapStemWeight += stemProb1 < stemProb2 ? stemProb1 : stemProb2;
			}
		}
		
		if (overlapStemWeight >= _manager.getOverlapStemWeightToMerge()) {
			return overlapStemWeight * _itemSumWeight;
		} else {
			return 0;
		}
	}
	
	public void addItem(TItem item) {
		
		if (item.getTag() != null) {
			throw new IllegalStateException("Item already assigned to a cluster");
		}
		
		if (_minDate == null || _minDate.compareTo(item.getDate()) > 0) {
			_minDate = item.getDate();
		}
		if (_maxDate == null || _maxDate.compareTo(item.getDate()) < 0) {
			_maxDate = item.getDate();
		}
		
		// merge stem indices
		Set<Integer> newStemIndexSet = new HashSet<>();
		newStemIndexSet.addAll(_allProbs.keySet());
		newStemIndexSet.addAll(item.getStemIndexSet());
		
		// calculate new probs
		SelectK<Integer, Double> selectTopStems = new SelectK<>(SortOrder.Desc, _manager.getTopStemCount());
		for (Integer stemIndex : newStemIndexSet) {
			
			WeightedAverage avg = _allProbs.get(stemIndex);
			if (avg == null) {
				avg = new WeightedAverage();
				if (_itemWeights.size() > 0) {
					avg.add(0, _itemSumWeight);
				}
				_allProbs.put(stemIndex, avg);
			}
			Double stemProb = item.getStemIndexMap().get(stemIndex);
			if (stemProb != null) {
				avg.add(stemProb.doubleValue(), item.getWeight());
			} else {
				avg.add(0.0, item.getWeight());
			}
			final double prob = avg.get();
			selectTopStems.add(new Pair<Integer, Double>(stemIndex, prob));
		}

		// update top probs
		updateTopProbs(selectTopStems.get());

		// keep the item
		_itemWeights.put(item, item.getWeight());
		_itemSumWeight += item.getWeight();
		item.setTag(this);
	}
	
	public void removeItem(TItem item) {
		
		if (item.getTag() != this) {
			throw new IllegalStateException("Item not assigned to this cluster");
		}
		
		if (_itemWeights.size() == 1) {
			
			_allProbs.clear();
			_topProbsList.clear();

		} else {
			
			// calculate new probs
			SelectK<Integer, Double> selectTopStems = new SelectK<>(SortOrder.Desc, _manager.getTopStemCount());
			List<Integer> removeStemIndices = new ArrayList<>();
			for (Integer stemIndex : _allProbs.keySet()) {
				WeightedAverage avg = _allProbs.get(stemIndex);
				Double stemProb = item.getStemIndexMap().get(stemIndex);
				if (stemProb != null) {
					avg.remove(stemProb, item.getWeight());
				} else {
					avg.remove(0.0, item.getWeight());
				}
				final double prob = avg.get();
				if (prob < -EPSILON) {
					throw new IllegalStateException("Removed more items than added before");
				}
				if (prob < +EPSILON) {
					removeStemIndices.add(stemIndex);
				} else {
					selectTopStems.add(new Pair<Integer, Double>(stemIndex, prob));
				}
			}
			
			// cleanup stems
			for (int i=0; i<removeStemIndices.size(); i++) {
				_allProbs.remove(removeStemIndices.get(i));
			}
	
			// update top probs
			updateTopProbs(selectTopStems.get());
		}

		// forget the item
		_itemWeights.remove(item);
		_itemSumWeight -= item.getWeight();
		item.setTag(null);
		
		// update min and max dates
		if (_itemWeights.size() == 0) {
			_minDate = null;
			_maxDate = null;
		} else if (
			_minDate.compareTo(item.getDate()) == 0 ||
			_maxDate.compareTo(item.getDate()) == 0) {
			
			_minDate = null;
			_maxDate = null;
			for (TItem remainingItem : _itemWeights.keySet()) {
				
				if (_minDate == null || _minDate.compareTo(remainingItem.getDate()) > 0) {
					_minDate = remainingItem.getDate();
				}
				if (_maxDate == null || _maxDate.compareTo(remainingItem.getDate()) < 0) {
					_maxDate = remainingItem.getDate();
				}
			}
		}
	}
	
	private void updateTopProbs(List<Pair<Integer, Double>> topStems) {

		_topProbsList = new ArrayList<>();
		_topProbsMap = new HashMap<>();

		if (topStems.size() > 0) {
			
			Set<Integer> topStemSet = new HashSet<>();
			for (int i=0; i<topStems.size(); i++) {
				topStemSet.add(topStems.get(i).v1());
			}
			
			// populate top probs
			for (Integer stemIndex : _allProbs.keySet()) {
				WeightedAverage avg = _allProbs.get(stemIndex);
				final double prob = avg.get();
				if (prob > _manager.getTopStemMinProb()) {
					if (topStemSet.contains(stemIndex) || prob >= _manager.getTopStemAlwaysProb()) {
						_topProbsList.add(new Pair<Integer, Double>(stemIndex, prob));
						_topProbsMap.put(stemIndex, prob);
					}
				}
			}
		}
	}
	
	public double getWeight() {
		return _itemSumWeight;
	}
	
	public Set<Integer> getStemIndexSet() {
		return _topProbsMap.keySet();
	}
	
	public List<Pair<Integer, Double>> getStemIndexList() {
		return _topProbsList;
	}
	
	public Map<Integer, Double> getStemIndexMap() {
		return _topProbsMap;
	}

	public Object getTag() {
		return _tag;
	}
	
	public void setTag(Object tag) {
		_tag = tag;
	}

	@Override
	public int compareTo(ProbItemCluster<TItem> o) {
		// descending by weight
		return - (int)Math.signum(_itemSumWeight - o._itemSumWeight);
	}

	public void print(StringBuilder sb, Index<String> stemsIndex) {
		
		DecimalFormat fmt = new DecimalFormat("0.0000");
		
		Collections.sort(_topProbsList, new PairComparator<Integer, Double>(SortOrder.Desc));
		
		for (int i=0; i<_topProbsList.size(); i++) {
			
			Pair<Integer, Double> pair2 = _topProbsList.get(i);
			Integer stemIndex = pair2.v1();
			Double stemProb = pair2.v2();
			
			String stem = stemsIndex.getValue(stemIndex);
			sb.append("     ");
			sb.append(StringUtils.trimOrFillSpaces(stem, 30));
			sb.append("  ");
			sb.append(fmt.format(stemProb));
			sb.append("\n");
		}
	}
}
