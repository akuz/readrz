Menu(cssClass, menu, icon) ::= <<

<div 
	class="
		$cssClass$
		menu
		noSelect
		$if (menu.isActive)$
		isActive
		$endif$
	">

	<div class="text">
		$if (icon)$
			<i class="fa $icon$"></i>
		$endif$
		$menu.rootItem.text$
	</div>
	
	$MenuExt(menu)$

</div>

>>