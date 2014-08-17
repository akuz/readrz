package com.readrz.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Summary kind: menu, list.
 *
 */
public final class SummKind {

	public final static List<SummKind> All;
	public final static SummKind Menu;
	public final static SummKind List;

	static {
		All = new ArrayList<>();
		
		Menu = new SummKind(1, "Menu");
		All.add(Menu);
		
		List = new SummKind(2, "List");
		All.add(List);
	}
	
	public static final SummKind get(int id) {
		for (int i=0; i<All.size(); i++) {
			SummKind kind = All.get(i);
			if (kind._id == id) {
				return kind;
			}
		}
		return null;
	}
	
	public static final SummKind getOrThrow(int id) {
		SummKind kind = get(id);
		if (kind == null) {
			throw new IllegalArgumentException("Unknown " + SummKind.class.getSimpleName() + " id: " + id);
		}
		return kind;
	}
	
	private final int _id;
	private final String _name;
	
	private SummKind(final int id, final String name) {
		_id = id;
		_name = name;
	}
	
	public int getId() {
		return _id;
	}
	
	public String getName() {
		return _name;
	}
	
	@Override
	public String toString() {
		return String.format("%s(%d)", _name, _id);
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}
	
	@Override
	public int hashCode() {
		return _id;
	}
}
