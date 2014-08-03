package com.readrz.summr;

import java.util.Comparator;

import me.akuz.core.Triple;

import com.readrz.data.AliveStatus;
import com.readrz.data.SummId;

public final class SummIdSorter implements Comparator<Triple<Boolean, AliveStatus, SummId>>{

	@Override
	public int compare(Triple<Boolean, AliveStatus, SummId> triple1, Triple<Boolean, AliveStatus, SummId> triple2) {
		
		Boolean isUser1 = triple1.v1();
		Boolean isUser2 = triple1.v1();
		
		if (isUser1 && !isUser2) {
			return -1; // user requests are more important
		}
		if (!isUser1 && isUser2) {
			return +1; // user requests are more important
		}
		
		AliveStatus aliveStatus1 = triple1.v2();
		AliveStatus aliveStatus2 = triple2.v2();
		
		if (aliveStatus1.equals(AliveStatus.Dead) && aliveStatus2.equals(AliveStatus.Dead) == false) {
			return -1; // dead is higher priority
		}
		if (aliveStatus1.equals(AliveStatus.Dead) == false && aliveStatus2.equals(AliveStatus.Dead)) {
			return +1; // dead is higher priority
		}
		
		SummId summId1 = triple1.v3();
		SummId summId2 = triple2.v3();

		{ // smaller kind id has higher priority

			int cmp = summId1.getKindId() - summId2.getKindId();
			if (cmp != 0) {
				return cmp;
			}
		}

		{ // more search terms => higher priority (easier to calculate)

			int cmp = - (summId1.getSearchKeyIds().size() - summId2.getSearchKeyIds().size());
			if (cmp != 0) {
				return cmp;
			}
		}

		{ // more groups => higher priority (easier to calculate)

			int cmp = - (summId1.getGroupKeyIds().size() - summId2.getGroupKeyIds().size());
			if (cmp != 0) {
				return cmp;
			}
		}

		// shorter period has higher priority
		return summId1.getPeriodId() - summId2.getPeriodId();
	}

}
