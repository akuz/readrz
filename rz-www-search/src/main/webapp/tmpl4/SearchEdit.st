SearchEdit(id, visible) ::= <<

	<div id="$id$" class="searchEdit" 
		$if (!visible)$
		style="display: none;"
		$endif$
		>
		<input class="searchInput" type="text" name="q" value="$page.summary.query.queryString$" />
		<div class="searchClear">x</div>
		
		<div class="clear">
		</div>
	</div>

>>