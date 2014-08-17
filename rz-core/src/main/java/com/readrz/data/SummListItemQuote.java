package com.readrz.data;

import java.util.Date;

import me.akuz.core.DateUtils;
import me.akuz.core.Pair;
import me.akuz.core.UtcDate;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.readrz.search.QueryKeyIds;
import com.readrz.search.QuoteExtractor;

public final class SummListItemQuote {

	private final static String _idField         = "id";
	private final static String _sourceField     = "source";
	private final static String _dateAgoField    = "dateAgo";
	private final static String _dateShortField  = "dateShort";
	private final static String _titleQuoteField = "titleQuote";
	private final static String _textQuoteField  = "textQuote";
	private final static String _hasImageField   = "hasImage";
	private final static String _urlField        = "url";
	
	private final DBObject _dbo;
	
	public SummListItemQuote(QueryKeyIds queryKeyIds, Snap snap, Source source) {
		
		_dbo = new BasicDBObject();
		
		_dbo.put(_idField, snap.getId().toString());
		_dbo.put(_sourceField, source.getName());
		
		Date snapDate = snap.getSrcDate();
		String dateAgo = DateUtils.formatAgo(snapDate, new Date());
		_dbo.put(_dateAgoField, dateAgo);
		
		String dateShort = new UtcDate(snapDate, UtcDate.ShortDateFormatString).toString();
		_dbo.put(_dateShortField, dateShort);

		QuoteExtractor quoteExtractor = new QuoteExtractor(queryKeyIds);
		Pair<String, String> pair = quoteExtractor.extractQuote(snap);
		
		_dbo.put(_titleQuoteField, pair.v1());
		_dbo.put(_textQuoteField, pair.v2());
		
		if (snap.isScannedImage()) {
			_dbo.put(_hasImageField, true);
		}

		String actualUrl = snap.getActualUrl();
		if (actualUrl != null) {
			_dbo.put(_urlField, actualUrl);
		} else {
			_dbo.put(_urlField, snap.getUrl());
		}
	}
	
	public DBObject getDbo() {
		return _dbo;
	}
	
	public String getId() {
		return (String)_dbo.get(_idField);
	}
	
	public String getSource() {
		return (String)_dbo.get(_sourceField);
	}
	
	public String getDateAgo() {
		return (String)_dbo.get(_dateAgoField);
	}
	
	public boolean hasImage() {
		Boolean has = (Boolean)_dbo.get(_hasImageField);
		return has != null && has.booleanValue();
	}
	
	public String getUrl() {
		return (String)_dbo.get(_urlField);
	}
	
	public String getTitleQuote() {
		return (String)_dbo.get(_titleQuoteField);
	}
	
	public String getTextQuote() {
		return (String)_dbo.get(_textQuoteField);
	}
	
}
