package com.readrz.zzz.categ.load;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Definition of a group feature (loaded from configuration).
 *
 */
public class GroupFeatureDef implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String _key;
	private String _name;
	private boolean _isUncrossable;
	private boolean _isTaxonomy;
	private boolean _isSecondaryTaxonomy;
	private boolean _isIgnoredForFullName;
	private List<String> _featureKeys;
	private List<GroupFeatureDef> _childGroups;
	
	/**
	 * Default constructor is needed for deserialization.
	 */
	public GroupFeatureDef() {
	}
	
	public GroupFeatureDef(String key, String name, boolean isTaxonomy) {
		_key = key;
		_name = name;
		_isTaxonomy = isTaxonomy;
		_featureKeys = new ArrayList<String>();
		_childGroups = new ArrayList<GroupFeatureDef>();
	}
	
	public String getKey() {
		return _key;
	}
	
	public String getName() {
		return _name;
	}

	public boolean isUncrossable() {
		return _isUncrossable;
	}
	
	public boolean isTaxonomy() {
		return _isTaxonomy;
	}
	
	public boolean isSecondaryTaxonomy() {
		return _isSecondaryTaxonomy;
	}
	
	public boolean isIgnoredForFullName() {
		return _isIgnoredForFullName;
	}
	
	public List<String> getFeatureKeys() {
		return _featureKeys;
	}
	
	public List<GroupFeatureDef> getChildGroups() {
		return _childGroups;
	}
	
}
