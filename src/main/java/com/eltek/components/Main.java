package com.eltek.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXStatelessComponent;
import er.extensions.foundation.ERXProperties;

public class Main extends ERXStatelessComponent {
	
	private static final long serialVersionUID = 1L;
	private String _username;
	private String _password;
	private String _errorMessage;

	private static final String USERNAME_KEY = "username";
	private static final String PASSWORD_KEY = "password";

	public Main(WOContext context) {
		super(context);
		
		this.setUsername(ERXProperties.stringForKey(USERNAME_KEY));
		this.setPassword(ERXProperties.stringForKey(PASSWORD_KEY));

	}
	
	public void setUsername(String username) {
		_username = username;
	}

	public String username() {
		return _username;
	}

	public void setPassword(String password) {
		_password = password;
	}

	public String password() {
		return _password;
	}

	public void setErrorMessage(String errorMessage) {
		_errorMessage = errorMessage;
	}

	public String errorMessage() {
		return _errorMessage;
	}
	
}
