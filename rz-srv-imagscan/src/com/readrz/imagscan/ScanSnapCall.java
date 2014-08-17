package com.readrz.imagscan;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import me.akuz.core.DateUtils;
import me.akuz.core.Pair;
import me.akuz.core.StringUtils;
import me.akuz.core.Triple;
import me.akuz.core.UrlUtils;
import me.akuz.core.http.HttpGetCall;
import me.akuz.core.http.HttpGetKind;
import me.akuz.core.logs.LogUtils;

import com.readrz.data.Snap;
import com.readrz.data.SnapHtml;
import com.readrz.data.SnapImag;

public final class ScanSnapCall implements Comparable<ScanSnapCall>, Callable<Boolean> {
	
	private static final Logger _log = LogUtils.getLogger(ScanSnapCall.class.getName());

	private static final int MAX_ATTEMPT_HTML_RETRIES = 3;
	private static final int MAX_ATTEMPT_IMAGE_RETRIES = 2;
	
	private static final int MAX_ATTEMPTS = 6;
	private static final int NEXT_ATTEMPT_DELAY_MINS = 10;
	
	private final ScanEngine _engine;
	private final Snap _snap;
	private int _attempt;
	private Date _dueDate;
	
	public ScanSnapCall(ScanEngine engine, Snap snap, int attempt, Date dueDate) {
		_engine = engine;
		_snap = snap;
		_attempt = attempt;
		_dueDate = dueDate;
	}
	
	public Snap getSnap() {
		return _snap;
	}
	
	public int getAttempt() {
		return _attempt;
	}
	
	public Date getDueDate() {
		return _dueDate;
	}
	
	public void setNextAttempt() {
		_attempt += 1;
		_dueDate = DateUtils.addMinutes(new Date(), NEXT_ATTEMPT_DELAY_MINS);
	}

	@Override
	public int compareTo(ScanSnapCall o) {
		if (_dueDate == null) {
			if (o._dueDate == null) {
				return 0;
			} else {
				return -1;
			}
		} else {
			if (o._dueDate == null) {
				return 1;
			} else {
				return _dueDate.compareTo(o._dueDate);
			}
		}
	}
	
	@Override
	public String toString() {
		return String.format("%s - %s: %s", 
				getClass().getSimpleName(), 
				_snap.getClass().getSimpleName(), 
				_snap.getId().toString());
	}

	@Override
	public Boolean call() throws Exception {

		try {
		
			_log.finer("Executing " + this);
			
			// prepare data
			String html = null;
			String actualUrl = null;
			
			// try to load html from database
			_log.finest("Trying to find HTML: " + _snap.getUrl());
			{
				SnapHtml snapHtml = SnapHtml.find(_engine.getSnapsHtmlColl(), _snap.getId());
				if (snapHtml != null) {
	
					_log.finest("Found downloaded HTML: " + _snap.getUrl());
					
					html = snapHtml.getHtml();
					actualUrl = _snap.getActualUrl();
				}
			}
			
			// try to download snap html
			if (html == null || actualUrl == null) {
				
				_log.finest("Downloading HTML: " + _snap.getUrl());
				
				// download snap html
				HttpGetCall htmlCall = new HttpGetCall(MAX_ATTEMPT_HTML_RETRIES, HttpGetKind.Text, _snap.getUrl(), "UTF-8");
				if (htmlCall.call()) {
		
					_log.finest("Downloaded HTML: " + _snap.getUrl());

					// get scan results
					html = htmlCall.getResultText();
					actualUrl = htmlCall.getResultUrl();
					
				} else { // html get failed
					
					_log.log(
							Level.FINER, 
							"Failed for download HTML: " + _snap.getUrl(), 
							htmlCall.getException());

					return removeOrReattempt(
							Level.FINER);
				}
			}

			// if got html, parse and scan for images
			Pair<BufferedImage, String> bestImage = null;
			if (html != null && actualUrl != null) {
				
				// extract candidate image urls
				List<Pair<String, Double>> imageUrlsRanked
					= _engine.getExtractImages()
						.extractImageUrlsRanked(
							_snap.getTitle(),
							_snap.getText(),
							html);
				
				if (imageUrlsRanked != null && imageUrlsRanked.size() > 0) {
					
					_log.finest("Found " + imageUrlsRanked.size() + " image urls in: " + _snap.getUrl());
					
					// download images
					List<Triple<BufferedImage, String, Double>> imagesRanked = new ArrayList<>();
					for (int i=0; i<imageUrlsRanked.size(); i++) {
						
						Pair<String, Double> pair = imageUrlsRanked.get(i);
						
						String imageUrl = pair.v1();
						Double imageRank = pair.v2();
						String absoluteImageUrl = UrlUtils.absolutizeUrl(actualUrl, imageUrl);
						
						// download the image
						_log.finest("Downloading image: " + absoluteImageUrl);
						HttpGetCall imageCall = new HttpGetCall(MAX_ATTEMPT_IMAGE_RETRIES, HttpGetKind.Image, absoluteImageUrl, "UTF-8");
						if (imageCall.call()) {
							
							BufferedImage image = imageCall.getResultImage();
							if (image != null) {
								imagesRanked.add(new Triple<BufferedImage, String, Double>(image, absoluteImageUrl, imageRank));
								_log.finest("Image: [" + image.getWidth() + " x " + image.getHeight() + "] " + absoluteImageUrl);
							} else {
								_log.finest("NULL image: " + absoluteImageUrl);
							}
						} else {
							_log.log(
									Level.FINEST,
									"FAILED image: " + absoluteImageUrl,
									imageCall.getException());
						}
					}
					
					_log.finest("Downloaded " + imagesRanked.size() + " images from: " + _snap.getUrl());

					// select best image
					double bestAdjImageRank = 0;
					for (int i=0; i<imagesRanked.size(); i++) {
						
						Triple<BufferedImage, String, Double> triple = imagesRanked.get(i);
						BufferedImage image = triple.v1();
						String imageUrl = triple.v2();
						Double imageRank = triple.v3();
						int height = image.getHeight();
						int width = image.getWidth();
						
						// check image size
						if (ExtractImages.isImageSizeOK(width, height)) {
						
							// adjusted image rank
							double imageArea = width * height;
							double adjImageRank = imageRank * imageArea;
							
							// update best image
							if (bestImage == null || bestAdjImageRank < adjImageRank) {
								
								bestImage = new Pair<>(image, imageUrl);
								bestAdjImageRank = adjImageRank;
							}
						}
					}
	
					if (bestImage == null) {
						_log.finest("-------- No images are good for: \n" + _snap.getUrl() 
								+ "\n ----- " + StringUtils.collectionToString(imageUrlsRanked, "\n ----- "));
					}
				} else {
					
					_log.finest("-------- No image urls for: \n" + _snap.getUrl());
				}
				
			} else {
				
				_log.finest("######## No HTML or actualURL: \n" + _snap.getUrl());
			}
			
			if (bestImage != null) {
				_log.finest("++++++++ Found best image for: \n" + _snap.getUrl() + "\n" + bestImage.v2());
			}
			
			// save snap image
			if (bestImage != null) {
				SnapImag snapImag = new SnapImag(_snap.getId(), bestImage.v2(), bestImage.v1());
				snapImag.upsertUnacknowledged(_engine.getSnapsImagColl());
			}
			
			// save snap html
			if (html != null) {
				SnapHtml snapHtml = new SnapHtml(_snap.getId(), html);
				snapHtml.upsertUnacknowledged(_engine.getSnapsHtmlColl());
			}
	
			// save actual url
			if (actualUrl != null) {
				_snap.setActualUrl(actualUrl);
				_snap.updateActualUrlUnacknowledged(_engine.getSnapsColl());
			}
	
			// update scan info
			_snap.isScanned(true);
			_snap.isScanFailed(false);
			if (bestImage != null) {
				_snap.isScannedImage(true);
				_snap.setScannedImageUrl(bestImage.v2());
			} else {
				_snap.isScannedImage(false);
				_snap.setScannedImageUrl(null);
			}
			_snap.updateScanInfoUnacknowledged(_engine.getSnapsColl());
	
			_log.finer("Completed " + this);
			_engine.remove(this);
			return true;
			
		} catch (Exception ex) {
			
			_log.log(
					Level.SEVERE, "Error during " + this, ex);
			
			return removeOrReattempt(
					Level.SEVERE);

		}
	}

	private boolean removeOrReattempt(Level logLevel) {
		
		if (_attempt >= MAX_ATTEMPTS) {
			
			_log.log(logLevel, "Failed max number of attempts " + MAX_ATTEMPTS + " for: " + _snap.getUrl());

			// update scan info
			_snap.isScanned(true);
			_snap.isScanFailed(true);
			_snap.isScannedImage(false);
			_snap.updateScanInfoUnacknowledged(_engine.getSnapsColl());
			_engine.remove(this);
			return true;
			
		} else {
			
			// reattempt
			setNextAttempt();
			_log.log(logLevel, "Scheduling next attempt #" + _attempt + " for: " + _snap.getUrl());
			_engine.reattempt(this);
			return true;
		}		
	}

}
