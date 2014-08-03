Tab(it) ::= <<

<a href="/search?tab=$it.tab.abbr$" title="$it.tab.name$">
<div class="
	tab
	sectionTab 
	noSelect
	$if (it.isActive)$
	active
	$endif$
	">
	
	$it.tab.name$
</div>
</a>

>>