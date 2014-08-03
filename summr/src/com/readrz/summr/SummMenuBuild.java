package com.readrz.summr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import me.akuz.core.Pair;
import me.akuz.core.PairComparator;
import me.akuz.core.SortOrder;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.readrz.data.SummMenu;
import com.readrz.data.SummMenuItem;
import com.readrz.data.ontology.Group;
import com.readrz.data.ontology.GroupCatalog;

public final class SummMenuBuild {

	public static final SummMenu createSummMenu(
			String rootText,
			Integer rootCount,
			GroupStats groupStats,
			GroupCatalog ... groupCatalogs) {
		
		// initialize menu level 1 items
		List<Pair<Group, SummMenuItem>> level1Items = new ArrayList<>();
		
		// create menu population queue
		Queue<Pair<Group, SummMenuItem>> queue = new LinkedList<>();
		
		// populate root menu items
		for (int i=0; i<groupCatalogs.length; i++){
			
			GroupCatalog groupCatalog = groupCatalogs[i];
			
			// get root groups
			List<Group> rootGroups = groupCatalog.getRootGroups();
			
			// create root menu items
			List<Pair<Group, SummMenuItem>> rootGroupsMenuItems 
				= createSortedMenuItems(
						rootGroups, 
						groupStats);
			
			// populate root menu items
			for (int j=0; j<rootGroupsMenuItems.size(); j++) {
				
				// get created menu item
				Pair<Group, SummMenuItem> pair = rootGroupsMenuItems.get(j);
				Group rootGroup = pair.v1();
				SummMenuItem rootGroupMenuItem = pair.v2();
				
				// add to parent
				level1Items.add(new Pair<Group, SummMenuItem>(rootGroup, rootGroupMenuItem));
				
				// queue for children population
				if (rootGroup.getChildGroups() != null) {
					queue.add(pair);
				}
			}
		}
		
		// populate items hierarchy
		while (queue.size() > 0) {
			
			// get next menu item and group to populate
			Pair<Group, SummMenuItem> pair = queue.poll();
			Group loopGroup = pair.v1();
			SummMenuItem loopMenuItem = pair.v2();
			
			// check if group has children
			BasicDBList childGroupsDBList = loopGroup.getChildGroups();
			if (childGroupsDBList != null && childGroupsDBList.size() > 0) {
				
				// convert to normal list
				List<Group> childGroups = new ArrayList<>();
				for (int i=0; i<childGroupsDBList.size(); i++) {
					childGroups.add(new Group((DBObject)childGroupsDBList.get(i)));
				}
				
				// create child menu items
				List<Pair<Group, SummMenuItem>> childGroupsMenuItems 
					= createSortedMenuItems(
							childGroups, 
							groupStats);

				// populate child menu items
				for (int i=0; i<childGroupsMenuItems.size(); i++) {
					
					// get created menu item
					Pair<Group, SummMenuItem> pair2 = childGroupsMenuItems.get(i);
					Group group2 = pair2.v1();
					SummMenuItem menuItem2 = pair2.v2();
					
					// add to parent
					loopMenuItem.addChild(menuItem2);
					
					// queue for children population
					if (group2.getChildGroups() != null &&
						group2.getChildGroups().size() > 0) {

						queue.add(pair2);
					}
				}
			}
		}
		
		// populate menu
		SummMenu menu = new SummMenu(rootText, rootCount);
		if (level1Items.size() > 1) {
			Collections.sort(level1Items, new PairComparator<Group, SummMenuItem>(SortOrder.Asc));
		}
		for (int i=0; i<level1Items.size(); i++) {
			menu.addChild(level1Items.get(i).v2());
		}
		
		return menu;
	}

	/**
	 * Creates a sorted *plain* list of menu items for the specified 
	 * list of Groups, using the counts from the Paths object.
	 * 
	 */
	private static final List<Pair<Group, SummMenuItem>> createSortedMenuItems(
			List<Group> groups, 
			GroupStats groupStats) {
		
		// init menu items
		List<Pair<Group, SummMenuItem>> menuItems = new ArrayList<>();

		// populate menu items
		for (int i=0; i<groups.size(); i++) {
			
			// get group and its key id
			Group loopGroup = groups.get(i);
			Integer loopCount = groupStats.getSnapCount(loopGroup.getKeyId());
			if (loopCount != null && loopCount > 0) {
				
				SummMenuItem menuItem = new SummMenuItem(loopGroup.getName(), loopGroup.getId(), loopCount);
				menuItem.isTaxonomy(loopGroup.isTaxonomy());
				menuItems.add(new Pair<Group, SummMenuItem>(loopGroup, menuItem));
			}
		}
		
		// sort menu items
		Collections.sort(menuItems, new PairComparator<Group, SummMenuItem>(SortOrder.Asc));

		return menuItems;
	}
}
