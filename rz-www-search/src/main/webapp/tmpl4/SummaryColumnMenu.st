SummaryColumnMenu(menu) ::= <<

	<div class="section">
		$menu.rootItem.children:{it |
			<a href="$it.url$">
			<div class="
				sectionTitle
				$if (it.isActive)$
				isActive
				$endif$
				$if (it.isChildActive___)$
				isChildActive
				$endif$
				">
				$it.text$
			</div>
			</a>
			$it.children:SummaryColumnMenuItem()$
		}$
	</div>
	
>>
