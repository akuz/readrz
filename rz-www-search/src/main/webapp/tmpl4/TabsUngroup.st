TabsUngroup(menu) ::= <<

<div class="
	tabs 
	tabsUngroup
	$if (menu.isOn)$
	isOn
	$endif$
	">
	
	$menu.rootItem.children:TabsItem()$
	
</div>

>>