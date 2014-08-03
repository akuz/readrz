JsonMenuItem(it) ::= <<
{
	name: "$it.text$",
	count: $it.count$,
	size: $it.size$
	$if (it.children)$
	,
	children: [
		$it.children:JsonMenuItem(); separator=", "$
	]
	$endif$
}
>>