package com.readrz.zlib.test;

import java.io.ByteArrayOutputStream;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.junit.Test;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.readrz.data.Snap;
import com.readrz.data.mongo.MongoColls;

public class Program {

	public static void main(String[] args) throws UnknownHostException, DataFormatException {
		
		new Program().testCompression();
	}
	
	@Test
	public void testCompression() throws UnknownHostException, DataFormatException {

		MongoClient mc = new MongoClient();
		
		try {
			
			DB db = mc.getDB("readrz");
			DBCollection coll = db.getCollection(MongoColls.snaps);
			
			DBCursor cur = Snap.selectBetweenDatesAsc(coll, null, null, null);
			int count = 0;
			
			Deflater def0 = new Deflater(0, true);
			Deflater def9 = new Deflater(9, true);
			
			Inflater inf = new Inflater(true);
			Charset utf8 = Charset.forName("UTF-8");
			byte[] buffer = new byte[1000];

			double totalDef0 = 0.0;
			double totalDef9 = 0.0;
			double avgCompr0 = 1.0;
			double avgCompr9 = 1.0;
			double totalInf0 = 0.0;
			double totalInf9 = 0.0;
			double avgDecom0 = 1.0;
			double avgDecom9 = 1.0;
			
			while (cur.hasNext() && count < 10000) {
				
				if (count % 100 == 0) {
					System.out.println("totalDef0: " + totalDef0);
					System.out.println("totalDef9: " + totalDef9);
					System.out.println("avgCompr0: " + avgCompr0);
					System.out.println("avgCompr9: " + avgCompr9);
					System.out.println("totalInf0: " + totalInf0);
					System.out.println("totalInf9: " + totalInf9);
					System.out.println("avgDecom0: " + avgDecom0);
					System.out.println("avgDecom9: " + avgDecom9);
				}
				
				Snap snap = new Snap(cur.next());
				
				String text = snap.getText();
				if (text != null && text.length() > 0) {
					
					{
						byte[] result;
						{
							Deflater def = def9;
							long ms = System.currentTimeMillis();
							byte[] bytes = text.getBytes(utf8);
							def.reset();
							def.setInput(bytes);
							def.finish();
	
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							while (!def.finished()) {
								int n = def.deflate(buffer);
								baos.write(buffer, 0, n);
							}
							baos.write(new byte[1], 0, 1);
							result = baos.toByteArray();
							totalDef9 += System.currentTimeMillis() - ms;
							double compr = (double)result.length / (double)bytes.length;
							avgCompr9 = avgCompr9 / (count+1) * count + compr / (count+1);
						}
						{
							long ms = System.currentTimeMillis();
							inf.reset();
							inf.setInput(result);
	
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							while (!inf.finished()) {
								int n = inf.inflate(buffer);
								baos.write(buffer, 0, n);
							}
							byte[] bytes2 = baos.toByteArray();
							String text2 = new String(bytes2, utf8);
							if (!text.equals(text2)) {
								throw new IllegalStateException("Invalid compresion");
							}
							totalInf9 += System.currentTimeMillis() - ms;
							double compr = (double)result.length / (double)bytes2.length;
							avgDecom9 = avgDecom9 / (count+1) * count + compr / (count+1);
						}
						
					}
					
					{
						byte[] result;
						{
							Deflater def = def0;
							long ms = System.currentTimeMillis();
							byte[] bytes = text.getBytes(utf8);
							def.reset();
							def.setInput(bytes);
							def.finish();
							
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							while (!def.finished()) {
								int n = def.deflate(buffer);
								baos.write(buffer, 0, n);
							}
							baos.write(new byte[1], 0, 1);
							result = baos.toByteArray();
							totalDef0 += System.currentTimeMillis() - ms;
							double compr = (double)result.length / (double)bytes.length;
							avgCompr0 = avgCompr0 / (count+1) * count + compr / (count+1);
						}
						{
							long ms = System.currentTimeMillis();
							inf.reset();
							inf.setInput(result);
	
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							while (!inf.finished()) {
								int n = inf.inflate(buffer);
								baos.write(buffer, 0, n);
							}
							byte[] bytes2 = baos.toByteArray();
							String text2 = new String(bytes2, utf8);
							if (!text.equals(text2)) {
								throw new IllegalStateException("Invalid compresion");
							}
							totalInf0 += System.currentTimeMillis() - ms;
							double compr = (double)result.length / (double)bytes2.length;
							avgDecom0 = avgDecom0 / (count+1) * count + compr / (count+1);
						}
					}
					count++;
				}
			}
			
			System.out.println("totalDef0: " + totalDef0);
			System.out.println("totalDef9: " + totalDef9);
			System.out.println("avgCompr0: " + avgCompr0);
			System.out.println("avgCompr9: " + avgCompr9);
			System.out.println("totalInf0: " + totalInf0);
			System.out.println("totalInf9: " + totalInf9);
			System.out.println("avgDecom0: " + avgDecom0);
			System.out.println("avgDecom9: " + avgDecom9);
			System.out.println("DONE on " + count + " docs");
			
		} finally {
			mc.close();
		}		
	}

}
