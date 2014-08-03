package com.readrz.www.pages;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.readrz.www.OG;
import com.readrz.www.RzPar;
import com.readrz.www.UrlBuilder;
import com.readrz.www.builders.BuildPeriodsMenu;
import com.readrz.www.objects.WwwMenu;

public abstract class AnyFrontPage extends AnyAbstractPage {
	
	private OG _og;
	private final WwwMenu _periodsMenu;
	
	public AnyFrontPage(HttpServletRequest req, HttpServletResponse resp) {
		
		super(req, resp);
		
		{ // build periods menu

			UrlBuilder requestUrl = new UrlBuilder(getServletPath()).addAllFromRequest(req);
			WwwMenu periodsMenu = BuildPeriodsMenu.createWithAvailablePeriods(requestUrl, RzPar.parSetPeriod, getTopPeriod(), null);
			_periodsMenu = periodsMenu;
		}
	}
	
	public OG getOg() {
		return _og;
	}
	public void setOg(OG og) {
		_og = og;
	}
	
	public WwwMenu getPeriodsMenu() {
		return _periodsMenu;
	}
}
