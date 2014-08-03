
function initPopups() {
	$(".popupLink").click(function(event) {
		var link = $(this);
		ga('send', 'event', link.attr('href'), 'open');
		openOnePage(link);
		return false;
	});
	var introHidden = $.cookie('introHidden');
	if (introHidden == null) {
		// openIntro();
	}
}

function openOnePage(link) {
	link.colorbox({
		iframe:true,
		width:"800px", 
		height:"600px",
		opacity: 0.7,
		loop: false, 
		open: true
	});
}

function openGuide() {
	$("a.guide").colorbox({
		iframe:true,
		current: "Page {current} of {total}",
		width:"800px", 
		height:"500px",
		opacity: 0.7,
		rel:'guide', 
		loop: false, 
		open: true
	});
}

function openIntro() {
	$("#introPanel").slideDown('fast');
	$(".introOpenLink").fadeOut();
    $.removeCookie('introHidden', { path: '/' });
}

function closeIntro() {
	$("#introPanel").slideUp('fast');
	$(".introOpenLink").fadeIn();
    $.cookie('introHidden', 'yes', { expires: 30, path: '/' });
}
