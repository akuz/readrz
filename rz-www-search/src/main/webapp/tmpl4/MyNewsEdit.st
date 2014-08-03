MyNewsEdit(page) ::= <<

<div class="dynamicPanel">
$if (page.user)$

	<div class="_actionUrl hidden">$page.actionUrl$</div>

	$if (!page.menu.isEmpty)$
		<div class="starText">
			Move to another tab:
		</div>
		$Menu("menuStar", page.menu)$
		<div class="clear">
		</div>
	$endif$
	
	$if (page.newTabAllowed)$
	
		<div class="starText">
			Move to a new tab:
		</div>
		<input type="text" class="tabName" value="" maxlength="30"/>
		<div class="addButton noSelect">Move</div>
		<div class="clear">
		</div>
		<div class="nameError">
		</div>
		
	$else$
	
		<div class="starText">
			You have the maximum number of tabs allowed,
			you cannot create new ones.
		</div>
		
	$endif$
	
	<div class="leftButton noSelect">
		&#9664;
	</div>
	<div class="rightButton noSelect">
		&#9654;
	</div>
	<div class="deleteButton noSelect">
		Delete Column
	</div>
	
$else$

	$MyNewsLogin()$
	
$endif$
</div>

>>