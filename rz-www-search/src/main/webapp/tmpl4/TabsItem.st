TabsItem(it) ::= <<

<a href="$it.url$">
<div class="
	tabsItem 
	noSelect 
	$if (it.isActive)$
	isActive
	$endif$
	$if (it.turnsMenuOn)$
	turnsMenuOn
	$endif$
	">

	<span class="text">
		$it.text$
	</snan>
	
	$if (it.count)$
	<span class="count">
		$it.count$
	</span>
	$endif$
	
</div>
</a>

>>