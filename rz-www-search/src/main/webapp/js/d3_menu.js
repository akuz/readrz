
function d3_menu(container, data, onZoom) {

	if (container.selectAll("div.items").size() == 0) {
		container.append("div").attr("class", "items");
	}
	
	d3_menu_init(container, data);
	var setActiveNode = d3_find_active_node(data);
	d3_menu_zoom(container, data, setActiveNode, null, onZoom);
}

function d3_menu_zoom(container, data, setActiveNode, onZoomNow, onZoom) {

	if (onZoomNow) {
		onZoomNow(setActiveNode);
	}

	var itemsContainer = container.select("div.items");
	
	//select root
	var root;
	if (setActiveNode.children) {
		root = setActiveNode;
	} else if (setActiveNode.parent){
		root = setActiveNode.parent;
	} else {
		root = setActiveNode;
	}
	data._rootNode = root;
	data._activeNode = setActiveNode;

	var itemBackColor = function(d) {
		if (d.isRoot) {
			return "#556677";
		} else if (data._activeNode == d) {
			return "#ee9933";
		} else if (d.level == 0) {
			return "#556677";
		} else {
			return "#334455";
		}
	};

	var itemNameColor = function(d) {
		if (d.isRoot) {
			return "#cccccc";
		} else if (data._activeNode == d) {
			return "#ffffff";
		} else if (d.level == 0) {
			return "#cccccc";
		} else {
			return "#aaaaaa";
		}
	};

	var itemCountColor = function(d) {
		if (d.isRoot) {
			return "#aaaaaa";
		} else if (data._activeNode == d) {
			return "#dddddd";
		} else if (d.level == 0) {
			return "#aaaaaa";
		} else {
			return "#888888";
		}
	};
	
	var itemBorderRight = function(d) {
		if (d.isActiveRoot) {
			return "1px solid #223344";
		} else {
			return "0px";
		}
	};

	var layout = d3_menu_layout(container, root);
	
	var shifts = container.selectAll("div.shift")
		.data(layout.buttons, function(d) { return d.key; });

	var items = container.select("div.items").selectAll("div.item")
		.data(layout.nodes, function(d) { return d.key; });
	
	d3.transition().duration(600).each(function() {

		{ // exit items
		
			items.exit()
				.each(function() {
					// make exiting elements least important
					itemsContainer[0][0].insertBefore(this, itemsContainer[0][0].firstChild);
				})
				.transition()
					.style("left", function(d) { return -d.width + 'px'; })
					.style("opacity", "0")
					.remove();
		}
		
		{ // enter items
		
			var newItems = items.enter()
				// make entering element next important
				.insert("div", ":first-child")
					.attr("class", "item")
					.style("opacity", "0")
					.style("left", function(d) { return -d.width + 'px'})
					.on("click", function(d) {
						d3.event.stopPropagation();
						d.childOffset = 0;
						d3_menu_zoom(container, data, d, onZoom, onZoom);
					});
			
			newItems
				.transition()
					.style("left", function(d) { return d.left + 'px'; })
					.style("background-color", itemBackColor)
					.style("border-right", itemBorderRight)
					.style("opacity", "1");
	
			newItems
				.append("div")
					.attr("class", "name")
					.html(function(d) { return d.name; })
				.transition()
					.style("color", itemNameColor);
				
			newItems
				.append("div")
					.attr("class", "count")
					.html(function(d) { return d.count; })
				.transition()
					.style("color", itemCountColor);
		}

		{ // update items
		
			items
				.transition()
					.style("left", function(d) { return d.left + 'px'; })
					.style("background-color", itemBackColor)
					.style("border-right", itemBorderRight);
	
			items
				.selectAll("div.name")
				.transition()
					.style("color", itemNameColor);
				
			items
				.selectAll("div.count")
				.transition()
					.style("color", itemCountColor);
		}
		
		// exit shifts
		{
			shifts.exit()
				.transition()
					.style("opacity", "0")
					.remove();
		}
		
		// enter shifts
		{
			shifts.enter()
				.append("div")
					.attr("class", function(d) { return d.left != null ? "shift left" : "shift right"; })
					.style("opacity", "0")
					.style("left", function(d) { return d.left != null ? d.left + 'px' : null; })
					.style("right", function(d) { return d.right != null ? d.right + 'px' : null; })
					.html(function(d) {
						if (d.left != null) {
							return '<i class="fa fa-angle-double-left"></i>'
						} else {
							return '<i class="fa fa-angle-double-right"></i>'
						}
					})
					.on("click", function() {
						d3.event.stopPropagation();
						var elem = d3.select(this);
						if (elem.classed("left")) {
							if (root.childOffset > 0) {
								root.childOffset -= 1;
								d3_menu_zoom(container, data, data._activeNode, null, onZoom);
							}
						} else {
							if (root.children && 
								root.childOffset < root.children.length - 1) {
								root.childOffset += 1;
								d3_menu_zoom(container, data, data._activeNode, null, onZoom);
							}
						}
					})
				.transition()
					.style("opacity", "1");
		}
	});
}

function d3_menu_init(container, data) {

	// start processing
	var queue = new Queue();
	var all = [];
	if (data) {
		data.isRoot = true;
		queue.enqueue([null, 0, data, 0]);
	}
	
	// process all nodes
	while (queue.getLength() > 0) {
	
		// get next node
		var tuple = queue.dequeue();
		var parent = tuple[0];
		var depth = tuple[1];
		var node = tuple[2];
		var idx = tuple[3];
		all.push(node);
		
		// init parent
		node.parent = parent;
		node.depth = depth;
		
		// init key
		if (parent) {
			node.key = parent.key + "." + padLeft(idx, 2);
		} else {
			node.key = padLeft(idx, 2);
		}
		
		// process children
		if (node.children) {
			for (var i=0; i<node.children.length; i++) {
				queue.enqueue([node, depth+1, node.children[i], i]);
			}
		}
		
		// init child offset
		node.childOffset = 0;
	}
	
	// init widths
	{
		var items = container.select("div.items").selectAll("div.item")
			.data(all)
			.enter()
				.append("div")
				.attr("class", "item")
				.style("opacity", "0");
		
		items.append("div")
			.attr("class", "name")
			.html(function(d) { return d.name });
	
		items.append("div")
			.attr("class", "count")
			.html(function(d) { return d.count });
	
		items.each(function(d) {
			var rect = this.getBoundingClientRect();
			d.width = rect.right - rect.left;
		});
		
		items.remove();
	}
}

function d3_menu_layout(container, root) {

	var itemsContainer = container.select("div.items");

	var nodes = [];
	var leftBtn = null;
	var rightBtn = null;
	
	if (root) {
	
		// get container width
		var containerWidth;
		{
			var rect = itemsContainer[0][0].getBoundingClientRect();
			containerWidth = rect.right - rect.left;
		}
	
		// populate path
		{
			var loop = root;
			while (loop) {
				loop.level = 0;
				loop.isActiveRoot = false;
				nodes.push(loop);
				loop = loop.parent;
			}
			if (nodes.length > 1) {
				nodes.reverse();
			}
		}
		root.isActiveRoot = true;

		// populate children
		var childrenOffset = 0;
		{	
			var children = root.children;
			if (children) {
				for (var i=0; i<children.length; i++) {
					var child = children[i];
					child.level = 1;
					child.isActiveRoot = false;
					nodes.push(child);
					
					if (root.childOffset > i) {
						childrenOffset += child.width;
					}
				}
			}
		}
		
		// set nodes.left
		var left = 0;
		for (var i=0; i<nodes.length; i++) {

			var node = nodes[i];
			if (node.level == 0) {
				node.left = left;
			} else {
				node.left = left - childrenOffset;
			}
			left += node.width;

			// init leftBtn			
			if (leftBtn == null &&
				root.childOffset > 0 &&
				node.level > 0) {
				
				leftBtn = {};
				leftBtn.left = node.left + childrenOffset;
				leftBtn.right = null;
				leftBtn.key = root.key + ".leftBtn";
			}
			
			// init rightBtn
			if (rightBtn == null &&
				containerWidth < node.left + node.width) {
				
				rightBtn = {};
				rightBtn.left = null;
				rightBtn.right = 10;
				rightBtn.key = root.key + ".rightBtn";
			}
		}
	}
	
	var buttons = [];
	if (leftBtn != null) {
		buttons.push(leftBtn);
	}
	if (rightBtn != null) {
		buttons.push(rightBtn);
	}
	
	return {
		'nodes': nodes,
		'buttons': buttons
	};
}