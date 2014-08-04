package com.readrz.imagscan;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

import me.akuz.core.StringUtils;

import org.imgscalr.Scalr;


public final class ZzzImageFetcher {

	private static boolean imageIOWriteWithKnownExceptions(BufferedImage image, String imageFileName) throws IOException {

		try {
			ImageIO.write(image, "jpg", new File(imageFileName));
		} catch (IIOException ex) {
			if (ex.getMessage() != null &&
				ex.getMessage().startsWith("Invalid argument to native writeImage")) {
				return false;
			} else {
				throw ex;
			}
		}
		
		return true;
	}

	public static boolean saveImageToDisk(
			BufferedImage image,
			String outputPath,
			Integer postId, int imageIndex, 
			Integer thumb1Width, Integer thumb1Height, 
			Integer thumb2Width, Integer thumb2Height) throws IOException {
		
		String imageFileName = StringUtils.concatPath(outputPath, 
				String.format("%d_img%d.jpg", postId, imageIndex));
		
		if (false == imageIOWriteWithKnownExceptions(image, imageFileName)) {
			return false;
		}

		if (thumb1Width != null || thumb1Height != null) {

			BufferedImage scaledImg = null;
			
			if (thumb1Width == null) {
				scaledImg = Scalr.resize(image, Scalr.Mode.FIT_TO_HEIGHT, thumb1Height);
			} else if (thumb1Height == null) {
				scaledImg = Scalr.resize(image, Scalr.Mode.FIT_TO_WIDTH, thumb1Width);
			} else {
				scaledImg = Scalr.resize(image, Scalr.Mode.AUTOMATIC, thumb1Width, thumb1Height);
			}
			
			String thumbFileName = StringUtils.concatPath(outputPath,
					String.format("%d_img%d_thumb1.jpg", postId, imageIndex));
			
			if (false == imageIOWriteWithKnownExceptions(scaledImg, thumbFileName)) {
				return false;
			}
		}
		
		if (thumb2Width != null || thumb2Height != null) {

			BufferedImage scaledImg = null;
			
			if (thumb2Width == null) {
				scaledImg = Scalr.resize(image, Scalr.Mode.FIT_TO_HEIGHT, thumb2Height);
			} else if (thumb2Height == null) {
				scaledImg = Scalr.resize(image, Scalr.Mode.FIT_TO_WIDTH, thumb2Width);
			} else {
				scaledImg = Scalr.resize(image, Scalr.Mode.AUTOMATIC, thumb2Width, thumb2Height);
			}
			
			String thumbFileName = StringUtils.concatPath(outputPath,
					String.format("%d_img%d_thumb2.jpg", postId, imageIndex));

			if (false == imageIOWriteWithKnownExceptions(scaledImg, thumbFileName)) {
				return false;
			}
		}
		
		return true;
	}
}
