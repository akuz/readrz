FullMenuItem(it) ::= <<

<div class="item $it.tags:{tag | $tag$}; separator=" "$">
	
	<a href="$it.url$">
	<div class="head">
	
		<div class="bullet">
		</div>

		<div class="text">
			$it.text$
		</div>

		$if (it.count)$
		<div class="count">
			$it.count$
		</div>
		$endif$

		<div class="clear">
		</div>
	</div>
	</a>
	
	$if (it.children)$
	<div class="children">
		
		$it.children:FullMenuItem()$

	</div>
	$endif$
</div>

>>