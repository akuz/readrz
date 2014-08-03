function ensureSocial(d, s, id, url) {
  var js, fjs = d.getElementsByTagName(s)[0];
  if (d.getElementById(id)) return true;
  js = d.createElement(s); js.id = id;
  js.src = url;
  fjs.parentNode.insertBefore(js, fjs);
  return false;
}

function ensureFacebook() {
  return ensureSocial(document, 'script', 'facebook-jssdk', '//connect.facebook.net/en_GB/all.js#xfbml=1&appId=103134133113111');  
}

function ensureTwitter() {
  return ensureSocial(document, 'script', 'twitter-wjs', 'https://platform.twitter.com/widgets.js');  
}

function ensureLinkedIn() {
  return ensureSocial(document, 'script', 'linkedin-jssdk', 'http://platform.linkedin.com/in.js');  
}
