
function toInt(n){ return Math.round(Number(n)); };

if (typeof String.prototype.startsWith != 'function') {
	String.prototype.startsWith = function (str) {
		return this.slice(0, str.length) == str;
	};
}

if (typeof String.prototype.endsWith != 'function') {
	String.prototype.endsWith = function (str) {
		return this.slice(-str.length) == str;
	};
}

function padLeft(num, len) {
	var str = '' + num;
	while (str.length < len) {
		str = '0' + str;
	}
	return str;
}

function appendIf(str, suffix) {
	if (str && str.length > 0) {
		return str + suffix;
	} else {
		return str;
	}
}

function encode2(str) {
	return encodeURIComponent(str).replace(/%20/g,'+')
}

function arraySplit(arr, len1) {
	var arr1 = [];
	var arr2 = [];
	if (arr) {
		for (var i=0; i<arr.length; i++) {
			if (i < len1) {
				arr1.push(arr[i]);
			} else {
				arr2.push(arr[i]);
			}
		}
	}
	return {"arr1": arr1, "arr2": arr2};
}
function arrayPush(arr1, arr2) {
	arr1.push.apply(arr1, arr2);
}

function hasClass(elem, clas) {
	var currClas = elem.attr("class");
	return currClas != null && currClas.indexOf(clas) >= 0;
}
function addClass(elem, clas) {
	var currClas = elem.attr("class");
	if (currClas != null) {
		if (currClas.indexOf(clas) < 0) {
			elem.attr("class", currClas + " " + clas);
		}
	} else {
		elem.attr("class", clas);
	}
}
function removeClass(elem, clas) {
	var currClas = elem.attr("class");
	if (currClas != null && currClas.indexOf(clas) >= 0) {
		elem.attr("class", currClas.replace(clas, "").trim());
	}
}

function reloadAfterGettingTrue(url) {
	setTimeout(function() {
		reloadAfterGettingTrueNow(url);
	}, 500);
}

function reloadAfterGettingTrueNow(url) {

	$.get(url, function(data) {
	
		if (data != null) {
	
			var trimmedData = data.trim();
			
			if (trimmedData == 'true') {
			
				location.reload();
				
			} else if (trimmedData == 'false') {

				setTimeout(function() {
					reloadAfterGettingTrueNow(url);
				}, 500);
			}
		}
	});
}
