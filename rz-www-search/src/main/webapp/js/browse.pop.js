
function browsePop() {

	var body = d3.select("body");

	var pop = body
		.append("div")
		.attr("class", "pop");
	
	pop.on("click", function() {
		d3.event.stopPropagation();
		pop.remove();
	});

	pop.append("div")
		.attr("class", "back");
		
	return pop;
}

function browseElemDataPanelPop(elem, offset) {
	
	var pop = browsePop();
	
	var elemRect = elem[0][0].getBoundingClientRect();
	var popRect  = pop[0][0].getBoundingClientRect();

	var childTop = - popRect.top + elemRect.bottom + offset;
	var popMargin = pop.append("div")
		.attr("class", "margin")
		.style("top", childTop + "px");
		
	var commonMargin = popMargin.append("div")
		.attr("class", "commonMargin");
		
	var popPanel = commonMargin.append("div")
		.attr("class", "panel")
		.on("click", function() {
			d3.event.stopPropagation();
		});
		
	var dataPanel = popPanel.append("div")
		.attr("class", "dataPanel");
		
	return dataPanel;
}
