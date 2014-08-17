package com.readrz.data.ontology;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.akuz.core.Pair;
import me.akuz.core.SortOrder;
import me.akuz.core.sort.SelectK;

import com.readrz.data.index.KeysIndex;

import Jama.Matrix;

/**
 * Topic model arranged by key ids, with limit on top key ids count.
 *
 */
public final class TopicModelKeyIds {
	
	private final Map<Integer, Map<Integer, Double>> _topKeyIdsByTopicKeyId;
	private final Map<Integer, Double> _uncommonTopKeyIds;
	
	public TopicModelKeyIds(TopicModel topicModel, int maxTopKeyIds, KeysIndex keysIndex) {
		{
			_topKeyIdsByTopicKeyId = new HashMap<>();
			Matrix mTopic = topicModel.getTopicProb();
			Matrix mStemTopic = topicModel.getStemTopicProb();

			for (int topicIndex=0; topicIndex<mTopic.getRowDimension(); topicIndex++) {
				
				Entity topicEntity = topicModel.getTopicEntity(topicIndex);
				Integer topicKeyId = topicEntity.getKeyId();
				
				SelectK<Integer, Double> selectTopKeyIds = new SelectK<>(SortOrder.Desc, maxTopKeyIds);
				for (int stemIndex=0; stemIndex<mStemTopic.getRowDimension(); stemIndex++) {
					
					Integer keyId = topicModel.getKeyIdByStemIndex(stemIndex);
					double prob = mStemTopic.get(stemIndex, topicIndex);
					selectTopKeyIds.add(new Pair<Integer, Double>(keyId, prob));
				}
				
				List<Pair<Integer, Double>> topKeyIdsList = selectTopKeyIds.get();
				Map<Integer, Double> topKeyIds = new HashMap<>();
				for (int i=0; i<topKeyIdsList.size(); i++) {
					
					Pair<Integer, Double> pair = topKeyIdsList.get(i);
					Integer keyId = pair.v1();
					Double prob = pair.v2();
					
					topKeyIds.put(keyId, prob);
				}
				
				_topKeyIdsByTopicKeyId.put(topicKeyId, topKeyIds);
			}
		}
		{
			Matrix mStemUncommon = topicModel.getStemUncommonProb();
				
			SelectK<Integer, Double> selectTopKeyIds = new SelectK<>(SortOrder.Desc, maxTopKeyIds);
			for (int stemIndex=0; stemIndex<mStemUncommon.getRowDimension(); stemIndex++) {
				
				Integer keyId = topicModel.getKeyIdByStemIndex(stemIndex);
				double prob = mStemUncommon.get(stemIndex, 0);
				selectTopKeyIds.add(new Pair<Integer, Double>(keyId, prob));
			}
			
			List<Pair<Integer, Double>> topKeyIdsList = selectTopKeyIds.get();
			Map<Integer, Double> topKeyIds = new HashMap<>();
			for (int i=0; i<topKeyIdsList.size(); i++) {
				
				Pair<Integer, Double> pair = topKeyIdsList.get(i);
				Integer keyId = pair.v1();
				Double prob = pair.v2();
				
				topKeyIds.put(keyId, prob);
			}
			
			_uncommonTopKeyIds = topKeyIds;
		}
	}
	
	public Map<Integer, Double> getTopKeyIds(Integer topicKeyId) {
		
		if (topicKeyId != null) {
			Map<Integer, Double> topKeyIds = _topKeyIdsByTopicKeyId.get(topicKeyId);
			if (topKeyIds != null) {
				return topKeyIds;
			} else {
				return _uncommonTopKeyIds;
			}
		} else {
			return _uncommonTopKeyIds;
		}
	}

}
