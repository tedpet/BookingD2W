package com.eltek;
/*
 * Booking App
 */
import java.util.NoSuchElementException;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WORequest;
import com.webobjects.directtoweb.D2W;
import com.webobjects.foundation.NSLog;

import er.directtoweb.ERD2WDirectAction;
import er.extensions.eof.ERXEC;
import er.extensions.foundation.ERXStringUtilities;

import com.eltek.model.Person;
import com.eltek.Session;
import com.eltek.components.Main;


public class DirectAction extends ERD2WDirectAction {
	public DirectAction(WORequest request) {
		super(request);
	}

	@Override
	public WOActionResults defaultAction() {
		return pageWithName(Main.class.getName());
	}
	
    /**
     * Checks if a page configuration is allowed to render.
     * Provide a more intelligent access scheme as the default just returns false. And
     * be sure to read the javadoc to the super class.
     * @param pageConfiguration
     * @return
     */
    protected boolean allowPageConfiguration(String pageConfiguration) {
        return false;
    }
    
	public WOActionResults loginAction() {
		
		WOComponent nextPage = null;

		String username = request().stringFormValueForKey("username");
		String password = request().stringFormValueForKey("password");
		
		boolean authFailed = true;

		NSLog.out.appendln("***DirectAction.loginAction - username: " + username + " : password: " + password + "***");
		String errorMessage = null;

		if (ERXStringUtilities.stringIsNullOrEmpty(username) || ERXStringUtilities.stringIsNullOrEmpty(password)){
			//there is something wrong so set the errorMessage
			errorMessage = "Please enter a username and password.";
		}
		else 
		{
			try {

				authFailed = false;
				System.out.println("DA - now validate the login");
				
				Person user = Person.validateLogin(ERXEC.newEditingContext(), username, password);
				((Session) session()).setUser(user);

				nextPage = ((Session) session()).navController().queryMainEvents();
			
			}
			catch (NoSuchElementException e) {
				errorMessage = "No user found for that combination of username and password.";
				authFailed = true;
				
			}
			catch (Exception e) {
			  errorMessage = "Some Error other than bad username password combination: " + e;
			  authFailed = true;

			}
		}
		if (authFailed) {
			NSLog.out.appendln("authFailed ");
			nextPage = pageWithName(Main.class.getName());
			nextPage.takeValueForKey(errorMessage, "errorMessage");
			nextPage.takeValueForKey(username, "username");
			nextPage.takeValueForKey(password, "password");
		}

		return nextPage;
	}
	
}
