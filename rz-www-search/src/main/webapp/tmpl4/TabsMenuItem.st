TabsMenuItem(it) ::= <<
<a href="$it.url$" title="$it.text$">
<div class="
	tab
	noSelect
	$if (it.isActive)$
	isActive
	$endif$
	">

	$it.text$
</div>
</a>
>>