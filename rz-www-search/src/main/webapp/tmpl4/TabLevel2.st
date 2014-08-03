TabLevel2(it) ::= <<

<a href="/dashboards?tab=$it.tab.abbr$" title="$it.tab.name$">
<div class="tabLevel2
	$if (it.isActive)$
	isActive
	$endif$
">

	$it.tab.name$
	
</div>
</a>

>>