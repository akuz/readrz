MyNewsAdd(page) ::= <<

<div class="dynamicPanel">
$if (page.user)$

	<div class="_actionUrl hidden">$page.actionUrl$</div>

	$if (page.menu.isEmpty)$
		<div class="starText">
			You have no personalized tabs yet. 
			Please enter the name of your first tab below.
		</div>
	$else$
		<div class="starText">
			Add to an existing tab:
		</div>
		$Menu("menuStar", page.menu)$
		<div class="clear">
		</div>
	$endif$
	
	$if (page.newTabAllowed)$
	
		<div class="starText">
			Add to a new tab:
		</div>
		<input type="text" class="tabName" value="$if (page.menu.isEmpty)$My News$endif$" maxlength="30"/>
		<div class="addButton noSelect">Add</div>
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
	
$else$

	$MyNewsLogin()$
	
$endif$
</div>

>>