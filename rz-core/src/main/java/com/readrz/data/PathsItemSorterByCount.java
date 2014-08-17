package com.readrz.data;

import java.util.Comparator;

public final class PathsItemSorterByCount implements Comparator<PathsItem> {

	@Override
	public int compare(PathsItem o1, PathsItem o2) {
		int cmp = o1.getCount() - o2.getCount();
		return - cmp; // desc
	}

}
