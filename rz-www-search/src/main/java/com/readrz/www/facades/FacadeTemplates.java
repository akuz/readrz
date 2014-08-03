package com.readrz.www.facades;

import javax.servlet.ServletContext;

import me.akuz.core.SystemUtils;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupDir;


public final class FacadeTemplates {
	
	private static final Object _lock = new Object();
	private static FacadeTemplates _instance;
	private final STGroupDir _stGroupDir;
	
	public static FacadeTemplates get(ServletContext context) {

		if (SystemUtils.isLocalhost()) {
			return new FacadeTemplates(context);
		}

		if (_instance == null) {
			synchronized (_lock) {
				if (_instance == null) {
					_instance = new FacadeTemplates(context);
				}
			}
		}
		return _instance;
	}
	
	private FacadeTemplates(ServletContext context) {
		String realPath = context.getRealPath("tmpl4");
		_stGroupDir = new STGroupDir(realPath, "UTF-8", '$', '$');
	}
	
	public ST getST(String templateName) {
		return _stGroupDir.getInstanceOf(templateName);
	}
	
	public ST getAuthPageST() {
		return _stGroupDir.getInstanceOf("AuthPage");
	}
	
	public ST getProfilePageST() {
		return _stGroupDir.getInstanceOf("ProfilePage");
	}
	
	public ST getBrowsePageST() {
		return _stGroupDir.getInstanceOf("BrowsePage");
	}
	
	public ST getHomePageST() {
		return _stGroupDir.getInstanceOf("HomePage");
	}
	
	public ST getPersonalPageST() {
		return _stGroupDir.getInstanceOf("PersonalPage");
	}
	
	public ST getTabPageST() {
		return _stGroupDir.getInstanceOf("TabPage");
	}
	
	public ST getColumnWrapST() {
		return _stGroupDir.getInstanceOf("ColumnWrap");
	}
	
	public ST getTabsST() {
		return _stGroupDir.getInstanceOf("Tabs");
	}
	
	public ST getMyNewsAddST() {
		return _stGroupDir.getInstanceOf("MyNewsAdd");
	}
	
	public ST getMyNewsEditST() {
		return _stGroupDir.getInstanceOf("MyNewsEdit");
	}

	public ST getTabUserOwnedST() {
		return _stGroupDir.getInstanceOf("TabUserOwned");
	}
	
}
