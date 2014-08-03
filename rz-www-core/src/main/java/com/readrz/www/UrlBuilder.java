package com.readrz.www;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import me.akuz.core.Pair;
import me.akuz.core.PairComparator;
import me.akuz.core.SortOrder;
import me.akuz.core.UrlUtils;


public final class UrlBuilder implements Cloneable, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String _base;
	private Map<String, Integer> _paramsOrder;
	private Map<String, Boolean> _paramsDontEncode;
	private Map<String, List<String>> _params;

	public UrlBuilder(String base) {
		_base = base;
		_params = new HashMap<String, List<String>>();
	}
	
	public String getBase() {
		return _base;
	}
	public void setBase(String base) {
		_base = base;
	}
	
	public int getParamCount() {
		return _params.size();
	}
	
	public void setParamOrder(String parameterName, Integer priority) {
		if (_paramsOrder == null) {
			_paramsOrder = new HashMap<String, Integer>();
		}
		_paramsOrder.put(parameterName, priority);
	}
	
	public void setParamDontEncode(String parameterName, boolean dont) {
		if (_paramsDontEncode == null) {
			_paramsDontEncode = new HashMap<String, Boolean>();
		}
		_paramsDontEncode.put(parameterName, dont);
	}
	
	public String get(String parameterName) {
		List<String> list = _params.get(parameterName);
		if (list != null && list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}
	
	public List<String> getList(String parameterName) {
		return _params.get(parameterName);
	}
	
	public boolean has(String parameterName) {
		List<String> list = _params.get(parameterName);
		return list != null && list.size() > 0;
	}
	
	@SuppressWarnings("rawtypes")
	public UrlBuilder addAllFromRequest(HttpServletRequest req) {
		
		Enumeration parameterNames = req.getParameterNames();
		while (parameterNames.hasMoreElements()) {
			
			String parameterName = (String)parameterNames.nextElement();
			String[] values = req.getParameterValues(parameterName);
			if (values != null && values.length > 0) {
				
				for (int i=0; i<values.length; i++) {
					String value = values[i];
					add(parameterName, value);
				}
			}
		}
		return this;
	}
	
	public UrlBuilder add(String parameterName, String value) {
		
		// only non-nulls
		if (value != null) {
			
			// get or create values list
			List<String> values = _params.get(parameterName);
			if (values == null) {
				values = new ArrayList<String>();
				_params.put(parameterName, values);
			}
			
			// remove at whatever position is was
			values.remove(value);
			
			// add at last position
			values.add(value);
		}
		
		return this;
	}
	
	public UrlBuilder remove(String parameterName, String value) {
		
		List<String> values = _params.get(parameterName);
		if (values != null) {
			values.remove(value);
			if (values.size() == 0) {
				remove(parameterName);
			}
		}
		return this;
	}
	
	public UrlBuilder remove(String parameterName) {
		_params.remove(parameterName);
		return this;
	}
	
	public UrlBuilder update(String parameterName, String value) {
		remove(parameterName);
		add(parameterName, value);
		return this;
	}
	
	@Override
	public UrlBuilder clone() {
		
		try {
			UrlBuilder copy = (UrlBuilder)super.clone();
			
			if (_paramsOrder != null) {
				copy._paramsOrder = new HashMap<String, Integer>(_paramsOrder);
			}
			
			copy._params = new HashMap<String, List<String>>();
			for (String parameterName : _params.keySet()) {
				copy._params.put(parameterName, new ArrayList<String>(_params.get(parameterName)));
			}
			
			return copy;
			
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			throw new InternalError("Clone not supported");
		}
	}

	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append(_base);

		if (_params.size() > 0) {

			List<Pair<String, Integer>> paramNamesByOrder = new ArrayList<Pair<String, Integer>>();
			for (String parameterName : _params.keySet()) {
				
				Integer parameterOrder = null;
				if (_paramsOrder != null)  {
					parameterOrder = _paramsOrder.get(parameterName);
				}
				if (parameterOrder == null) {
					parameterOrder = 0;
				}
				paramNamesByOrder.add(new Pair<String, Integer>(parameterName, parameterOrder));
			}
			
			if (paramNamesByOrder.size() > 1) {
				Collections.sort(paramNamesByOrder, new PairComparator<String, Integer>(SortOrder.Asc));
			}
	
			boolean first = true;
			for (int i=0; i<paramNamesByOrder.size(); i++) {
				
				String paramName = paramNamesByOrder.get(i).v1();
				
				List<String> values = _params.get(paramName);
				for (int j=0; j<values.size(); j++) {
					if (first) {
						sb.append("?");
						first = false;
					} else {
						sb.append("&");
					}
					sb.append(paramName);
					sb.append("=");
					
					boolean encode = true;
					if (_paramsDontEncode != null) {
						Boolean dont = _paramsDontEncode.get(paramName);
						if (dont != null && dont.booleanValue()) {
							encode = false;
						}
					}
					if (encode) {
						sb.append(UrlUtils.encodeUtf8(values.get(j)));
					} else {
						sb.append(values.get(j));
					}
				}
			}
		}
		
		return sb.toString();
	}
}
