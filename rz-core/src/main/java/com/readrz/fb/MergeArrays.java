package com.readrz.fb;

public final class MergeArrays {

	public static void mergeIntoB(int[] a, int lena, int b[], int lenb) {
		
		int i = lena - 1;
		int j = lenb - 1;
		int k = lena + lenb -1;
		
		while (i >= 0 && j >= 0) {
			if (a[i] > b[j]) {
				b[k--] = a[i--];
			} else {
				b[k--] = b[j--];
			}
		}
		
		while (i >= 0) {
			b[k--] = a[i--];
		}
		
		// don't need to copy from b into b
	}
}
