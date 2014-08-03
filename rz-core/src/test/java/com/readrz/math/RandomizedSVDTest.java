package com.readrz.math;

import java.util.Random;

import me.akuz.core.math.RandomizedSVD;
import me.akuz.core.math.SpaMatrix;
import junit.framework.TestCase;

public class RandomizedSVDTest extends TestCase {
	
	public void test() {
		Random random = new Random(System.currentTimeMillis());
		int outNum = 40;
		int overNum = 1;
		int rows = 1000;
		int cols = 1100;
		double fillRate = 0.0133;
		System.out.println("Creating sparse matrix (" + rows + ", " + cols + ") with fill rate " + fillRate + "...");
		SpaMatrix sm = new SpaMatrix(rows, cols);
		double entries = rows*cols*fillRate;
		System.out.println("Populating " + (long)entries + " entries...");
		for (int k=0; k<entries; k++) {
			if (k%50000 == 0) {
				System.out.println("Now at index " + k);
			}
			int i = (int)Math.floor(random.nextDouble()*rows);
			int j = (int)Math.floor(random.nextDouble()*cols);
			sm.set(i, j, random.nextDouble());
		}
		long millis = System.currentTimeMillis();
		System.out.print("Optimizing sparce matrix... ");
		sm.optimize();
		System.out.println(System.currentTimeMillis() - millis + " ms");
		
		RandomizedSVD rsvd = new RandomizedSVD(sm, (int)outNum, overNum);

		System.out.print("U... ");
		rsvd.getU().getMatrix(0, 10, 0, 10).print(8, 4);
		
		System.out.print("V... ");
		rsvd.getV().getMatrix(0, 10, 0, 10).print(8, 4);
		
	}

}
