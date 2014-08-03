package com.readrz.zzz.categ;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.readrz.zzz.ParsedPost;
import com.readrz.zzz.parse.matches.EntityMatch;

/**
 * Feature that indicates a mention of the Entity.
 *
 */
public final class EntityFeature extends AbstractFeature implements Feature, Serializable {

	private static final long serialVersionUID = 1L;

	private String _key;
	private List<String> _subKeys;
	private String[] _groupKeys;
	private Map<String, String> _attributes;
	private String[] _patterns;
	
	private transient double _evaluatedSignal;
	private transient Set<FeatureMatch> _evaluatedMatches;
	
	/**
	 * Needed for deserialization.
	 */
	public EntityFeature() {
		// nothing here
	}
	
	public EntityFeature(
			String key, String name, String[] groupKeys, 
			Map<String,String> attributes, String[] patterns) {

		_key = key;
		setName(name);
		_groupKeys = groupKeys;
		_attributes = attributes;
		_patterns = patterns;
	}
	
	public String getKey() {
		return _key;
	}
	
	public List<String> getSubKeys() {
		if (_subKeys == null) {
			_subKeys = new ArrayList<String>(1);
			_subKeys.add(_key);
		}
		return _subKeys;
	}
	
	public String[] getGroupKeys() {
		return _groupKeys;
	}
	
	public Map<String, String> getAttributes() {
		return _attributes;
	}
	
	public String getAttribute(String name) {
		return _attributes != null ? _attributes.get(name) : null;
	}
	
	public String[] getPatterns() {
		return _patterns;
	}
	
	public boolean evaluate(ParsedPost parsedPost) {

		final double entityMatchSignal = 1.0;
		
		_evaluatedSignal = 0.0;
		_evaluatedMatches = null;
		
		Map<EntityFeature,List<EntityMatch>> entityMatchMap = parsedPost.getEntityMatchMap();
		
		List<EntityMatch> entityMatches = entityMatchMap != null ? entityMatchMap.get(this) : null;
		
		if (entityMatches != null && entityMatches.size() > 0) {
			
			_evaluatedMatches = new HashSet<FeatureMatch>();
			
			for (int i=0; i<entityMatches.size(); i++) {
				
				EntityMatch entityMatch = entityMatches.get(i);
				
				FeatureMatch featureMatch = new FeatureMatch(
						this, 
						entityMatch.getMatchIndex(), 
						entityMatchSignal,
						null);
				
				_evaluatedMatches.add(featureMatch);
				
				if (_evaluatedSignal < entityMatchSignal) {
					_evaluatedSignal = entityMatchSignal;
				}
			}
		}

		if (_evaluatedSignal >= CategConstants.MIN_FEATURE_DETECTION_SIGNAL) {
			if (isUnpublished() == false) {
				parsedPost.addFeature(this, _evaluatedSignal, _evaluatedMatches);
			}
			return true;
		} else {
			return false;
		}
	}
	
	public Set<FeatureMatch> getEvaluatedMatches() {
		return _evaluatedMatches;
	}
	
	public double getEvaluatedSignal() {
		return _evaluatedSignal;
	}
	
	@Override
	public String toString() {
		return getKey();
	}

	public boolean isUnpublished() {
		// always publish entities
		return false;
	}

	public boolean isUncrossable() {
		// always cross entity features
		return false;
	}
}
