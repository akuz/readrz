
function init() {

	// facebook login fix
	if (window.location.href.indexOf("#_=_") > 0) { 
		window.location = window.location.href.replace(/#.*/, "");
	}
	
	if (typeof(initPopups) != 'undefined') {
		initPopups();
	}
	if (typeof(initMenus) != 'undefined') {
		initMenus();
	}
	if (typeof(initSearch) != 'undefined') {
		initSearch();
	}
	if (typeof(initTabs) != 'undefined') {
		initTabs();
	}
	if (typeof(initColumns) != 'undefined') {
		initColumns();
	}
	if (typeof(initTabsList) != 'undefined') {
		initTabsList();
	}
	if (typeof(initMoreButtons) != 'undefined') {
		initMoreButtons();
	}
	if (typeof(initOverlay) != 'undefined') {
		initOverlay();
	}
	if (typeof(initFullMenu) != 'undefined') {
		initFullMenu();
	}
	if (typeof(initTopFix) != 'undefined') {
		initTopFix();
	}
	if (typeof(initSummaryItems) != 'undefined') {
		initSummaryItems();
	}
}

function load() {
	if (typeof(initSummaryBlocks) != 'undefined') {
		initSummaryBlocks();
	}
}

function getTabNameRegex() {
	return new RegExp('^[ .a-zA-Z0-9_-]+$', 'gi');
}
function getTabNameRegexDesc() {
	return 'Letters, numbers, dot, _, -.';
}

function callActionUrl(actionUrl, onSuccess, onFailure) {
	$.get(actionUrl, function(data) {
		if (data != null && data.startsWith("true")) {
			if (onSuccess != null) {
				var result = null;
				if (data != null && data.startsWith("true|")) {
					result = data.substring(5);
				}
				onSuccess(result);
			}
		} else {
			if (onFailure != null) {
				var result = null;
				if (data != null && data.startsWith("false|")) {
					result = data.substring(6);
				}
				onFailure(result);
			}
		}
	});
}

function callActionUrlAlertOnError(actionUrl) {
	callActionUrl(
		actionUrl,
		null,
		function(message) {
			alert("ERROR: Could not update your data\nMessage: " + message);
		});
}
