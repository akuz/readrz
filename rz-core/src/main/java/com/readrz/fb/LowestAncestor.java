package com.readrz.fb;

public final class LowestAncestor {
	
	public Node find(Node root, Node p, Node q) {
		
		if (root == null) {
			return null;
		}
		
		if (root.left() == p || root.left() == q || root.right() == p || root.right() == q) {
			return root;
		}
		
		Node l = find(root.left(), p, q);
		Node r = find(root.right(), p, q);
		
		if (l != null && r != null) {
			return root;
		}
		
		return l != null ? l : r;
	}

}
