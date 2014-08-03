MenuExt(menu) ::= <<

<div class="menuExt">

	$if (menu.rootItem.children)$
		$menu.rootItem.children:MenuItem()$
	$else$
		$MenuItem(false)$
	$endif$

</div>

>>