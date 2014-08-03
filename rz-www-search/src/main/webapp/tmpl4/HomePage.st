HomePage(page) ::= <<
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xmlns:fb="http://ogp.me/ns/fb#">

<head>

	$AnyPageHead()$
	
	<link rel="stylesheet" type="text/css" href="/css/all.css">
	<link rel="stylesheet" type="text/css" href="/css/frontPanel.css">
	<link rel="stylesheet" type="text/css" href="/css/dialogs.css">
	<link rel="stylesheet" type="text/css" href="/css/tabsList.css">
	<link rel="stylesheet" type="text/css" href="/css/topBar.css">
	<link rel="stylesheet" type="text/css" href="/css/menu.css">
	<link rel="stylesheet" type="text/css" href="/css/menuStar.css">
	<link rel="stylesheet" type="text/css" href="/css/menuGroups.css">
	<link rel="stylesheet" type="text/css" href="/css/menuPeriods.css">
	<link rel="stylesheet" type="text/css" href="/css/menuPeriodsTop.css">
	<link rel="stylesheet" type="text/css" href="/css/tabsUngroup.css">
	<link rel="stylesheet" type="text/css" href="/css/tabsLevel2.css">
	<link rel="stylesheet" type="text/css" href="/css/column.css">
	<link rel="stylesheet" type="text/css" href="/css/columnItems.css">
	<link rel="stylesheet" type="text/css" href="/css/columnResults.css">
	<link rel="stylesheet" type="text/css" href="/css/result.css">
	<link rel="stylesheet" type="text/css" href="/css/myNewsInfo.css">
	<link rel="stylesheet" type="text/css" href="/css/btnQuery.css">
	<link rel="stylesheet" type="text/css" href="/css/btnShare.css">
	<link rel="stylesheet" type="text/css" href="/css/btnStar.css">
	<link rel="stylesheet" type="text/css" href="/css/intro.css">
	<link rel="stylesheet" type="text/css" href="/css/colorbox/example3/colorbox.css" />
	<script type="text/javascript" src="/js/json2.js"></script>
	<script type="text/javascript" src="/js/jquery/jquery-1.9.1.min.js"></script>
	<script type="text/javascript" src="/js/jquery/jquery.mousewheel.js"></script>
	<script type="text/javascript" src="/js/jquery/jquery.colorbox.js"></script>
	<script type="text/javascript" src="/js/jquery/jquery.cookie.js"></script>
	<script type="text/javascript" src="/js/utils.js"></script>
	<script type="text/javascript" src="/js/dialogs.js"></script>
	<script type="text/javascript" src="/js/tabs.js"></script>
	<script type="text/javascript" src="/js/tabsList.js"></script>
	<script type="text/javascript" src="/js/menus.js"></script>
	<script type="text/javascript" src="/js/morebtns.js"></script>
	<script type="text/javascript" src="/js/columns.js"></script>
	<script type="text/javascript" src="/js/popups.js"></script>
	<script type="text/javascript" src="/js/search.js"></script>
	<script type="text/javascript" src="/js/init.js"></script>

	$HeadGoogleAnalytics()$

	<script>
	\$(function() {
		init();
	});
	</script>
	
</head>

<body>

	$HeadFacebookInit()$
	$HeadTwitterInit()$	
	$TopBar()$

	$if (page.templateSummary)$
	<div id="topPeriodAbbr" class="hidden">$page.topPeriod.abbr$</div>
	<div id="templateColumn" class="hidden">
		$Column(page.templateSummary)$
	</div>
	$endif$

	<div class="tabsLevel2">
		<div class="tabsLevel2Inner">
		$page.tabs:TabLevel2()$
		<div class="clear">
		</div>
		</div>
		<div class="clear">
		</div>
		<div class="tabsLevel2Bottom">
		</div>
	</div>

	<div id="columnsContainer">
		<div id="columnsContainerInner">

			$page.summaries:Column()$

		</div>
	</div>

</body>
</html>
>>