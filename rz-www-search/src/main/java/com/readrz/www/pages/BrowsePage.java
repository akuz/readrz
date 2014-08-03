package com.readrz.www.pages;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.akuz.core.StringUtils;
import me.akuz.core.Triple;

import org.bson.types.ObjectId;

import com.readrz.data.Feed;
import com.readrz.data.Period;
import com.readrz.data.Snap;
import com.readrz.data.Source;
import com.readrz.data.SummListItemQuote;
import com.readrz.data.ontology.GroupInfo;
import com.readrz.lang.parse.PreprocessUtils;
import com.readrz.search.Query;
import com.readrz.search.QueryKeyIds;
import com.readrz.www.OG;
import com.readrz.www.RzPar;
import com.readrz.www.RzUrls;
import com.readrz.www.UrlBuilder;
import com.readrz.www.facades.FacadeQueryParser;
import com.readrz.www.facades.FacadeSearcher;

public class BrowsePage extends AnyFrontPage {

	private final UrlBuilder _loadUrl;
	private final UrlBuilder _blockUrl;
	private SummListItemQuote _quote;
	private UrlBuilder _quoteUrl;
	
	public BrowsePage(HttpServletRequest req, HttpServletResponse resp) {
		
		super(req, resp);
		
		StringBuilder blockTitle = new StringBuilder();
		StringBuilder blockDescription = new StringBuilder();
		blockDescription.append("Summary of news");
		
		// parse query
		String queryString = req.getParameter(RzPar.parQuery);
		Query query = FacadeQueryParser.get().parse(queryString);
		if (!query.getIsEmpty()) {
			
			String capQueryString = StringUtils.capitalizeIfNoCaps(query.getQueryString());
			
			StringUtils.appendIfNotEmpty(blockTitle, " &amp; ");
			blockTitle.append("&ldquo;" + capQueryString + "&rdquo;");
			blockDescription.append(" matching search &ldquo;" + capQueryString + "&rdquo;");
		}
		
		// parse group infos
		List<GroupInfo> groupInfos = RzPar.getGroupInfos(req, RzPar.parGroupIds);
		if (groupInfos != null && groupInfos.size() > 0) {
			blockDescription.append(" in section" + (groupInfos.size() > 1 ? "s" : "") + " ");
			for (int i=0; i<groupInfos.size(); i++) {
				GroupInfo groupInfo = groupInfos.get(i);
				StringUtils.appendIfNotEmpty(blockTitle, " &amp; ");
				blockTitle.append(groupInfo.getGroup().getName());
				if (i > 0) {
					blockDescription.append(" &amp; ");
				}
				blockDescription.append(groupInfo.getGroup().getName());
			}
		}
		
		// parse period
		Period period = RzPar.parseAndCheckPeriod(req, getTopPeriod());
		StringUtils.appendIfNotEmpty(blockTitle, " - ");
		blockTitle.append(period.getName());
		blockDescription.append(" during the last " + period.getName() + ".");
		
		// append readrz
		StringUtils.appendIfNotEmpty(blockTitle, " - ");
		blockTitle.append("Readrz");

		// create load/block urls
		_loadUrl = RzPar.createBrowseUrl(RzUrls.summary, period, query, groupInfos);
		_blockUrl = RzPar.createBrowseUrl(RzUrls.browse, period, query, groupInfos);
		
		// prepare open graph info
		OG og = null;
		String domain = "http://www.readrz.com";
		String defaultImage = "/img/logo/rz_100_white.png";
		
		// extract quote, if snap is specified
		String snapIdStr = RzPar.getStringOptional(req, RzPar.parSnapId);
		if (snapIdStr != null && snapIdStr.length() > 0) {

			// parse snap id
			ObjectId snapId = new ObjectId(snapIdStr);
			
			// find quote snap
			Triple<Snap, Feed, Source> triple = FacadeSearcher.get().findSnapFeedSource(snapId);
			if (triple != null) {
				
				String snapQueryStr = RzPar.getStringOptional(req, RzPar.parSnapQuery);
				Query snapQuery = FacadeQueryParser.get().parse(snapQueryStr);
				QueryKeyIds quoteQeryKeyIds = snapQuery.getQueryKeyIds().clone();
				
				if (groupInfos != null) {
					for (int i=0; i<groupInfos.size(); i++) {
						GroupInfo groupInfo = groupInfos.get(i);
						quoteQeryKeyIds.addSenCheckKeyId(groupInfo.getGroup().getKeyId());
					}
				}
				
				// create quote and quote url
				_quote = new SummListItemQuote(quoteQeryKeyIds, triple.v1(), triple.v3());
				_quoteUrl = RzPar.createBrowseUrl(RzUrls.browse, period, query, groupInfos, snapId, snapQuery);
				
				// open graph
				og = new OG();
				og.setTitle(PreprocessUtils.removeTags(_quote.getTitleQuote()).trim() + " - " + _quote.getSource() + " - via Readrz");
				if (_quote.hasImage()) {
					og.setImage(domain + "/image?id=" + _quote.getId() + "&kind=2");
				} else {
					og.setImage(domain + defaultImage);
				}
				og.setType("article");
				og.setUrl(domain + _quoteUrl.toString());
				og.setSiteName("Readrz");
				og.setDescription(PreprocessUtils.removeTags(_quote.getTextQuote()).trim());
			}
		}
		if (og == null) {
			
			// open graph
			og = new OG();
			og.setTitle(blockTitle.toString());
			og.setImage(domain + defaultImage);
			og.setType("article");
			og.setUrl(domain + _blockUrl.toString());
			og.setSiteName("Readrz");
			og.setDescription(blockDescription.toString());
		}
		setOg(og);
	}
	
	public UrlBuilder getLoadUrl() {
		return _loadUrl;
	}
	
	public SummListItemQuote getQuote() {
		return _quote;
	}
	
	public UrlBuilder getQuoteUrl() {
		return _quoteUrl;
	}
}
