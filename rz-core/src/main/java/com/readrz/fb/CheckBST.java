package com.readrz.fb;

import me.akuz.core.Out;

public final class CheckBST {

	public static final boolean check1(Node root) {
		return check1(root, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}
	
	public static final boolean check1(Node root, int minValue, int maxValue) {
		
		if (root == null) {
			return true;
		}
		
		if (root.value() <= minValue ||
			root.value() > maxValue) {
			return false;
		}
		
		return 
			check1(root.left(), minValue, root.value()) &&
			check1(root.right(), root.value(), maxValue);
	}
	
	public static final boolean check2(Node root) {
		Out<Integer> last = new Out<>(null);
		return check2(root, last);
	}
	
	public static final boolean check2(Node node, Out<Integer> last) {
		
		if (node == null) {
			return true;
		}
		
		if (!check2(node.left(), last)) {
			return false;
		}
		
		if (last.getValue() != null && 
			last.getValue() > node.value()) {
			return false;
		}
		last.setValue(node.value());
		
		if (!check2(node.right(), last)) {
			return false;
		}

		return true;
	}
	
}
