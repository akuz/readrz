
function searchInit(parent, onSearch) {
	
	var searchInput = parent.select("input.searchInput");
	var searchClear = parent.select("div.searchClear");
	
	var updateSearchClear = function() {
		var value = searchInput.property("value");
		if (value != null && value.length > 0) {
			searchClear.style("display", "block")
		} else {
			searchClear.style("display", "none");
		}
	}
	
	searchInput.on("keyup", function() {
		updateSearchClear();
	});
	
	searchClear.on("click", function() {
		searchInput.property("value", "");
		updateSearchClear();
		searchInput[0][0].focus();
	});

	searchInput.on("keypress", function() {
		if (d3.event.keyCode == 13) {
			if (onSearch) {
				var value = searchInput.property("value");
				onSearch(value);
			}
			return false;
	    }
	});
	
	updateSearchClear();
}
