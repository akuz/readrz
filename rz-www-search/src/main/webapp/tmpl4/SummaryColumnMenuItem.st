SummaryColumnMenuItem(it) ::= <<

	<a href="$it.url$">
	<div class="
		$if (it.isTaxonomy)$
		group
		$else$
		button
		$endif$
		$if (it.isActive)$
		isActive
		$endif$
		$if (!it.turnsMenuOn)$
		isDefault
		$endif$
		">
		$it.text$
		
		$if (!it.isTaxonomy && it.count)$
		<span class="count">$it.count$</span>
		$endif$
	</div>
	</a>
	
	$if (it.isActive && it.children && !it.isTaxonomy)$
	<div class="wrap">
	$endif$
	$it.children:SummaryColumnMenuItem()$
	$if (it.isActive && it.children && !it.isTaxonomy)$
	</div>
	$endif$
>>