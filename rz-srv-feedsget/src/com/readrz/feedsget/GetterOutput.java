package com.readrz.feedsget;

import java.util.ArrayList;
import java.util.List;

import com.readrz.data.Snap;

public final class GetterOutput {

	private final List<Snap> _snaps;
	
	public GetterOutput() {
		_snaps = new ArrayList<>();
	}
	public void addSnap(Snap snap) {
		_snaps.add(snap);
	}
	public int getSnapCount() {
		return _snaps.size();
	}
	public List<Snap> getSnaps() {
		return _snaps;
	}
}
