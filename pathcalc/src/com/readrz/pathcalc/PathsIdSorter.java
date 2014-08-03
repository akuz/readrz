package com.readrz.pathcalc;

import java.util.Comparator;

import me.akuz.core.Triple;

import com.readrz.data.AliveStatus;
import com.readrz.data.PathsId;

public final class PathsIdSorter implements Comparator<Triple<Boolean, AliveStatus, PathsId>>{

	@Override
	public int compare(Triple<Boolean, AliveStatus, PathsId> o1, Triple<Boolean, AliveStatus, PathsId> o2) {
		
		Boolean isUser1 = o1.v1();
		Boolean isUser2 = o1.v1();
		
		AliveStatus aliveStatus1 = o1.v2();
		AliveStatus aliveStatus2 = o2.v2();
		
		PathsId pathsId1 = o1.v3();
		PathsId pathsId2 = o2.v3();
		
		if (isUser1 && !isUser2) {
			return -1; // user requests are more important
		}
		if (!isUser1 && isUser2) {
			return +1; // user requests are more important
		}
		
		if (pathsId1.getQueryKeyIds().isEmpty() && !pathsId2.getQueryKeyIds().isEmpty()) {
			return -1; // overall paths are more important
		}
		if (!pathsId1.getQueryKeyIds().isEmpty() && pathsId2.getQueryKeyIds().isEmpty()) {
			return +1; // overall paths are more important
		}
		
		if (aliveStatus1.equals(AliveStatus.Dead) && aliveStatus2.equals(AliveStatus.Dying)) {
			return -1; // dead is higher priority
		}
		if (aliveStatus1.equals(AliveStatus.Dying) && aliveStatus2.equals(AliveStatus.Dead)) {
			return +1; // dead is higher priority
		}

		// shorter period has higher priority
		return pathsId1.getPeriodId() - pathsId2.getPeriodId();
	}

}
