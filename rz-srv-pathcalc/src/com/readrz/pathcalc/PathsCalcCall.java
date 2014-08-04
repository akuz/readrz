package com.readrz.pathcalc;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import me.akuz.core.logs.LogUtils;

import com.readrz.data.Paths;
import com.readrz.data.PathsId;
import com.readrz.data.PathsRequest;
import com.readrz.data.Period;
import com.readrz.data.AliveStatus;
import com.readrz.math.wordpaths.PathsCall;

/**
 * A call for scanning the feed, which can be executed on a thread pool.
 *
 */
public final class PathsCalcCall implements Callable<Boolean> {
	
	private static final Logger _log = LogUtils.getLogger(PathsCalcCall.class.getName());

	private final PathsEngine _engine;
	private final boolean _isUserRequested;
	private final PathsId _pathsId;
	private final String _pathsIdString;
	private final ProgramOptions _options;
	private volatile boolean _isFinished;
	private Exception _exception;
	
	public PathsCalcCall(
			PathsEngine engine,
			boolean isUserRequested,
			PathsId pathsId,
			ProgramOptions options) {
		
		_engine = engine;
		_isUserRequested = isUserRequested;
		_pathsId = pathsId;
		_pathsIdString = _pathsId.toString(engine.getKeysIndex());
		_options = options;
	}
	
	public boolean isUserRequested() {
		return _isUserRequested;
	}
	
	public PathsId getPathsId() {
		return _pathsId;
	}
	
	public ProgramOptions getOptions() {
		return _options;
	}
	
	@Override
	public Boolean call() {

		try {
			Period period = _pathsId.getPeriod();

			// check if need to calculate paths
			boolean saveRequestAsCompleted = false;
			Date now = new Date();
			AliveStatus aliveStatus = Paths.findAliveStatus(_engine.getPathsColl(), _pathsId.getData(), now, period);
			if (aliveStatus.equals(AliveStatus.Alive)) {

				_log.fine("Found alive paths for " + _pathsIdString + ", will *not* recalculate");
				saveRequestAsCompleted = true;

			} else {

				_log.fine("Calculating paths for " + _pathsIdString + "...");
				PathsCall pathsCall = new PathsCall(
						_pathsId, 
						now,
						_engine.getKeysIndex(), 
						_engine.getStopKeyIds(),
						_engine.getQueryParser(),
						_engine.getSnapSearcher(), 
						_engine.getPathsColl());
				
				Boolean donePaths = pathsCall.call();
				
				if (donePaths == null) {
					_log.warning("Paths result is NULL for " + _pathsIdString);
				} else {
					if (donePaths) {
						_log.fine("Calculated and saved paths for " + _pathsIdString);
						saveRequestAsCompleted = true;
					} else {
						_log.warning("Could not calculate paths for " + _pathsIdString);
					}
				}
			}
			
			if (saveRequestAsCompleted) {

				_log.fine("Saving request as false for " + _pathsIdString + " as false...");
				PathsRequest upd = new PathsRequest(_pathsId.getData(), false);
				upd.upsertUnacknowledged(_engine.getPathsreqColl());
			}
			
		} catch (Exception ex) {
			_exception = ex;
		}
		
		_isFinished = true;
		return _exception != null;
	}

	public boolean isFinished() {
		return _isFinished;
	}
	
	public boolean hasException() {
		return _exception != null;
	}
	
	public Exception getException() {
		return _exception;
	}
	
	@Override
	public String toString() {
		return _pathsIdString;
	}
}
