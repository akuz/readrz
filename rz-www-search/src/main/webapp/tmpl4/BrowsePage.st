BrowsePage(page) ::= <<
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:fb="http://ogp.me/ns/fb#">
<head>

	$AnyPageHead()$
	
	<link rel="stylesheet" type="text/css" href="/css/all.css">
	<link rel="stylesheet" type="text/css" href="/css/top.css">
	<link rel="stylesheet" type="text/css" href="/css/topBar.css">
	<link rel="stylesheet" type="text/css" href="/css/topFix.css">
	<link rel="stylesheet" type="text/css" href="/css/bottom.css">

	<link rel="stylesheet" type="text/css" href="/css/browse.all.css">
	<link rel="stylesheet" type="text/css" href="/css/browse.btn.css">
	<link rel="stylesheet" type="text/css" href="/css/browse.list.css">
	<link rel="stylesheet" type="text/css" href="/css/browse.menu.css">
	<link rel="stylesheet" type="text/css" href="/css/browse.pop.css">
	<link rel="stylesheet" type="text/css" href="/css/browse.quote.css">
	<link rel="stylesheet" type="text/css" href="/css/browse.search.css">
	<link rel="stylesheet" type="text/css" href="/css/browse.share.css">
	<link rel="stylesheet" type="text/css" href="/css/browse.zzz.css">

	<link rel="stylesheet" type="text/css" href="/css/colorbox/example3/colorbox.css" />
	<link rel="stylesheet" type="text/css" href="/css/font-awesome-4.0.3/css/font-awesome.min.css" />
	<link href='http://fonts.googleapis.com/css?family=Open+Sans:300,400,600,700|Open+Sans+Condensed:300,700' rel='stylesheet' type='text/css'>
	<script type="text/javascript" src="/js/json2.js"></script>
	<script type="text/javascript" src="/js/d3/d3.v3.min.js"></script>
	<script type="text/javascript" src="/js/jquery/jquery-1.9.1.min.js"></script>
	<script type="text/javascript" src="/js/jquery/jquery.colorbox.js"></script>
	<script type="text/javascript" src="/js/jquery/jquery.cookie.js"></script>
	<script type="text/javascript" src="/js/utils/utils.js"></script>
	<script type="text/javascript" src="/js/utils/Queue.compressed.js"></script>
	<script type="text/javascript" src="/js/getJSON2.js"></script>
	<script type="text/javascript" src="/js/social.js"></script>
	<script type="text/javascript" src="/js/browse.js"></script>
	<script type="text/javascript" src="/js/browse.pop.js"></script>
	<script type="text/javascript" src="/js/browse.quote.js"></script>
	<script type="text/javascript" src="/js/search.js"></script>
	<script type="text/javascript" src="/js/popups.js"></script>
	<script type="text/javascript" src="/js/d3_utils.js"></script>
	<script type="text/javascript" src="/js/d3_menu.js"></script>

	$HeadGoogleAnalytics()$

	<script>
	\$(function() {
	
		browseInit();
		initPopups();
		
		var dataPanel = browse.rootPanel.append("div").attr("class", "dataPanel");
		browseLoadSummary(false, "$page.loadUrl$", dataPanel);
		
		var rootQuote = d3.select("div#rootQuote");
		if (rootQuote.size() > 0) {
			var quote = rootQuote.select("div.quote");
			var quoteBtns = quote.select("div.quoteBtns");
			quoteBtns.select("div.btnClose")
				.on("click", function() {
					d3.transition().duration(400).each(function() {
					
						quote.transition()
							.style("opacity", 0);
							
						rootQuote.transition()
							.style("padding", "0px")
							.style("height", "0px")
							.remove();
					});
				});
			var quoteLinks = quote.select("div.links");
			quoteLinks.select("div.link.share")
				.on("click", function() {
				
					var elem = d3.select(this);
					var url = "$page.quoteUrl$";

					onShareUrlText(elem, url, \$(quote[0][0]).find("div.title").text().trim(), false, -110, -50, null);

					ga('send', 'pageview', url + "/share");
				});
		}
	})
	</script>
	
</head>

<body>

	<div id="fb-root"></div>

	<div id="top">
		$TopBar()$
	</div>
	
	$if (page.quote)$
	<div id="rootQuote">
	<div class="commonMargin">
	
		<div class="quote">
			<div class="quoteBtns">
				<div class="btn btnClose">
					<i class="fa fa-times-circle"></i>
				</div>
			</div>
			<div class="title">
				<a href="$page.quote.url$" target="_blank">
				$page.quote.titleQuote$
				</a>
			</div>
			<div class="info">
				<a href="$page.quote.url$" target="_blank">
				<span class="source">
					$page.quote.source$
				</span>
				<span class="dateAgo">
					$page.quote.dateAgo$
				</span>
				</a>
			</div>
			<div class="textArea">
				$if (page.quote.image)$
				<a href="$page.quote.url$" target="_blank" class="image">
				<img src="/image?id=$page.quote.image$&kind=2" />
				</a>
				$endif$
				<span>$page.quote.textQuote$</span>
				<div class="clear">
				</div>
			</div>
			<div class="links">
				<a href="$page.quote.url$" target="_blank">
				<div class="link">
					<i class="fa fa-external-link-square"></i>
					<span>Read Full Article</span>
				</div>
				</a>
				<div class="link share">
					<i class="fa fa-share-square"></i>
					<span>Share</span>
				</div>
				<div class="clear">
				</div>
			</div>
		</div>
	
	</div>
	</div>
	$endif$

	<div class="commonMargin">
	<div id="rootPanel">
		
	</div>
	</div>

	<div class="commonMargin">
	<div id="bottom">
	
		<div class="links left">
			<a class="noSelect popupLink" href="/page/about" title="About Readrz">About</a>
			<a class="noSelect" href="http://blog.readrz.com" target="_new" title="Blog">Blog</a>
			<a class="noSelect" href="http://eepurl.com/d48hj" target="_new" title="Subscribe">Subscribe</a>
			<a class="noSelect popupLink" href="/page/share" title="Share Readrz">Share</a>
		</div>
	
		<div class="links right">
			<a class="noSelect popupLink" href="/page/terms" title="Terms of Use">&copy; 2013</a>
			<a class="noSelect popupLink" href="/page/terms" title="Terms of Use">Terms</a>
		</div>
		
		<div class="clear">
		</div>
		
	</div>
	</div>
	
</body>
</html>
>>