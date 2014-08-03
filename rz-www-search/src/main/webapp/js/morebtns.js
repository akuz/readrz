
function initMoreButtons(parentElem) {
	var moreButtons;
	if (parentElem != null) {
		moreButtons = parentElem.find("div.result").find("div.moreButton");
	} else {
		moreButtons = $("div.result").find("div.moreButton");
	}
	
	moreButtons.unbind('click');
	moreButtons.click(function(event) {
		event.stopPropagation();
		return onMoreButtonClick($(this));
	});
}

function onMoreButtonClick(moreElem) {
	
	var pathsItemElem = moreElem.closest(".pathsItem");
	var loadUrlAsync = null;
	if (pathsItemElem.length > 0) {
		loadUrlAsync = getLoadUrlAsync(pathsItemElem);
	}

	var resultElem = moreElem.closest(".result");
	var textQuoteElem = resultElem.find(".textQuote");

	var titleLinkElem = resultElem.find("div.title").find("a");
	var postUrl = null;
	if (titleLinkElem.length > 0) {
		postUrl = titleLinkElem.attr("href");
	}

	var clas = moreElem.attr('class');
	if (clas.indexOf(" selected") >= 0) {
	
		moreElem.attr('class', clas.replace(' selected',''));
		textQuoteElem.hide();
		
		// google analytics
		if (loadUrlAsync != null) {
			ga('send', 'event', 'path', 'less', loadUrlAsync);
		}
		if (postUrl != null) {
			ga('send', 'event', 'post', 'less', postUrl);
		}
		
	} else {
		moreElem.attr('class', clas + ' selected');
		textQuoteElem.show();
		
		// google analytics
		if (loadUrlAsync != null) {
			ga('send', 'event', 'path', 'more', loadUrlAsync);
		}
		if (postUrl != null) {
			ga('send', 'event', 'post', 'more', loadUrlAsync);
		}
	}
	
	return false;
}
