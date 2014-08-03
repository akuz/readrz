function initTabsList(parent) {

	var root;
	if (parent) {
	 	root = parent;
	} else {
	 	root = $("div.tabsList");
	}
	
	var openTabLink = root.find("a.openTabLink");
	openTabLink.unbind("click");
	openTabLink.click(function(event) {
		
		$(this).colorbox({
			iframe:true,
			width: "90%", // $(window).width() - 100, 
			height: "90%", // $(window).height() - 50,
			opacity: 0.9,
			loop: false, 
			open: true
		});
		
		return false;
	});
	
	
	var optionRename = root.find(".option.rename");
	optionRename.unbind("click");
	optionRename.click(function(event) {
	
		var elem = $(this);
		var closestItem = elem.closest(".item");
		var actionUrl = closestItem.children(".actionUrl.hidden").text();
		if (actionUrl == null || actionUrl.length == 0) {
			alert("ERROR: Action URL unknown");
			return false;
		}
		var nameElem = closestItem.find(".name");
		var openTabLinkElem = closestItem.find(".openTabLink");

		inlineConfirmDialog(
			$(this), 
			"Please enter a new name:",
			function(tabName) {
			
				nameElem.text(tabName);
				openTabLinkElem.attr("title", tabName);
				
				callActionUrlAlertOnError(actionUrl + "&tabName=" + encodeURIComponent(tabName));
			},
			null,
			{
				input: true,
				inputValue: nameElem.text().trim(),
				inputMaxLen: 30,
				inputRegex: getTabNameRegex(),
				inputRegexDesc: getTabNameRegexDesc(),
				width: 180,
				confirmText: 'Rename'
			});

		event.stopPropagation();
		return false;
	});
		
		
		
	
	var optionAdd = root.find(".option.add");
	optionAdd.unbind("click");
	optionAdd.click(function(event) {
	
		var elem = $(this);
		var closestItem = elem.closest(".item");
		var actionUrl = closestItem.children(".actionUrl.hidden").text();
		if (actionUrl == null || actionUrl.length == 0) {
			alert("ERROR: Action URL unknown");
			return false;
		}

		inlineConfirmDialog(
			elem, 
			"Please enter a tab name:",
			function(tabName) {

				var loadingItem = $('<div class="item loading"></div>');
				loadingItem.insertBefore(closestItem)

				callActionUrl(
					actionUrl + "&tabName=" + encodeURIComponent(tabName),
					function(result) {
						
						var newItem = $(result);
						newItem.insertAfter(loadingItem);
						loadingItem.remove();
						initTabsList(newItem);
					},
					function(result) {
						alert('ERROR: Could not update your data');
					});
			},
			null,
			{
				input: true,
				inputValue: '',
				inputMaxLen: 30,
				inputRegex: getTabNameRegex(),
				inputRegexDesc: getTabNameRegexDesc(),
				width: 180,
				confirmText: 'Create'
			});

		event.stopPropagation();
		return false;
	});
		
		
		
	var optionPrivacy = root.find(".option.privacy");
	optionPrivacy.unbind("click");
	optionPrivacy.click(function(event) {
	
		var elem = $(this);
		var closestItem = elem.closest(".item");
		var isPublicElem = closestItem.children(".isPublic.hidden");
		var isPublic = isPublicElem.text() == 'true';
		var actionUrl = closestItem.children(".actionUrl.hidden").text();
		if (actionUrl == null || actionUrl.length == 0) {
			alert("ERROR: Action URL unknown");
			return false;
		}

		if (isPublic) {
			inlineConfirmDialog(
				elem, 
				"This tab is <b>Public</b>.<br />Switch to <b>Private</b>?",
				function() {

					isPublicElem.text('false');
					removeClass(elem, 'isPublic');
					addClass(elem, 'isPrivate');
					elem.text('Private');
					
					callActionUrlAlertOnError(actionUrl + "&public=no");
				}, 
				null,
				{
					confirmText: 'Switch'
				});
		} else {
			inlineConfirmDialog(
				elem, 
				'This tab is <b>Private</b>.<br />Switch to <b>Public</b>?',
				function() {
					isPublicElem.text('true');
					removeClass(elem, 'isPrivate');
					addClass(elem, 'isPublic');
					elem.text('Public');
					
					callActionUrlAlertOnError(actionUrl + "&public=yes");
				}, 
				null,
				{
					confirmText: 'Switch'
				});
		}
			
		event.stopPropagation();
		return false;
	});
	
	
	var handleMoveUp = function(closestItem) {
	
		var actionUrl = closestItem.children(".actionUrl.hidden").text();
		if (actionUrl == null || actionUrl.length == 0) {
			alert("ERROR: Action URL unknown");
			return false;
		}
		
		var prevItem = closestItem.prev(".item.exist");
		if (prevItem.length > 0) {
			prevItem.insertAfter(closestItem);
			callActionUrlAlertOnError(actionUrl + "&up=yes");
		}
	}
	
	var optionMoveUp = root.find(".option.moveUp");
	optionMoveUp.unbind("click");
	optionMoveUp.click(function(event) {

		var elem = $(this);
		var closestItem = elem.closest(".item");
		handleMoveUp(closestItem);
		event.stopPropagation();
		return false;
	});
	
	
	
	var optionMoveDown = root.find(".option.moveDown");
	optionMoveDown.unbind("click");
	optionMoveDown.click(function(event) {

		var elem = $(this);
		var closestItem = elem.closest(".item");
		var nextItem = closestItem.next(".item.exist");
		if (nextItem.length > 0) {
			handleMoveUp(nextItem.first());
		}
		event.stopPropagation();
		return false;
	});
	
	
	
	var optionDelete = root.find(".option.delete");
	optionDelete.unbind("click");
	optionDelete.click(function(event) {

		var elem = $(this);
		var closestItem = elem.closest(".item");
		var actionUrl = closestItem.children(".actionUrl.hidden").text();
		if (actionUrl == null || actionUrl.length == 0) {
			alert("ERROR: Action URL unknown");
			return false;
		}
	
		inlineConfirmDialog(
			$(this), 
			"Delete this tab?",
			function() {
				
				closestItem.slideUp('fast', function() {
					closestItem.remove();
				});
				
				callActionUrlAlertOnError(actionUrl + "&delete=yes");
			});
			
		event.stopPropagation();
		return false;
	});


}