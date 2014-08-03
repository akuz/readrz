package com.readrz.www;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class ProfileConst {

	public static final Pattern usernamePattern = Pattern.compile("[A-Z0-9_]{3,15}", Pattern.CASE_INSENSITIVE);
	public static final String usernamePatternMessage = "Username: 3 to 15 characters: letters, numbers, _";
	
	public static final Pattern passwordPattern = Pattern.compile(".{5,20}");
	public static final String passwordPatternMessage = "Password: 5 to 20 characters";
	
	public static final Pattern emailPattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}$", Pattern.CASE_INSENSITIVE);
	public static final String emailPatternMessage = "Email is incorrectly formatted";
	
	public static final String servletPathRoot = "/";
	public static final String servletPathLogin = "/login";
	public static final String servletPathProfile = "/profile";
	public static final String servletPathRecover = "/recover";
	public static final String servletPathRegister = "/register";
	public static final String servletPathLogout = "/logout";
	
	public static final Set<String> knownServletPaths;
	
	static {
		knownServletPaths = new HashSet<>();
		knownServletPaths.add(servletPathRoot);
		knownServletPaths.add(servletPathLogin);
		knownServletPaths.add(servletPathProfile);
		knownServletPaths.add(servletPathRecover);
		knownServletPaths.add(servletPathRegister);
		knownServletPaths.add(servletPathLogout);
	}
	
}
