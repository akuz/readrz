package com.readrz.www;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.readrz.data.Period;
import com.readrz.data.Summ;
import com.readrz.data.SummIdBuilder;
import com.readrz.data.SummKind;
import com.readrz.data.SummMenu;
import com.readrz.data.SummMenuItem;
import com.readrz.data.WwwSumm;
import com.readrz.data.ontology.Group;
import com.readrz.data.ontology.GroupInfo;
import com.readrz.http.JsonResponse;
import com.readrz.search.Query;
import com.readrz.www.facades.FacadeQueryParser;
import com.readrz.www.facades.FacadeSummSearcher;

public final class SummaryServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Logger _log;

	@Override
	public void init() throws ServletException {
		super.init();
		_log = Log.getLogger(SummaryServlet.class);
	}
	
	@Override
	public void destroy() {
		super.destroy();
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		
		try {
			
			Date now = new Date();
			
			// parse period
			Period topPeriod = RzPar.getTopPeriodOfUser(req);
			Period period = RzPar.parseAndCheckPeriod(req, topPeriod);

			// parse query
			Query query = FacadeQueryParser.get().parse(RzPar.getStringOptional(req, RzPar.parQuery));
			List<Integer> searchKeyIds = new ArrayList<>();
			searchKeyIds.addAll(query.getQueryKeyIds().getSentenceKeyIds());
			searchKeyIds.addAll(query.getQueryKeyIds().getSenCheckKeyIds());
			searchKeyIds.addAll(query.getQueryKeyIds().getDocumentKeyIds());
			
			// get group infos
			List<GroupInfo> groupInfos = RzPar.getGroupInfos(req, RzPar.parGroupIds);
			List<Integer> groupKeyIds = new ArrayList<>();
			for (int i=0; i<groupInfos.size(); i++) {
				groupKeyIds.add(groupInfos.get(i).getGroup().getKeyId());
			}
			
			// check parent menus alive
			boolean parentMenusAlive = true;
			List<byte[]> parentMenuSummIds = new ArrayList<>();
			if (groupInfos.size() > 0) {
				
				List<Integer> rollingGroupKeyIds = new ArrayList<>();
				
				for (int i=0; i<groupInfos.size(); i++) {
					
					// build parent menu summary id
					SummIdBuilder sib = new SummIdBuilder(SummKind.Menu.getId(), period.getId());
					sib.setSearchKeyIds(searchKeyIds);
					sib.setGroupKeyIds(rollingGroupKeyIds);
					byte[] idData = sib.getData();
					parentMenuSummIds.add(idData);
					
					// check rolling parent menu summary is alive
					if (false == FacadeSummSearcher.get().checkAliveOrRequest(idData, now)) {
						parentMenusAlive = false;
					}
					
					// add next group key id to check
					Integer rollingGroupKeyId = groupInfos.get(i).getGroup().getKeyId();
					rollingGroupKeyIds.add(rollingGroupKeyId);
				}
			}
			
			// check final menu alive
			boolean finalMenuAlive = true;
			byte[] finalMenuSummId;
			{
				// build parent menu summary id
				SummIdBuilder sib = new SummIdBuilder(SummKind.Menu.getId(), period.getId());
				sib.setSearchKeyIds(searchKeyIds);
				sib.setGroupKeyIds(groupKeyIds);
				byte[] idData = sib.getData();
				finalMenuSummId = idData;
				
				// check final menu summary is alive
				finalMenuAlive = FacadeSummSearcher.get().checkAliveOrRequest(idData, now);
			}
			
			// check final list alive
			boolean finalListAlive = true;
			byte[] finalListSummId;
			{
				// build parent menu summary id
				SummIdBuilder sib = new SummIdBuilder(SummKind.List.getId(), period.getId());
				sib.setSearchKeyIds(searchKeyIds);
				sib.setGroupKeyIds(groupKeyIds);
				byte[] idData = sib.getData();
				finalListSummId = idData;
				
				// check final menu summary is alive
				finalListAlive = FacadeSummSearcher.get().checkAliveOrRequest(idData, now);
			}
			
			// check all needed summaries are alive
			if (!parentMenusAlive || !finalMenuAlive || !finalListAlive) {

				writeNotReady(resp);
				return;
			}

			// start creating www summary
			WwwSumm wwwSumm = new WwwSumm(period, query.getQueryString(), groupInfos);
			
			// populate parent menus
			for (int i=0; i<parentMenuSummIds.size(); i++) {
				
				byte[] idData = parentMenuSummIds.get(i);
				Summ summ = FacadeSummSearcher.get().findAliveOrRequest(idData, now);
				if (summ != null) {
					
					SummMenu menu = new SummMenu(summ.getMenu());
					
					// set active menu item
					{
						// get active group info
						GroupInfo activeGroupInfo = groupInfos.get(i);
						Group activeGroup = activeGroupInfo.getGroup();
						
						// find active group path
						List<Group> activeGroupPath = activeGroup.findParents();
						activeGroupPath.add(activeGroup);
						
						// start from root menu item
						SummMenuItem menuItem = menu.getRootItem();
						for (int j=0; j<activeGroupPath.size(); j++) {
							
							Group group = activeGroupPath.get(j);
							
							// find group menu item
							SummMenuItem groupItem = null;
							BasicDBList children = menuItem.getChildren();
							if (children != null && children.size() > 0) {
								for (int k=0; k<children.size(); k++) {
									
									SummMenuItem childItem = new SummMenuItem((DBObject)children.get(k));
									if (group.getId().equals(childItem.getGroupId())) {
										groupItem = childItem;
										break;
									}
								}
							}
							
							// if group menu item not found
							if (groupItem == null) {
								groupItem = new SummMenuItem(group.getName(), group.getId(), 0);
								menuItem.addChild(groupItem);
							}
							
							// make group item active
							groupItem.isActive(true);
							menuItem = groupItem;
						}
					}
					
					wwwSumm.addMenu(menu.getDbo());
				} else {
					writeNotReady(resp);
					return;
				}
			}
			
			// populate final menu
			{
				Summ summ = FacadeSummSearcher.get().findAliveOrRequest(finalMenuSummId, now);
				if (summ != null) {
					wwwSumm.addMenu(summ.getMenu());
				} else {
					writeNotReady(resp);
					return;
				}
			}
			
			// populate final list
			{
				Summ summ = FacadeSummSearcher.get().findAliveOrRequest(finalListSummId, now);
				if (summ != null) {
					wwwSumm.setList(summ.getList());
					wwwSumm.setSummId(summ.getSummId());
				} else {
					writeNotReady(resp);
					return;
				}
			}
			
			// write www summary result
			JsonResponse jsonResp = JsonResponse.createOkReady(null, wwwSumm.getDbo());
	    	HttpResponseUtils.writeJsonUtf8Response(resp, jsonResp.toString());
			
		} catch (Exception ex) {
			
			_log.warn("Could not process request", ex);
			JsonResponse jsonResp = JsonResponse.createNotOk("Could not summarize, error: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
	    	HttpResponseUtils.writeJsonUtf8Response(resp, jsonResp.toString());
		}
	}
	
	private static final void writeNotReady(HttpServletResponse resp) throws IOException {

		JsonResponse jsonResp = JsonResponse.createOkNotReady();
    	HttpResponseUtils.writeJsonUtf8Response(resp, jsonResp.toString());
    	return;
	}

}
