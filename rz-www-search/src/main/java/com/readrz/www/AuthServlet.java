package com.readrz.www;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.akuz.core.SystemUtils;
import me.akuz.core.crypt.bcrypt.BCrypt;
import me.akuz.core.http.HttpUtils;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.stringtemplate.v4.ST;

import com.readrz.data.user.User;
import com.readrz.data.user.UserCodeDA;
import com.readrz.www.facades.FacadeTemplates;
import com.readrz.www.facades.FacadeUser;
import com.readrz.www.facades.FacadeUserCode;
import com.readrz.www.facades.FacadeUserOldName;
import com.readrz.www.facades.FacadeUserSession;
import com.readrz.www.facades.Session;
import com.readrz.www.pages.AuthPage;

public final class AuthServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private Logger _log;

	@Override
	public void init() throws ServletException {
		super.init();
		_log = Log.getLogger(AuthServlet.class);
	}
	
	@Override
	public void destroy() {
		super.destroy();
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		// ensure security on non-localhost
		if (SystemUtils.isLocalhost() == false) {
			SchemeUtils.ensureScheme_orThrow(SchemeUtils.https, req, resp);
		}

		String servletPath = req.getServletPath();

		// create page
		AuthPage page = new AuthPage(req, resp);
		page.setCode(req.getParameter("code"));
		
		// set current user
		String sessionId = Session.getSessionId(req, resp);
		User user = FacadeUserSession.get().findUserBySession(sessionId);
		page.setUser(user);

		// check not logged in
		if (user == null) {
			
			if (ProfileConst.servletPathLogin.equals(servletPath)) {
				
				String usernameDisplay = req.getParameter("username");
				if (usernameDisplay == null || ProfileConst.usernamePattern.matcher(usernameDisplay).matches() == false) {
					page.addError(ProfileConst.usernamePatternMessage);
				}
				
				String password = req.getParameter("password");
				if (password == null || ProfileConst.passwordPattern.matcher(password).matches() == false) {
					page.addError(ProfileConst.passwordPatternMessage);
				}
				
				if (page.hasErrors() == false) {
					
					String usernameSystem = User.toUsernameSystem(usernameDisplay);
					User candidateUser = FacadeUser.get().findByUsernameSystem(usernameSystem);
					
					if (candidateUser != null) {
						if (BCrypt.checkpw(password, candidateUser.getPasswordHash())) {
							
							// reset session id
							FacadeUserSession.get().expireSession(sessionId);
							sessionId = Session.resetSessionId(req, resp);
							
							// password matches
							FacadeUserSession.get().linkSessionToUser(sessionId, candidateUser.getId());
							
							// redirect to root
							resp.sendRedirect(ProfileConst.servletPathRoot);
							return;
						}
					}
					
					// no user or wrong password
					page.addError("Username or password is not recognized");

				}
				
			} else if (ProfileConst.servletPathRecover.equals(servletPath)) {
				
				String recover = req.getParameter("recover");
				
				if ("password".equals(recover)) {
					
					// check if reset code already specified
					String code = req.getParameter("code");
					
					if (code == null) { // no code yet
						
						// check username
						String usernameDisplay = req.getParameter("username");
						if (usernameDisplay != null) {
							if (ProfileConst.usernamePattern.matcher(usernameDisplay).matches() == false) {
								page.addError(ProfileConst.usernamePatternMessage);
							}		
						} else {
							page.addError("Please enter a username");
						}
						
						// try find user & check email
						User candidateUser = null;
						if (page.hasErrors() == false) {
							String usernameSystem = User.toUsernameSystem(usernameDisplay);
							candidateUser = FacadeUser.get().findByUsernameSystem(usernameSystem);
							if (candidateUser != null) {
								String candidateEmail = candidateUser.getEmail();
								if (candidateEmail == null || candidateEmail.isEmpty()) {
									page.addError("This user haven't specified their email");
								}
							} else {
								page.addError("Username not found");
							}
						}
						
						// save code and send email
						if (page.hasErrors() == false) {
							
							String newCode = UUID.randomUUID().toString();
							FacadeUserCode.get().saveCode(candidateUser.getId(), newCode);
							
							// prepare the url to send
							final String requestUrl = req.getRequestURL().toString();
							final String sendUrl;
							if (SystemUtils.isLocalhost()) {
								sendUrl = requestUrl;
							} else {
								sendUrl = SchemeUtils.updateScheme(SchemeUtils.https, requestUrl);
							}

							StringBuilder sb = new StringBuilder();
							sb.append("Please open this link to reset your password: ");
							sb.append("\n");
							sb.append("\n");
							sb.append(sendUrl);
							sb.append("?code=");
							sb.append(newCode);
							sb.append("\n");
							sb.append("\n");
							sb.append("If you haven't requested this email, please ignore it.");
							sb.append("\n");
							sb.append("\n");
							sb.append("This link will expire in " + UserCodeDA.EXPIRY_MINS + " minutes.");
							sb.append("\n");
							sb.append("\n");
							sb.append("Yours, Readrz Team");
							sb.append("\n");
							sb.append("www.readrz.com");
							String text = sb.toString();
							
							try {
							
								AWSEmail.sendOnePlain(candidateUser.getEmail(), "Password Reset", text);
								page.addMessage("Password reset email sent, please check your email.");

							} catch (Exception e) {
								_log.warn("Could not send email", e);
								page.addError("Error sending email, sorry");
							}
						}
					
					} else { // code is specified
						
						// check username
						String usernameDisplay = req.getParameter("username");
						if (usernameDisplay != null) {
							if (ProfileConst.usernamePattern.matcher(usernameDisplay).matches() == false) {
								page.addError(ProfileConst.usernamePatternMessage);
							}		
						} else {
							page.addError("Please enter a username");
						}
						
						// check passwords
						String password = req.getParameter("password");
						String password2 = req.getParameter("password2");
						if (password == null || ProfileConst.passwordPattern.matcher(password).matches() == false ||
							password2 == null || ProfileConst.passwordPattern.matcher(password2).matches() == false) {
							page.addError(ProfileConst.passwordPatternMessage);
						
						} else {
							if (password.equals(password2) == false) {
								page.addError("Password and its confirmation don't match");
							}
						}

						// try find user
						User candidateUser = null;
						if (page.hasErrors() == false) {
							String usernameSystem = User.toUsernameSystem(usernameDisplay);
							candidateUser = FacadeUser.get().findByUsernameSystem(usernameSystem);
							if (candidateUser == null) {
								page.addError("Username not found");
							}
						}
						
						// validate captcha, not localhost
						if (page.hasErrors() == false) {
							if (checkCaptchaValid(req) == false) {
								page.addError("Unable to verify you are not a robot, please try again");
							}
						}
						
						// check the code
						if (page.hasErrors() == false) {
							if (false == FacadeUserCode.get().checkCode(candidateUser.getId(), code)) {
								page.addError("Your password reset code is invalid or has expired");
							}
						}
						
						// reset the password
						if (page.hasErrors() == false) {
							
							String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
							candidateUser.setPasswordHash(passwordHash);
							
							FacadeUser.get().saveUser(candidateUser.getDbo());
							FacadeUserCode.get().removeCode(candidateUser.getId());
							
							page.addMessage("Your password has been reset successfully!");
							page.addMessage("Please now <a href=\"" + ProfileConst.servletPathLogin + "\">Log in</a> using your new password.");
						}
						
					}
					
				} else if ("username".equals(recover)) {
					
					String rawEmail = req.getParameter("email");
					String validEmail = null;
					if (rawEmail != null) {
						String trimmedEmail = rawEmail.trim();
						if (ProfileConst.emailPattern.matcher(trimmedEmail).matches() == false) {
							page.addError(ProfileConst.emailPatternMessage);
						} else {
							validEmail = trimmedEmail;
						}
					} else {
						page.addError("Please enter an email");
					}
					if (validEmail != null) {
						
						User candidateUser = FacadeUser.get().findByEmail(validEmail);
						if (candidateUser != null) {
							
							StringBuilder sb = new StringBuilder();
							sb.append("Your username is: ");
							sb.append(candidateUser.getUsernameDisplay());
							sb.append("\n");
							sb.append("\n");
							sb.append("If you haven't requested this email, please ignore it.");
							sb.append("\n");
							sb.append("\n");
							sb.append("Yours, Readrz Team");
							sb.append("\n");
							sb.append("www.readrz.com");
							String text = sb.toString();
							
							try {
							
								AWSEmail.sendOnePlain(validEmail, "Username Reminder", text);
								page.addMessage("Username reminder sent, please check your email.");
								page.addMessage("Please <a href=\"" + ProfileConst.servletPathLogin + "\">Log in</a> after you get your username.");

							} catch (Exception e) {
								_log.warn("Could not send email", e);
								page.addError("Error sending email, sorry");
							}
							
						} else {
							
							page.addError("No user with this email");
						}
					}
					
				} else {

					throw new ServletException("Unknown request");
				}
				
				
			} else if (ProfileConst.servletPathRegister.equals(servletPath)) {
				
				// check username
				String usernameDisplay = req.getParameter("username");
				if (usernameDisplay == null || ProfileConst.usernamePattern.matcher(usernameDisplay).matches() == false) {
					page.addError(ProfileConst.usernamePatternMessage);
				}
				
				// check email
				String rawEmail = req.getParameter("email");
				String validEmail = null;
				if (rawEmail != null) {
					String trimmedEmail = rawEmail.trim();
					if (trimmedEmail.length() > 0) {
						if (ProfileConst.emailPattern.matcher(trimmedEmail).matches() == false) {
							page.addError(ProfileConst.emailPatternMessage);
						} else {
							validEmail = trimmedEmail;
						}
					}
				}
				
				// check passwords
				String password = req.getParameter("password");
				String password2 = req.getParameter("password2");
				if (password == null || ProfileConst.passwordPattern.matcher(password).matches() == false ||
					password2 == null || ProfileConst.passwordPattern.matcher(password2).matches() == false) {
					page.addError(ProfileConst.passwordPatternMessage);
				
				} else {
					if (password.equals(password2) == false) {
						page.addError("Password and its confirmation don't match");
					}
				}
				
				// check username availability
				if (page.hasErrors() == false) {

					String usernameSystem = User.toUsernameSystem(usernameDisplay);
					User candidateUser = FacadeUser.get().findByUsernameSystem(usernameSystem);
					if (candidateUser != null) {
						page.addError("Username is already taken");
					} else {
						if (FacadeUserOldName.get().canTakeUsername(usernameSystem, null) == false) {
							page.addError("Username is not available");
						}
					}
				}
				
				// check email availability
				if (validEmail != null && page.hasErrors() == false) {
						
					User candidateUser = FacadeUser.get().findByEmail(validEmail);
					if (candidateUser != null) {
						page.addError("A user with this email already exists");
						page.addError("Did you <a href=\"" + ProfileConst.servletPathRecover + "\">forget your username</a>?");
					}
				}
				
				// validate captcha
				if (page.hasErrors() == false) {
					if (checkCaptchaValid(req) == false) {
						page.addError("Unable to verify you are not a robot, please try again");
					}
				}
				
				// all good, register user
				if (page.hasErrors() == false) {

					// create new user
					String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
					User newUser = new User(usernameDisplay, validEmail, passwordHash);
					FacadeUser.get().saveUser(newUser.getDbo());
					
					// reset session id 
					FacadeUserSession.get().expireSession(sessionId);
					sessionId = Session.resetSessionId(req, resp);
					
					// link session to user
					FacadeUserSession.get().linkSessionToUser(sessionId, newUser.getId());
					
					
					StringBuilder sb = new StringBuilder();
					sb.append("Welcome to Readrz!");
					sb.append("\n");
					sb.append("\n");
					sb.append("Your username is: ");
					sb.append(newUser.getUsernameDisplay());
					sb.append("\n");
					sb.append("\n");
					sb.append("Yours, Readrz Team");
					sb.append("\n");
					sb.append("www.readrz.com");
					String text = sb.toString();
					
					try {
						if (newUser.getEmail() != null) {
							AWSEmail.sendOnePlain(newUser.getEmail(), "Readrz Registration", text);
						}
						AWSEmail.sendOnePlain("admin@readrz.com", "Admin: Readrz Registration", text);

					} catch (Exception e) {
						_log.warn("Could not send email", e);
					}
					
					// redirect to root
					resp.sendRedirect(ProfileConst.servletPathRoot);
					return;
				}
				
			} else {
				
				throw new ServletException("Unknown request");
			}
		}

		// keep params if error
		if (page.hasErrors()) {
			page.preservePars(req);
		}
		
		// render page html
		ST authPageST = FacadeTemplates.get(getServletContext()).getAuthPageST();
		authPageST.add("page", page);
		String htmlResponse = authPageST.render();
    	HttpResponseUtils.writeHtmlUtf8Response(resp, htmlResponse);
	}

	private boolean checkCaptchaValid(HttpServletRequest req) throws IOException {
		
		// always valid on localhost
		if (SystemUtils.isLocalhost()) {
			return true;
		}
		
		Map<String, String> captchaParams = new HashMap<>();
		captchaParams.put("privatekey", "6Lc36OESAAAAAFB11VDWkX8D_ssa_VzeGXil1jOy");
		captchaParams.put("remoteip", req.getRemoteAddr());
		captchaParams.put("challenge", req.getParameter("recaptcha_challenge_field"));
		captchaParams.put("response", req.getParameter("recaptcha_response_field"));
		String captchaResponse = HttpUtils.getPOST("http://www.google.com/recaptcha/api/verify", captchaParams);
		return captchaResponse.startsWith("true");
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		
		String servletPath = req.getServletPath();
		
		if (
			ProfileConst.servletPathLogin.equals(servletPath) ||
			ProfileConst.servletPathRecover.equals(servletPath) ||
			ProfileConst.servletPathRegister.equals(servletPath)) {
			
			// ensure security on non-localhost
			if (SystemUtils.isLocalhost() == false) {
				if (SchemeUtils.ensureScheme_sendRedirect(SchemeUtils.https, req, resp, null)) {
					return;
				}
			}
			
			// create page
			AuthPage page = new AuthPage(req, resp);
			page.setCode(req.getParameter("code"));
			
			// set current user
			String sessionId = Session.getSessionId(req, resp);
			User user = FacadeUserSession.get().findUserBySession(sessionId);
			page.setUser(user);

			// render page html
			ST authPageST = FacadeTemplates.get(getServletContext()).getAuthPageST();
			authPageST.add("page", page);
			String htmlResponse = authPageST.render();
	    	HttpResponseUtils.writeHtmlUtf8Response(resp, htmlResponse);
			
		} else if (ProfileConst.servletPathLogout.equals(servletPath)) {
			
			// reset session
			Session.resetSessionId(req, resp);
			
			// get goto parameter
			String gotoPath = req.getParameter(RzPar.parGoto);
			
			// ** check ** the goto parameter
			if (gotoPath != null && ProfileConst.knownServletPaths.contains(gotoPath)) {
				resp.sendRedirect(gotoPath);
			} else {
				resp.sendRedirect(ProfileConst.servletPathRoot);
			}
			
		} else {
			
			throw new ServletException("Unknown request");
		}
	}

}
