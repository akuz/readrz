package com.readrz.imagscan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.akuz.core.Hit;
import me.akuz.core.HitsUtils;
import me.akuz.core.Pair;
import me.akuz.core.logs.LogUtils;
import me.akuz.core.math.SampleVariance;
import me.akuz.core.math.StatsUtils;
import me.akuz.nlp.detect.WordsDetector;
import me.akuz.nlp.porter.PorterStemmer;

import com.readrz.lang.parse.PreprocessUtils;

public final class ExtractImages {

	private static final Logger _log = LogUtils.getLogger(ExtractImages.class.getName());
	
	private final WordsDetector _wordsDetector;
	private final Pattern _blockedUrlsPattern;
	
	// <meta property="og:image" content="http://msnbcmedia2.msn.com/j/streams/2013/September/130912/8C8959948-costaship.blocks_desktop_tease.JPG" />
	// <meta name="twitter:image:src" content="http://msnbcmedia2.msn.com/j/streams/2013/September/130912/8C8959948-costaship.blocks_desktop_tease.JPG" />

	
	private final static int    TEXT_MIN_KEY_WORDS = 20;
	private final static int    TEXT_MAX_KEY_WORDS = 50;
	private final static double TEXT_KEY_WORDS_TO_FIND_FRAQ = 0.75;
	private final static double TEXT_KEY_WORDS_DEVIATION_SIGMAS = 10.0;

	private final static int    IMAGE_AT_LEAST_WIDTH_OR_LENGTH = 200;
	private final static int    IMAGE_MAX_WIDTH_OR_LENGTH = 800;
	private final static double IMAGE_MIN_ASPECT_RATIO = 0.5;
	private final static double IMAGE_MAX_ASPECT_RATIO = 2.0;
	private final static int    IMAGE_MIN_SRC_LENGTH = 5;
	private final static int    IMAGE_MAX_SRC_LENGTH = 255;
	
	private static final Pattern _metaFacebookPattern = Pattern.compile(
			"<meta[^>]+=\"og:image\"[^>]+content=\"([^\"]+)\"[^>]*>", Pattern.CASE_INSENSITIVE);
	
	private static final Pattern _metaTwitterPattern = Pattern.compile(
			"<meta[^>]+=\"twitter:image:src\"[^>]+content=\"([^\"]+)\"[^>]*>", Pattern.CASE_INSENSITIVE);
	
	private static final Pattern _bodyPattern = Pattern.compile(
			"<body[^>]*>(.+)</body>", Pattern.CASE_INSENSITIVE);
	
	private static final Pattern _imgPattern = Pattern.compile(
			"<img\\s+[^>]+>", Pattern.CASE_INSENSITIVE);
	
	private static final Pattern _srcAttrPattern = Pattern.compile(
			"\\s+src=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
	
	private static final Pattern _altAttrPattern = Pattern.compile(
			"\\s+alt=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE);
	
	private static final Pattern _widthAttrPattern = Pattern.compile(
			"\\s+width=\"(\\d+)\"", Pattern.CASE_INSENSITIVE);
	
	private static final Pattern _heightAttrPattern = Pattern.compile(
			"\\s+height=\"(\\d+)\"", Pattern.CASE_INSENSITIVE);

	public ExtractImages(PorterStemmer porterStemmer, Set<String> stopStems, Pattern blockedUrlsPattern) {
		_wordsDetector = new WordsDetector(porterStemmer, stopStems);
		_blockedUrlsPattern = blockedUrlsPattern;
	}

	public List<Pair<String, Double>> extractImageUrlsRanked(
			String title, 
			String summary,
			String html) {
		
		// prepare result list
		List<Pair<String, Double>> imageUrlsRanked = new ArrayList<>();
		
		{ // try to find facebook meta
			
			Matcher m = _metaFacebookPattern.matcher(html);
			if (m.find()) {
				String src = m.group(1);
				
				if (_blockedUrlsPattern == null ||
					_blockedUrlsPattern.matcher(src).find() == false) {
				
					imageUrlsRanked.add(new Pair<String, Double>(src, 1.0));
					return imageUrlsRanked;
				}
			}
		}
		
		{ // try to find twitter meta
			
			Matcher m = _metaTwitterPattern.matcher(html);
			if (m.find()) {
				String src = m.group(1);
				
				if (_blockedUrlsPattern == null ||
					_blockedUrlsPattern.matcher(src).find() == false) {
					
					imageUrlsRanked.add(new Pair<String, Double>(src, 1.0));
					return imageUrlsRanked;
				}
			}
		}
		
		// find body tag
		Hit bodyHit;
		{
			Matcher mBody = _bodyPattern.matcher(html);
			if (mBody.find()) {
			
				// get body bounds
				bodyHit = new Hit(mBody.start(1), mBody.end(1));

			} else {
				
				_log.finest("No body tag");

				// no body tag, no images
				return imageUrlsRanked;
			}
		}
		
		// find images in body
		List<ImageTag> imageTags = new ArrayList<>();
		{
			Matcher m = _imgPattern.matcher(html);
			m.region(bodyHit.start(), bodyHit.end());
			
			while (m.find()) {
				
				// create image hit
				Hit imgHit = new Hit(m.start(), m.end());
				
				// find image src
				String src = null;
				{
					Matcher mSrc = _srcAttrPattern.matcher(html);
					mSrc.region(imgHit.start(), imgHit.end());
					if (mSrc.find()) {
						src = mSrc.group(1);
						if (src.length() < IMAGE_MIN_SRC_LENGTH ||
							src.length() > IMAGE_MAX_SRC_LENGTH) {
							continue;
						}
						if (_blockedUrlsPattern != null) {
							if (_blockedUrlsPattern.matcher(src).find()) {
								continue;
							}
						}
					} else {
						continue;
					}
				}
				
				// find image alt
				String alt = null;
				{
					Matcher mAlt = _altAttrPattern.matcher(html);
					mAlt.region(imgHit.start(), imgHit.end());
					if (mAlt.find()) {
						alt = mAlt.group(1);
					}
				}
				
				// check image size
				{
					Matcher mWidth = _widthAttrPattern.matcher(html);
					mWidth.region(imgHit.start(), imgHit.end());
					if (mWidth.find()) {

						Matcher mHeight = _heightAttrPattern.matcher(html);
						mHeight.region(imgHit.start(), imgHit.end());
						if (mHeight.find()) {
						
							int width = Integer.parseInt(mWidth.group(1));
							int height = Integer.parseInt(mHeight.group(1));
							
							if (isImageSizeOK(width, height) == false) {
								continue;
							}
						}
					}
				}
				
				// potentially valid image
				{
					ImageTag imageTag = new ImageTag(src, alt, imgHit);
					imageTags.add(imageTag);
				}
			}
		}
		
		// check some image tags found
		if (imageTags == null || imageTags.size() == 0) {
			
			_log.finest("No image tags");

			// no image tags, no images
			return imageUrlsRanked;
		}
		
		// prepare key words pattern
		Pattern keyWordsPattern;
		Set<String> keyWords = new HashSet<>();
		{
			// collect key words
			if (title != null) {
				collectKeyWords(title, keyWords);
			}
			if (summary != null) {
				collectKeyWords(summary, keyWords);
			}
			
			// check enough key words found
			if (keyWords.size() < TEXT_MIN_KEY_WORDS) {
				
				_log.finest("Not enough key words (" + keyWords.size() + ")");
				
				// not enough key words to find, no images
				return imageUrlsRanked;
			}
			
			// create key words regex
			StringBuilder sb = new StringBuilder();
			for (String keyWord : keyWords) {
				
				if (sb.length() > 0) {
					sb.append("|");
				}
				sb.append("(?:");
				sb.append(keyWord);
				sb.append(")");
			}

			// compile key words pattern
			keyWordsPattern = Pattern.compile(sb.toString(), Pattern.CASE_INSENSITIVE);
		}
		
		// convert html into text
		String text;
		{
			StringBuilder sb = new StringBuilder();
			for (int i=0; i<imageTags.size(); i++) {
				
				ImageTag imageTag = imageTags.get(i);
				Hit imageHit = imageTag.getHit();
				
				{ // append block before image
					
					String htmlBlock;
					if (i == 0) {
						htmlBlock = html.substring(bodyHit.start(), imageHit.start());
					} else {
						ImageTag prevImageTag = imageTags.get(i-1);
						Hit prevImageHit = prevImageTag.getHit();
						htmlBlock = html.substring(prevImageHit.end(), imageHit.start());
					}
					String textBlock = PreprocessUtils.removeTags(htmlBlock);
					textBlock = PreprocessUtils.textUnescape(textBlock);
					sb.append(textBlock);
				}
				
				// remember image position
				int imagePosition = sb.length();
				
				// append block after last image
				if (i == imageTags.size()-1) {
					String htmlBlock = html.substring(imageHit.end(), bodyHit.end());
					String textBlock = PreprocessUtils.removeTags(htmlBlock);
					textBlock = PreprocessUtils.textUnescape(textBlock);
					sb.append(textBlock);
				}
				
				// update image hit
				Hit newImageHit = new Hit(imagePosition, imagePosition);
				imageTag.setHit(newImageHit);
			}
			
			// get html converted to text
			text = sb.toString();
		}
		
		// collect key words hits
		List<Hit> keyWordsHits = new ArrayList<>();
		{
			// estimate key words to find
			final int keyWordsToFindCount = (int)(TEXT_KEY_WORDS_TO_FIND_FRAQ * keyWords.size());
			
			{ // find key word hits in text
				
				Matcher m = keyWordsPattern.matcher(text);
				while (m.find()) {
					Hit keyWordHit = new Hit(m.start(), m.end());
					keyWordsHits.add(keyWordHit);
					if (keyWordsHits.size() >= keyWordsToFindCount) {
						break;
					}
				}
			}
			
			// check enough key word hits found
			if (keyWordsHits.size() < keyWordsToFindCount) {
				
				_log.finest("Not enough key words found");

				// not enough key words in text, no images
				return imageUrlsRanked;
			}
		}
		
		// find key word hits median and sigma
		Hit medianKeyWordHit = keyWordsHits.get(keyWordsHits.size() / 2);
		double keyWordHitsSigma;
		{
			SampleVariance keyWordHitsVariance = new SampleVariance();
			for (int i=0; i<keyWordsHits.size(); i++) {
				Hit keyWordHit = keyWordsHits.get(i);
				int diff = keyWordHit.start() - medianKeyWordHit.start();
				keyWordHitsVariance.add(diff);
			}
			keyWordHitsSigma = Math.sqrt(keyWordHitsVariance.getVariance());
		}
		
		{ // select images around found key word hits

			int maxDistanceFromMedianKeyHit = (int)(keyWordHitsSigma * TEXT_KEY_WORDS_DEVIATION_SIGMAS);

			_log.finest("Images text area: [" 
					+ (medianKeyWordHit.start() - maxDistanceFromMedianKeyHit) + ", " 
					+ (medianKeyWordHit.start() + maxDistanceFromMedianKeyHit) + "], text length: " + text.length());

			for (int i=0; i<imageTags.size(); i++) {
				
				ImageTag imageTag = imageTags.get(i);
				Hit imageHit = imageTag.getHit();
				
				// check image is within the allowed distance from key hits
				int distanceFromMedianKeyHit = imageHit.distanceTo(medianKeyWordHit);
				if (distanceFromMedianKeyHit > maxDistanceFromMedianKeyHit) {
					continue;
				}
				
				// calculate image rank based on distance from median
				double rank = StatsUtils.calcDistanceWeightExponential(distanceFromMedianKeyHit, maxDistanceFromMedianKeyHit);
				
				// create result ranked image entry
				imageUrlsRanked.add(new Pair<String, Double>(imageTag.getSrc(), rank));
			}
		}
		
		return imageUrlsRanked;
	}

	public static boolean isImageSizeOK(int width, int height) {
		
		if (height <= 0 || width <= 0) {
			return false;
		}
		if (width  < IMAGE_AT_LEAST_WIDTH_OR_LENGTH && 
			height < IMAGE_AT_LEAST_WIDTH_OR_LENGTH) {
			return false;
		}
		if (width > IMAGE_MAX_WIDTH_OR_LENGTH) {
			return false;
		}
		if (height > IMAGE_MAX_WIDTH_OR_LENGTH) {
			return false;
		}
		double aspectRatio = (double)height / (double)width;
		if (aspectRatio < IMAGE_MIN_ASPECT_RATIO) {
			return false;
		}
		if (aspectRatio > IMAGE_MAX_ASPECT_RATIO) {
			return false;
		}
		return true;
	}

	public final void collectKeyWords(String str, Set<String> keyWords) {

		Map<String, List<Hit>> hitsMap = _wordsDetector.extractHitsByStem(str, null);
		if (hitsMap != null) {
			
			List<Hit> uniqueHits =
					HitsUtils.getUniqueHits(
						HitsUtils.flattenHitMaps(hitsMap));
			
			for (int i=0; i<uniqueHits.size(); i++) {
				if (keyWords.size() >= TEXT_MAX_KEY_WORDS) {
					break;
				}
				Hit hit = uniqueHits.get(i);
				String word = str.substring(hit.start(), hit.end());
				keyWords.add(word.toLowerCase());
			}
		}
	}

}
