package com.readrz.search;

import java.util.Date;

import com.mongodb.DBCollection;
import com.readrz.data.AliveStatus;
import com.readrz.data.Summ;
import com.readrz.data.SummRequest;
import com.readrz.data.SummResult;

public final class SummSearcher {
	
	private static DBCollection _summColl;
	private static DBCollection _summreqColl;

	public SummSearcher(DBCollection summColl, DBCollection summreqColl) {
		_summColl = summColl;
		_summreqColl = summreqColl;
	}
	
	public SummResult findOne(byte[] idData) {
		return SummResult.findOne(_summColl, idData);
	}

	public void request(SummRequest summRequest) {
		summRequest.upsertUnacknowledged(_summreqColl);
	}

	public final boolean checkAliveOrRequest(byte[] idData, Date now) {
		
		AliveStatus aliveStatus = SummResult.findAliveStatus(_summColl, idData, now);

		// if summary is dying or dead
		if (aliveStatus.equals(AliveStatus.Dying) ||
			aliveStatus.equals(AliveStatus.Dead)) {
			
			// request summary to be recalculated
			SummRequest summRequest = new SummRequest(idData, true);
			request(summRequest);
		}

		return aliveStatus.equals(AliveStatus.Dead) == false;
	}

	public final Summ findAliveOrRequest(byte[] idData, Date now) {
		
		// try find by id
		SummResult summResult = findOne(idData);
		
		// check alive
		if (summResult != null) {
			
			// check if alive
			AliveStatus aliveStatus = summResult.getAliveStatus(now);

			// if summary is dying or dead
			if (aliveStatus.equals(AliveStatus.Dying) ||
				aliveStatus.equals(AliveStatus.Dead)) {
				
				// request summary to be recalculated
				SummRequest summRequest = new SummRequest(idData, true);
				request(summRequest);
			}
			
			// don't return dead summary
			if (aliveStatus.equals(AliveStatus.Dead)) {
				return null;
			} else {
				return summResult.getSumm();
			}
			
		} else {
			
			// request summary to be calculated
			SummRequest summRequest = new SummRequest(idData, true);
			request(summRequest);

			return null;
		}
	}
	
}
