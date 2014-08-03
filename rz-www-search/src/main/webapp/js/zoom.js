
function zoomAppendSvg(parent, par, root, onZoom) {
	
	// init selection
	root._rootNode = null;
	root._activeNode = null;

	// append svg element
	var svg = parent.append("svg")
		.datum(root)
	    .attr("width", par.margin.left + par.margin.right)
	    .attr("height", par.margin.top + par.margin.bottom)
	  .append("g")
	    .attr("transform", "translate(" + par.margin.left + "," + par.margin.top + ")");
	
	// init entire partition
	var partition = d3.layout.partition()
	    .sort(function(a, b) { return d3.ascending(a.name, b.name); })
	    .size([2 * Math.PI, par.radius])
	    .value(function(d) {
	    	// if count iz zero, we will still 
	    	// have a positive volume for the node
	    	return Math.log(2 + d.count) / Math.LN10; 
	    });
	
	// Compute the initial layout on the entire tree to sum sizes.
	// Also compute the full name and fill color for each node.
	var nodes = partition.nodes(root);

	// compute max child value
	var maxChildValue = 1;
	if (root.children) {
		for (var i=0; i<root.children.length; i++) {
			var childValue = root.children[i].value;
			if (maxChildValue < childValue) {
				maxChildValue = childValue;
			}
		}
	}
	
	// create colour luminances
	var activeLuminance = d3.scale.pow().exponent(0.4)
		.clamp(true)
		.domain([0, maxChildValue])
		.range([par.activeLuminanceMax, par.activeLuminanceMin]);
	var passiveLuminance = d3.scale.pow().exponent(0.4)
		.clamp(true)
		.domain([0, maxChildValue])
		.range([par.passiveLuminanceMax, par.passiveLuminanceMin]);
	
	// node fill function
	var nodeFill = function(d) {
		if (root._activeNode && 
			root._activeNode.groupId) {
			return d3.hsl(
				par.activeHue,
				par.activeSaturation,
				activeLuminance(d.sum));
		} else {
			return d3.hsl(
				par.passiveHue,
				par.passiveSaturation,
				passiveLuminance(d.sum));
		}
	};
	
	// node text-color function
	var nodeTextColor = function(d) {
		if (root._activeNode === d &&
			d.groupId) {
			return par.activeTextColor;
		} else {
			if (d.depth == 0 ||
				d.depth == 1) {
				return par.innerTextColor;
			} else {
				return par.outerTextColor;
			}
		}
	};
	
	var nodeTextSize = function(d) {
		if (root._activeNode === d) {
			return par.activeFontSize;
		} else {
			if (d.depth == 0 ||
				d.depth == 1) {
				return par.innerFontSize;
			} else {
				return par.outerFontSize;
			}
		}
	};
	
	// set permanent node fields
	nodes.forEach(function(d) {
		d.key = key(d);
		d.sum = d.value;
	});
	
	// limit partition depth
	partition.children(function(d, depth) { 
		return depth < (par.levels-1) ? d.children : null; 
	});

	// init center circle
	var circle = svg.append("circle")
		.attr("class", "center")
	    .attr("r", par.radius / par.levels)
	    .style("fill", par.centerBackColor)
		.style("opacity", 0)
	    .on("click", zoomOut);
	circle.append("title")
		.text("Zoom Out");

	// create groups for arcs and text	    
	var gPath = svg.append("g");
	var gText = svg.append("g");
	var gPathTop = svg.append("g");
	var gTextTop = svg.append("g");

	// create arc shape
	var arc = d3.svg.arc()
	    .startAngle(function(d) {
	    	var value = d.x;
	    	if (d.active) {
	    		value -= Math.PI / 40.0 * d.active;
	    	}
			return value; 
		})
	    .endAngle(function(d) { 
	    	var value = d.x + d.dx;
	    	if (d.active) {
	    		value += Math.PI / 40.0 * d.active;
	    	}
			return value; 
	    })
	    .innerRadius(function(d) {
	    	var value = par.radius / par.levels * d.depth; 
	    	if (d.active) {
	    		value *= 1.0 + (0.1 * d.active);
	    	}
	    	return value;
	   	})
	    .outerRadius(function(d) {
	    	var value = par.radius / par.levels * (d.depth + 1); 
	    	if (d.active) {
	    		value *= 1.0 + (0.1 * d.active);
	    	}
	    	return value;
	    });
	
	var path = gPath.selectAll("path");
	var text = gText.selectAll("text.sector");
	var textRoot = gText.selectAll("text.root");

	// initial zoom
	var loopNode = root;
	while (true) {
		var loopChildren = loopNode.children;
		if (loopChildren) {
			var newLoopNode = null;
			for (var i=0; i<loopChildren.length; i++) {
				var loopChild = loopChildren[i];
				if (loopChild.isActive) {
					newLoopNode = loopChild;
					break;
				}
			}
			if (newLoopNode !== null) {
				loopNode = newLoopNode;
				continue;
			}
		}
		break;
	}
	{
		var rootNode;
		if (loopNode.children){
			rootNode = loopNode;
		} else {
			rootNode = loopNode.parent;
		}
		var zoomNode = rootNode;	
		var activeNode = loopNode;    	

	    zoom(rootNode, zoomNode, activeNode, true, true);
	}
	
	// zoom in func    
	function zoomIn(p) {

		var rootNode;
		if (p.children){
			rootNode = p;
		} else {
			rootNode = p.parent;
		}
		var zoomNode = rootNode;	
		var activeNode = p;
	    zoom(rootNode, zoomNode, activeNode, true, false);
	}
	
	// zoom out func
	function zoomOut(p) {
	
		var rootNode;
		var zoomNode;
		var activeNode;
		
		if (root._activeNode === p) {
			if (p.parent) {
				rootNode = p.parent;
				zoomNode = p;
				activeNode = p.parent;
			} else {
				rootNode = p;
				zoomNode = p;
				activeNode = p;
			}
		} else {
			rootNode = p;
			zoomNode = p;
			activeNode = p;
		}
	    
	    zoom(rootNode, zoomNode, activeNode, false, false);
	}
	
	// zoom func
	// Zoom to the specified new root.
	function zoom(rootNode, zoomNode, activeNode, isZoomIn, isInitial) {
	
	    // update selection
	    var oldRootNode = root._rootNode;
	    var oldActiveNode = root._activeNode;
	    root._rootNode = rootNode;
	    root._activeNode = activeNode;
	    var speed = oldRootNode === rootNode ? 100 : 400;
	    
	    // check if selection changed
	    if (oldRootNode === rootNode &&
	    	oldActiveNode === activeNode) {
	    	
	    	var flashEase = function(t) {
	    		if (t <= 0.5) {
	    			var used = 1.0 - t*2;
		    		return Math.sqrt(1.0 - used*used);
	    		} else if (t <= 1.0) {
	    			var used = (t - 0.5)*2;
		    		return Math.sqrt(1.0 - used*used);
	    		} else {
	    			return 0.0;
	    		}
	    	};
	    	
	    	if (isZoomIn) {
	    	
	    		var activePath = gPathTop.select("path");
	    		if (activePath.size() == 1) {
	    		
	    			// need to set data after select
		    		activePath.datum(activeNode);
		    		
		    		// highlight
		    		var oldFill = activePath.style("fill");
		    		activePath
		    			.transition()
						.duration(speed)
						.ease(flashEase)
					    .style("fill", par.backFlashColor);
				}

	    	} else {
	    		
	    		var oldFill = circle.style("fill");
				circle
					.transition()
					.duration(speed)
					.ease(flashEase)
				    .style("fill", par.backFlashColor);
	    	}
	    	
	    	return;
	    }
	    
	    if (!isInitial && onZoom) {
			onZoom(activeNode);
		}
	
	    // Stash the original root dimensions for transitions.
	    var rootScale = d3.scale.linear()
			.domain([0, 2 * Math.PI])
			.range([rootNode.x, rootNode.x + rootNode.dx]);
	
	    function insideArc(d) {
	    	return zoomNode.key > d.key 
	    		? {
	    			depth: d.depth - 1, 
	    			x: 0, 
	    			dx: 0, 
	    			active: (root._activeNode === d ? 1.0 : 0.0) 
	    		} 
	    		: zoomNode.key < d.key 
	    		? {
	    			depth: d.depth - 1, 
	    			x: 2 * Math.PI, 
	    			dx: 0, 
	    			active: (root._activeNode === d ? 1.0 : 0.0) 
	    		} 
	    		: {
	    			depth: 0, 
	    			x: 0, 
	    			dx: 2 * Math.PI, 
	    			active: (root._activeNode === d ? 1.0 : 0.0) 
	    		};
	    }
	
	    function outsideArc(d) {
			return {
				depth: d.depth + 1, 
				x: rootScale(d.x), 
				dx: rootScale(d.x + d.dx) - rootScale(d.x), 
				active: (root._activeNode === d ? 1.0 : 0.0) 
			};
	    }
	
	    // When zooming in, arcs enter from the outside and exit to the inside.
	    // When zooming out, arcs enter from the inside and exit to the outside.
	    var enterArc = rootNode === zoomNode ? outsideArc : insideArc,
	        exitArc = rootNode === zoomNode ? insideArc : outsideArc;

	    // when starting, appear from inside
	    if (oldRootNode === null) {
	    	enterArc = insideArc;
	    }

		// transition everything
	    d3.transition().duration(speed).each(function() {
	    
	    	// move top elements to back
	    	gPathTop.selectAll("*").each(function() {
	    		gPath[0][0].appendChild(this);
	    	});
	    	gTextTop.selectAll("*").each(function() {
	    		gText[0][0].appendChild(this);
	    	});
		
			// update circle
			circle.datum(rootNode)
				.transition()
				.style("fill", nodeFill)
				.style("opacity", 1);

			// partition from new root
			nodes = partition.nodes(rootNode).slice(1);
			
			// update paths
			path = path.data(nodes, function(d) { return d.key; });
			path.transition()
				.style("fill", nodeFill)
				.attrTween("d", function(d) { return pathTween.call(this, updateArc(d)); });

			// udpate texts	
			text = text.data(nodes, function(d) { return d.key; });
			textRoot = textRoot.data([rootNode], function(d) { return d.key; });
			text.transition()
				.attrTween("transform", function(d) { return textTween.call(this, updateArc(d)); })
				.style("font-size", nodeTextSize)
				.style("fill", nodeTextColor);
			textRoot.transition()
				.style("font-size", nodeTextSize)
				.style("fill", nodeTextColor);
			
			// enter paths
			path.enter().append("path")
				.style("stroke", par.pathStrokeColor)
				.style("opacity", 0)
				.style("fill", nodeFill)
				.on("click", zoomIn)
				.each(function(d) { this._current = enterArc(d); })
			.transition()
				.style("opacity", 1)
				.attrTween("d", function(d) { return pathTween.call(this, updateArc(d)); });

			// enter texts
			text.enter()
				.append("text")
					.attr("class", "sector noSelect")
					.style("opacity", 0)
					.attr("dy", "8")
					.attr("text-anchor", "middle")
					.text(function(d) { return d.name; })
					.on("click", zoomIn)
					.each(function(d) { this._current = enterArc(d); })
				.transition()
					.style("opacity", 1)
					.attrTween("transform", function(d) { return textTween.call(this, updateArc(d)); })
					.style("font-size", nodeTextSize)
					.style("fill", nodeTextColor);
			textRoot.enter()
				.append("text")
					.attr("class", "root noSelect")
					.attr("x", 0)
					.attr("y", 0)
					.attr("dy", "8")
					.style("opacity", 0)
					.attr("text-anchor", "middle")
					.text(function(d) { return d.name; })
					.on("click", zoomOut)
				.transition()
					.style("opacity", 1)
					.style("font-size", nodeTextSize)
					.style("fill", nodeTextColor);

			// after enter,
			// bring to front
			// active node path
			path.each(function(d) {
				if (root._activeNode === d) {
					gPathTop[0][0].appendChild(this);
				}
			});
			text.each(function(d) {
				if (root._activeNode === d) {
					gTextTop[0][0].appendChild(this);
				}
			});
			textRoot.each(function(d) {
				if (root._activeNode === d) {
					gTextTop[0][0].appendChild(this);
				}
			});

			// exit paths
			path.exit().transition()
				.style("opacity", 0)
				.attrTween("d", function(d) { return pathTween.call(this, exitArc(d)); })
				.remove();
				
			// exit texts
			text.exit().transition()
				.style("opacity", 0)
				.attrTween("transform", function(d) { return textTween.call(this, exitArc(d)); })
				.remove();
			textRoot.exit().transition()
				.style("opacity", 0)
				.remove();

		}); // end transition

	} // end zoom
	
	function key(d) {
		var k = [], p = d;
		while (p.depth) k.push(p.name), p = p.parent;
		return k.reverse().join(".");
	}
	
	function pathTween(b) {
		var i = d3.interpolate(this._current, b);
		this._current = i(0);
		return function(t) {
			return arc(i(t));
		};
	}
	
	function textTween(b) {
		var i = d3.interpolate(this._current, b);
		this._current = i(0);
		return function(t) {
			return "translate(" + arc.centroid(i(t)) + ")";
		};
	}
	
	function updateArc(d) {
		return {
			depth: d.depth, 
			x: d.x, 
			dx: d.dx, 
			active: (root._activeNode === d ? 1.0 : 0.0)
		};
	}

}