TabPage(page) ::= <<
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xmlns:fb="http://ogp.me/ns/fb#">

<head>
	<title>$page.title$</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	
	$if (page.facebookMeta.title)$
	<meta property="og:title" content="$page.facebookMeta.title$"/>
	<meta property="og:description" content="News column"/>
	$endif$
	
	$if (page.facebookMeta.type)$
	<meta property="og:type" content="$page.facebookMeta.type$"/>
	$endif$

	$if (page.facebookMeta.url)$
	<meta property="og:url" content="$page.facebookMeta.url$"/>
	$endif$

	<meta property="og:site_name" content="Readrz"/>
	<meta property="fb:app_id" content="$FacebookAppId()$"/>
	<meta property="og:image" content="http://www.readrz.com/img/rz_400_4.png"/>
	<meta property="og:description" content="Readrz organizes news into tabs personalized for you."/>
	
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
	<!-- link rel="stylesheet" type="text/css" href="/css/ui-flick/jquery-ui-1.10.3.custom.min.css" /-->
	<script type="text/javascript" src="/js/json2.js"></script>
	<script type="text/javascript" src="/js/jquery/jquery-1.9.1.min.js"></script>
	<!-- script type="text/javascript" src="/js/jquery/jquery-ui-1.10.3.custom.min.js"></script-->
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
	
	<div id="columnsContainer">
		<div id="columnsContainerInner">

			$page.summaries:Column()$

		</div>
	</div>
	
</body>
</html>
>>