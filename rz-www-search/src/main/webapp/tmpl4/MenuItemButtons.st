MenuItemButtons(children) ::= <<

$children:{it | 

	$if (it.isTaxonomy)$
		
		<div class="menuGroup noSelect">
			$it.text$
		</div>
		
		$if (it.children)$
		$MenuItemButtons(it.children)$
		$endif$
		
	$else$
	
		$MenuItem(it)$

	$endif$

}$

>>