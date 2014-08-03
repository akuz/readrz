package com.readrz.www;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import me.akuz.core.gson.GsonSerializers;

import com.google.gson.Gson;
import com.readrz.http.JsonResponseOld;

public final class HttpResponseUtils {
	
	public static void writeHtmlUtf8Response(HttpServletResponse resp, String html) throws IOException {
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().println(html);
	}
	
	public static void writeBytesResponse(HttpServletResponse resp, byte[] bytes, String contentType) throws IOException {
        resp.setContentType(contentType);
        if (bytes != null) {
	        resp.setContentLength(bytes.length);
	        resp.getOutputStream().write(bytes);
        } else {
        	resp.setContentLength(0);
        }
	}
	
	public static void writeJsonUtf8Response(HttpServletResponse resp, String text) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().println(text);
	}
	
	public static void writePlainUtf8Response(HttpServletResponse resp, String text) throws IOException {
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().println(text);
	}
	
	public static void writeXmlUtf8Response(HttpServletResponse resp, String xml) throws IOException {
        resp.setContentType("text/xml");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().println(xml);
	}
	
	public static final void writeJsonResponse(HttpServletResponse resp, JsonResponseOld response) throws IOException {
		Gson gson = GsonSerializers.NoHtmlEscapingPretty;
		String responseJson = gson.toJson(response);
		writePlainUtf8Response(resp, responseJson);
	}
}