package com.readrz.lang.corpus;

import com.readrz.data.index.FwdHit;

public final class CorpusPlace implements Cloneable {
	
	private final int _stemIndex;
	private final int _wordIndex;
	private final FwdHit _fwdHit;
	private Object _tag;
	
	public CorpusPlace(int stemIndex, int wordIndex) {
		this(stemIndex, wordIndex, null);
	}
	
	public CorpusPlace(int stemIndex, int wordIndex, FwdHit fwdHit) {
		_stemIndex = stemIndex;
		_wordIndex = wordIndex;
		_fwdHit = fwdHit;
	}
	
	public int getStemIndex() {
		return _stemIndex;
	}
	
	public int getWordIndex() {
		return _wordIndex;
	}
	
	public FwdHit getFwdHit() {
		return _fwdHit;
	}
	
	public Object getTag() {
		return _tag;
	}
	
	public void setTag(Object tag) {
		_tag = tag;
	}
	
	@Override
	public CorpusPlace clone() {
		try {
			return (CorpusPlace)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError("Clone not supported");
		}
	}
}
