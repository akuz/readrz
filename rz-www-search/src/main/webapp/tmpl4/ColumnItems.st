ColumnItems(summary) ::= <<

<div class="items">

	$summary.items:{it| 
	<div class="item">
	
		<div class="_loadUrl hidden">$it.loadUrl$</div>
		<div class="_queryHtml hidden">$it.allWordsDisplay$</div>
		<div class="_periodName hidden">$it.periodName$</div>
		<div class="_isTopPeriod hidden">$if (it.isTopPeriod)$yes$endif$</div>

		<a href="$it.loadUrl$" class="sublink">
		<div class="sublinkBar">
			<div class="sublinkText">
				$it.leafWordsDisplay$
				<span>$it.count$</span>
			</div>
			<div class="clear">
			</div>
		</div>
		</a>

		$if (it.result)$
		<div class="result">
			$Result(it.result, false)$
		</div>
		$endif$
		
	</div>
	}$
	
	$if (summary.count)$
	<div class="item">
		<div class="totalBar">
			Total news: <span>$summary.count$</span>
		</div>
	</div>
	$endif$

</div>

>>