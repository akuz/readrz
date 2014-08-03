package com.readrz.lang.parse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.akuz.core.Hit;
import me.akuz.core.Pair;
import me.akuz.core.PairComparator;
import me.akuz.core.SortOrder;

import com.readrz.data.ontology.Group;
import com.readrz.data.ontology.GroupCatalog;

/**
 * Detects group ids occurrences.
 *
 */
public final class GroupIdsDetector {

	private final List<Pair<Group, String>> _groupsWithIds;
	private final Pattern _patternGroups;
	
	public GroupIdsDetector(GroupCatalog... groupCatalogs) {
		
		_groupsWithIds = new ArrayList<>();
		if (groupCatalogs != null) {
			for (GroupCatalog catalog : groupCatalogs) {
				for (Entry<String, Group> entry : catalog.getAllGroupsById().entrySet()) {
					_groupsWithIds.add(new Pair<Group, String>(entry.getValue(), entry.getKey()));
				}
			}
		}

		// sort descending by group id, so that we detect *longest* groups first
		Collections.sort(_groupsWithIds, new PairComparator<Group, String>(SortOrder.Desc));
		
		StringBuilder sb = new StringBuilder();
		if (_groupsWithIds.size() > 0) {

			// after non-letter or at start
			sb.append("(?<=(?:\\W|^))(?:");

			for (int i=0; i<_groupsWithIds.size(); i++) {
				
				Pair<Group, String> pair = _groupsWithIds.get(i);
				String groupId = pair.v2();
				
				if (i>0) {
					sb.append("|");
				}
				sb.append("(");

				String groupIdRegex = groupId.replaceAll("/", "\\/");
				sb.append(groupIdRegex);
				
				sb.append(")");
			}
			
			// followed by non-letter or end
			sb.append(")(?=(?:\\W|$))");
		}
		if (sb.length() > 0) {
			_patternGroups = Pattern.compile(sb.toString());
		} else {
			_patternGroups = null;
		}
	}
	
	public Group getGroupByIndex(int index) {
		return _groupsWithIds.get(index).v1();
	}

	public Map<Integer, List<Hit>> extractHitsByGroupIndex(String str, Hit bounds) {

		Map<Integer, List<Hit>> hitsByGroupIndex = null;
		
		if (_patternGroups != null && str != null) {

			Matcher matcher = _patternGroups.matcher(str);
			matcher.region(bounds.start(), bounds.end());
			while (matcher.find()) {
				
				int entityIndex = getMatchedPatternGroupIndex(matcher);
				int matchStart = matcher.start(entityIndex+1);
				int matchEnd = matcher.end(entityIndex+1);
				
				if (matchStart >= matchEnd) {
					// can't match empty strings
					// possibly bad pattern
					// for entity
					continue;
				}

				Hit hit = new Hit(matchStart, matchEnd);

				// register the hit
				if (hitsByGroupIndex == null) {
					hitsByGroupIndex = new HashMap<Integer, List<Hit>>();
				}
				List<Hit> hits = hitsByGroupIndex.get(entityIndex);
				if (hits == null) {
					hits = new ArrayList<Hit>();
					hitsByGroupIndex.put(entityIndex, hits);
				}
				hits.add(hit);
			}
		}
		
		return hitsByGroupIndex;
	}

	private int getMatchedPatternGroupIndex(Matcher matcher) {
		
		for (int i=0; i<_groupsWithIds.size(); i++) {
			String m = matcher.group(i + 1);
			if (m != null && m.length() > 0) {
				return i;
			}
		}

		throw new IllegalStateException("Cannot find which entity was found by matcher");
	}
	
}
