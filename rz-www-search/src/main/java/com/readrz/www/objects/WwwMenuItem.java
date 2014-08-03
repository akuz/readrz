package com.readrz.www.objects;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import me.akuz.core.Pair;
import me.akuz.core.math.StatsUtils;

import com.readrz.www.UrlBuilder;

public final class WwwMenuItem {
	
	private int _level;
	private String _text;
	private Integer _count;
	private UrlBuilder _url;
	private List<WwwMenuItem> _children;
	private final Set<String> _tags;
	private boolean _isActive;
	private boolean _isChildActive;
	private boolean _isTaxonomy;
	private boolean _turnsMenuOn;
	private boolean _isExpanded;
	
	public WwwMenuItem(String text, Integer count, UrlBuilder url) {
		_text = text;
		_count = count;
		_url = url;
		_tags = new HashSet<>();
	}
	
	public int getLevel() {
		return _level;
	}
	private void setLevelThisOnly(int level) {
		_level = level;
		setTag("level" + level, true);
	}
	private void setLevelAndUpdateChildren(int level) {
		
		// update this
		setLevelThisOnly(level);
		
		// update children
		if (_children != null && _children.size() > 0) {
			Queue<Pair<WwwMenuItem, Integer>> queue = new LinkedList<>();
			for (int i=0; i<_children.size(); i++) {
				queue.add(new Pair<WwwMenuItem, Integer>(_children.get(i), level + 1));
			}
			while (queue.size() > 0) {
				Pair<WwwMenuItem, Integer> pair = queue.poll();
				WwwMenuItem childItem = pair.v1();
				Integer childLevel = pair.v2();
				
				// update child
				childItem.setLevelThisOnly(childLevel);
				
				if (childItem._children != null && childItem._children.size() > 0) {
					for (int j=0; j<childItem._children.size(); j++) {
						queue.add(new Pair<WwwMenuItem, Integer>(childItem._children.get(j), childLevel + 1));
					}
				}
			}
		}
	}
	
	public String getText() {
		return _text;
	}
	public void setText(String text) {
		_text = text;
	}
	
	public Integer getCount() {
		return _count;
	}
	public void addCount(Integer add) {
		if (add == null) {
			return;
		}
		if (_count == null) {
			_count = add;
		} else {
			_count = _count + add;
		}
	}
	
	public double getSize() {
		return StatsUtils.log2(2 + _count);
	}
	
	public UrlBuilder getUrl() {
		return _url;
	}
	public void setUrl(UrlBuilder url) {
		_url = url;
	}
	
	public boolean getTurnsMenuOn() {
		return _turnsMenuOn;
	}
	public void setTurnsMenuOn(boolean flag) {
		_turnsMenuOn = flag;
		setTag("turnsMenuOn", flag);
	}
	
	public boolean getIsTaxonomy() {
		return _isTaxonomy;
	}
	public void setIsTaxonomy(boolean flag) {
		_isTaxonomy = flag;
		setTag("isTaxonomy", flag);
	}
	
	public boolean getIsActive() {
		return _isActive;
	}
	public void setIsActive(boolean flag) {
		_isActive = flag;
		setTag("isActive", flag);
	}
	
	public boolean getIsChildActive() {
		return _isChildActive;
	}
	public void setIsChildActive(boolean flag) {
		_isChildActive = flag;
		setTag("isChildActive", flag);
	}
	
	public boolean getIsExpanded() {
		return _isExpanded;
	}
	public void setIsExpanded(boolean flag) {
		_isExpanded = flag;
		setTag("isExpanded", flag);
	}
	
	public void addChild(WwwMenuItem menuItem) {
		if (_children == null) {
			_children = new ArrayList<>();
		}
		_children.add(menuItem);
		
		setTag("hasChildren", true);
		menuItem.setLevelAndUpdateChildren(_level + 1);
	}
	
	public int getChildCount() {
		return _children != null ? _children.size() : 0;
	}
	
	public List<WwwMenuItem> getChildren() {
		return _children;
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
