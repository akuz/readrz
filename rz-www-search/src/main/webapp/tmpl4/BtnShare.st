BtnShare(paths) ::= <<

<div class="btnShare noSelect" title="Share the column">
</div>
<div class="sharePanel">

	<div class="shareTitle">
		Share this Column
	</div>

	<div class="_twitterSection shareSection">
		<a href="https://twitter.com/share" class="twitter-share-button" data-url="$paths.shareUrl$" data-text="" data-via="readrz" data-dnt="true">Tweet</a>
	</div>

	<div class="_facebookSection shareSection">
		<fb:like href="$paths.shareUrl$" send="true" layout="button_count" width="250" show_faces="false" font="verdana" action="recommend"></fb:like>
	</div>

	<div class="shareSection">
		<input class="shareLink" onClick="this.select();" value="$paths.shareUrl$">
	</div>
	
	<div class="closeButton">
		Close 
	</div>
</div>

>>