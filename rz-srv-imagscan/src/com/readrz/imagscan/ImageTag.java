package com.readrz.imagscan;

import me.akuz.core.Hit;

public final class ImageTag {
	
	private final String _src;
	private final String _alt;
	private Hit _hit;
	
	public ImageTag(String src, String alt, Hit hit) {
		_hit = hit;
		_src = src;
		_alt = alt;
	}
	
	public String getSrc() {
		return _src;
	}
	
	public String getAlt() {
		return _alt;
	}
	
	public Hit getHit() {
		return _hit;
	}

	public void setHit(Hit hit) {
		_hit = hit;
	}
}
