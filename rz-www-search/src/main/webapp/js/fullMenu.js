function initFullMenu() {

	$("div.fullMenuBody1 div.bullet").click(function (event) {
		
		var elem = $(this);
		var item = elem.closest("div.item");
		
		if (hasClass(item, "isExpanded")) {
			removeClass(item, "isExpanded");
		} else {
			addClass(item, "isExpanded");
		}

		event.stopPropagation();
		return false;
	});

}