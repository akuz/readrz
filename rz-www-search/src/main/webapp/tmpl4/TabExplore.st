TabExplore(it) ::= <<

<div class="box">
	<a href="/tab?tabid=$it.tab.id$" title="$it.tab.name$" class="openTabLink">
	<div class="head">
		$it.tab.name$
	</div>
	<div class="keys">
		$it.columns:{it2 |
		<div class="key">
			$it2.query.queryDisplay$
			$if (it2.group)$
			(<em>$it2.group.name$</em>)
			$endif$
		</div>}$
	</div>
	</a>
	
	<!--div class="text count noSelect">
	$it.tab.followersCount$
	</div>

	<div class="text noSelect">
	followers,
	</div>

	<div class="text noSelect">
	by
	</div>

	<a href="/explore?user=$it.user.usernameSystem$">
	<div class="text user noSelect">
	$it.user.usernameDisplay$
	</div>
	</a-->
	
	<div class="clear">
	</div>

</div>

>>