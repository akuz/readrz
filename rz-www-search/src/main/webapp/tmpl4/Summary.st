Summary(summary) ::= <<

<div id="_topicGroupId"   class="hidden">$summary.topicGroupEncodedId$</div>
<div id="_patternGroupId" class="hidden">$summary.patternGroupEncodedId$</div>

<div class="summary2">
<div class="commonMargin">

	<div class="summaryContent">
	
		$if (summary.isLoading)$
		
			<script>
				reloadAfterGettingTrue("$summary.checkUrl$");
			</script>
			<div class="summaryLoading">
				Summarizing, please wait... 
			</div>
	
		$else$
			$if (summary.isResultList)$
				$SummaryResults(summary)$
			$else$
				
				
				
				
				<!--div id="summaryContents">
				</div-->

				<div class="summaryL">

					$summary.rootItem.children:SummaryBlock()$
	
					$summary.evenItems___:SummaryBlock()$

					<div class="summaryBlock">
					<div class="resultBlock">
					<div class="total">
					$if (summary.rootItem.count)$
						Total news: <span>$summary.rootItem.count$</span>
					$else$
						There are no news for selected parameters.
					$endif$
					</div>
					</div>
					</div>
	
					<div class="clear">
					</div>
				</div>
	
				<div class="summaryR">
					
					$summary.oddItems___:SummaryBlock()$
					
					<div class="clear">
					</div>
				</div>
	
			$endif$
				
		$endif$

		<div class="clear">
		</div>
	</div>

</div>
</div>

>>