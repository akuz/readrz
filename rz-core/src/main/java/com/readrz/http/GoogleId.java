package com.readrz.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.InvalidParameterException;
import java.util.Map;

import me.akuz.core.StringUtils;


public class GoogleId {
	
	public final static String SERVICE_AH = "ah";
	public final static String SERVICE_READER = "reader";
	public final static String APP_MY_APP = "MyApp";
	
	private String _AUTH;
	private String _AUTHHeader;
	private String _cookie;

	public GoogleId(String service, String appName, String appUrl, String email, String password) throws Exception {
		
		if (appUrl != null && appUrl.toLowerCase().startsWith("http:")) {
			
			System.out.println("WARNING: URL is NOT HTTPS, skipping authorization with Google.");

		} else if (appUrl != null && appUrl.toLowerCase().contains("local")) {
			
			System.out.println("WARNING: URL contains 'local', skipping authorization with Google.");

		} else {
			
			URL url = new URL("https://www.google.com/accounts/ClientLogin" + 
					"?Email=" + email + 
					"&Passwd=" + password + 
					"&service=" + service + 
					"&source=" + appName +
					"&accountType=HOSTED_OR_GOOGLE");
			
			URLConnection conn = url.openConnection();
			conn.connect();
			
			String line = null;
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((line = br.readLine()) != null) {
				System.out.println(line);
				if (line.toUpperCase().startsWith("AUTH=")) {
					_AUTH = line.substring(5);
					_AUTHHeader = "GoogleLogin auth=" + _AUTH;
				}
			}
			if (_AUTH == null) {
				throw new Exception("Google service did not return AUTH field.");
			}
			if (appUrl != null) {
	
				String appLoginURL = StringUtils.concatPath(appUrl, "_ah/login?auth=" +_AUTH);
				
				URL loginURL = new URL(appLoginURL);
				URLConnection loginConn = loginURL.openConnection();
				loginConn.connect();
				
				String setCookie = loginConn.getHeaderField("set-cookie");
				if (setCookie == null) {
					throw new Exception("App login did not return set-cookie header.");
				}
				if (setCookie != null) {
				setCookie = setCookie.trim();
					// Example: SACSID=AJKiYcEjM9OgNhd67-P4xsRHiK199ZNeC3WH6B7vXvPBfIu6z7meyGcDwnO9CncSFn5m4BqQSszLBuBlrzU12nFzabu0em5PiNk_3FLIbKsTlve_CKrCJzGWAnEmLqJjE-0fXy4gVStmvSoUfT1OmuUP0ThWx7AItoDSjbPsiAQsz9CWOIOAGOStY-n_pVq1gcrlsfq0GUNuG4HnQrEcw0SsW56SYqcwT18toZo_S29ao1Wu4LmB0LkNW5FCfQyheqpnY9d7RK8I8IweaLSVX2iwS0AX6f8FaIJn2Hunf1ppug4E6_Fpbn1UcOeP4GRaQ69hM8_khmtXa1QRBUGHFvAxK8-Uha0kEZgODXwMYiu59lIUtEzKbni5G2UDqM-KcrckKSDjuBJ8XGcfSdRYsOOikCYtJ-w_ft-teV0pC0MWIRSW_JxmAX0nwvx8BX-dUmrf06mSI7eZSB3NY3uQVRoxNASjXTqX6s_nPLS8Bt3vEqr7BiG56Y7lYxYuzgL3wJZ5uwW2LV1wcypvz_Kvaozeb8LbKKmvOqngwuK1S6LwG1fYOAfG4mgZvQxryuterzjZehQLmAEEQbI7WPjmZhqkAxOVbY-Q-iTz6fZ403taYkrso8a8o-zeqHAetmDeS2Q1HG4TvxFJr9BIf9c6gGW2XW7bGsVQfcZu_9Kgx7fVG5Qn1pmCcLU; expires=Fri, 21-Oct-2011 21:55:52 GMT; path=/; secure
					if (false == setCookie.toUpperCase().startsWith("SACSID=")) {
						throw new Exception("App login did not return SACSID cookie.");
					}
				}
				_cookie = setCookie;
				System.out.println("Cookie: " + _cookie);
			}			
		}
	}
	
	public String makeSecureUrlCall(String urlString) throws IOException {
		return makeSecureUrlCall(urlString, null, "UTF-8", null);
	}
	
	public String makeSecureUrlCall(String urlString, byte[] bytes) throws IOException {
		return makeSecureUrlCall(urlString, null, "UTF-8", bytes);
	}
	
	public String makeSecureUrlCall(String urlString, Map<String, String> postData) throws IOException {
		return makeSecureUrlCall(urlString, postData, "UTF-8", null);
	}
	
	public String makeSecureUrlCall(String urlString, Map<String, String> postData, String encoding, byte[] bytes) throws IOException {
		
		if (encoding == null || encoding.length() == 0) {
			throw new InvalidParameterException("encoding must not be empty");
		}
		
		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();

		// add security information
		if (_AUTHHeader != null) {
			conn.addRequestProperty("Authorization", _AUTHHeader);
		}
		if (_cookie != null) {
			conn.addRequestProperty("Cookie", _cookie);
		}

		// add post data
		if (postData != null && postData.size() > 0) {
			
			if (conn instanceof HttpURLConnection) {
				((HttpURLConnection)conn).setRequestMethod("POST");
			}
			
			StringBuilder sb = new StringBuilder();
			for (String key : postData.keySet()) {
				String value = postData.get(key);
				if (sb.length() > 0) {
					sb.append("&");
				}
				sb.append(URLEncoder.encode(key, encoding));
				sb.append("=");
				sb.append(URLEncoder.encode(value, encoding));
			}
			conn.setDoOutput(true);
			OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), encoding);
			try {
			    writer.write(sb.toString());
			    writer.flush();
			} finally {
				writer.close();
			}

		} else if (bytes != null) {
			
			if (conn instanceof HttpURLConnection) {
				((HttpURLConnection)conn).setRequestMethod("POST");
			}

			conn.setRequestProperty("Content-Type", "application/octet-stream");
			
			conn.setDoOutput(true);

			conn.getOutputStream().write(bytes);
			conn.getOutputStream().flush();
			conn.getOutputStream().close();

		} else {
			
			if (conn instanceof HttpURLConnection) {
				((HttpURLConnection)conn).setRequestMethod("GET");
			}
		}
		
		conn.connect();
		
		StringBuffer sb = new StringBuffer();
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), encoding));

		try {
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} finally {
			reader.close();
		}
		
		return sb.toString();
	}
}
