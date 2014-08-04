package com.readrz.summr;

import java.util.Comparator;

import com.readrz.data.SummListItemSource;

public final class SummItemMemberSorter implements Comparator<SummListItemSource> {

	@Override
	public int compare(SummListItemSource member1, SummListItemSource member2) {
		
		// ascending alphabetically by source name
		return member1.getSourceName().compareTo(member2.getSourceName());
	}

}
