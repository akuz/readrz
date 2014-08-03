TopBar() ::= <<

$if (!page.isTab && !page.isOverlay)$
<div id="topBar">
<div class="commonMargin">

	<div class="scrolltop" title="Scroll to top">
	</div>

	<a href="/" target="_top" title="Readrz">
	<div class="readrz">
		readrz
	</div>
	</a>

	<!--
	<div class="period">
		$Menu("menuPeriodsTop", page.periodsMenu, "fa-clock-o")$
	</div>
	-->

	<!--
	<div class="links linksLeft">
		<a class="noSelect popupLink" href="/page/about" title="About Readrz">About</a>
		<a class="noSelect" href="http://blog.readrz.com" target="_new" title="Blog">Blog</a>
		<a class="noSelect popupLink" href="/page/share" title="Share Readrz">Share</a>
		<a class="noSelect popupLink" href="/page/terms" title="Terms of Use">Terms</a>
	</div>

	<div class="links linksRight">
		<span>First 1,000 subscribers get personalization on day one.</span>
		<a class="noSelect" href="http://eepurl.com/d48hj" target="_new" title="Subscribe">
			Subscribe NOW
		</a>
	</div>
	-->

	$if (page._________)$
	<div class="profile">
		$if (page.user)$
			<a class="name" href="/profile">$page.user.usernameDisplay$</a>
			<a class="logout" href="/logout">Log out</a>
		$else$
			<a class="login" href="/login">Log in</a>
			<a class="logout" href="/register">Register</a>
		$endif$
	</div>
	$endif$

	<div class="clear">
	</div>
</div>
</div>
$endif$

>>