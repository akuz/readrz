
	
	/*
	var links = container.append("div")
		.attr("class", "magLinks");
	
	{
		var readLinkA = links.append("a")
			.attr("href", function(d) { return d.quote.url; })
			.attr("target", "_blank")
			.on("click", function() {
				var elem = d3.select(this);
				onQuoteElemTrackView(elem, "block");
			});

		var readLink = readLinkA.append("div")
			.attr("class", "link");
			
		readLink.append("i")
			.attr("class", "fa fa-external-link-square");
		
		readLink.append("span")
			.text("Read Full Article");
	}
	{
		var shareLink = links.append("div")
			.attr("class", "link")
			.on("click", function() {
				var elem = d3.select(this);
				onQuoteElemShareClick(elem, "block", -110, -50, null);
			});
		
		shareLink.append("i")
			.attr("class", "fa fa-share-square");
		
		shareLink.append("span")
			.text("Share");
	}
		
	links.append("div")
		.attr("class", "clear");
	*/
	
		
		/*
		var mainHead = mainBlock.append("div")
			.attr("class", "mainHead");
		
		if (d.leafSearch) {
			mainHead.on("click", onMagazineSearchLinkClick);
		}

		// main search
		if (d.count) {
			
			var search = mainHead.append("div")
				.attr("class", "search");
				
			search.append("div")
				.attr("class", "text")
				.text(d.hierSearch ? d.hierSearchDisplay : "Other");
				
			search.append("div")
				.attr("class", "count")
				.html('<i class="fa fa-check"></i> ' + d.count + ' articles');
				
			search.append("div")
				.attr("class", "clear");
		}
		
		var mainBody = mainBlock.append("div")
			.attr("class", "mainBody");
		*/
		
		/*
		// other blocks
		renderMagazineOtherBlocksChunks(mainBody);
		*/



/**
 * Render magazine other blocks chunks.
 */
function renderMagazineOtherBlocksChunks(mainBody, data) {

	// get block data
	var d = mainBody.datum();
	
	// child datas
	var arr = d.children;
	
	// render other blocks chunk array
	renderMagazineOtherBlocksChunksCount(mainBody, data, arr, d.quote ? 0 : 1);
}

/**
 * Render magazine other blocks chunks count.
 */
function renderMagazineOtherBlocksChunksCount(mainBody, data, arr, count) {

	var CHUNK_SIZE = 3;
	
	var arr1;
	var arr2;
	if (count == 0) {
		arr1 = [];
		arr2 = arr;
	} else {
		var arrSplit = arraySplit(arr, CHUNK_SIZE);
		arr1 = arrSplit.arr1;
		arr2 = arrSplit.arr2;
	}

	if (arr1 && arr1.length > 0) {
	
		renderMagazineOtherBlocksChunk(mainBody, arr1);
	}
	
	if (count > 1 && arr2 && arr2.length) {
	
		renderMagazineOtherBlocksChunksCount(mainBody, data, arr2, count-1);
		
	} else {

		var otherButtons = mainBody.append("div")
			.attr("class", "otherButtons");

		var mainData = mainBody.datum();
		
		if (mainData.fullSearch) {
			
			otherButtons.append("div")
				.attr("class", "button more")
				.html('<i class="fa fa-search"></i> ' + mainData.fullSearchDisplay)
				.on("click", onMagazineSearchLinkClick);
		}
		
		if (arr2 && arr2.length) {
		
			otherButtons.append("div")
				.attr("class", "button more2")
				.html("More <i class=\"fa fa-angle-double-down\"></i>")
				.on("click", function() {
				
					d3.select(this.parentNode).remove();
					renderMagazineOtherBlocksChunksCount(mainBody, data, arr2, 2);
					
					var url = createDataShareUrl(data, mainData.fullSearch);
					ga('send', 'pageview', url + "&more");
					//alert(url);
				});
		}
		
		otherButtons.append("div")
			.attr("class", "clear");
	}
}

/**
 * Render magazine other blocks chunk.
 */
function renderMagazineOtherBlocksChunk(mainBody, arr) {

	var otherChunk = mainBody.append("div")
		.attr("class", "otherChunk")
		.style("opacity", 0);

	var otherBlocks = otherChunk.selectAll("div.block.other")
		.data(arr)
		.enter()
			.append("div")
			.attr("class", "block other");
		
	renderMagazineOtherBlocksDetails(otherBlocks);
	
	otherChunk.append("div")
		.attr("class", "clear");
		
	otherChunk.transition()
		.duration(500)
		.style("opacity", 1);
}

/**
 * Render magazine other blocks details.
 */
function renderMagazineOtherBlocksDetails(otherBlocks) {

	otherBlocks.each(function(d) {
		
		var otherBlock = d3.select(this);
		
		// other search
		if (d.count && d.hierSearch) {
		
			var otherHead = otherBlock.append("div")
				.attr("class", "otherHead");
	
			if (d.leafSearch) {
				otherHead.on("click", onMagazineSearchLinkClick);
			}
	
			var search = otherHead.append("div")
				.attr("class", "search");
			
			if (d.count && d.hierSearch) {
				search.append("div")
					.attr("class", "text")
					.html(d.hierSearchDisplay);
			} else {
				search.append("div")
					.attr("class", "text")
					.html("&nbsp;");
			}
			
			if (d.count) {
				search.append("div")
					.attr("class", "count")
					.html('<i class="fa fa-check"></i> ' + d.count);
			}
			
			search.append("div")
				.attr("class", "clear");
		}
		
		// main quote
		if (d.quote) {
		
			var otherQuote = otherBlock.append("div")
				.attr("class", "otherQuote");
			
			renderQuoteDetails(otherQuote);
		}
	});
}
	
	