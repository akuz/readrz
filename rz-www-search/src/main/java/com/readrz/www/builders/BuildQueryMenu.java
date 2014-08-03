package com.readrz.www.builders;

import java.util.List;

import me.akuz.core.StringUtils;

import com.readrz.search.Query;
import com.readrz.search.QueryTerm;
import com.readrz.www.RzPar;
import com.readrz.www.UrlBuilder;
import com.readrz.www.objects.WwwMenu;
import com.readrz.www.objects.WwwMenuItem;

public final class BuildQueryMenu {

	public static final WwwMenu fromQuery(Query query, UrlBuilder baseUrl) {
		
		WwwMenu menu = new WwwMenu(query.getQueryString(), null);
		WwwMenuItem rootMenuItem = menu.getRootItem();
		
		if (!query.getIsEmpty()) {
			
			List<QueryTerm> terms = query.getTerms();
			for (int i=0; i<terms.size(); i++) {
				
				// get term info
				QueryTerm term = terms.get(i);
				String displayText = term.getBetterWord();
				String queryString = term.getOrigWord();
				
				// build term url
				UrlBuilder url = baseUrl.clone();
				url.update(RzPar.parQuery, queryString);
				
				// create term menu item
				WwwMenuItem menuItem = new WwwMenuItem(displayText, null, url);
				rootMenuItem.addChild(menuItem);
				
				// build removal url
				UrlBuilder removeUrl = baseUrl.clone();
				String queryStringAfterRemoval = extractQueryStringAfterTermRemoval(query, term);
				if (queryStringAfterRemoval == null || queryStringAfterRemoval.length() == 0) {
					removeUrl.remove(RzPar.parQuery);
				} else {
					removeUrl.update(RzPar.parQuery, queryStringAfterRemoval);
				}
				
				// create removal menu item
				WwwMenuItem removeMenuItem = new WwwMenuItem("x", null, removeUrl);
				menuItem.addChild(removeMenuItem);
			}
			
			if (rootMenuItem.getChildCount() > 1) {
				
				// create parent for clear menu item
				WwwMenuItem menuItem = new WwwMenuItem(null, null, null);
				rootMenuItem.addChild(menuItem);
				
				// build clear url
				UrlBuilder clearUrl = baseUrl.clone();
				clearUrl.remove(RzPar.parQuery);
				
				// create clear menu item
				WwwMenuItem clearMenuItem = new WwwMenuItem("Clear", null, clearUrl);
				menuItem.addChild(clearMenuItem);
			}
		}
		
		return menu;
	}
	
	private final static String extractQueryStringAfterTermRemoval(Query query, QueryTerm removedTerm) {
		
		StringBuilder sb = null;
		
		List<QueryTerm> terms = query.getTerms();
		
		for (int i=0; i<terms.size(); i++) {

			QueryTerm term = terms.get(i);
			if (term != removedTerm) {
				
				if (sb == null) {
					sb = new StringBuilder();
				}
				StringUtils.appendIfNotEmpty(sb, " ");
				sb.append(term.getOrigWord());
			}
		}
		
		return sb == null ? null : sb.toString();
	}
}
