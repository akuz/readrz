package com.readrz.www;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.types.ObjectId;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.imgscalr.Scalr;

import com.mongodb.DBCollection;
import com.readrz.data.SnapImag;
import com.readrz.data.SnapThumb;
import com.readrz.data.mongo.MongoColls;
import com.readrz.www.facades.FacadeDB;

public final class ImageServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Logger _log;
	
	private DBCollection _snapsImagColl;
	private DBCollection _snapsThumbColl;

	@Override
	public void init() throws ServletException {
		super.init();
		_log = Log.getLogger(ImageServlet.class);
		_snapsImagColl = FacadeDB.get().getCollection(MongoColls.snapsimag);
		_snapsThumbColl = FacadeDB.get().getCollection(MongoColls.snapsthumb);
	}
	
	@Override
	public void destroy() {
		super.destroy();
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		
		try {
			
			ObjectId snapId = RzPar.getObjectIdRequired(req, RzPar.parId);
			Integer kindId = RzPar.getIntegerOptional(req, RzPar.parKind);
			
			if (kindId != null) {
				
				int thumbWidth;
				int thumbHeight;
				
				if (kindId.equals(1)) {
					thumbWidth = 400;
					thumbHeight = 266;
				} else if (kindId.equals(2)) {
					thumbWidth = 150;
					thumbHeight = 100;
				} else {
					throw new IllegalArgumentException("Illegal image kind: " + kindId);
				}
				
				SnapThumb snapThumb = SnapThumb.getBySnapAndKind(_snapsThumbColl, snapId, kindId);
				if (snapThumb == null) {
					
					SnapImag snapImage = SnapImag.find(_snapsImagColl, snapId);
					
					if (snapImage != null && snapImage.getImageBytes() != null) {

						InputStream in = new ByteArrayInputStream(snapImage.getImageBytes());
						BufferedImage img = ImageIO.read(in);
						BufferedImage thumbImg = Scalr.resize(img, Scalr.Mode.AUTOMATIC, thumbWidth, thumbHeight);

						snapThumb = new SnapThumb(snapId, kindId, thumbImg);
						snapThumb.upsertUnacknowledged(_snapsThumbColl);
					}
				}

				if (snapThumb != null) {
					HttpResponseUtils.writeBytesResponse(resp, snapThumb.getImageBytes(), "image/jpeg");
				} else {
					HttpResponseUtils.writeBytesResponse(resp, null, "image/jpeg");
				}
				
			} else { // full image requested
			
				SnapImag snapImage = SnapImag.find(_snapsImagColl, snapId);
				
				if (snapImage != null) {
					HttpResponseUtils.writeBytesResponse(resp, snapImage.getImageBytes(), "image/jpeg");
				} else {
					HttpResponseUtils.writeBytesResponse(resp, null, "image/jpeg");
				}
			}
			
		} catch (Exception ex) {
			
			_log.warn("Could not process request", ex);
			HttpResponseUtils.writeBytesResponse(resp, null, "image/jpeg");
		}
	}

}
