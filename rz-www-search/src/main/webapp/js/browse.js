
var browse = {};

/**
 * Browse init.
 */
function browseInit() {

	browse.absoluteUrl = "http://www.readrz.com";
	browse.subscribeUrl = "http://eepurl.com/d48hj";

	// find permanent elements
	browse.rootPanel = d3.select("div#rootPanel");

	// load requests counter	
	browse.requestCounter = 0;

	// init params
	browse.par = {};

	// zoom params
	browse.par.zoom = {}
	browse.par.zoom.levels = 3;
	browse.par.zoom.margin = {top: 180, right: 210, bottom: 180, left: 210};
	browse.par.zoom.radius = 160;
	browse.par.zoom.centerBackColor  = "#ffffff";
	browse.par.zoom.backFlashColor   = "#ffffdd";
	browse.par.zoom.pathStrokeColor  = "#ffffff";
	browse.par.zoom.activeFontSize   = "16px";
	browse.par.zoom.activeTextColor  = "#000000";
	browse.par.zoom.innerFontSize    = "12px";
	browse.par.zoom.innerTextColor   = "#222222";
	browse.par.zoom.outerFontSize    = "10px";
	browse.par.zoom.outerTextColor   = "#333333";
	browse.par.zoom.activeHue           = 60; // yellow
	browse.par.zoom.activeSaturation    = 0.7;
	browse.par.zoom.activeLuminanceMin  = 0.6;
	browse.par.zoom.activeLuminanceMax  = 0.8;
	browse.par.zoom.passiveHue          = 220; // blue greenish
	browse.par.zoom.passiveSaturation   = 0.1;
	browse.par.zoom.passiveLuminanceMin = 0.85;
	browse.par.zoom.passiveLuminanceMax = 0.95;
}

/**
 * Browse load summary.
 */
function browseLoadSummary(isInteraction, url, dataPanel) {

	// use single request key
	var requestKey = "singleActive";
	
	// count request
	browse.requestCounter += 1;
	var requestNumber = browse.requestCounter;

	// abort current, if any
	getJSON2_abort(requestKey);
	
	if (url) {
	
		// show loading sign
		browseSetLoading(dataPanel, true);
		
		// initial request delay
		setTimeout(function() {
			
			// check no other request still
			if (requestNumber != browse.requestCounter) {
				return;
			}

			// log as pageview
			if (isInteraction) {
				ga('send', 'pageview', url);
			}

			// make request
			getJSON2(1000, url, requestKey,
				function(data) {
				
					// if still the same request
					if (requestNumber == browse.requestCounter) {
					
						// show data
						browseSetLoading(dataPanel, false);
						browseSetSummary(dataPanel, data);
					}
				},
				function(warning) {
				
					// if error, show nothing
					browseSetLoading(dataPanel, false);
					alert(warning);
				}
			);
		}, 10);
		
	} else {
	
		// null url, show nothing
		browseSetLoading(dataPanel, false);
		browseSetSummary(dataPanel, null);
	}
}

/**
 * Browse set loading.
 */
function browseSetLoading(dataPanel, on) {

	var loading = dataPanel.selectAll("div.loading")
		.data(on ? [0] : []);
	
	loading.enter().append("div")
		.attr("class", "loading")
		.style("opacity", 0)
		.transition()
			.duration(1000)
			.style("opacity", 1);
	
	loading.exit().remove();
}

/**
 * Browse set summary data.
 */
function browseSetSummary(dataPanel, data) {

	if (data) {
		dataPanel.datum(data);
		updateDataPanelDetails(dataPanel);
	} else {
		dataPanel.selectAll("*").remove();
	}
}

/**
 * Browse set history.
 */
function browseSetHistory(dataPanel, data) {

	var listPanel = dataPanel.select("div.listPanel");
	if (listPanel.size() == 0) {
		listPanel = dataPanel
			.append("div")
			.datum(null)
			.attr("class", "listPanel");
	} else {
		listPanel.selectAll("*").remove();
	}
	renderMagazine(listPanel, data);
}

/**
 * Render data panel details.
 */
function updateDataPanelDetails(dataPanel) {

	var data = dataPanel.datum();
	
	// --------------------
	// ----- controls -----
	// --------------------
	if (data.summId) {
	
		var controlsPanel = dataPanel.select("div.controlsPanel");
		if (controlsPanel.size() == 0) {
		
			controlsPanel = dataPanel
				.append("div")
				.datum(null)
				.attr("class", "controlsPanel");
		
			{ // home button
			
				controlsPanel
					.append("div")				
					.attr("class", "btn home")
					.on("click", function() {
						dataPanel.select("div.searchPanel").remove();
						dataPanel.select("div.menuPanels").remove();
						dataPanel.select("div.listPanel").remove();
						var d = dataPanel.datum();
						var url = createDataHomeUrl(d);
						browseLoadSummary(true, url, dataPanel);
					})
					.append("i")
					.attr("class", "fa fa-home");
			}
		
			{ // refresh button
			
				controlsPanel
					.append("div")				
					.attr("class", "btn refresh")
					.on("click", function() {
						dataPanel.select("div.searchPanel").remove();
						dataPanel.select("div.menuPanels").remove();
						dataPanel.select("div.listPanel").remove();
						var d = dataPanel.datum();
						var url = "/summary" + createDataUrl(d, null, null, false);
						browseLoadSummary(true, url, dataPanel);
					})
					.append("i")
					.attr("class", "fa fa-refresh");
			}
		
			{ // search button
			
				controlsPanel.append("div")				
					.attr("class", "btn search")
					.on("click", function() {
						showSearchPanel(dataPanel);
						var searchPanel = dataPanel.select("div.searchPanel");
						var searchInput = searchPanel.select("input.searchInput");
						searchInput[0][0].focus();
					})
					.append("i")
					.attr("class", "fa fa-search");
			}
			
			{ // favourite button
		
				controlsPanel
					.append("a")
					.attr("href", browse.subscribeUrl)
					.attr("target", "_blank")
						.append("div")
						.attr("class", "btn")
							.append("i")
							.attr("class", "fa fa-star");
			}
		
			{ // share button
			
				controlsPanel
					.append("div")
					.attr("class", "btn")
					.on("click", function() {
	
						var d = dataPanel.datum();
						var url = createDataShareUrl(d, null);
						var text = createDataText(d, null, null, true);
						
						var elem = d3.select(this);
						onShareUrlText(elem, url, text, true, 10, 0, null);
	
						ga('send', 'pageview', url + "&share");
						//alert(url);
					})
					.append("i")
					.attr("class", "fa fa-share");
			}
		
			/*
			{ // btc button
			
				controlsPanel
					.append("div")
					.attr("class", "btn")
					.on("click", function() {
						alert("TODO: Donate!");
					})
					.append("i")
					.attr("class", "fa fa-btc");
			}
			*/
		}
	}

	// ------------------
	// ----- search -----
	// ------------------
	if (data.summId) {
	
		var searchPanel = dataPanel.select("div.searchPanel");
		if (searchPanel.size() == 0) {
		
			searchPanel = dataPanel
				.append("div")
				.datum(null)
				.attr("class", "searchPanel")
				.style("margin-bottom", "0px")
				.style("height", "0px");
	
			var searchInput = searchPanel.append("input")
				.attr("class", "searchInput")
				.attr("type", "text")
				.property("value", data.search ? data.search : "");
	
			searchPanel.append("i")
				.attr("class", "fa fa-search searchIcon");
	
			searchPanel.append("div")
				.attr("class", "searchClear")
				.append("i")
				.attr("class", "fa fa-times");
	
			searchInit(searchPanel, function(value) {
				dataPanel.select("div.menuPanels").remove();
				dataPanel.select("div.listPanel").remove();
				var d = dataPanel.datum();
				var url = "/summary" + createDataUrl(d, value, null, false);
				browseLoadSummary(true, url, dataPanel);
			});
						
			if (data.search) {
				showSearchPanel(dataPanel);
			}
		} else {
		
			searchPanel.property("value", data.search);
			
			if (!data.search) {
				hideSearchPanel(dataPanel);
			}
		}
	}

	// -----------------
	// ----- menus -----
	// -----------------
	if (data.menus) {
	
		var menuPanels = dataPanel.select("div.menuPanels");
		if (menuPanels.size() == 0) {
			menuPanels = dataPanel
				.append("div")
				.datum(null)
				.attr("class", "menuPanels");
		}
		{ // init all menus
		
			var menus = data.menus;
			menus = arraySplit(menus, 2).arr1;
			
			var menuPanel = menuPanels
				.selectAll("div.menuPanel")
				.data(menus);
				
			var onZoom = function(p, d) {
			
				var data = dataPanel.datum();
				var index = parseInt(p.attr("index"));
				
				var newGroupIds = "";
				for (var i=0; i<index; i++) {
					var activeNode = data.menus[i]._activeNode;
					 if (activeNode) {
						if (activeNode.groupId) {
							if (newGroupIds.length > 0) {
								newGroupIds = newGroupIds + ",";
							}
							newGroupIds = newGroupIds + activeNode.groupId;
						}
					} else {
						activeNode = d3_find_active_node(data.menus[i]);
						if (activeNode.groupId) {
							if (newGroupIds.length > 0) {
								newGroupIds = newGroupIds + ",";
							}
							newGroupIds = newGroupIds + activeNode.groupId;
						}
					}
				}
				if (d.groupId) {
					if (newGroupIds.length > 0) {
						newGroupIds = newGroupIds + ",";
					}
					newGroupIds = newGroupIds + d.groupId;
				}
	
				for (var i=index+1; i<data.menus.length; i++) {
					menuPanels.select("div.index" + i).remove();
				}
	
				dataPanel.select("div.listPanel").remove();
				var url = "/summary" + createDataUrl(data, null, newGroupIds, false);
				//var url = createDataAddGroupUrl(data, d.groupId);
				//alert(url);
				browseLoadSummary(true, url, dataPanel);
			}
	
			menuPanel
				.enter()
					.append("div")
					.attr("class", function(d, i) { return "menuPanel index" + i;})
					.attr("index", function(d, i) { return i; })
					.each(function(d) {
						var menuPanel = d3.select(this);
						d3_menu(menuPanel, d, function(d) {
							onZoom(menuPanel, d);
						});
					});
		}
	}
	
	// ----------------
	// ----- list -----
	// ----------------
	if (data.list) {
	
		var listPanel = dataPanel.select("div.listPanel");
		if (listPanel.size() == 0) {
			listPanel = dataPanel
				.append("div")
				.datum(null)
				.attr("class", "listPanel");
		} else {
			listPanel.selectAll("*").remove();
		}
		renderMagazine(listPanel, data);
	}

	// -----------------------
	// ----- stop clicks -----	
	// -----------------------
	dataPanel.on("click", function() {
		d3.event.stopPropagation();
	});
}

function showSearchPanel(dataPanel) {
	dataPanel.selectAll("div.searchPanel")
		.transition()
			.duration(300)
			.style("height", "33px")
			.style("margin-bottom", "30px");
}

function hideSearchPanel(dataPanel) {
	dataPanel.selectAll("div.searchPanel")
		.transition()
			.duration(300)
			.style("height", "0px")
			.style("margin-bottom", "0px");
}

/**
 * Render magazine.
 */
function renderMagazine(listPanel, data) {

	listPanel.selectAll("*").remove();
	renderMagazineMainBlocksChunks(listPanel, data);
}

/**
 * Render magazine main blocks chunks.
 */
function renderMagazineMainBlocksChunks(listPanel, data) {

	// render from data
	if (data && data.list) {
		
		// render main blocks chunks count
		renderMagazineMainBlocksChunksCount(listPanel, data, data.list, 1);
	}
}

/**
 * Render magazine main blocks chunks count.
 */
function renderMagazineMainBlocksChunksCount(listPanel, data, arr, count) {

	var CHUNK_SIZE = 20;
	
	var arrSplit = arraySplit(arr, CHUNK_SIZE);
	var arr1 = arrSplit.arr1;
	var arr2 = arrSplit.arr2;

	if (arr1 && arr1.length > 0) {
	
		renderMagazineMainBlocksChunk(listPanel, data, arr1);
	}
	
	if (arr2 && arr2.length) {

		if (count > 1) {
		
			renderMagazineMainBlocksChunksCount(listPanel, data, arr2, count-1);
			
		} else {
	
			var mainButtons = listPanel.append("div")
				.attr("class", "mainButtons");
				
			var showMoreHtml = "Show more <i class=\"fa fa-chevron-down\"></i>";

			mainButtons.append("div")
				.attr("class", "more")
				.html(showMoreHtml)
				.on("click", function(d) {

					d3.select(this.parentNode).remove();
					renderMagazineMainBlocksChunksCount(listPanel, data, arr2, 1);

					var url = createDataShareUrl(data, null);
					ga('send', 'pageview', url + "&more");
					//alert(url);
				});
			
			mainButtons.append("div")
				.attr("class", "clear");
		}
	}
}

/**
 * Render magazine main blocks chunk.
 */
function renderMagazineMainBlocksChunk(listPanel, data, arr) {

	var mainChunk = listPanel.append("div")
		.attr("class", "mainChunk")
		.style("opacity", 0);
		
	var mainBlocks = mainChunk.selectAll("div.block")
		.data(arr)
		.enter()
			.append("div")
			.attr("class", "block");

	renderMagazineMainBlocksDetails(mainBlocks);
	
	mainChunk.transition()
		.duration(500)
		.style("opacity", 1);
}

/**
 * Render magazine main blocks details.
 */
function renderMagazineMainBlocksDetails(blocks) {

	blocks.each(function(d) {
		
		var block = d3.select(this);
		
		if (d.quote) {

			var mainQuote = block.append("div")
				.attr("class", "mainQuote");
			
			renderQuoteDetails(mainQuote);
		}
		
		if (d.count > 1) {
		
			{
				var mainKeywords = block.append("div")
					.attr("class", "mainKeywords");
				
				mainKeywords.selectAll("div.mainKeyword")
					.data(d.keywords)
					.enter()
						.append("div")
						.attr("class", "mainKeyword")
						.attr("title", function(d) { return d.w; })
						.text(function(d) { return d.w; });
			}
			{
				var mainSources = block.append("div")
					.attr("class", "mainSources");
				
				mainSources.selectAll("div.mainSource")
					.data(d.sources)
					.enter()
						.append("div")
						.attr("class", "mainSource")
						.text(function(d) { return d.source; })
						.on("click", function(d) {
						
							var ids = "";
							if (d.items) {
								for (var i=0; i<d.items.length; i++) {
									var item = d.items[i];
									if (item.snapId) {
										if (ids.length > 0) {
											ids = ids + ",";
										}
										ids = ids + item.snapId;
									}
								}
							}
						
							var elem = d3.select(this);
							
							var dataPanel = browseElemDataPanelPop(elem, -170);
							
							browseLoadSummary(true, "/history?ids=" + ids, dataPanel);
						});

				mainSources
					.append("div")
					.attr("class", "mainSource")
					.html('Read All Articles <i class="fa fa-chevron-circle-down"></i>')
					.on("click", function() {
					
						var ids = "";
						for (var j=0; j<d.sources.length; j++) {
							var source = d.sources[j];
							if (source.items) {
								for (var i=0; i<source.items.length; i++) {
									var item = source.items[i];
									if (item.snapId) {
										if (ids.length > 0) {
											ids = ids + ",";
										}
										ids = ids + item.snapId;
									}
								}
							}
						}
						
						var elem = d3.select(this);
						
						var dataPanel = browseElemDataPanelPop(elem, -170);
						
						browseLoadSummary(true, "/history?ids=" + ids, dataPanel);

					});
			}
		}
	});
}

/********/
/* URLs */
/********/

function onMagazineSearchLinkClick() {
	
	var link = d3.select(this);
	onSearchElementClick(link, -80);
}

function onSearchElementClick(elem, offset) {

	// create pop data panel	
	var popDataPanel = browseElemDataPanelPop(elem, offset);

	// create summary url
	var dataPanel = d3_closest(elem, "dataPanel");
	var data = dataPanel.datum();
	var search = elem.datum().fullSearch;
	var url = createSearchUrl(data, search);
	
	// load pop data panel
	browseLoadSummary(true, url, popDataPanel);
}

function createSearchUrl(data, search) {
	return "/summary" + createDataUrl(data, search, null, true);
}

function createFilterUrl(data, groupId) {
	return "/summary" + createDataUrl(data, null, groupId, false);
}

function createDataShareUrl(data, newSearch) {
	return "/browse" + createDataUrl(data, newSearch, null, newSearch);
}

function createDataText(data, newSearch, addGroupName, keepNonTopicGroups) {

	var text = "";

	if (newSearch) {
		text = text + '&ldquo;' + newSearch + '&rdquo;';
	} else if (data.searchDisplay) {
		text = text + '&ldquo;' + data.searchDisplay + '&rdquo;';
	}
	
	if (data.groups) {
		for (var i=0; i<data.groups.length; i++) {
			var group = data.groups[i];
			if (group.isTopic || keepNonTopicGroups) {
				text = appendIf(text, " & ");
				text = text + group.name;
			}
		}
	}
	
	if (addGroupName) {
		text = appendIf(text, " & ");
		text = text + addGroupName;
	}
	
	text = appendIf(text, " - ");
	text = text + data.periodName;
	
	return text;
}

function createDataHomeUrl(data) {
	var url = "/summary?p=" + data.period.abbr;
	return url;
}
function createDataSearchUrl(data, newSearch) {
	var url = "/summary?p=" + data.period.abbr;
	if (newSearch) {
		url = url + "&q=" + encode2(newSearch);
	}
}
function createDataAddGroupUrl(data, addGroupId) {
	var url = "/summary?p=" + data.period.abbr;
	if (data.search) {
		url = url + "&q=" + encode2(data.search);
	}
	if (data.groups) {
		url = url + "&g=";
		for (var i=0; i<data.groups.length; i++) {
			if (i > 0) {
				url = url + ",";
			}
			var group = data.groups[i];
			url = url + group.id;
		}
		if (addGroupId) {
			url = url + ",";
			url = url + addGroupId;
		}
	} else {
		if (addGroupId) {
			url = url + "&g=";
			url = url + addGroupId;
		}
	}
	return url;
}
function createDataUrl(data, newSearch, newGroupIds, removeNonTopicGroups) {

	var url = "?p=" + data.period.abbr;

	if (newSearch != null) {
		if (newSearch) { // not empty string
			url = url + "&q=" + encode2(newSearch);
		}
	} else if (data.search) {
		url = url + "&q=" + encode2(data.search);
	}
	
	if (newGroupIds != null) {
		if (newGroupIds.length > 0) {
			url = url + "&g=" + newGroupIds;
		}
	} else {
		var groupsAdded = 0;
		if (data.groups) {
			for (var i=0; i<data.groups.length; i++) {
				var group = data.groups[i];
				if (removeNonTopicGroups && group.isTopic == false) {
					continue;
				}
				if (groupsAdded == 0) {
					url = url + "&g=";
				} else {
					url = url + ",";
				}
				url = url + group.id;
				groupsAdded += 1;
			}
		}
	}
	
	return url;
}

function onShareUrlText(elem, url, text, isBlock, offsetTop, offsetLeft, offsetRight) {

	var pop = browsePop();

	var elemRect = elem[0][0].getBoundingClientRect();
	var popRect  = pop[0][0].getBoundingClientRect();

	var dialogTop = - popRect.top  + elemRect.bottom + offsetTop;
	
	var shareDialog = pop.append("div")
		.attr("class", "shareDialog")
		.style("top", dialogTop + "px")
		.on("click", function() {
			d3.event.stopPropagation();
		})
		.on("keydown", function() {
			if (d3.event.keyCode == 27) {
				d3.event.stopPropagation();
				pop.remove();
			}
		});
	
	if (offsetLeft != null) {
		var dialogLeft = + elemRect.left + offsetLeft;
		shareDialog.style("left", dialogLeft + "px");
	} else if (offsetRight != null) {
		var dialogRight = + popRect.right - elemRect.right + offsetRight;
		shareDialog.style("right", dialogRight + "px");
	} else {
		alert("Error: no (left or right) offset is not provided");
	}
	
	{ // close
	
		var btns = shareDialog.append("div")
			.attr("class", "shareBtns");
			
		var closeBtn = btns.append("div")
			.attr("class", "btn")
			.on("click", function() {
				d3.event.stopPropagation();
				pop.remove();
			});
			
		closeBtn.append("i")
			.attr("class", "fa fa-times");
	}
	{ // title
	
		shareDialog.append("div")
			.attr("class", "title")
			.text(isBlock ? "Share News Block" : "Share Article");
	}
	
	var absUrl = browse.absoluteUrl + url;

	{ // input
	
		var input = shareDialog.append("input")
			.property("value", absUrl)
			.on("cut", function() {
				d3.event.preventDefault();
			})
			.on("paste", function() {
				d3.event.preventDefault();
			})
			.on("keydown", function() {
				if (d3.event.metaKey ||
					d3.event.ctrlKey ||
					d3.event.altKey) {
					return;
				}
				if (d3.event.keyCode == 27) {
					return;
				}
				d3.event.preventDefault();
			})
			.on("click", function() {
				this.select();
				this.focus();
			});
			
		input[0][0].select();
		input[0][0].focus();
	}
		
	{ // links
	
		var linksRow = shareDialog.append("div")
			.attr("class", "linksRow");
			
		{ // facebook
		
			var fbLink = linksRow.append("div")
				.attr("class", "link")
				.html('<fb:share-button href="' + absUrl + '" type="button_count"></fb:share-button>');

			if (ensureFacebook()) {
				FB.XFBML.parse(fbLink[0][0]);
			}
		}
		
		{ // twitter
		
			var twLink = linksRow.append("div")
				.attr("class", "link")
				.html('<a class="twitter-share-button" ' + 
				(text ? ' data-text="' + text + '" ' : '') +
				' href="' + absUrl + '" data-url="' + absUrl + 
				'" data-size="small" data-via="readrz">Tweet</a>');
		
			if (ensureTwitter()) {
				twttr.widgets.load(twLink[0][0]);
			}
		}
		
		{ // linkedin
		
			var inLink = linksRow.append("div")
				.attr("class", "link")
				.html('<script type="IN/Share" data-url="' + absUrl + '" data-counter="right"></script>');
		
			if (ensureLinkedIn()) {
				IN.parse(inLink[0][0]);
			}
		}
	}
}

function createQuoteShareUrl(data, d) {
	
	var url = createDataShareUrl(data, d.fullSearch);
	url = url + "&s=" + d.quote.id;
	url = url + "&sq=" + encode2(d.fullSearch);
	return url;
}
function onQuoteElemShareClick(elem, quoteClass, offsetTop, offsetLeft, offsetRight) {

	var quote = d3_closest(elem, quoteClass);
	var d = quote.datum();
	
	var dataPanel = d3_closest(quote, "dataPanel");
	var data = dataPanel.datum();
	
	var url = createQuoteShareUrl(data, d);
	
	onShareUrlText(elem, url, $(quote[0][0]).find("div.title").text().trim(), false, offsetTop, offsetLeft, offsetRight);
	
	ga('send', 'pageview', url + "&share");
	//alert(url);
}
function onQuoteElemTrackView(elem, quoteClass) {

	var quote = d3_closest(elem, quoteClass);
	var d = quote.datum();
	
	var dataPanel = d3_closest(quote, "dataPanel");
	var data = dataPanel.datum();
	
	var url = createQuoteShareUrl(data, d);
	
	ga('send', 'pageview', url + "&view");
	//alert(url);
}
function onQuoteTrackPreview(quote) {

	var d = quote.datum();
	
	var dataPanel = d3_closest(quote, "dataPanel");
	var data = dataPanel.datum();
	
	var url = createQuoteShareUrl(data, d);
	
	ga('send', 'pageview', url + "&preview");
	//alert(url);
}
