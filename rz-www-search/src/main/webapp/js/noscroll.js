
// left: 37, up: 38, right: 39, down: 40, spacebar: 32, pageup: 33, pagedown: 34, end: 35, home: 36
var noscroll_keys = [32, 33, 34, 35, 36, 37, 38, 39, 40];

function noscroll_preventDefault(e) {
	e = e || window.event;
	if (e.preventDefault)
		e.preventDefault();
	e.returnValue = false;  
}

function noscroll_keydown(e) {
	for (var i = noscroll_keys.length; i--;) {
		if (e.keyCode === noscroll_keys[i]) {
			noscroll_preventDefault(e);
			return;
		}
	}
}

function noscroll_wheel(e) {
	noscroll_preventDefault(e);
}

function disable_scroll() {
//	if (window.addEventListener) {
//		window.addEventListener('DOMMouseScroll', noscroll_wheel, false);
//	}
//	window.onmousewheel = document.onmousewheel = noscroll_wheel;
//	document.onkeydown = noscroll_keydown;
	$("body").bind("mousewheel", function(e){
		e.preventDefault();
	});
	$("body").bind("touchmove", function(e){
		e.preventDefault();
	});
}

function enable_scroll() {
//	if (window.removeEventListener) {
//		window.removeEventListener('DOMMouseScroll', noscroll_wheel, false);
//	}
//	window.onmousewheel = document.onmousewheel = document.onkeydown = null;
	$("body").unbind("mousewheel");
	$("body").unbind("touchmove");
}