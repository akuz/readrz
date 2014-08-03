package com.readrz.zzz.categ;

import java.util.List;
import java.util.Set;

import com.readrz.zzz.ParsedPost;

/**
 * Feature that can be detected in a text.
 * In this class, feature definition interface is bundled together
 * with an interface needed for feature detection, so that we can
 * reuse the definition objects for doing actual work.
 * 
 */
public interface Feature {
	
	/**
	 * Globally unique key of the feature.
	 */
	String getKey();
	
	/**
	 * List of the crossed features, if it's a cross feature, 
	 * or just with a list with one entry equal to key.
	 */
	List<String> getSubKeys();
	
	/**
	 * Display name of the feature (regardless of hierarchy).
	 */
	String getName();
	
	/**
	 * Hierarchical full name of the feature.
	 */
	String getFullName();
	
	/**
	 * Name of the top level group in the hierarchy.
	 */
	String getTopName();
	
	/**
	 * Get parent group feature, if any.
	 */
	GroupFeature getParentGroup();
	
	/**
	 * Set parent feature.
	 */
	void setParentGroup(GroupFeature parent);
	
	/**
	 * True for the features that are detected but not published.
	 */
	boolean isUnpublished();
	
	/**
	 * True for features that cannot be crossed with other features.
	 */
	boolean isUncrossable();
	
	/**
	 * Keys of groups to which this feature should be added; only valid for leaf features.
	 */
	String[] getGroupKeys();

	/**
	 * Evaluate feature on a given parsed post; 
	 * returns true if feature was detected.
	 * @param parsedPost
	 */
	boolean evaluate(ParsedPost parsedPost);
	
	/**
	 * Get evaluated feature matches.
	 */
	Set<FeatureMatch> getEvaluatedMatches();
	
	/**
	 * Get evaluated feature signal.
	 */
	double getEvaluatedSignal();
}
