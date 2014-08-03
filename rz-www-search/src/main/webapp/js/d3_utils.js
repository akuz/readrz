
function d3_find_active_node(data) {

	var activeNode = data;
	while (true) {
		var activeNodeChildren = activeNode.children;
		if (activeNodeChildren) {
			var newActiveNode = null;
			for (var i=0; i<activeNodeChildren.length; i++) {
				var child = activeNodeChildren[i];
				if (child.isActive) {
					newActiveNode = child;
					break;
				}
			}
			if (newActiveNode !== null) {
				activeNode = newActiveNode;
				continue;
			}
		}
		break;
	}
	return activeNode;
}

function d3_closest(elem, classes) {
	
	var dom = elem[0][0];
	while (true) {
		dom = dom.parentNode;
		if (dom == null) {
			break;
		}
		var loopElem = d3.select(dom);
		if (loopElem.classed(classes)) {
			return loopElem;
		}
	}
	return null;
}

function d3_siblings_after(elem) {
	
	var result = [];
	var dom = elem[0][0];
	while (dom.nextSibling) {
		dom = dom.nextSibling;
		result.push(dom);
	}
	return d3.selectAll(result);
}
