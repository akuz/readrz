package com.readrz.data;

import com.mongodb.DBObject;

public final class SummMenu {
	
	private final SummMenuItem _rootItem;
	
	public SummMenu(String rootName, Integer rootCount) {
		_rootItem = new SummMenuItem(rootName, null, rootCount);
		_rootItem.isTaxonomy(true);
	}
	
	public SummMenu(DBObject dbo) {
		_rootItem = new SummMenuItem(dbo);
	}
	
	public DBObject getDbo() {
		return _rootItem.getDbo();
	}
	
	public SummMenuItem getRootItem() {
		return _rootItem;
	}
	
	public void addChild(SummMenuItem item) {
		_rootItem.addChild(item);
	}
}
