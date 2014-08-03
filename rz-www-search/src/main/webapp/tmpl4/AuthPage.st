AuthPage(page) ::= <<
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" xmlns:fb="http://ogp.me/ns/fb#">

<head>
	<title>Log in to Readrz</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">

	<link rel="stylesheet" type="text/css" href="/css/authPage.css">

	$HeadGoogleAnalytics()$
	
</head>

<body>

	<div class="authPanel">
	
		<div class="head">
			Secure Access to Readrz
			<div class="headR">
				<a href="/">
					Home
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

	$if (page.user)$
	
		<div class="title">
			Active Profile
		</div>
		<div class="parag">
			You are currently logged in as <a href="/profile">$page.user.usernameDisplay$</a> (<a href="/logout?goto=$page.servletPath$">log out</a>).
		</div>
		<div class="title">
			Log in as Another User?
		</div>
		<div class="parag">
			Not $page.user.usernameDisplay$? Please <a href="/logout?goto=$page.servletPath$">Log out</a> first.
		</div>
		
	$elseif (page.isLogin)$
		
		<div class="title">
			Existing Users
		</div>
		$if (page.user)$
			<div class="parag">
				You have customized Readrz for yourself while not logged in!
			</div>
			<div class="parag">
				But don't worry, once you log in you will be able to merge your 
				current configuration with your account, if you want to.
			</div>
		$endif$
		<form action="/login" method="POST">
		<div class="parag">
			<input type="text" class="text" name="username" value="$page.pars.username$" />
			<span>Username</span>
		</div>
		<div class="parag">
			<input type="password" class="text" name="password" value="$page.pars.password$" />
			<span>Password</span>
		</div>
		<div class="parag">
			 <a href="/recover">Forgot your details?</a>
		</div>
		<div class="parag">
			<input type="submit" value="Log in" class="button" />
		</div>
		</form>
		<div class="title">
			Don't Have an Account?
		</div>
		<div class="parag">
			New users, please <a href="/register">Register here</a>.
		</div>
	
	$elseif (page.isRecover)$
		
		$if (page.code)$

		<div class="title">
			Reset Your Password
		</div>
		<div class="parag">
			Please confirm your username:
		</div>
		<form action="/recover" method="POST">
		<input type="hidden" class="hidden" name="recover" value="password" />
		<input type="hidden" class="hidden" name="code" value="$page.code$" />
		<div class="parag">
			<input type="text" class="text" name="username" value="$page.pars.username$" />
			<span>Username</span>
		</div>
		<div class="parag">
			<input type="password" class="text" name="password" value="$page.pars.password$" />
			<span>New Password</span>
		</div>
		<div class="parag">
			<input type="password" class="text" name="password2" value="$page.pars.password2$" />
			<span>Confirm New Password</span>
		</div>
		<div class="parag">
			Finally, please prove you are a not a robot:
		</div>
		<div class="parag captcha">
			$ReCaptcha()$
		</div>
		<div class="parag">
			<input type="submit" value="Reset" class="button" />
		</div>
		</form>
		<div class="title">
			Remember Your Details?
		</div>
		<div class="parag">
			Existing users, please <a href="/login">Log in here</a>.
		</div>
		<div class="parag">
			New users, please <a href="/register">Register here</a>.
		</div>
		
		$else$
		<div class="title">
			Forgot Your Password?
		</div>
		<div class="parag">
			We will send you an email with a password reset link.
		</div>
		<form action="/recover" method="POST">
		<input type="hidden" class="hidden" name="recover" value="password" />
		<div class="parag">
			<input type="text" class="text" name="username" value="$page.pars.username$" />
			<span>Username</span>
		</div>
		<div class="parag">
			<input type="submit" value="Recover" class="button" />
		</div>
		</form>
		
		<div class="title">
			Forgot Your Username?
		</div>
		<div class="parag">
			We will send you an email with your username.
		</div>
		<form action="/recover" method="POST">
		<input type="hidden" class="hidden" name="recover" value="username" />
		<div class="parag">
			<input type="text" class="text email" name="email" value="$page.pars.email$" />
			<span>Email</span>
		</div>
		<div class="parag">
			<input type="submit" value="Recover" class="button" />
		</div>
		</form>

		<div class="title">
			Remember Your Details?
		</div>
		<div class="parag">
			Existing users, please <a href="/login">Log in here</a>.
		</div>
		<div class="parag">
			New users, please <a href="/register">Register here</a>.
		</div>
		$endif$
		
	$elseif (page.isRegister)$
		
		<div class="title">
			New User Registration
		</div>
		<form action="/register" method="POST">
		<div class="parag">
			Your username will publicly identify your profile, it will be
			used as a part of web address if you share anything on Readrz.
		</div>
		<div class="parag">
			<input type="text" class="text" name="username" value="$page.pars.username$" />
			<span>Username</span>
		</div>
		<div class="parag">
			Please specify your email address to receive notifications 
			and/or for password recovery. We will not share your email.
		</div>
		<div class="parag">
			<input type="text" class="text email" name="email" value="$page.pars.email$" />
			<span>Email (optional)</span>
		</div>
		<div class="parag">
			<input type="password" class="text" name="password" value="$page.pars.password$" />
			<span>Password</span>
		</div>
		<div class="parag">
			<input type="password" class="text" name="password2" value="$page.pars.password2$" />
			<span>Confirm Password</span>
		</div>
		<div class="parag">
			Finally, please prove you are a not a robot:
		</div>
		<div class="parag captcha">
			$ReCaptcha()$
		</div>
		<div class="parag">
			<input type="submit" value="Register" class="button" />
		</div>
		</form>
		<div class="title">
			Already Have an Account?
		</div>
		<div class="parag">
			Existing users, please <a href="/login">Log in here</a>.
		</div>
		
	$endif$

	</div> 
	
	
</body>
</html>
>>