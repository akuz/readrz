package com.readrz.zzz.report;

import java.io.Serializable;

public final class Word implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	// keeping member names short 
	// for small serialized size:
	private String    t; // text
	private Sentiment s; // sentiment
	private Emphasis  e; // emphasis
	private Highlight h; // highlight

	/**
	 * Needed for deserialization.
	 */
	public Word() {
	}

	public Word(String text) {
		t = text;
	}

	public String getText() {
		return t;
	}

	public void setSentiment(Sentiment sentiment) {
		s = sentiment;
	}
	
	public void setEmphasis(Emphasis emphasis) {
		e = emphasis;
	}
	
	public void setHighlight(Highlight highlight) {
		h = highlight;
	}
	
	public boolean getIsEm() {
		return e != null && Emphasis.ON.equals(e);
	}
	
	public boolean getIsHi() {
		return h != null && Highlight.ON.equals(h);
	}
	
	public boolean getIsPos() {
		return s != null && Sentiment.POS.equals(s);
	}
	
	public boolean getIsNeg() {
		return s != null && Sentiment.NEG.equals(s);
	}

}
