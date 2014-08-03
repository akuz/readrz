package com.readrz.www;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.types.ObjectId;

import com.readrz.data.Period;
import com.readrz.data.ontology.Group;
import com.readrz.data.ontology.GroupInfo;
import com.readrz.search.Query;
import com.readrz.www.facades.FacadeOntology;

/**
 * Readrz web app parameters.
 *
 */
public final class RzPar {
	
	public final static String periodCookieName = "rzPeriod";
	public final static String yes = "yes";
	public final static String no  = "no";
	public final static String parGoto       = "goto";
	public final static String parUngroup    = "ungroup";
	public final static String parSetPeriod  = "set-period";
	
	public final static String parId           = "id";
	public final static String parIds          = "ids";
	public final static String parKind         = "kind";
	public final static String parSearchKeyIds = "sids";
	public final static String parGroupKeyIds  = "sids";
	
	public final static String parPeriod     = "p";
	public final static String parQuery      = "q";
	public final static String parGroupIds   = "g";
	public final static String parSnapId     = "s";
	public final static String parSnapQuery  = "sq";
	
	public static Period defaultGroupingPeriod;
	public static List<Period> availableGroupingPeriods;
	public static Period maxResultsPeriod;
	
	static {
		
		defaultGroupingPeriod = Period.getOrThrow(Period.Id1d);
		availableGroupingPeriods = new ArrayList<>();
		availableGroupingPeriods.add(Period.getOrThrow(Period.Id4h));
		availableGroupingPeriods.add(Period.getOrThrow(Period.Id1d));
		availableGroupingPeriods.add(Period.getOrThrow(Period.Id3d));
		
		maxResultsPeriod = Period.getOrThrow(Period.Id1w);
	}
	
	public static UrlBuilder createBrowseUrl(String base, Period period, Query query, List<GroupInfo> groupInfos) {

		UrlBuilder url = new UrlBuilder(base);
		
		url.setParamDontEncode(parPeriod, true);
		url.setParamOrder(parPeriod, url.getParamCount());
		url.update(parPeriod, period.getAbbr());
		
		if (query != null && query.getIsEmpty() == false) {
			url.setParamOrder(parQuery, url.getParamCount());
			url.update(parQuery, query.getQueryString());
		}
		
		if (groupInfos != null) {
			StringBuilder sb = new StringBuilder();
			for (int i=0; i<groupInfos.size(); i++) {
				GroupInfo groupInfo = groupInfos.get(i);
				if (sb.length() > 0) {
					sb.append(",");
				}
				sb.append(groupInfo.getGroup().getId());
			}
			if (sb.length() > 0) {
				url.setParamDontEncode(parGroupIds, true);
				url.setParamOrder(parGroupIds, url.getParamCount());
				url.update(parGroupIds, sb.toString());
			}
		}
		
		return url;
	}

	public static UrlBuilder createBrowseUrl(String base, Period period, Query query, List<GroupInfo> groupInfos, ObjectId snapId, Query snapQuery) {

		UrlBuilder url = createBrowseUrl(base, period, query, groupInfos);
		
		if (snapId != null) {
			
			url.setParamDontEncode(parSnapId, true);
			url.setParamOrder(parSnapId, url.getParamCount());
			url.update(parSnapId, snapId.toString());

			if (snapQuery != null && snapQuery.getIsEmpty() == false) {
				url.setParamOrder(parSnapQuery, url.getParamCount());
				url.update(parSnapQuery, snapQuery.getQueryString());
			}
		}

		return url;
	}
	
	public static Period getTopPeriodOfUser(HttpServletRequest req) {
		Cookie[] cookies = req.getCookies();
		Period topPeriod = null;
		if (cookies != null) {
			for (int i=0; i<cookies.length; i++) {
				Cookie cookie = cookies[i];
				if (periodCookieName.equals(cookie.getName())) {
					topPeriod = Period.get(cookie.getValue());
					break;
				}
			}
		}
		if (topPeriod == null) {
			topPeriod = defaultGroupingPeriod;
		}
		return topPeriod;
	}
	
	public static void setTopPeriodOfUser(HttpServletResponse resp, Period period) {
		Cookie cookie = new Cookie(periodCookieName, period.getAbbr());
		resp.addCookie(cookie);
	}

	public static List<GroupInfo> getGroupInfos(HttpServletRequest req, String par) {
		
		List<GroupInfo> groupInfos = new ArrayList<>();
		String values[] = req.getParameterValues(par);
		if (values != null) {
			
			for (int i=0; i<values.length; i++) {
				String[] parts = values[i].split(",");
				for (int j=0; j<parts.length; j++) {
					
					String groupId = parts[j];
					Group group = null;
					{
						group = FacadeOntology.get().getTopicGroupCatalog().getGroupById(groupId);
						if (group != null) {
							groupInfos.add(new GroupInfo(group, true));
							continue;
						}
					}
					{
						group = FacadeOntology.get().getPatternGroupCatalog().getGroupById(groupId);
						if (group != null) {
							groupInfos.add(new GroupInfo(group, false));
							continue;
						}
					}
					throw new IllegalArgumentException("Group not found (" + groupId + ")");
				}
			}
		}
		return groupInfos;
	}

	public static Period parseAndCheckPeriod(HttpServletRequest req, Period defaultPeriod) {
		String str = req.getParameter(RzPar.parPeriod);
		return parseAndCheckPeriod(str, defaultPeriod);
	}
	
	public static Period parseAndCheckPeriod(String str, Period defaultOrThrow) {
		
		if (str != null && str.length() > 0) {
			Period period = Period.getOrThrow(str);
			if (period != null) {
				for (int i=0; i<availableGroupingPeriods.size(); i++) {
					if (availableGroupingPeriods.get(i).equals(period)) {
						return period;
					}
				}
				throw new IllegalArgumentException(period.getName() + " period is not available");
			}
		}
		
		if (defaultOrThrow != null) {
			return defaultOrThrow;
		} else {
			throw new IllegalArgumentException("Period not specified");
		}
	}
	
	public static final String getStringRequired(HttpServletRequest req, String par) {
		String parStr = req.getParameter(par);
		if (parStr != null && parStr.length() > 0) {
			return parStr;
		} else {
			throw new IllegalArgumentException("Required parameter '" + par + "' not provided");
		}
	}
	public static final String getStringOptional(HttpServletRequest req, String par) {
		String parStr = req.getParameter(par);
		if (parStr != null && parStr.length() > 0) {
			return parStr;
		} else {
			return null;
		}
	}
	
	public static final Integer getIntegerRequired(HttpServletRequest req, String par) {
		String parStr = req.getParameter(par);
		if (parStr != null && parStr.length() > 0) {
			return Integer.parseInt(parStr);
		} else {
			throw new IllegalArgumentException("Required parameter '" + par + "' not provided");
		}
	}
	public static final Integer getIntegerOptional(HttpServletRequest req, String par) {
		String parStr = req.getParameter(par);
		if (parStr != null && parStr.length() > 0) {
			return Integer.parseInt(parStr);
		} else {
			return null;
		}
	}
	
	public static final ObjectId getObjectIdRequired(HttpServletRequest req, String par) {
		String parStr = req.getParameter(par);
		if (parStr != null && parStr.length() > 0) {
			return new ObjectId(parStr);
		} else {
			throw new IllegalArgumentException("Required parameter '" + par + "' not provided");
		}
	}
	public static final ObjectId getObjectIdOptional(HttpServletRequest req, String par) {
		String parStr = req.getParameter(par);
		if (parStr != null && parStr.length() > 0) {
			return new ObjectId(parStr);
		} else {
			return null;
		}
	}
	
	public static final List<Integer> getIntegerListOptional(HttpServletRequest req, String par) {
		List<Integer> results = null;
		String str = getStringOptional(req, par);
		if (str != null && str.length() > 0) {
			String[] parts = str.split(",");
			for (int i=0; i<parts.length; i++) {
				Integer result = Integer.parseInt(parts[i]);
				if (results == null) {
					results = new ArrayList<>();
				}
				results.add(result);
			}
		}
		return results;
	}
	
	public static final List<ObjectId> getObjectIdListOptional(HttpServletRequest req, String par) {
		List<ObjectId> results = null;
		String str = getStringOptional(req, par);
		if (str != null && str.length() > 0) {
			String[] parts = str.split(",");
			for (int i=0; i<parts.length; i++) {
				ObjectId result = new ObjectId(parts[i]);
				if (results == null) {
					results = new ArrayList<>();
				}
				results.add(result);
			}
		}
		return results;
	}
	
}
