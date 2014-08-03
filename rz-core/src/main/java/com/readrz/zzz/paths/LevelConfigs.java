package com.readrz.zzz.paths;

import java.util.HashMap;
import java.util.Map;

public final class LevelConfigs {
	
	private final Map<Integer, LevelConfig> _configs;
	private int _maxLevel;
	
	public LevelConfigs() {
		_configs = new HashMap<Integer, LevelConfig>();
	}
	
	public void add(LevelConfig config) {
		_maxLevel += 1;
		_configs.put(_maxLevel, config);
	}
	
	public int getMaxLevel() {
		return _maxLevel;
	}
	
	public LevelConfig get(Integer level) {
		LevelConfig config = _configs.get(level);
		if (config == null) {
			throw new IndexOutOfBoundsException("No config for level " + level);
		}
		return config;
	}
}
