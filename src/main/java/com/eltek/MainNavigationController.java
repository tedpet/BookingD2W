package com.eltek;

import java.text.spi.DateFormatSymbolsProvider;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.joda.time.DateTime;

import com.eltek.model.Book;
import com.eltek.model.Event;
import com.eltek.model.Instrument;
import com.eltek.model.Person;
import com.eltek.model.Show;
import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.directtoweb.EditPageInterface;
import com.webobjects.directtoweb.ErrorPageInterface;
import com.webobjects.directtoweb.ListPageInterface;
import com.webobjects.directtoweb.QueryPageInterface;
import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;

import er.directtoweb.pages.ERD2WQueryPage;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXFetchSpecification;
import er.extensions.eof.ERXQ;

public class MainNavigationController {

	public String SHOW = "Show";
	public String PERSON = "Person";
	public String EVENT = "Event";
	public String INSTRUMENT = "Instrument";
	public String BOOK = "Book";
	public String PERSONBOOK = "PersonBook";

	private Session _session;

	public MainNavigationController(Session s) {
		super();
		_session = s;
	}

	// NAV ACTIONS

	public WOComponent homeAction() {

		return queryPageForEntityName(EVENT);
		// return D2W.factory().defaultPage(session());
	}

	//Person
	public WOComponent listPersonAction() {
		EOEditingContext ec = ERXEC.newEditingContext();
		ec.lock();

		ListPageInterface lpi;
		try {
			EODatabaseDataSource ds = new EODatabaseDataSource(ec, PERSON);

			ERXFetchSpecification<Person> fs = 
					new ERXFetchSpecification<Person>(Person.ENTITY_NAME, null, null);

			ds.setFetchSpecification(fs);

			lpi = D2W.factory().listPageForEntityNamed(Person.ENTITY_NAME, session());
			lpi.setDataSource(ds);

			//			if(lpi instanceof D2WPage) {
			//				
			//				((D2WPage) lpi).d2wContext().takeValueForKey("Person", "navigationState");
			//				//((D2WPage) lpi).d2wContext().takeValueForKey("AgendaInstructions", "headerInstructionComponentName");
			//			}			
		}
		finally {
			ec.unlock();
		}
		return (WOComponent) lpi;
	}

	public WOComponent reportAction() {
		System.out.println("reportAction");
		return D2W.factory().pageForConfigurationNamed("MainReport", session());
	}
	
	public WOComponent createPersonAction() {
		return newObjectForEntityName(PERSON);
	}

	public WOComponent queryPersonAction() {
		return queryPageForEntityName(PERSON);
	}

	// Shows
	public WOComponent listShowsAction() {
		EOEditingContext ec = ERXEC.newEditingContext();
		ec.lock();

		ListPageInterface lpi;
		try {
			EODatabaseDataSource ds = new EODatabaseDataSource(ec, SHOW);

			ERXFetchSpecification<Show> fs = 
					new ERXFetchSpecification<Show>(Show.ENTITY_NAME, Show.RUNNING.eq(true), null);

			ds.setFetchSpecification(fs);

			lpi = D2W.factory().listPageForEntityNamed(Show.ENTITY_NAME, session());
			lpi.setDataSource(ds);

			//			if(lpi instanceof D2WPage) {
			//				
			//				((D2WPage) lpi).d2wContext().takeValueForKey("Person", "navigationState");
			//				//((D2WPage) lpi).d2wContext().takeValueForKey("AgendaInstructions", "headerInstructionComponentName");
			//			}			
		}
		finally {
			ec.unlock();
		}
		return (WOComponent) lpi;
	}

	public WOComponent createShowAction() {
		return newObjectForEntityName(SHOW);
	}

	public WOComponent queryShowAction() {
		return queryPageForEntityName(SHOW);
	}

	//query events main page
	@SuppressWarnings("unchecked")
	public WOComponent queryMainEvents() {

		EOEditingContext ec = ERXEC.newEditingContext();
		ec.lock();

		ERD2WQueryPage page = (ERD2WQueryPage)D2W.factory().queryPageForEntityNamed(Event.ENTITY_NAME, session());
		
		Calendar fromDate = GregorianCalendar.getInstance();
		Calendar toDate = GregorianCalendar.getInstance();
		
		Date minDate =  fromDate.getTime();
		Date maxDate =  toDate.getTime();
				
		fromDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		minDate =  fromDate.getTime();
		
		toDate.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		toDate.add(toDate.DATE, 7);
		maxDate =  toDate.getTime();
	    
		NSTimestamp min = new NSTimestamp(minDate);
		NSTimestamp max = new NSTimestamp(maxDate);
		page.displayGroup().queryMin().setObjectForKey(min, Event.EVENT_DATE_KEY);
		page.displayGroup().queryMax().setObjectForKey(max, Event.EVENT_DATE_KEY);

		try {
			EODatabaseDataSource ds = new EODatabaseDataSource(ec, EVENT);

			ERXFetchSpecification<Event> fs = 
					new ERXFetchSpecification<Event>(Event.ENTITY_NAME, null, null);

			ds.setFetchSpecification(fs);
			page.setDataSource(ds);

		}
		finally {
			ec.unlock();
		}

		return page;

	}

	// Instruments
	public WOComponent listInstrumentAction() {
		EOEditingContext ec = ERXEC.newEditingContext();
		ec.lock();

		ListPageInterface lpi;
		try {
			EODatabaseDataSource ds = new EODatabaseDataSource(ec, INSTRUMENT);

			ERXFetchSpecification<Instrument> fs = 
					new ERXFetchSpecification<Instrument>(Instrument.ENTITY_NAME, null, null);

			ds.setFetchSpecification(fs);

			lpi = D2W.factory().listPageForEntityNamed(Instrument.ENTITY_NAME, session());
			lpi.setDataSource(ds);

			//			if(lpi instanceof D2WPage) {
			//				
			//				((D2WPage) lpi).d2wContext().takeValueForKey("Person", "navigationState");
			//				//((D2WPage) lpi).d2wContext().takeValueForKey("AgendaInstructions", "headerInstructionComponentName");
			//			}			
		}
		finally {
			ec.unlock();
		}
		return (WOComponent) lpi;
	}

	public WOComponent createInstrumentAction() {
		return newObjectForEntityName(INSTRUMENT);
	}

	public WOComponent queryInstrumentAction() {
		return queryPageForEntityName(INSTRUMENT);
	}


	//Event
	public WOComponent listEventsAction() {
		EOEditingContext ec = ERXEC.newEditingContext();
		ec.lock();

		ListPageInterface lpi;
		try {
			EODatabaseDataSource ds = new EODatabaseDataSource(ec, EVENT);

			ERXFetchSpecification<Event> fs = 
					new ERXFetchSpecification<Event>(Event.ENTITY_NAME, null, null);

			ds.setFetchSpecification(fs);

			lpi = D2W.factory().listPageForEntityNamed(Event.ENTITY_NAME, session());
			lpi.setDataSource(ds);

		}
		finally {
			ec.unlock();
		}
		return (WOComponent) lpi;
	}

	public WOComponent createEventAction() {
		return newObjectForEntityName(EVENT);
	}

	public WOComponent queryEventAction() {
		return queryPageForEntityName(EVENT);
	}


	//Book
	public WOComponent listBookAction() {
		EOEditingContext ec = ERXEC.newEditingContext();
		ec.lock();

		ListPageInterface lpi;
		try {
			EODatabaseDataSource ds = new EODatabaseDataSource(ec, BOOK);

			ERXFetchSpecification<Book> fs = 
					new ERXFetchSpecification<Book>(Book.ENTITY_NAME, Book.IS_RETIRED.eq(false), null);

			ds.setFetchSpecification(fs);

			lpi = D2W.factory().listPageForEntityNamed(Book.ENTITY_NAME, session());
			lpi.setDataSource(ds);

		}
		finally {
			ec.unlock();
		}
		return (WOComponent) lpi;
	}

	public WOComponent createBookAction() {
		return newObjectForEntityName(BOOK);
	}

	public WOComponent queryBookAction() {
		return queryPageForEntityName(BOOK);
	}

	// GENERIC ACTIONS

	public WOComponent queryPageForEntityName(String entityName) {
		QueryPageInterface newQueryPage = D2W.factory().queryPageForEntityNamed(entityName, session());
		return (WOComponent) newQueryPage;
	}

	public WOComponent newObjectForEntityName(String entityName) {
		WOComponent nextPage = null;
		try {
			EditPageInterface epi = D2W.factory().editPageForNewObjectWithEntityNamed(entityName, session());
			epi.setNextPage(session().context().page());
			nextPage = (WOComponent) epi;
		} catch (IllegalArgumentException e) {
			ErrorPageInterface epf = D2W.factory().errorPage(session());
			epf.setMessage(e.toString());
			epf.setNextPage(session().context().page());
			nextPage = (WOComponent) epf;
		}
		return nextPage;
	}

	// ACCESSORS

	public Session session() {
		return _session;
	}

	public void setSession(Session s) {
		_session = s;
	}
}
