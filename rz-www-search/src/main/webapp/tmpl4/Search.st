Search() ::= <<

<div id="search">
	$if (page.summary.query.isEmpty)$
		$SearchEdit("searchEdit", true)$
	$else$
		$SearchEdit("searchEdit", false)$
		$SearchTerms("searchTerms", true)$
	$endif$
</div>

>>