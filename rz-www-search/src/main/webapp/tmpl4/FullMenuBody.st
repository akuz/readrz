FullMenuBody(class, menu) ::= <<

<div class="$class$ $menu.tags:{tag | $tag$}; separator=" "$">
	$menu.rootItem.children:FullMenuItem()$
</div>

>>