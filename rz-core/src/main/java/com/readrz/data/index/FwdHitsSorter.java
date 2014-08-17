package com.readrz.data.index;

import java.util.Comparator;

public final class FwdHitsSorter implements Comparator<FwdHit> {

	@Override
	public int compare(FwdHit o1, FwdHit o2) {
		return o1.compareTo(o2);
	}

}
