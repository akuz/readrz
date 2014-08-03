
function initSummaryItems() {

	var expandBtns = $("div.summary2 div.itemMore div.expandBtn");
	
	expandBtns.click(function (event) {
	
		event.stopPropagation();
		
		var expandBtn = $(this);
		
		var itemMore = expandBtn.closest("div.itemMore");
		
		var subRows = itemMore.siblings("div.subRows");
		
		if (subRows.is(":visible")) {
			subRows.slideUp('fast', function() {
				removeClass(itemMore, "isActive");
			});
		} else {
			addClass(itemMore, "isActive");
			subRows.slideDown('fast');
		}
	});
}