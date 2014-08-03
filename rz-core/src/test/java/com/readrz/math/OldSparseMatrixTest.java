package com.readrz.math;

import java.util.Random;

import me.akuz.core.math.SpaMatrix;

import org.junit.Assert;

import Jama.Matrix;
import junit.framework.TestCase;

public final class OldSparseMatrixTest extends TestCase {

	public void testPopulation() {
		
		Random random = new Random(System.currentTimeMillis());
		long millis;
		
		int M = 400;
		int N = 400;

		Matrix m;
		m = new Matrix(M, N);
		
		System.out.print("Col-row order... ");
		millis = System.currentTimeMillis();
		for (int j=0; j<N; j++) {
			for (int i=0; i<M; i++) {
				m.set(i, j, random.nextDouble());
			}
		}
		System.out.println(System.currentTimeMillis() - millis + "ms");
		
		System.out.print("Row-col order... ");
		millis = System.currentTimeMillis();
		for (int i=0; i<M; i++) {
			for (int j=0; j<N; j++) {
				m.set(i, j, random.nextDouble());
			}
		}
		System.out.println(System.currentTimeMillis() - millis + "ms");
		
		System.out.print("Row-col order + transpose... ");
		millis = System.currentTimeMillis();
		for (int i=0; i<M; i++) {
			for (int j=0; j<N; j++) {
				m.set(i, j, random.nextDouble());
			}
		}
		m.transpose();
		System.out.println(System.currentTimeMillis() - millis + "ms");
	}

	public void testMultiplication() {
		
		Matrix fm = Matrix.random(100, 500);
		SpaMatrix sm = new SpaMatrix(fm);
		sm.optimize();
		Matrix right = Matrix.random(500, 80);

		Matrix res1 = fm.times(right);
		Matrix res2 = sm.multOnRightBy(right);
		
		double diff = res1.minus(res2).normF();
		Assert.assertTrue(diff < 0.000001);
	}
	
	public void testPerformance() {
		SpaMatrix sparse = SpaMatrix.randomGaussian(1000, 1000, .1);

		Matrix dense = sparse.toDense();
		
		long millis;
		Matrix res1, res2;
		long timeDense, timeSparse;
		double diff;
		
		System.out.print("Dense multiplication on left... ");
		millis = System.currentTimeMillis();
		res1 = dense.times(dense);
		timeDense = System.currentTimeMillis() - millis;
		System.out.println(timeDense + "ms");
		
		System.out.print("Sparse multiplication on left... ");
		millis = System.currentTimeMillis();
		res2 = sparse.multOnLeftBy(dense);
		timeSparse = System.currentTimeMillis() - millis;
		System.out.println(timeSparse + "ms");

		diff = res1.minus(res2).normF();
		Assert.assertTrue(diff < 0.000001);
		System.out.println("Results match.");

//		Assert.assertTrue(timeSparse < timeDense);
		
		System.out.print("Dense multiplication on right... ");
		millis = System.currentTimeMillis();
		res1 = dense.times(dense);
		timeDense = System.currentTimeMillis() - millis;
		System.out.println(timeDense + "ms");
		
		System.out.print("Sparse multiplication on right... ");
		millis = System.currentTimeMillis();
		res2 = sparse.multOnRightBy(dense);
		timeSparse = System.currentTimeMillis() - millis;
		System.out.println(timeSparse + "ms");

		diff = res1.minus(res2).normF();
		Assert.assertTrue(diff < 0.000001);
		System.out.println("Results match.");

//		Assert.assertTrue(timeSparse < timeDense);
	}
}
