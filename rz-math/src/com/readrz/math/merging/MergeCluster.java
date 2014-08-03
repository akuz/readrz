package com.readrz.math.merging;

public interface MergeCluster<TItem> {
	
	double rankItem(TItem item);
	
	void addItem(TItem item);
	
	void removeItem(TItem item);
	
	int getItemCount();
}
