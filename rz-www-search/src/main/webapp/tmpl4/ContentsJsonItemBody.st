ContentsJsonItemBody(it) ::= <<

	level: $it.level$,
	name: "$it.leafSearch$",
	count: $it.count$
	$if (it.result.properties.image)$
	, image: "$it.result.properties.image$"
	$endif$

>>