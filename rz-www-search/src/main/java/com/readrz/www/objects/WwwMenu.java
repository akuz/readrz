package com.readrz.www.objects;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.readrz.www.UrlBuilder;

public final class WwwMenu {
	
	private boolean _isActive;
	private final WwwMenuItem _rootItem;
	private final Set<String> _tags;
	
	public WwwMenu() {
		this(null, null, null);
	}
	public WwwMenu(String rootText) {
		this(rootText, null, null);
	}
	public WwwMenu(String rootText, Integer rootCount) {
		this(rootText, rootCount, null);
	}
	public WwwMenu(String rootText, Integer rootCount, UrlBuilder rootUrl) {
		_rootItem = new WwwMenuItem(rootText, rootCount, rootUrl);
		_rootItem.setIsTaxonomy(true);
		_tags = new HashSet<>();
	}
	
	public boolean getIsEmpty() {
		return _rootItem.getChildCount() == 0;
	}
	
	public WwwMenuItem getRootItem() {
		return _rootItem;
	}
	
	public int getItemCount() {
		return _rootItem.getChildCount();
	}
	
	public List<WwwMenuItem> getItems() {
		return _rootItem.getChildren();
	}
	
	public void addItem(WwwMenuItem menuItem) {
		_rootItem.addChild(menuItem);
	}
	
	public boolean getIsActive() {
		return _isActive;
	}
	public void setIsActive(boolean is) {
		_isActive = is;
	}

	public Set<String> getTags() {
		return _tags;
	}
	public void setTag(String tag, boolean flag) {
		if (flag) {
			_tags.add(tag);
		} else {
			_tags.remove(tag);
		}
	}
}
