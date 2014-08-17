package com.readrz.data;

import java.util.Date;

public interface SnapsListener {
	
	void onSnap(Date currDate, Snap snap);
	
}
