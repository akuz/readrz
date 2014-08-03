SummaryResults(summary) ::= <<

	$summary.results:{ it |
	<div class="summaryBlock">
	<div class="resultBlock">
	<div class="resultItem">
		$SummaryResult(it)$
	</div>
	</div>
	</div>
	}$
	
>>