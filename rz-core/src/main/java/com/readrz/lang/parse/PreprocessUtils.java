package com.readrz.lang.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.readrz.utils.EscapeUtils;

import me.akuz.core.Hit;

public final class PreprocessUtils {

	private static Pattern _patternTag = Pattern.compile("\\s*<[^>]*?>\\s*");
	
	public static final String removeTags(String html) {
		
		// replace all tags with spaces
		Matcher m = _patternTag.matcher(html);
		String text = m.replaceAll(" ");
		
		return text;
	}
	
	public static final String titleUnescape(String str) {
		str = EscapeUtils.unescapeHtmlMultilevel(str).trim();
		str = str.replaceAll("\\s{2,}", " ");
		return str;
	}

	public static final String textUnescape(String str) {
		str = EscapeUtils.unescapeHtmlMultilevel(str).trim();
		str = str.replaceAll("\\s{4,}", "   ");
		return str;
	}

	private static final Pattern _videoStart = Pattern.compile("^\\s*video\\s*:\\s*", Pattern.CASE_INSENSITIVE);
	private static final Pattern _videoEnd = Pattern.compile("\\s*\\(\\s*video\\s*\\)\\s*$", Pattern.CASE_INSENSITIVE);
	private static final Pattern _verticalPipe = Pattern.compile("\\s*\\|\\s*");
	
	public static final Hit titleFindBounds(String title) {
		
		if (title == null) {
			return Hit.Empty;
		}
		
		int start = 0;
		int end = title.length();
		if (end > Short.MAX_VALUE) {
			end = title.lastIndexOf(" ", Short.MAX_VALUE);
			if (end < 0) {
				end = Short.MAX_VALUE;
			}
		}
		
		final int CHARS_FROM_EDGE = 50;
		Matcher m;
		
		m = _verticalPipe.matcher(title);
		m.region(0, Math.min(title.length(), CHARS_FROM_EDGE));
		while (m.find()) {
			if (start < m.end() && m.end() < title.length() / 2) {
				start = m.end();
			}
		}
		m = _verticalPipe.matcher(title);
		m.region(Math.max(0, title.length() - CHARS_FROM_EDGE), title.length());
		if (m.find()) {
			if (end > m.start() && m.start() > title.length() / 2) {
				end = m.start();
			}
		}
		m = _videoStart.matcher(title);
		if (m.find()) {
			if (start < m.end()) {
				start = m.end();
			}
		}
		m = _videoEnd.matcher(title);
		if (m.find()) {
			if (end > m.start()) {
				end = m.start();
			}
		}

		if (start <= end) {
			return new Hit(start, end);
		} else {
			return Hit.Empty;
		}
	}
	
	private static final Pattern _agencyIntro = Pattern.compile("\\)\\s*[\\-–—]\\s*");
	private static final Pattern _followOnTwitter = Pattern.compile("follow\\.{1,40}on\\s+twitter", Pattern.CASE_INSENSITIVE);
	private static final Pattern _joinTheConversation = Pattern.compile("join(\\s+the)?\\s+conversation", Pattern.CASE_INSENSITIVE);
	private static final Pattern _seeAlso = Pattern.compile("see\\s+also", Pattern.CASE_INSENSITIVE);
	private static final Pattern _allRightsReserved = Pattern.compile("all\\s+rights\\s+reserved", Pattern.CASE_INSENSITIVE);
	private static final Pattern _theRestOfTheStory = Pattern.compile("(see)?\\s+the\\s+rest\\s+of\\s+the\\s+(article|story)", Pattern.CASE_INSENSITIVE);
	private static final Pattern _readFullArticle = Pattern.compile("(read)?(\\s+the)?\\s+full\\s+(article|story)", Pattern.CASE_INSENSITIVE);
	private static final Pattern _relatedStories = Pattern.compile("related\\s+(stories|articles)", Pattern.CASE_INSENSITIVE);
	
	public static Hit textFindBounds(String text) {
		
		if (text == null) {
			return Hit.Empty;
		}
		
		int start = 0;
		int end = text.length();
		if (end > Short.MAX_VALUE) {
			end = text.lastIndexOf(" ", Short.MAX_VALUE);
			if (end < 0) {
				end = Short.MAX_VALUE;
			}
		}

		Matcher m;
		
		final int CHARS_FROM_START = 50;
		m = _agencyIntro.matcher(text);
		m.region(0, Math.min(text.length(), CHARS_FROM_START));
		while (m.find()) {
			if (start < m.end()) {
				start = m.end();
			}
		}
		
		final int CHARS_FROM_END = 500;
		
		m = _followOnTwitter.matcher(text);
		m.region(Math.max(0, text.length() - CHARS_FROM_END), text.length());
		if (m.find()) {
			if (end > m.start()) {
				end = m.start();
			}
		}
		
		m = _joinTheConversation.matcher(text);
		m.region(Math.max(0, text.length() - CHARS_FROM_END), text.length());
		if (m.find()) {
			if (end > m.start()) {
				end = m.start();
			}
		}
		
		m = _seeAlso.matcher(text);
		m.region(Math.max(0, text.length() - CHARS_FROM_END), text.length());
		if (m.find()) {
			if (end > m.start()) {
				end = m.start();
			}
		}
		
		m = _allRightsReserved.matcher(text);
		m.region(Math.max(0, text.length() - CHARS_FROM_END), text.length());
		if (m.find()) {
			if (end > m.start()) {
				end = m.start();
			}
		}
		
		m = _theRestOfTheStory.matcher(text);
		m.region(Math.max(0, text.length() - CHARS_FROM_END), text.length());
		if (m.find()) {
			if (end > m.start()) {
				end = m.start();
			}
		}
		
		m = _readFullArticle.matcher(text);
		m.region(Math.max(0, text.length() - CHARS_FROM_END), text.length());
		if (m.find()) {
			if (end > m.start()) {
				end = m.start();
			}
		}
		
		m = _relatedStories.matcher(text);
		m.region(Math.max(0, text.length() - CHARS_FROM_END), text.length());
		if (m.find()) {
			if (end > m.start()) {
				end = m.start();
			}
		}
		
		if (start <= end) {
			return new Hit(start, end);
		} else {
			return Hit.Empty;
		}
	}
}
