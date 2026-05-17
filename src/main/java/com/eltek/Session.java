package com.eltek;


import com.eltek.model.Person;
//import com.webobjects.eoaccess.EOModelGroup;

import er.extensions.appserver.ERXSession;
//import er.extensions.eof.ERXModelGroup;
import er.extensions.foundation.ERXThreadStorage;
import er.corebusinesslogic.ERCoreBusinessLogic;


public class Session extends ERXSession {
	private static final long serialVersionUID = 1L;
	private Person _user;

	private MainNavigationController _navController;
	
	public Session() {

	}
	
	public MainNavigationController navController() {
		if (_navController == null) {
			_navController = new MainNavigationController(this);
		}

		return _navController;
	}
	
	public void awake() {
        super.awake();
        ERXThreadStorage.takeValueForKey(user(), "currentUser");
        if (user() != null) {
        	ERCoreBusinessLogic.setActor(user());
		}
     }
	
	public Person user() {
		return _user;
	}
	
	public void setUser(Person user) {
		System.out.println("this is setUser in the Session");

		_user = user;
		ERCoreBusinessLogic.setActor(user);
	}
	
	public void sleep() {
		ERCoreBusinessLogic.setActor(null);
		super.sleep();
	}

}
