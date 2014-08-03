package com.readrz.zzz.categ;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class AbstractFeature implements Feature {
	
	private GroupFeature _parentGroup;
	private String _name;

	protected AbstractFeature() {
		// needed for deserialization
	}
	
	public String getFullName() {
		
		StringBuilder sb = new StringBuilder();

		sb.append(_name);
		
		String topName = getTopName();
		if (topName != null) {
			sb.append(" (");
			sb.append(topName);
			sb.append(")");
		}

		return sb.toString();
	}
	
	public String getTopName() {
		if (_parentGroup == null) {
			return null;
		} else if (_parentGroup.getParentGroup() == null) {
			return null;
		} else if (_parentGroup.getParentGroup().getParentGroup() == null) {
			return _parentGroup.getName();
		} else {
			return _parentGroup.getTopName();
		}
	}

	public String getName() {
		return _name;
	}
	
	protected void setName(String name) {
		_name = name;
	}
	
	public GroupFeature getParentGroup() {
		return _parentGroup;
	}
	
	public void setParentGroup(GroupFeature parentGroup) {
		if (_parentGroup == null) {
			// only set once, to remember first parent by priority
			_parentGroup = parentGroup;
		}
	}
	
	public GroupFeature getParentNonTaxonomyGroup() {
		
		GroupFeature loopGroup = _parentGroup;
		
		while (loopGroup != null) {
			
			if (loopGroup.isTaxonomy() == false) {
				return loopGroup;
			}
			
			loopGroup = loopGroup.getParentGroup();
		}
		
		return null;
	}
	
	/**
	 * Returns full key, and fills a list of sub keys in sorted order.
	 */
	protected final static String prepareKeys(Set<String> keySet, List<String> keysToFill) {
		
		keysToFill.addAll(keySet);
		Collections.sort(keysToFill);

		StringBuilder sb = new StringBuilder();
		for (int i=0; i<keysToFill.size(); i++) {
			if (i > 0) {
				sb.append("+");
			}
			sb.append(keysToFill.get(i));
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o instanceof AbstractFeature == false) {
			return false;
		}
		AbstractFeature other = (AbstractFeature)o;
		return other.getKey().equals(getKey());
	}
}
