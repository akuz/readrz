
function initMenus(parentElem) {
	
	var menusElem;
	if (parentElem != null) {
		menusElem = parentElem.find("div.menu");
	} else {
		menusElem = $("div.menu");
	}
	
	menusElem.unbind('click');
	menusElem.click(function(event) {
		toggleMenu($(this));
	});
	
	menusElem.unbind('mouseleave');
	menusElem.mouseleave(function(event) {
		closeMenu($(this));
	});
	
	var menuButtons = menusElem.find(".menuExt").find(".menuButton");

	menuButtons.unbind('click');
	menuButtons.click(function(event) {
		var elem = $(this);
		var elemText = elem.children(".text");
		if (elemText.length > 0) {
		
			var menu = elem.closest("div.menu");
			
			if (hasClass(elem, "turnsMenuOn")) {
				addClass(menu, "isOn");
			} else {
				removeClass(menu, "isOn");
			}
			
			//var menuText = menu.children("div.text");
			//menuText.text(elemText.text());
			
			closeMenu(menu);
		}
	});

	menuButtons.unbind('mouseenter');
	menuButtons.mouseenter(function(event) {
	
		var elem = $(this);

		// hide siblings' children		
		elem.parent().parent().siblings().find('.menuChildren').hide();
		
		// show my children
		var children = elem.parent().next(".menuChildren");
		children.show();

		//var pos = elem.parent().position();
		children.css('left', elem.parent().parent().width() + 10);
	});
}

function toggleMenu(elem) {
	if (hasClass(elem, "isOpen")) {
		closeMenu(elem);
	} else {
		addClass(elem, "isOpen");
		elem.children(".menuExt").show();
	}
}

function closeMenu(elem) {
	removeClass(elem, "isOpen");
	elem.children(".menuExt").hide();
}
