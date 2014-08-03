package com.readrz.www;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.akuz.core.Pair;
import me.akuz.core.PairComparator;
import me.akuz.core.SortOrder;

import org.bson.types.ObjectId;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.mongodb.BasicDBList;
import com.readrz.data.SnapInfo;
import com.readrz.data.SummListItem;
import com.readrz.data.SummListItemQuote;
import com.readrz.data.WwwSumm;
import com.readrz.http.JsonResponse;
import com.readrz.search.QueryKeyIds;
import com.readrz.www.facades.FacadeSearcher;

public final class HistoryServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Logger _log;

	@Override
	public void init() throws ServletException {
		super.init();
		_log = Log.getLogger(HistoryServlet.class);
	}
	
	@Override
	public void destroy() {
		super.destroy();
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		
		try {
			
			List<Pair<SummListItemQuote, Date>> quotes = new ArrayList<>();
			
			// get ids from the request
			List<ObjectId> snapIds = RzPar.getObjectIdListOptional(req, RzPar.parIds);
			if (snapIds != null && snapIds.size() > 0) {
				
				QueryKeyIds queryKeyIds = new QueryKeyIds();
				{
					List<Integer> searchKeyIds = RzPar.getIntegerListOptional(req, RzPar.parSearchKeyIds);
					if (searchKeyIds != null) {
						queryKeyIds.addSentenceKeyIds(searchKeyIds);
					}
				}
				{
					List<Integer> groupKeyIds = RzPar.getIntegerListOptional(req, RzPar.parSearchKeyIds);
					if (groupKeyIds != null) {
						queryKeyIds.addDocumentKeyIds(groupKeyIds);
					}
				}
				
				// load snap infos
				for (int i=0; i<snapIds.size(); i++) {
					SnapInfo snapInfo = FacadeSearcher.get().findSnapInfo(snapIds.get(i));
					if (snapInfo != null) {
						
						// create quote
						SummListItemQuote quote = new SummListItemQuote(queryKeyIds, snapInfo.getSnap(), snapInfo.getSource());
						quotes.add(new Pair<SummListItemQuote, Date>(quote, snapInfo.getSnap().getSrcDate()));
					}
				}
			}
			
			// sort quotes
			if (quotes.size() > 1) {
				Collections.sort(quotes, new PairComparator<SummListItemQuote, Date>(SortOrder.Desc));
			}
			
			// create results
			WwwSumm wwwSumm = new WwwSumm();
			BasicDBList list = new BasicDBList();
			for (int i=0; i<quotes.size(); i++) {
				SummListItem item = new SummListItem(1, quotes.get(i).v1().getDbo(), null, null);
				list.add(item.getDbo());
			}
			wwwSumm.setList(list);
			
			// write results
			JsonResponse jsonResp = JsonResponse.createOkReady(null, wwwSumm.getDbo());
	    	HttpResponseUtils.writeJsonUtf8Response(resp, jsonResp.toString());
			
		} catch (Exception ex) {
			
			_log.warn("Could not process request", ex);
			JsonResponse jsonResp = JsonResponse.createNotOk("Could not get history, error: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
	    	HttpResponseUtils.writeJsonUtf8Response(resp, jsonResp.toString());
		}
	}

}
