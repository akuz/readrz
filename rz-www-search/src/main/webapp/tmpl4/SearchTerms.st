SearchTerms(id, visible) ::= <<

	<div id="$id$" class="searchTerms"
		$if (!visible)$
		style="display: none;"
		$endif$
		>

		$page.queryMenu.rootItem.children:{it |
		<div class="searchTermBlock">
			$if (it.text)$
				<a href="$it.url$">
				<div class="searchTerm">
					$it.text$
				</div>
				</a>
			$endif$

			$it.children:{it2 |
				<a href="$it2.url$">
				<div class="searchMore">
					$it2.text$
				</div>
				</a>
			}$
		</div>
		}$
		
		<div class="searchTermBlock">
			<div class="searchMore searchEditButton">
				Edit
			</div>
		</div>

		<div class="clear">
		</div>

	</div>

>>