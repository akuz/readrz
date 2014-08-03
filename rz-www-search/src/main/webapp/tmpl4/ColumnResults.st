ColumnResults(summary) ::= <<

<div class="results">

	$summary.results:{ it |
		$Result(it, false)$
	}$

</div>

>>