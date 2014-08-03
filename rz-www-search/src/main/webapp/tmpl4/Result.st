Result(it, alwaysOpen) ::= <<

$if (it.isElement)$

<div class="element">
	$it.text$
</div>

$else$

<div class="result">
	
	<div class="source">
		<a target="_blank" href="$it.snap.url$">
			$it.source.name$ &ndash;
			$it.properties.dateAgo$
		</a>
	</div>
	
	<div class="title">
		<a target="_blank" href="$it.snap.url$">
			$it.properties.titleQuote$
		</a>
	</div>
	
	<div class="dateMore">
		<span class="date">
			$it.properties.dateShort$
		</span>
		
		$if (!alwaysOpen)$
			$if (it.properties.textQuote)$
			<div class="moreButton noSelect">
				<span class="more">More</span>
				<span class="less">Less</span>
			</div>
			$endif$
		$endif$
	</div>
	
	$if (it.properties.textQuote)$
	<div class="textQuote $if (alwaysOpen)$visible$endif$">
		$it.properties.textQuote$
	</div>
	$endif$

	<div style="display: none">
		t#$it.snap.titleHash$ 
		u#$it.snap.urlHash$
		id#$it.snap.id$
	</div>
</div>

$endif$

>>