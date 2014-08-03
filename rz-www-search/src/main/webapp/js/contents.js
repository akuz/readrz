function initContents(root) {

	var summaryContents = $("div#summaryContents");
	if (summaryContents.length > 0) {

		/*
		var width = 900,
			height = 600,
		    format = d3.format(",d"),
		    color = d3.scale.category20c();
		
		var bubble = d3.layout.pack()
		    .sort(null)
		    .size([width, height])
		    .padding(1.5);
		
		var svg = d3.select("div#summaryContents").append("svg")
		    .attr("width", width)
		    .attr("height", height)
		    .attr("class", "bubble");
		
		var node = svg.selectAll(".node")
		      .data(bubble.nodes(classes(root))
		      .filter(function(d) { return !d.children; }))
		    .enter().append("g")
		      .attr("class", "node")
		      .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
		
		node.append("title")
		      .text(function(d) { return d.className + ": " + format(d.value); });
		
		node.append("circle")
		      .attr("r", function(d) { return d.r; })
		      .style("fill", function(d) { return color(d.packageName); });
		
		//node.append("ellipse")
		//      .attr("ry", function(d) { return d.r / 2; })
		//      .attr("rx", function(d) { return d.r * 2; })
		//      .style("fill", function(d) { return color(d.packageName); });
		
		node.append("text")
		      .attr("dy", ".3em")
		      .style("text-anchor", "middle")
		      .text(function(d) { return d.className.substring(0, d.r / 3); });
		
		// Returns a flattened hierarchy containing all leaf nodes under the root.
		function classes(root) {
		  var classes = [];
		
		  function recurse(name, node) {
		    if (node.children) node.children.forEach(function(child) { 
		    	recurse(node.name, child); 
		    });
		    else classes.push({packageName: name, className: node.name, value: node.count});
		  }
		
		  recurse(null, root);
		  return {children: classes};
		}
		*/
		
		var margin = {top: 0, right: 0, bottom: 0, left: 0},
		    width = 938 - margin.left - margin.right,
		    height = 550 - margin.top - margin.bottom;
		
		var color = d3.scale.category20c();
		
		var treemap = d3.layout.treemap()
		    .size([width, height])
		    .round(true)
		    .sticky(false)
		    .mode("squarify")
		    .ratio(1.0)
		    .padding(function(d) { return d.level == 0 ? [6, 3, 3, 3] : [30, 4, 6, 4]; })
		    .value(function(d) { return Math.pow(d.count, 0.2); })
		    .sort(function(a,b) {
		    	return - b.value + a.value;
		    });
		
		var div = d3.select("div#summaryContents").append("div")
		    .style("position", "relative")
		    .style("width", (width + margin.left + margin.right) + "px")
		    .style("height", (height + margin.top + margin.bottom) + "px")
		    .style("left", margin.left + "px")
		    .style("top", margin.top + "px");
		
		function position() {
		  this.style("left", function(d) { return d.x + "px"; })
		      .style("top", function(d) { return d.y + "px"; })
		      .style("width", function(d) { return Math.max(0, d.dx - 1) + "px"; })
		      .style("height", function(d) { return Math.max(0, d.dy - 1) + "px"; });
		}
		
		var node = div.datum(root).selectAll(".node")
		      .data(treemap.nodes)
		    .enter().append("div")
		      .attr("class", function(d) { return d.children ? "node parent" : "node child"; })
		      .call(position);
		      
		node.append("span")
			.attr("class", function(d) {
				if (d.level == 0) {
					return "hidden";
				}
				if (d.children) {
					return "parent";
				}
				if (d.level == 1) {
					return "hidden";
				}
				return "child";
			 })
			.text(function(d) { 
				return d.name; 
			});

		node.append("div")
			.attr("class", function(d) {
				if (d.level == 1 && !d.children) {
					return "image";
				}
				if (d.level == 1 && !d.children && d.image) {
					return "image";
				} else {
					return "hidden";
				}
			})
			.style("background-image", function(d) {
				if (d.level == 1 && !d.children && d.image) {
					return null; // FIXME
					return "url(/image?id=" + d.image + "&kind=2)";
				}
				return null;
			})
			.style("font-size", function(d) {
				var logc = Math.log(d.count + 1) / Math.log(2);
				var size = 10 + logc * 2;
				return size + "px";
			})
			.style("top", function(d) {
				var logc = Math.log(d.count + 1) / Math.log(2);
				var size = logc * 2;
				return size + "px";
			})
			.style("left", function(d) {
				var logc = Math.log(d.count + 1) / Math.log(2);
				var size = logc;
				return size + "px";
			})
			.text(function(d){ return d.count; });
		
		$("div#summaryContents div.node.child").mouseenter(function(event) {
			var pos = $(this).position();
			var width = $(this).width();
			var height = $(this).height();
			$(this).css("cursor", "move");
			$(this).animate({top: pos.top - 20, left: pos.left - 20, width: width + 40, height: height + 40}, 100);
		});
		$("div#summaryContents div.node.child").mouseleave(function(event) {
			var pos = $(this).position();
			var width = $(this).width();
			var height = $(this).height();
			$(this).css("cursor", "pointer");
			$(this).animate({top: pos.top + 20, left: pos.left + 20, width: width - 40, height: height - 40}, 100);
		});

		/*
		var width = 380,
		    height = 380,
		    radius = Math.min(width, height) / 2,
		    color = d3.scale.category20c();
		
		var svg = d3.select("div#summaryContents").append("svg")
		    .attr("width", width)
		    .attr("height", height)
		  .append("g")
		    .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")");
		
		var partition = d3.layout.partition()
		    .sort(null)
		    .size([2 * Math.PI, radius * radius])
		    .value(function(d) { return d.count; });
		
		var arc = d3.svg.arc()
		    .startAngle(function(d) { return d.x; })
		    .endAngle(function(d) { return d.x + d.dx; })
		    .innerRadius(function(d) { return Math.sqrt(d.y); })
		    .outerRadius(function(d) { return Math.sqrt(d.y + d.dy); });
		
		var path = svg.datum(root).selectAll("path")
		    .data(partition.nodes)
		    .enter().append("path")
		      .attr("display", function(d) { return d.depth ? null : "none"; }) // hide inner ring
		      .attr("d", arc)
		      .style("stroke", "#fff")
		      .style("fill", function(d) { return color((d.children ? d : d.parent).name); })
		      .style("fill-rule", "evenodd");
		      
		$("div#summaryContents svg path").tipsy({
			gravity: "s",
			html: true,
			title: function() {
				var d = this.__data__;
				return d.name + " (" + d.count + ")";
			}
		});
		
		*/
	}
	
}