package com.eltek;

import com.eltek.components.Main;

// Booking D2W

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOSession;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import er.extensions.appserver.ERXApplication;
import er.extensions.appserver.navigation.ERXNavigationManager;
import er.extensions.foundation.ERXDictionaryUtilities;
import er.extensions.foundation.ERXPatcher;
import er.corebusinesslogic.ERCoreBusinessLogic;

public class Application extends ERXApplication {
	
	public static void main(String[] argv) {
		ERXApplication.main(argv, Application.class);
	}

	public Application() {
		ERXApplication.log.info("Welcome to " + name() + " !");
		setDefaultRequestHandler(requestHandlerForKey(directActionRequestHandlerKey()));
		setAllowsConcurrentRequestHandling(true);		
	}
	
    @Override
    public void finishInitialization() {
    	super.finishInitialization();
    	
    	// Setup main navigation
    	ERXNavigationManager.manager().configureNavigation();
    	
    	ERCoreBusinessLogic.sharedInstance().addPreferenceRelationshipToActorEntity("Person");

    }
    
    public void didFinishLaunching() {
    	super.didFinishLaunching();
    	//Setup preferences for User

    	WOApplication.application().setSessionTimeOut(14400);
    }
    
    /*
     * dictionary 'data' needed for excel export!!
     */
    
    public NSDictionary<?,?> data() {
    	NSDictionary<?,?> dict = ERXDictionaryUtilities.dictionaryFromPropertyList("ExcelStyles", NSBundle.mainBundle());		
    	//System.out.println("the dict = " + dict);    			
    	return dict;
    }
    
	/**
	 * Determines the {@link WOSession} class to instantiate.
	 *
	 * @see com.webobjects.appserver.WOApplication#_sessionClass()
	 */

	@Override
	protected Class<? extends WOSession> _sessionClass() {
		return Session.class;
	}


	/**
	 * Installs patches, including ensuring that {@code Main} is correctly resolved at runtime.
	 *
	 * @see er.extensions.appserver.ERXApplication#installPatches()
	 */

	@Override
	public void installPatches() {
		super.installPatches();
		ERXPatcher.setClassForName(Main.class, "Main");
	}
}
