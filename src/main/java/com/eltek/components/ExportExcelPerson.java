package com.eltek.components;

import com.eltek.model.Person;
import com.eltek.model.PersonInstrument;
import com.webobjects.appserver.WOContext;
import com.webobjects.foundation.NSArray;

import er.extensions.components.ERXComponent;

public class ExportExcelPerson extends ERXComponent {
	
	private static final long serialVersionUID = 1L;
	public boolean enabled = false;
	private NSArray<Person> _thePersonArray = null;
	public Person aPerson;
	private PersonInstrument api = null;
	
    public ExportExcelPerson(WOContext context) {
        super(context);
    }

	public NSArray<Person> thePersonArray() {
		System.out.println("asking for the person array" + _thePersonArray); 

		return _thePersonArray;
	}

	public void setThePersonArray(NSArray<Person> thePersonArray) {
		System.out.println("setting the person array"+thePersonArray); 
		this._thePersonArray = thePersonArray;
	}


	public PersonInstrument api() {
		return api;
	}

	public void setApi(PersonInstrument api) {
		this.api = api;
	}


	
	
	
}