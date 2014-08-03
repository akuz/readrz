function initTabs(parentElem) {
        
        var tabsElem;
        if (parentElem != null) {
                tabsElem = parentElem.find("div.tabs");
        } else {
                tabsElem = $("div.tabs");
        }
        
        var itemsElem = tabsElem.find("div.tabsItem");

        itemsElem.unbind('click');
        itemsElem.click(function(event) {
                var elem = $(this);
                var tabs = elem.closest("div.tabs");
                var items = tabs.find("div.tabsItem");
                items.each(function(index, value) {
                        removeClass($(this), "isActive");
                });
                addClass(elem, "isActive");
        });
}
 