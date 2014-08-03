MenuItem(it) ::= <<

<div class="menuItem">

	$if (it)$
	
		<a href="$it.url$" rel="nofollow">
		<div class="
			menuButton 
			noSelect 
			$if (it.isActive)$
			active
			$endif$
			$if (it.turnsMenuOn)$
			turnsMenuOn
			$endif$
			">
			<span class="text">
				$it.text$ 
			</span>
			$if (it.count)$
				<span class="count">
					$it.count$
				</span>
			$endif$
		</div>
		</a>
		
		$if (it.children)$
		<div class="menuChildren">
			$MenuItemButtons(it.children)$
		</div>
		$endif$
		
	$else$
	
		<div class="menuButton noSelect $if (it.isActive)$active$endif$">
			<img src="/img/loadingWhite.gif" 
				title="Loading..." alt="Loading..." />
		</div>

	$endif$
	
</div>

>>