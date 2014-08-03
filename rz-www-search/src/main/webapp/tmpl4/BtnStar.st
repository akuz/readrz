BtnStar(paths) ::= <<

$if (paths.prepareEditUrl)$
<div class="btnStar noSelect" title="Modify Column">
</div>
$else$
<div class="btnStar noSelect" title="Add to My News">
</div>
$endif$

<div class="starPanel">
$if (paths.prepareEditUrl)$
	<div class="_dynamicAct hidden">edit</div>
	<div class="_dynamicUrl hidden">$paths.prepareEditUrl$</div>
	<div class="starTitle">
		Modify Column
	</div>
$else$
	<div class="_dynamicAct hidden">add</div>
	<div class="_dynamicUrl hidden">$paths.prepareAddUrl$</div>
	<div class="starTitle">
		Add to My News
	</div>
$endif$
	<div class="dynamicPanel">
	</div>
	<div class="closeButton">
		Close 
	</div>
</div>

>>