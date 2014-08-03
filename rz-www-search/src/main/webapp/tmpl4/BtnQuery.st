BtnQuery(query) ::= <<

<div class="
	btnQuery
	noSelect
	$if (!query.isEmpty)$
	isOn
	$endif$
	" title="$query.queryString$">
	
	$if (query.isEmpty)$
		All
	$else$
		$query.queryDisplay$
	$endif$
</div>

>>