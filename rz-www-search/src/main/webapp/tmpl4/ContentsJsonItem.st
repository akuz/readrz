ContentsJsonItem(it) ::= <<
{
	$ContentsJsonItemBody(it)$
	
	$if (it.isLevel0)$
	, children: [
		$it.summaryChildren:ContentsJsonItem(); separator=", "$
	]
	$elseif (it.isLevel1)$
	, children: [
		{
			$ContentsJsonItemBody(it)$
		},
		$it.summaryChildren:ContentsJsonItem(); separator=", "$
	]
	$endif$
}
>>