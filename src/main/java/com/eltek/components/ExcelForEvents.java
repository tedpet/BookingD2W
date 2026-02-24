package com.eltek.components;

import com.eltek.model.EventBook;
import com.eltek.model.EventPerson;
import com.eltek.model.Person;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.extensions.components.ERXComponent;

public class ExcelForEvents extends  ERXComponent {
	
	public boolean enabled = true;

	private NSArray<com.eltek.model.Event> theEvents = null;
	public com.eltek.model.Event anEvent;
	public EventPerson ePerson = null;
	public Person aPerson;
	private String _theShowName;
	public EventBook anEventBook;
	
    public ExcelForEvents(WOContext context) {
        super(context);
    }

	public NSArray<com.eltek.model.Event> theEvents() {
		return theEvents;
	}

	public void setTheEvents(NSArray<com.eltek.model.Event> _theEvents) {
		System.out.println("here is the first name: " + _theEvents);	
		this.theEvents = _theEvents;
	}

	public String theShowName() {
		return _theShowName;
	}

	public void setTheShowName(String _theShowName) {
		this._theShowName = _theShowName;
	}

    
    
    
    
    
}