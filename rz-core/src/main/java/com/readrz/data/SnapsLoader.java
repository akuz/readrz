package com.readrz.data;

import java.util.Date;
import java.util.concurrent.CountDownLatch;

import com.mongodb.BasicDBList;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

public final class SnapsLoader {

	private volatile boolean _stopRequested;
	private final CountDownLatch _stopped;
	private final SnapsListener[] _listeners;

	public SnapsLoader(SnapsListener[] listeners) {
		if (listeners == null || listeners.length == 0) {
			throw new IllegalArgumentException("Listeners collection must not be empty");
		}
		_stopped = new CountDownLatch(1);
		_listeners = listeners;
	}
	
	public void stopAsync() {
		_stopRequested = true;
	}
	
	public void stop() {
		_stopRequested = true;
		try {
			_stopped.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("Waiting for the " + getClass().getSimpleName() + " to stop was interupted.");
		}
	}
	
	public void load(
			DBCollection coll,
			Date minDateInc, 
			Date maxDateExc,
			BasicDBList extraConditions) {
		
		try {
			
			System.out.println("Creating db cursor to get " + Snap.class.getSimpleName() +"s...");
			DBCursor cursor = Snap.selectBetweenDatesAsc(coll, minDateInc, maxDateExc, extraConditions);
	
			int counter = 0;
			System.out.println("Reading " + Snap.class.getSimpleName() + "s from db...");
			try {
				
				while (cursor.hasNext()) {
					
					if (_stopRequested) {
						System.out.println(getClass().getSimpleName() + " stop requested...");
						break;
					}
					
					Snap snap = new Snap(cursor.next());
					
					for (int i=0; i<_listeners.length; i++) {
						_listeners[i].onSnap(snap.getSrcDate(), snap);
					}

					counter += 1;
					
					if (counter % 10000 == 0) {
						System.out.println("Read " + counter + " " + Snap.class.getSimpleName() +"s.");
					}
				}
				
			} finally {
				cursor.close();
			}
			
			System.out.println("Read a total of " + counter + " " + Snap.class.getSimpleName() + "s.");
			
		} finally {
			_stopped.countDown();
		}
	}

}
