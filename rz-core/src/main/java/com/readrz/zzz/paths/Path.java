package com.readrz.zzz.paths;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Path implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final transient Path _root;
	private final transient Path _parent;
	private final PathCondition _leafCondition;
	private final double _leafProbability;
	private final double _totalProbability;
	private final List<PathCondition> _allConditions;
	private final Set<String> _allPositiveConditionsStems;
	private final Set<String> _allConditionsStems;
	private final int _level;
	private final int _levelTrueSiblingsCount;
	private final List<Path> _children;
	private transient PathStats _stats;
	
	public Path(Path root, Path parent, PathCondition leafCondition, double leafProbability) {
		if (parent == null && leafCondition != null) {
			throw new IllegalArgumentException("Cannot create a root path with non-null leafCondition");
		}
		if (parent != null && leafCondition == null) {
			throw new IllegalArgumentException("Cannot create a child path with null leafCondition");
		}
		_root = root;
		_parent = parent;
		_leafCondition = leafCondition;
		_leafProbability = leafProbability;
		if (parent != null) {
			_totalProbability = parent.getTotalProbability() * leafProbability;
		} else {
			_totalProbability = leafProbability;
		}
		
		_allConditions = new ArrayList<PathCondition>();
		_allPositiveConditionsStems = new HashSet<String>();
		_allConditionsStems = new HashSet<String>();
		
		Path loopPath = this;
		while (loopPath != null) {
			if (loopPath._leafCondition != null) {
				_allConditions.add(loopPath._leafCondition);
				if (loopPath._leafCondition.isPositive()) {
					_allPositiveConditionsStems.add(loopPath._leafCondition.getStem());
				}
				_allConditionsStems.add(loopPath._leafCondition.getStem());
			}
			loopPath = loopPath._parent;
		}
		
		// calculate level
		int level = 0;
		int levelPositiveSiblingsCount = 0;
		
		if (_parent != null) {
			
			// child is positive
			if (_leafCondition.isPositive()) {
				
				// parent is root or positive
				if (_parent.getLeafCondition() == null || 
					_parent.getLeafCondition().isPositive()) {
					
					// first positive sibling (no other siblings)
					levelPositiveSiblingsCount = 0;
					
				} else { // parent is negative

					// take the number of positive siblings from the previous negative sibling
					levelPositiveSiblingsCount = _parent.getLevelPositiveSiblingsCount();
				}
				
			} else { // child is negative
				
				// parent is root or positive condition
				if (_parent.getLeafCondition() == null || 
					_parent.getLeafCondition().isPositive()) {
					
					// first negative sibling (one positive sibling already)
					levelPositiveSiblingsCount = 1;
					
				} else { // parent is negative

					// the previous negative sibling had another positive stemming from it
					levelPositiveSiblingsCount = _parent.getLevelPositiveSiblingsCount() + 1;
				}
			}

			// parent is root or positive condition
			if (_parent.getLeafCondition() == null || 
				_parent.getLeafCondition().isPositive()) {
				
				// new level
				level = _parent.getLevel() + 1;
		
			} else { // parent is negative condition

				// same as the parent level
				level = _parent.getLevel();
			}
		}
		_level = level;
		_levelTrueSiblingsCount = levelPositiveSiblingsCount;

		// create children array
		_children = new ArrayList<Path>();
	}
	
	public Path getRoot() {
		return _root;
	}
	
	public Path getParent() {
		return _parent;
	}
	
	public double getLeafProbability() {
		return _leafProbability;
	}
	
	public double getTotalProbability() {
		return _totalProbability;
	}
	
	public int getLevel() {
		return _level;
	}
	
	public int getLevelPositiveSiblingsCount() {
		return _levelTrueSiblingsCount;
	}
	
	public void addChild(Path path) {
		_children.add(path);
	}
	
	public List<Path> getChildren() {
		return _children;
	}
	
	public PathCondition getLeafCondition() {
		return _leafCondition;
	}
	
	public Set<String> getAllPositiveConditionsStems() {
		return _allPositiveConditionsStems;
	}
	
	public Set<String> getAllConditionsStems() {
		return _allConditionsStems;
	}
	
	public List<PathCondition> getAllConditions() {
		return _allConditions;
	}
	
	public void setStats(PathStats stats) {
		_stats = stats;
	}
	
	public PathStats getStats() {
		return _stats;
	}
}
