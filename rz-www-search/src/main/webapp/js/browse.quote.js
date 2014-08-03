
function renderQuoteDetails(container) {

	{ // title
	
		var title = container.append("div")
			.attr("class", "title");
	
		title.append("a")
			.attr("target", "_blank")
			.attr("href", function(d) { return d.quote.url; })
			.html(function(d) { return d.quote.titleQuote; })
			.on("click", function() {
				var elem = d3.select(this);
				onQuoteElemTrackView(elem, "block");
			});
	}
	
	{ // image
	
		var imageA = container.selectAll("a.image")
			.data(function(d) { return d.quote.hasImage ? [d] : []; })
			.enter()
				.append("a")
					.attr("class", "image")
					.attr("target", "_blank")
					.attr("href", function(d) { return d.quote.url; })
					.on("click", function() {
						var elem = d3.select(this);
						onQuoteElemTrackView(elem, "block");
					});
		
		imageA.append("img")
			.attr("src", function(d) {
				var imageUrl = "/image?id=" + d.quote.id + "&kind=1";
				return imageUrl;
			})
	}

	{ // info
	
		var info = container.append("div")
			.attr("class", "info");
	
		var infoA = info.append("a")
			.attr("target", "_blank")
			.attr("href", function(d) { return d.quote.url; })
			.on("click", function() {
				var elem = d3.select(this);
				onQuoteElemTrackView(elem, "block");
			});
			
		infoA.append("span")
			.attr("class", "source")
			.html(function(d) { return d.quote.source; });
			
		infoA.append("span")
			.attr("class", "dateAgo")
			.html(function(d) { return d.quote.dateAgo; });
	}
	
	{ // text
	
		var text = container.selectAll("div.text")
			.data(function(d) { return d.quote.textQuote ? [d] : []; })
			.enter()
				.append("div")
				.attr("class", "text")
				.html(function(d) { return d.quote.textQuote; });
	}
		
	container.append("div")
		.attr("class", "clear");

}

