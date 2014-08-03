package com.readrz.www;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.akuz.core.SystemUtils;
import me.akuz.core.crypt.bcrypt.BCrypt;

import org.stringtemplate.v4.ST;

import com.readrz.data.user.User;
import com.readrz.www.facades.FacadeTemplates;
import com.readrz.www.facades.FacadeUser;
import com.readrz.www.facades.FacadeUserOldName;
import com.readrz.www.facades.Session;
import com.readrz.www.pages.ProfilePage;

public final class ProfileServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	@Override
	public void init() throws ServletException {
		super.init();
	}
	
	@Override
	public void destroy() {
		super.destroy();
	}
	
	private static void initPars(ProfilePage page, User user) {
		page.setPar("username", user.getUsernameDisplay());
		page.setPar("email", user.getEmail());
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		// ensure security on non-localhost
		if (SystemUtils.isLocalhost() == false) {
			SchemeUtils.ensureScheme_orThrow(SchemeUtils.https, req, resp);
		}

		// create page
		ProfilePage page = new ProfilePage(req, resp);

		// current user
		User user = page.getUser();
		if (user == null) {
			resp.sendRedirect(ProfileConst.servletPathLogin);
			return;
		}
		initPars(page, user);

		String servletPath = req.getServletPath();

		if (ProfileConst.servletPathProfile.equals(servletPath)) {

			String update = req.getParameter("update");
			if ("email".equals(update)) {
				
				String rawEmail = req.getParameter("email");
				String validEmail = null;
				if (rawEmail != null && rawEmail.length() > 0) {
					String trimmedEmail = rawEmail.trim();
					if (ProfileConst.emailPattern.matcher(trimmedEmail).matches() == false) {
						page.addError(ProfileConst.emailPatternMessage);
					} else {
						validEmail = trimmedEmail;
					}
				}
				
				if (page.hasErrors() == false) {
					
					if (validEmail == null) {

						user.setEmail(validEmail);
						FacadeUser.get().saveUser(user.getDbo());
						page.addMessage("Your email address has been removed");
						page.addMessage("You won't be able to recover your password if you forget it");
						page.setPar("email", validEmail);
						
					} else {
						
						User emailUser = FacadeUser.get().findByEmail(validEmail);
						if (emailUser != null && emailUser.getId().equals(user.getId()) == false) {
							
							page.addError("Another user with this email already exists");
							page.addError("Did you <a href=\"" + ProfileConst.servletPathRecover + "\">forget your username</a>?");
						
						} else {
							
							user.setEmail(validEmail);
							FacadeUser.get().saveUser(user.getDbo());
							page.addMessage("Your email address has been updated");
							page.setPar("email", validEmail);
						}
					}
				}
				
			} else if ("password".equals(update)) {
				
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
				
				// all good, update password
				if (page.hasErrors() == false) {
					
					String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
					user.setPasswordHash(passwordHash);
					FacadeUser.get().saveUser(user.getDbo());
					page.addMessage("Your password has been updated");
				}
				
			} else if ("username".equals(update)) {
				
				// check username format
				String usernameDisplay = req.getParameter("username");
				if (usernameDisplay == null || ProfileConst.usernamePattern.matcher(usernameDisplay).matches() == false) {
					page.addError(ProfileConst.usernamePatternMessage);
				}
				
				// check username availability
				if (page.hasErrors() == false) {

					String usernameSystem = User.toUsernameSystem(usernameDisplay);
					User candidateUser = FacadeUser.get().findByUsernameSystem(usernameSystem);
					if (candidateUser != null) {
						if (candidateUser.getId().equals(user.getId()) == false) {
							page.addError("Username is already taken");
						}
					} else {
						if (FacadeUserOldName.get().canTakeUsername(usernameSystem, user.getId()) == false) {
							page.addError("Username is not available");
						}
					}
				}
				
				// all good, update username
				if (page.hasErrors() == false) {
					
					if (usernameDisplay.equals(user.getUsernameDisplay()) == false) {

						// remember old username
						String usernameSystem = User.toUsernameSystem(usernameDisplay);
						FacadeUserOldName.get().saveOldUsername(usernameSystem, user.getId());
						
						// set new username
						user.setUsernameDisplay(usernameDisplay);
						FacadeUser.get().saveUser(user.getDbo());
						page.setPar("username", usernameDisplay);

						page.addMessage("Your username has been updated");
						
					} else {
						
						page.addMessage("Your username hasn't changed");
					}
				}
				
			} else if ("delete".equals(update)) {
				
				// remember old username
				FacadeUserOldName.get().saveOldUsername(user.getUsernameSystem(), user.getId());
				
				// delete user
				user.setUsernameDisplay(null);
				FacadeUser.get().saveUser(user.getDbo());
				
				// logout
				Session.resetSessionId(req, resp);
				resp.sendRedirect(ProfileConst.servletPathRoot);
				return;
				
			} else {
				 
				throw new ServletException("Unknown request");
			}
			 
		} else {
			 
			throw new ServletException("Unknown request");
		}
		
		// keep params if error
		if (page.hasErrors()) {
			page.preservePars(req);
		}
		
		// render page html
		ST profilePageST = FacadeTemplates.get(getServletContext()).getProfilePageST();
		profilePageST.add("page", page);
		String htmlResponse = profilePageST.render();
    	HttpResponseUtils.writeHtmlUtf8Response(resp, htmlResponse);
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {

		// ensure security on non-localhost
		if (SystemUtils.isLocalhost() == false) {
			if (SchemeUtils.ensureScheme_sendRedirect(SchemeUtils.https, req, resp, null)) {
				return;
			}
		}
		
		String servletPath = req.getServletPath();
		
		if (ProfileConst.servletPathProfile.equals(servletPath)) {
			
			// create page
			ProfilePage page = new ProfilePage(req, resp);
			User user = page.getUser();
			if (user == null) {
				resp.sendRedirect(ProfileConst.servletPathLogin);
				return;
			}
			initPars(page, user);
			
			// render page html
			ST profilePageST = FacadeTemplates.get(getServletContext()).getProfilePageST();
			profilePageST.add("page", page);
			String htmlResponse = profilePageST.render();
	    	HttpResponseUtils.writeHtmlUtf8Response(resp, htmlResponse);
			
		} else {
			
			throw new ServletException("Unknown request");
		}
	}

}
