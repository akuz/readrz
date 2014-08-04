package com.readrz.feedsget;

import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.akuz.core.http.HttpGetCall;
import me.akuz.core.http.HttpGetKind;

import org.apache.commons.lang.StringEscapeUtils;

import com.readrz.data.Feed;
import com.readrz.data.Snap;
import com.readrz.lang.parse.PreprocessUtils;

public final class Getter {
	
	// comment: *? and +? means "lazy" matching
	private static Pattern _patternEntry = Pattern.compile("(<entry.+?</entry>)");
	private static Pattern _patternItem = Pattern.compile("(<item.+?</item>)");
	private static Pattern _patternSource = Pattern.compile("(<source[^>]*>)(.*?)(</source>)");
	private static Pattern _patternLinkHref  = Pattern.compile("(<link.+?href=[\"'])([^\"']+)([^>]*?>)");
	private static Pattern _patternLinkInner = Pattern.compile("(<link)([^>]*?>)(.+?)(</link>)");
	private static Pattern _patternTitle = Pattern.compile("(<title[^>]*>)(.*?)(</title>)");
	private static Pattern _patternSummary = Pattern.compile("(<summary[^>]*>)(.*?)(</summary>)");
	private static Pattern _patternDescription = Pattern.compile("(<description[^>]*>)(.*?)(</description>)");
	private static Pattern _patternPubDate = Pattern.compile("(<pubDate[^>]*>)(.*?)(</pubDate>)");
	private static Pattern _patternUpdated = Pattern.compile("(<updated[^>]*>)(.*?)(</updated>)");
	private static Pattern _patternPublished = Pattern.compile("(<published[^>]*>)(.*?)(</published>)");
	private static Pattern _patternCreated = Pattern.compile("(<created[^>]*>)(.*?)(</created>)");
	private static Pattern _patternA = Pattern.compile("(<a.*?href=[\"'])([^\"']+)([^>].*?>)");
	
	public static GetterOutput getOutput(FeedGet feedGet) throws Exception {
		
		Feed feed = feedGet.getFeed();
		String urlString = feed.getUrl();
		HttpGetCall httpGetCall = new HttpGetCall(1, HttpGetKind.Text, urlString, "UTF-8");
		httpGetCall.call();
		if (httpGetCall.getException() != null) {
			throw new IOException("Could not download URL: " + urlString, httpGetCall.getException());
		}
		return parseResults(feed, httpGetCall.getResultText());
	}

	private static GetterOutput parseResults(Feed feed, String sourceStr) throws Exception {
		
		GetterOutput output = new GetterOutput();
		int counter;

		// entries
		counter = 0;
		Matcher matcherEntry = _patternEntry.matcher(sourceStr);
		while (matcherEntry.find()) {
			
			String entryStr = matcherEntry.group(1);

			try {
				Snap snap = parseOneItem(feed, entryStr);
				output.addSnap(snap);
				
			} catch (Exception ex) {

				if (!(ex instanceof AcceptableException)) {
					ex.printStackTrace(System.out);
				}
				System.out.println("ERROR: Could not parse entry #" + counter + " from feed " + feed.getUrl());
				System.out.println("EXCEPTION: " + ex.getMessage());
			}
			counter++;
		}
		
		// items
		counter = 0;
		Matcher matcherItem = _patternItem.matcher(sourceStr);
		while (matcherItem.find()) {
			
			String itemStr = matcherItem.group(1);

			try {
				Snap snap = parseOneItem(feed, itemStr);
				output.addSnap(snap);
				
			} catch (Exception ex) {

				if (!(ex instanceof AcceptableException)) {
					ex.printStackTrace(System.out);
				}
				System.out.println("ERROR: Could not parse item #" + counter + " from feed " + feed.getUrl());
				System.out.println("EXCEPTION: " + ex.getMessage());
			}
			counter++;
		}
		
		if (output.getSnapCount() == 0) {
			String msg = "WARNING: No items in feed " + feed.getUrl();
			System.out.println(msg);
		}

		return output;
	}

	private static Snap parseOneItem(Feed feed, String entryStr) throws Exception {
		
		// remove <source> tag
		{
			Matcher mSource = _patternSource.matcher(entryStr);
			entryStr = mSource.replaceAll("");
		}
		
		// extract link
		String url = "";
		if (url.startsWith("http") == false) {
			Matcher mLink = _patternLinkInner.matcher(entryStr);
			if (mLink.find()) {
				url = removeCDATA(mLink.group(3).trim());
			}
		}
		if (url.startsWith("http") == false) {
			Matcher mLink = _patternLinkHref.matcher(entryStr);
			if (mLink.find()) {
				url = removeCDATA(mLink.group(2).trim());
			}
		}
		
		// extract title
		String titleHtml = null;
		Matcher matcherTitle= _patternTitle.matcher(entryStr);
		if (matcherTitle.find()) {
			titleHtml = removeCDATA(matcherTitle.group(2));
			titleHtml = StringEscapeUtils.unescapeHtml(titleHtml);
		} else {
			throw new AcceptableException("Title not found");
		}
		
		// try to extract link from title
		if (url.startsWith("http") == false) {
			Matcher mA = _patternA.matcher(titleHtml);
			if (mA.find()) {
				url = removeCDATA(mA.group(2).trim());
			}
		}
		
		// get title
		String title = PreprocessUtils.removeTags(titleHtml);
		title = PreprocessUtils.titleUnescape(title);

		// get summary
		String textHtml = "";
		if (textHtml == null || textHtml.length() == 0) {
			Matcher mSummary = _patternSummary.matcher(entryStr);
			if (mSummary.find()) {
				textHtml = removeCDATA(mSummary.group(2));
				textHtml = StringEscapeUtils.unescapeHtml(textHtml).trim();
			}
		}
		if (textHtml == null || textHtml.length() == 0) {
			Matcher mSummary = _patternDescription.matcher(entryStr);
			if (mSummary.find()) {
				textHtml = removeCDATA(mSummary.group(2));
				textHtml = StringEscapeUtils.unescapeHtml(textHtml).trim();
			}
		}

		// remove all tags from html
		String text = PreprocessUtils.removeTags(textHtml);
		text = PreprocessUtils.textUnescape(text);

		String srcDateStr = null;
		
		if (srcDateStr == null) {
			Matcher m = _patternPubDate.matcher(entryStr);
			if (m.find()) {
				srcDateStr = removeCDATA(m.group(2));
			}
		}
		if (srcDateStr == null) {
			Matcher m = _patternUpdated.matcher(entryStr);
			if (m.find()) {
				srcDateStr = removeCDATA(m.group(2));
			}
		}
		if (srcDateStr == null) {
			Matcher m = _patternPublished.matcher(entryStr);
			if (m.find()) {
				srcDateStr = removeCDATA(m.group(2));
			}
		}
		if (srcDateStr == null) {
			Matcher m = _patternCreated.matcher(entryStr);
			if (m.find()) {
				srcDateStr = removeCDATA(m.group(2));
			}
		}
		if (srcDateStr == null) {
			throw new AcceptableException("Source date not found");
		}
		
		// parse source date
		Date srcDate;
		try {
			srcDate = DateFormats.instance.parse(srcDateStr);
		} catch (Exception e) {
			throw new Exception("Could not parse date: " + srcDateStr, e);
		}
		
		// create new snap
		Snap snap = new Snap(feed.getId(), srcDate, title, url, text);
		
		return snap;
	}
	
	private final static String removeCDATA(String str) {
		
		// Example: <![CDATA[Thu, 27 Jun 2013 19:42:35 PDT]]>
		
		return str.trim().replaceAll("^<!\\[CDATA\\[", "").replaceAll("\\]\\]>$", "");
	}

}
