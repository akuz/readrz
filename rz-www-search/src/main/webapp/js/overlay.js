
function initOverlay() {
	/*
	$(".itemLinksTop a.link").click(function(event){
		return overlayLinkClick(event, $(this));
	});
	$(".itemLinksBottom a.link").click(function(event){
		return overlayLinkClick(event, $(this));
	});
	$(".overlayCloseButton").click(function(event){
		if (typeof(parent.overlayClose) != 'undefined') {
			parent.overlayClose();
		}
	});
	*/
}

function overlayLinkClick(event, link) {

	var href = link.attr("href");
	if (href == null || href.length == 0) {
		return true;
	}

	var body = $("body");
	var bodyPos = body.position();

	var overlay = $("<div class=\"overlay\"><div><iframe src=\"" + href + "\"></iframe></div></div>");
	overlay
		.css('top', bodyPos.top + body.scrollTop())
		.css('left', bodyPos.left)
		.css('right', 0)
		.css('bottom', - body.scrollTop());
		
	body.append(overlay);
	overlay.fadeIn('fast');

	disable_scroll();

	return false;
}

function overlayClose() {
	$("div.overlay").remove();
	enable_scroll();
}
