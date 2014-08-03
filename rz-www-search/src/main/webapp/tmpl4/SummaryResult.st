SummaryResult(it) ::= <<

$if (it.isElement)$

<div class="summaryElement">
	$it.text$
</div>

$else$

<div class="summaryResult">
	
	<div class="head">
		<div class="resultTitle">
			<a target="_blank" href="$it.properties.url$">
				$it.properties.titleQuote$
			</a>
		</div>
	
		$if (it.properties.image)$
		
			<a target="_blank" href="$it.properties.url$">
			<img src="/image?id=$it.properties.image$&kind=1" style="max-height: 266px; max-width: 400px; margin-top: 15px; border: 0; border-radius: 5px;"/>
			</a>
			
		$endif$

		<a target="_blank" href="$it.properties.url$">
		<div class="resultDate">
				<span class="source">$it.source.name$</span>
				$it.properties.dateAgo$
				<!--,
			$it.properties.dateShort$-->
		</div>
		</a>
	</div>
	
	$if (it.properties.textQuote)$
	<div class="quote">
		$it.properties.textQuote$
	</div>
	$endif$
	
	<div class="hidden">
		$it.snap.id$
	</div>
	
	<div class="clear">
	</div>

</div>

$endif$

>>