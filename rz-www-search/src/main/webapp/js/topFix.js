
var _topBarElem;
var _topFixElem;
var _scrollTop;
var _sectionInfos;
var _topFixIsOn;

function initTopFix() {

	return;

	_topBarElem = $("div#topBar");
	_topFixElem = $("div#topFix");
	_sectionInfos = _topFixElem.find("div.sectionInfos");
	_scrollTop = _topFixElem.find("div.scrollTop");
	_topFixIsOn = false;
	
	_sectionInfos.click(function(event) {
		event.stopPropagation();
		$("html, body").animate({ scrollTop: 0 }, "fast");
	});
	
	_scrollTop.click(function(event) {
		event.stopPropagation();
		$("html, body").animate({ scrollTop: 0 }, "fast");
	});

	topFixUpdateLoop($(window), true);
	
	$(window).scroll(function() {
		topFixUpdate($(this), false);
	});
}

function topFixUpdateLoop(elem, force) {

	topFixUpdate(elem, force);
	
	setTimeout(function() {
		topFixUpdateLoop(elem, false);
	}, 1000);
}

function topFixUpdate(elem, force) {

	if (elem.scrollTop() < 100) {
		if (_topFixIsOn || force) {
			_topBarElem.slideDown('fast');
			_sectionInfos.fadeOut('fast');
			_scrollTop.fadeOut('fast');
			_topFixIsOn = false;
		}
	} else {
		if (_topFixIsOn == false || force) {
			_topBarElem.slideUp('fast');
			_sectionInfos.fadeIn('fast');
			_scrollTop.fadeIn('fast');
			_topFixIsOn = true;
		}
	}	
}