package com.readrz.data.index;

import me.akuz.core.Hit;

/**
 * Forward index hit (including key id).
 *
 */
public final class FwdHit implements Comparable<FwdHit> {

	private final int _keyId;
	private final Hit _hit;
	
	public FwdHit(final int keyId, final Hit hit) {
		_keyId = keyId;
		_hit = hit;
	}
	
	public final int getKeyId() {
		return _keyId;
	}
	
	public final Hit getHit() {
		return _hit;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		FwdHit other = (FwdHit)obj;
		return _keyId == other._keyId &&
			_hit.equals(other._hit);
	}
	
	@Override
	public int hashCode() {
		return _keyId + 11 * _hit.hashCode();
	}
	
	@Override
	public String toString() {
		return String.format("%d:%s", _keyId, _hit.toString());
	}

	@Override
	public int compareTo(FwdHit o) {
		return _hit.compareTo(o._hit);
	}
}
