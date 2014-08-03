SummaryTOCItems(summary) ::= <<

$summary.items:{it| 
	<a class="link" href="#$it.fullSearchUrl$">
	<div class="button">
		$it.leafSearch$
		<span class="count">$it.count$</span>
	</div>
	</a>
	<div class="clear">
	</div>
}$

$if (summary.count)$
	<div class="total">
		Total News: <span>$summary.count$</span>
	</div>
$endif$

>>