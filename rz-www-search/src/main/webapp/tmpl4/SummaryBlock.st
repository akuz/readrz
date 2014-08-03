SummaryBlock(it) ::= <<

<div class="summaryBlock">

	$if (it.result)$
	<div class="resultBlock">
		$SummaryResult(it.result)$
	</div>
	$endif$
	
	$if (it.isSummary)$
		<div class="itemMore">
		
			<a class="link" href="$it.fullSearchUrl$">
			<div class="linkBtn">
				$it.leafSearch$
				<span class="count">$it.count$</span>
			</div>
			</a>

			$if (it.children)$
			<div class="expandBtn noSelect">
			</div>
			$endif$
							
			<div class="clear">
			</div>
		</div>

		$if (it.children)$
		<div class="subRows">
		
			$it.children:{ch |
			<div class="subRow">
	
				$if (ch.isSummary)$
				<a href="$ch.fullSearchUrl$">
				<div class="subRowLink">
					<div class="subRowLinkWords">
						$ch.fullSearch$
					</div>
					<div class="subRowLinkCount">
						$ch.count$
					</div>
				</div>
				</a>
				$endif$
				
				$if (ch.isSummary)$
				<div class="subRowHeadFloat">
				$else$
				<div class="subRowHeadNormal">
				$endif$
					<a target="_blank" href="$ch.result.properties.url$">
					<div class="subRowTitle">
						$ch.result.properties.titleQuote$
					</div>
					</a>
					
					<a target="_blank" href="$ch.result.properties.url$">
					<div class="subRowDate">
						<span class="source">$ch.result.source.name$</span> 
						$ch.result.properties.dateAgo$
					</div>
					</a>
				
					<div class="subRowText">
						$if (ch.result.properties.image)$
						
							<a target="_blank" href="$ch.result.properties.url$">
							<img src="/image?id=$ch.result.properties.image$&kind=2" style="float: left; max-height: 100px; max-width: 150px; margin-top: 5px; margin-right: 15px; margin-bottom: 5px; border: 0; border-radius: 3px;"/>
							</a>
							
						$endif$
						$ch.result.properties.textQuote$
					</div>
				</div>

				<div class="clear">
				</div>
			</div>
			}$
			
			<div class="clear">
			</div>
		</div>
		$endif$
		
	$endif$

	<div class="clear">
	</div>
	
</div>

>>