
function getLoadUrl(parent) {
	var elem = parent.children("._loadUrl");
	var loadUrl = elem.text();
	if (loadUrl == null || loadUrl.length == 0) {
		alert("Load url not found");
	}
	return loadUrl;
}

function getColumnLeft(index) {
	return 10 + 312 * index;
}

function initColumns() {

	var columnsContainerInner = $("#columnsContainerInner");
	$("#columnsContainer").mousewheel(function (event, delta, deltaX, deltaY) {
		var elem = $(this);
		if (elem.scrollLeft() == 0 && deltaX < 0) {
			event.preventDefault();
			return false;
		}

		if (elem.scrollLeft() + elem.width() >= columnsContainerInner.width() && deltaX > 0) {
			event.preventDefault();
			return false;
		}
	});

	$(".column").each(function(index) {
	
		var columnElem = $(this);
		var columnsContainerElem = columnElem.closest("#columnsContainer");
		var columnsContainerInnerElem = columnElem.closest("#columnsContainerInner");
		
		// only init columns within container
		if (columnsContainerElem.length > 0) {
		
			// handle menu link clicks
			attachColumnLinkHandlers(columnElem);
			
			// only begin loading, if has load url
			var loadUrl = getLoadUrl(columnElem);
			beginLoadingColumn(columnElem, loadUrl);
			
			// position columns
			var indexElem = columnElem.children("._index");
			if (indexElem.length > 0) {
			
				var index = parseInt(indexElem.text());
				var left = getColumnLeft(index);
				columnElem.css('left', left);
				columnElem.show();
				
				columnsContainerInnerElem.width(left + 400);
			}
		}
	});
}

function beginLoadingColumn(columnElem, loadUrl) {

	columnElem.children(".column").remove();

	var columnBodyElem = columnElem.children(".body");
	columnBodyElem.empty();
	
	var columnLoading = $('<div class="loading"><img src="/img/loadingBlack.gif" title="Loading..." alt="Loading..." />Loading...</div>');
	columnBodyElem.append(columnLoading);

	setTimeout(function() {
		loadColumnCall(columnElem, new Date(), loadUrl);
	}, 500);
	
	// google analytics
	ga('send', 'event', 'path', 'load', loadUrl);
	ga('send', 'pageview', {'page': loadUrl, 'title': 'In-page path load'});
}

function loadColumnCall(columnElem, startDate, loadUrl) {

	if (columnElem.is(":visible")) {
	
		$.get(loadUrl, function(data) {
		
			if (data != null && data.trim().length > 0) {
		
				finishLoadingColumn(columnElem, startDate, loadUrl, data);
				
			} else {
			
				setTimeout(function() {
					loadColumnCall(columnElem, startDate, loadUrl);
				}, 500);
				
			}
		});
	}
}

function finishLoadingColumn(columnElem, startDate, loadUrl, data) {

	// calculate time spent
	var endDate = new Date();
	var timeSpent = endDate.getTime() - startDate.getTime();
	
	// google analytics
	ga('send', 'event', 'path', 'loaded', loadUrl, timeSpent);
	ga('send', 'timing', 'path', 'loaded', timeSpent, loadUrl);

	var result = $(data);

	{ // update
		
		columnElem.empty();
		var newChildren = result.find(".column").children();
		columnElem.append(newChildren);
	}

	{ // init

		initTabs(columnElem);
		initMenus(columnElem);
		initMoreButtons(columnElem);
		attachColumnLinkHandlers(columnElem);
	}
}

function attachColumnLinkHandlers(columnElem) {

	var columnHeadElem = columnElem.find('.head');
	
	var btnStar = columnHeadElem.find(".btnStar");
	btnStar.unbind("click");
	btnStar.click(function(event) {
		var elem = $(this);
		var starPanel = elem.next(".starPanel")
		if (starPanel.is(":visible")) {
			starPanel.hide();
		} else {
			starPanel.show();

			loadStarPanel(starPanel);

			var closeButton = starPanel.find(".closeButton");
			closeButton.unbind("click");
			closeButton.click(function(event) {
				event.stopPropagation();
				starPanel.hide();
				return false;
			});
		}
	});	
	
	var btnShare = columnHeadElem.find(".btnShare");
	btnShare.unbind("click");
	btnShare.click(function(event) {
		var elem = $(this);
		var sharePanel = elem.next(".sharePanel")
		if (sharePanel.is(":visible")) {
			sharePanel.hide();
		} else {
			sharePanel.show();
	
			FB.XFBML.parse(sharePanel.find("._facebookSection").get(0));
			twttr.widgets.load();
			
			var closeButton = sharePanel.find(".closeButton");
			closeButton.unbind("click");
			closeButton.click(function(event) {
				event.stopPropagation();
				sharePanel.hide();
				return false;
			});
		}
	});	
	
	var columnHeadLinks = columnHeadElem.find("a");
	columnHeadLinks.unbind('click');
	columnHeadLinks.click(function(event) {
	
		// get link
		var aElem = $(this);
		
		// check link
		if (hasClass(aElem, "noJSWrap")) {
			return;
		}
		
		// get load url
		var loadUrl = aElem.attr("href");

		// find column elem
		var columnElem = aElem.closest(".column");

		// hide all menu items
		closeMenu(columnElem.find(".menu"));
		
		// begin loading
		beginLoadingColumn(columnElem, loadUrl);
		
		event.stopPropagation();
		return false;
	});
	
	var columnBodyElem = columnElem.find('.body');
	var columnBodySublinks = columnBodyElem.find("a.sublink");
	columnBodySublinks.unbind('click');
	columnBodySublinks.click(function(event) {
		event.stopPropagation();
		var itemElem = $(this).closest(".item");
		onColumnItemClick(itemElem);
		return false;
	});
}

function disappearColumn(column) {
	
	var next = column.next();
	var diff = 0;
	if (next.length > 0) {
		diff = column.position().left - next.position().left;
	}

	column.fadeOut('fast', function() {
		while (next.length > 0) {
			var newLeft = next.position().left + diff;
			next.animate({left: newLeft}, 'fast');
			next = next.next();
		}
	});
}

function shiftColumnLeft(column) {
	var prev = column.prev();
	if (prev.length > 0) {
		return shiftColumnRight(prev);
	} else {
		return false;
	}
}

function shiftColumnRight(column) {
	
	var next = column.next();
	if (next.length > 0) {
	
		var columnsContainerElem = column.closest("#columnsContainer");
	
		var columnLeft = column.position().left;
		var nextLeft = next.position().left;
		
		column.animate({left: nextLeft + columnsContainerElem.scrollLeft()}, 'fast');
		next.animate({left: columnLeft + columnsContainerElem.scrollLeft()}, 'fast');
		
		// change elements order
		column.insertAfter(next);
		
		// update moving buttons
		var leftButtons = column.parent().find(".leftButton");
		leftButtons.each(function(index, value) {
			var elem = $(this);
			var closestColumn = elem.closest('.column');
			if (closestColumn.prev('.column').length > 0) {
				removeClass(elem, 'disabledButton');
			} else {
				addClass(elem, 'disabledButton');
			}
		});
		var rightButtons = column.parent().find(".rightButton");
		rightButtons.each(function(index, value) {
			var elem = $(this);
			var closestColumn = elem.closest('.column');
			if (closestColumn.next('.column').length > 0) {
				removeClass(elem, 'disabledButton');
			} else {
				addClass(elem, 'disabledButton');
			}
		});
		return true;	
	} else {
		return false;
	}
}

function loadStarPanel(starPanel) {

	var closestColumn = starPanel.closest(".column")
	var dynamicAct = starPanel.children("._dynamicAct").text();
	var dynamicUrl = starPanel.children("._dynamicUrl").text();
	var isAdd  = (dynamicAct == 'add');
	var isEdit = (dynamicAct == 'edit');
	
	var showLoading = function(message) {
	
		var dynamicPanel = starPanel.children(".dynamicPanel");
		dynamicPanel.empty();
		
		var loadingIcon = $('<div class="loadingIcon">' + message + '...</div>');
		dynamicPanel.append(loadingIcon);
	}
	
	var showSuccess = function(message) {
		
		// get the loaded dynamic panel
		var dynamicPanel = starPanel.children(".dynamicPanel");
		dynamicPanel.empty();
		
		// show success message
		var dynamicContent = $('<div class="starText">' + message + '</div>');
		dynamicPanel.append(dynamicContent);
		
		// cloas star panel
		setTimeout(function() {
			starPanel.fadeOut('slow');
		}, 1000);
	};
	
	var showFailure = function() {
	
		// get the loaded dynamic panel
		var dynamicPanel = starPanel.children(".dynamicPanel");
		dynamicPanel.empty();
		
		// show error message
		var dynamicContent = $('<div class="starText">WARNING: Could not perform action :(</div>');
		dynamicPanel.append(dynamicContent);
	};
	
	showLoading("Loading");

	$.get(dynamicUrl, function(data) {
		
		if (data != null && data.length > 0) {
		
			var dynamicPanel = starPanel.children(".dynamicPanel");
			dynamicPanel.empty();
	
			// show dynamic panel
			var result = $(data);
			result.insertAfter(dynamicPanel);
			dynamicPanel.remove();
			
			// init panel menus
			initMenus(result);
			
			var funcDynamicUpdate = function(actionUrl, message, isNewTab) {
			
				showLoading("Updating");
				
				callActionUrl(
					actionUrl,
					function() {
					
						showSuccess(message);
						
						if (dynamicAct == 'edit') {
							disappearColumn(closestColumn);
						}
					},
					showFailure);
			};
			
			var menuLinks = result.find(".menuExt").find("a");
			menuLinks.unbind("click");
			menuLinks.click(function(event) {
			
				var elem = $(this);
				var href = elem.attr("href");
				if (href != null && href.length > 0) {
					
					if (isAdd) {
		
						funcDynamicUpdate(href, 'Column added!', false);
						
					} else if (isEdit) {
		
						disappearColumn(closestColumn);
						callActionUrl(href, null, function() { alert('Error updating your data'); });
					}
				}
				event.stopPropagation();
				return false;
			});
			
			var addButton = result.find(".addButton");
			addButton.unbind("click");
			addButton.click(function(event) {
	
				var tabNameElem = result.find(".tabName");
				var tabName = tabNameElem.val().trim();
		
				if (tabName.length > 0) {
			
					if (tabName.match(getTabNameRegex())) {
					
						var actionUrl = result.find("._actionUrl").text();
						actionUrl = actionUrl + "&tabName=" + encodeURIComponent(tabName);

						if (isAdd) {
			
							funcDynamicUpdate(actionUrl, 'Column added!', true);
							
						} else if (isEdit) {
			
							disappearColumn(closestColumn);
							callActionUrl(actionUrl, null, function() { alert('Error updating your data'); });
						}

					} else {
					
						var nameError = result.find(".nameError");
						nameError.text(getTabNameRegexDesc());
						nameError.show();
					}
				}
				event.stopPropagation();
				return false;
			});
			
			var leftButton = result.find(".leftButton");
			if (closestColumn.prev(".column").length == 0) {
				addClass(leftButton, "disabledButton");
			} 
			leftButton.unbind("click");
			leftButton.click(function(event) {
	
				if (shiftColumnLeft(closestColumn)) {
				
					var actionUrl = result.find("._actionUrl").text();
					actionUrl = actionUrl + "&left=yes";
					
					callActionUrl(actionUrl, null, function() { alert('Error updating your data'); });
				}

				event.stopPropagation();
				return false;
			});
			
			var rightButton = result.find(".rightButton");
			if (closestColumn.next(".column").length == 0) {
				addClass(rightButton, "disabledButton");
			} 
			rightButton.unbind("click");
			rightButton.click(function(event) {
	
				if (shiftColumnRight(closestColumn)) {

					var actionUrl = result.find("._actionUrl").text();
					actionUrl = actionUrl + "&right=yes";
					
					callActionUrl(actionUrl, null, function() { alert('Error updating your data'); });
				}

				event.stopPropagation();
				return false;
			});
			
			var deleteButton = result.find(".deleteButton");
			deleteButton.unbind("click");
			deleteButton.click(function(event) {
	
				inlineConfirmDialog(
					$(this), 
					"Delete this column?",
					function() {
						var actionUrl = result.find("._actionUrl").text();
						actionUrl = actionUrl + "&delete=yes";
						
						disappearColumn(closestColumn);
						callActionUrl(actionUrl, null, function() { alert('Error updating your data'); });
					}
				);

				event.stopPropagation();
				return false;
			});
			
		} else {
			alert('Could not load, empty result');
		}
	});

}

function onColumnItemClick(columnItemElem) {

	// get column elem
	var columnElem = columnItemElem.closest(".column");

	// get all words to display
	var queryHtml = columnItemElem.children("._queryHtml").html();
	var periodName = columnItemElem.children("._periodName").text();
	var isTopPeriod = columnItemElem.children("._isTopPeriod").text().length > 0;
	
	// extract urls
	var loadUrl = getLoadUrl(columnItemElem);
	var pos = columnElem.offset();
	
	showPopupColumn(pos, loadUrl, queryHtml, true, periodName, !isTopPeriod, null, false);
}

function showPopupColumn(pos, loadUrl, queryHtml, queryIsOn, periodName, periodIsOn, groupName, groupIsOn) {

	// create new column
	var newColumnElem = $("#templateColumn").clone(true).find(".column");
	newColumnElem.children("._loadUrl").text(loadUrl);
	
	// update head of new column
	var newColumnHeadElem = newColumnElem.find(".head");
	
	// update query head
	var btnQuery = newColumnHeadElem.find(".btnQuery")
	btnQuery.html(queryHtml);
	if (queryIsOn) {
		addClass(btnQuery, "isOn");
	}
	
	// update periods menu
	newColumnHeadElem.find(".menuPeriods").each(function(index, value) {
		var menu = $(this);
		if (periodName != null) {
			var menuText = menu.children(".text");
			menuText.text(periodName);
		}
		if (periodIsOn) {
			addClass(menu, "isOn");
		} else {
			removeClass(menu, "isOn");
		}
	});
	
	// update groups menu
	newColumnHeadElem.find(".menuGroups").each(function(index, value) {
		var menu = $(this);
		if (groupName != null) {
			var menuText = menu.children(".text");
			menuText.text(groupName);
		}
		if (groupIsOn) {
			addClass(menu, "isOn");
		} else {
			removeClass(menu, "isOn");
		}
	});

	// position column
	var body = $("body");
	var newColumnsContainer = body;
	var background = newColumnsContainer.children("#columnsPopupBackground");
	if (background.length == 0) {

		background = $('<div id="columnsPopupBackground"></div>');
		background.mousewheel(function (event, delta, deltaX, deltaY) {
			event.preventDefault();
			return false;
		});

		background.click(function(event) {
			var nextAll = $(this).nextAll();
			if (nextAll.length > 1) {
				nextAll.last().remove();
			} else {
				nextAll.remove();
				$(this).remove();
				newColumnsContainer.css("overflow","scroll");
			}
		})
		newColumnsContainer.append(background);
		newColumnsContainer.css("overflow","hidden");
		background.width(newColumnsContainer.width() + newColumnsContainer.scrollLeft());
	}
	newColumnsContainer.append(newColumnElem);

	// show new column
	newColumnElem.css("top", pos.top);
	newColumnElem.css("left", pos.left);
	newColumnElem.show();
	
	// restrict scolling
	newColumnElem.mousewheel(function (event, delta, deltaX, deltaY) {
		if (deltaX < 0 || deltaX > 0) {
			event.preventDefault();
			return false;
		}
	});
	
	// handle column links
	initMenus(newColumnElem);
	attachColumnLinkHandlers(newColumnElem);

	// start loading new column
	beginLoadingColumn(newColumnElem, loadUrl);
}