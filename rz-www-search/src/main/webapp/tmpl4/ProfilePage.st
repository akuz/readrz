ProfilePage(page) ::= <<
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xmlns:fb="http://ogp.me/ns/fb#">

<head>
	<title>Your Profile at Readrz</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">

	<link rel="stylesheet" type="text/css" href="/css/authPage.css">

	$HeadGoogleAnalytics()$
	
</head>

<body>

	<div class="authPanel">
	
		<div class="head">
			Your Readrz Profile
			<div class="headR">
				<a href="/">
					Home
				</a>
				<a href="/logout" class="logout">
					Log out
				</a>
			</div>
		</div>
	
	$if (page.messages)$
		<div class="messages">
		$page.messages:{it | 
			<div>
				$it$
			</div>
		}$
		</div>
	$endif$
	
	$if (page.errors)$
		<div class="errors">
			<div>
				Please check your input:
			</div>
		$page.errors:{it | 
			<div>
				&bull; $it$
			</div>
		}$
		</div>
	$endif$
		
		<div class="title">
			Your Email
		</div>
		<form method="POST">
		<input type="hidden" class="hidden" name="update" value="email" />
		<div class="parag">
			Please specify your email address to receive notifications 
			and/or for password recovery. We will not share your email.
		</div>
		<div class="parag">
			<input type="text" class="text email" name="email" value="$page.pars.email$" />
			<span>Email (optional)</span>
		</div>
		<div class="parag">
			<input type="submit" value="Update" class="button" />
		</div>
		</form>
		
		<div class="title">
			Change Password
		</div>
		<form method="POST">
		<input type="hidden" class="hidden" name="update" value="password" />
		<div class="parag">
			<input type="password" class="text" name="password" value="$page.pars.password$" />
			<span>New Password</span>
		</div>
		<div class="parag">
			<input type="password" class="text" name="password2" value="$page.pars.password2$" />
			<span>Confirm Password</span>
		</div>
		<div class="parag">
			<input type="submit" value="Update" class="button" />
		</div>
		</form>

		<div class="title">
			Change Username
		</div>
		<form method="POST">
		<input type="hidden" class="hidden" name="update" value="username" />
		<div class="parag">
			Your username publicly identifies your profile, it is used as 
			a part of your web address if you share anything on Readrz.
		</div>
		<div class="parag">
			<input type="text" class="text" name="username" value="$page.pars.username$" />
			<span>Username</span>
		</div>
		<div class="parag">
			Username is not case-sensitive. You can set the default way
			it should be displayed here (e.g. Name, name, nAMe, etc).
		</div>
		<div class="parag">
			You can change your username. However, after one week your old username will
			be available for registration by others. You can still change back to the
			old username during this time.
		</div>
		<div class="parag">
			<input type="submit" value="Update" class="button" />
		</div>
		</form>

		<div class="title">
			Delete Account
		</div>
		<form method="POST">
		<input type="hidden" class="hidden" name="update" value="delete" />
		<div class="parag">
			You can delete your Readrz account permanently.
		</div>
		<div class="parag">
			Your username will be available for others after one week.
		</div>
		<div class="parag">
			<input type="submit" value="Delete" class="button" onclick="return confirm('Delete your Readrz account permanently?');" />
		</div>
		</form>
		
		<div class="title">
			Not $page.user.usernameDisplay$?
		</div>
		<div class="parag">
			Please <a href="/logout?goto=$page.servletPath$">Log out</a>.
		</div>

	</div>
	
</body>
</html>
>>