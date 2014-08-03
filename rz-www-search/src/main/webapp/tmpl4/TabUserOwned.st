TabUserOwned(it) ::= <<

$if (it.tab)$
<div class="item exist">

	<div class="isPublic hidden">$it.tab.isPublic$</div>
	<div class="actionUrl hidden">/action?action=editTab&tabid=$it.tab.id$</div>
	
	<a href="/tab?tabid=$it.tab.id$" title="$it.tab.name$" class="openTabLink">
	<div class="name noSelect">
	$it.tab.name$
	</div>
	</a>
	
	<div class="option noSelect rename">
	Rename
	</div>
	
	<div class="option noSelect moveUp">
	&#9650;
	</div>
	
	<div class="option noSelect moveDown">
	&#9660;
	</div>
	
	<div class="option noSelect privacy
		$if (it.tab.isPublic)$
		isPublic
		$else$
		isPrivate
		$endif$
		">
	$if (it.tab.isPublic)$
	Public
	$else$
	Private
	$endif$
	</div>
	
	<div class="option noSelect delete">
	Delete
	</div>
	
	<!--div class="text count noSelect">
	$it.tab.followersCount$
	</div>

	<div class="text noSelect">
	followers
	</div-->

	<div class="clear">
	</div>
</div>
$else$
<div class="item add">

	<div class="actionUrl hidden">/action?action=addTab</div>
	
	<div class="option noSelect add">
		Add Tab
	</div>
	
	<div class="clear">
	</div>
</div>
$endif$
>>