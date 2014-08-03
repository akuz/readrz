package com.readrz.www.builders;

import com.readrz.data.Period;
import com.readrz.www.RzPar;
import com.readrz.www.UrlBuilder;
import com.readrz.www.objects.WwwMenu;
import com.readrz.www.objects.WwwMenuItem;

public final class BuildPeriodsMenu {

	public static final WwwMenu createLoading(Period period, Period topPeriod) {
		
		WwwMenu periodsMenu = new WwwMenu(period.getName(), null);
		
		if (period.equals(topPeriod) == false) {
			periodsMenu.setIsActive(true);
		}

		return periodsMenu;
	}
	
	public static final WwwMenu createWithAvailablePeriods(
			UrlBuilder baseUrl, String setParameterName,
			Period selectedPeriod, Period defaultPeriod) {
		
		WwwMenu periodsMenu = new WwwMenu(selectedPeriod.getName(), null);
		
		if (selectedPeriod.equals(defaultPeriod) == false) {
			periodsMenu.setIsActive(true);
		}

		for (int i=0; i<RzPar.availableGroupingPeriods.size(); i++) {
			Period availablePeriod = RzPar.availableGroupingPeriods.get(i);
			
			UrlBuilder periodUrl = baseUrl.clone();
			periodUrl.remove(RzPar.parPeriod);
			periodUrl.update(setParameterName, availablePeriod.getAbbr());

			WwwMenuItem periodMenuItem = new WwwMenuItem(
					availablePeriod.getName(), 
					null, 
					periodUrl);
			
			if (availablePeriod.equals(defaultPeriod) == false) {
				periodMenuItem.setTurnsMenuOn(true);
			}
			
			periodsMenu.addItem(periodMenuItem);
			
			if (availablePeriod.equals(selectedPeriod)) {
				periodMenuItem.setIsActive(true);
			}
		}
		
		return periodsMenu;
	}
}
