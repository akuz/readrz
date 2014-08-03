Column(summary) ::= <<
<div class="column">
	
	<div class="_index hidden">$summary.index$</div>
	<div class="_loadUrl hidden">$summary.loadUrl$</div>
	
	<div class="head">
		<div class="head1">
			$BtnStar(summary)$
			$if (summary.periodsMenu)$
			$Menu("menuPeriods", summary.periodsMenu)$
			$endif$
			$BtnQuery(summary.query)$
			<div class="clear">
			</div>
		</div>
		<div class="head2">
			$BtnShare(summary)$
			$if (summary.ungroupMenu)$
			$TabsUngroup(summary.ungroupMenu)$
			$endif$
			$if (summary.fullSortByMenu)$
			$Menu("menuGroups", summary.fullSortByMenu)$
			$endif$
			<div class="clear">
			</div>
		</div>
	</div>
	
	<div class="body">
	
		$if (summary.notice)$
		<div class="notice">
			$summary.notice$
		</div>
		$endif$
		
		$if (summary.isResultList)$
			$ColumnResults(summary)$
		$else$
			$ColumnItems(summary)$
		$endif$

	</div>
</div>

>>