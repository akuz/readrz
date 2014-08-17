package com.readrz.data.ontology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.akuz.core.HashIndex;
import me.akuz.core.Index;
import me.akuz.core.math.MatrixUtils;
import Jama.Matrix;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.readrz.data.index.KeysIndex;

/**
 * Topic model with topic probabilities and probabilities of stems in topics,
 * created from the previously saved topic entities.
 *
 */
public final class TopicModel {

	private final Index<String> _stemsIndex;
	private final Map<Integer, Integer> _stemIndexByKeyId;
	private final Map<Integer, Integer> _keyIdByStemIndex;
	private final List<Entity> _topicEntities;
	
	private final Matrix _mTopicProb;
	private final Matrix _mStemTopicProb;
	private final Matrix _mStemUncommonProb;
	
	public TopicModel(List<EntityList> topicEntityLists, KeysIndex keysIndex) {
		
		_stemsIndex = new HashIndex<>();
		_stemIndexByKeyId = new HashMap<>();
		_keyIdByStemIndex = new HashMap<>();
		_topicEntities = new ArrayList<>();
		
		List<Double> topicProbList = new ArrayList<>();
		List<Map<Integer, Double>> topicStemProbMapList = new ArrayList<>();
		List<Double> topicSumStemProbList = new ArrayList<>();
		
		// process entities
		double sumTopicProb = 0;
		for (int i=0; i<topicEntityLists.size(); i++) {
			
			EntityList topicEntityList = topicEntityLists.get(i);
			BasicDBList topicEntityDBList = topicEntityList.getList();
			if (topicEntityDBList != null) {
				
				for (int j=0; j<topicEntityDBList.size(); j++) {
					
					Entity topicEntity = new Entity((DBObject)topicEntityDBList.get(j));
					Double topicProb = topicEntity.getTopicProb();
					if (topicProb != null && topicProb > 0) {
						
						BasicDBList topicWordsDBList = topicEntity.getTopicWords();
						if (topicWordsDBList == null || topicWordsDBList.size() == 0) {
							throw new IllegalStateException("Entity " + topicEntity.getId() 
									+ " specifies topic prob, but has no topic words");
						}
						sumTopicProb += topicProb;
						double sumTopicStemProb = 0;
						Map<Integer, Double> stemProbMap = new HashMap<>();
						for (int k=0; k<topicWordsDBList.size(); k++) {
							
							TopicWord topicWord = new TopicWord((DBObject)topicWordsDBList.get(k));
							Double topicStemProb = topicWord.getProb();
							if (topicStemProb == null) {
								throw new IllegalStateException("Topic word " + topicWord.getStem() 
										+ " in topic entity " + topicEntity.getId() + " has no probability");
							}
							sumTopicStemProb += topicStemProb;
							Integer stemIndex = _stemsIndex.ensure(topicWord.getStem());
							Integer keyId = keysIndex.getId(topicWord.getStem());
							_stemIndexByKeyId.put(keyId, stemIndex);
							_keyIdByStemIndex.put(stemIndex, keyId);
							stemProbMap.put(stemIndex, topicStemProb);
						}
						
						_topicEntities.add(topicEntity);
						topicProbList.add(topicProb);
						topicStemProbMapList.add(stemProbMap);
						topicSumStemProbList.add(sumTopicStemProb);
					}
				}
			}
		}
		
		// check there are topics
		if (_topicEntities.size() == 0) {
			throw new IllegalStateException("Topic entities with positive probability not found in entity lists");
		}
		
		// create matrices
		_mTopicProb = new Matrix(_topicEntities.size(), 1);
		for (int i=0; i<_mTopicProb.getRowDimension(); i++) {
			_mTopicProb.set(i, 0, topicProbList.get(i) / sumTopicProb);
		}
		_mStemTopicProb = new Matrix(_stemsIndex.size(), _topicEntities.size());
		_mStemUncommonProb = new Matrix(_stemsIndex.size(), 1);
		for (int topicIndex=0; topicIndex<_mStemTopicProb.getColumnDimension(); topicIndex++) {
			
			double topicSumStemProb = topicSumStemProbList.get(topicIndex);
			Map<Integer, Double> topicStemProbMap = topicStemProbMapList.get(topicIndex);
			double missingStemProb = (1.0 - topicSumStemProb) / (_stemsIndex.size() - topicStemProbMap.size());
			
			for (int stemIndex=0; stemIndex<_mStemTopicProb.getRowDimension(); stemIndex++) {
				
				Double stemProb = topicStemProbMap.get(stemIndex);
				if (stemProb == null) {
					stemProb = missingStemProb;
				}
				_mStemTopicProb.set(stemIndex, topicIndex, stemProb);
				
				if (topicIndex > 0) {
					
					double topicProb = _mTopicProb.get(topicIndex, 0);
					double stemUncommonProb = _mStemUncommonProb.get(stemIndex, 0);
					stemUncommonProb += topicProb * stemProb;
					_mStemUncommonProb.set(stemIndex, 0, stemUncommonProb);
				}
			}
		}
	}
	
	public Entity getTopicEntity(int topicIndex) {
		return _topicEntities.get(topicIndex);
	}
	
	public Index<String> getStemsIndex() {
		return _stemsIndex;
	}
	
	public Integer getStemIndexByKeyId(Integer keyId) {
		return _stemIndexByKeyId.get(keyId);
	}
	
	public Integer getKeyIdByStemIndex(Integer stemIndex) {
		return _keyIdByStemIndex.get(stemIndex);
	}
	
	public Matrix getTopicProb() {
		return _mTopicProb;
	}
	
	public Matrix getStemTopicProb() {
		return _mStemTopicProb;
	}
	
	public Matrix getStemUncommonProb() {
		return _mStemUncommonProb;
	}

	public void validate() {
		
		Matrix mSumTopicProb = MatrixUtils.sumRows(_mTopicProb);
		if (Math.abs(mSumTopicProb.get(0, 0) - 1.0) > 0.0001) {
			throw new IllegalStateException("Topic probabilities do not sum up to one");
		}
		
		Matrix mSumStemTopicProb = MatrixUtils.sumRows(_mStemTopicProb);
		for (int j=0; j<mSumStemTopicProb.getColumnDimension(); j++) {
			if (Math.abs(mSumStemTopicProb.get(0, j) - 1.0) > 0.0001) {
				throw new IllegalStateException("Topic (index " + j + ") stems probabilities do not sum up to one");
			}
		}
	}

	public int getTopicCount() {
		return _mTopicProb.getRowDimension();
	}
	
	public int getStemCount() {
		return _stemsIndex.size();
	}

}
