package com.eltek.components;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.concurrent.Callable;

import com.eltek.FileTaskDownloadController;
import com.eltek.Reports;
import com.eltek.model.EventPerson;
import com.eltek.model.Event;
import com.eltek.model.Instrument;
import com.eltek.model.Person;
import com.eltek.model.PersonInstrument;
import com.eltek.model.Show;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2W;
import com.webobjects.directtoweb.ListPageInterface;
import com.webobjects.eocontrol.EOArrayDataSource;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOQualifier;

import er.coolcomponents.CCAjaxLongResponsePage;
import er.directtoweb.pages.templates.ERD2WInspectPageTemplate;
import er.extensions.eof.ERXEC;
import er.extensions.foundation.ERXStringUtilities;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSTimestamp;

public class MainReport extends ERD2WInspectPageTemplate {

	private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	
	private NSArray<Show> _showList;
	private Show theShow;
	private String _reportTypeExtension;
	private Show selectedShow;
	private NSArray<String> _buttonList = new NSArray<String>("Last Week", "This Week", "Next Week");
	private NSArray<String> _reportTypeButtonList = new NSArray<String>("xls", "html", "pdf");
	private String _buttonLabel;
	private NSMutableSet<String> _selectedItems;
	
    public MainReport(WOContext context) {
        super(context);
        
    }
    
    @Override
    public void awake() {
    	// TODO Auto-generated method stub
    	super.awake();
    	
    	_showList = Show.fetchShows(ERXEC.newEditingContext(), Show.RUNNING.eq(true), Show.SHOW_NAME.ascs());
    	_selectedItems = new NSMutableSet<String>();
    	
    	// the default is This Week, null would be bad!!
        setButtonLabel("This Week");
        setSelected(true);
 
    }

    public WOActionResults reportForThisWeek() {
    	WOActionResults theResult = null;

    	if (reportTypeExtension().equals("xls")) {
    		theResult = exportThisWeekXLS();
    	}

    	return theResult;
    }

    public WOActionResults exportThisWeekXLS() {
    	System.out.println("exportThisWeekXLS:");
    	
    	LocalDate now = LocalDate.now();
    	//System.out.println("the dates: " + thisWeekSunday + "   " + endOfWeek);

       	EOEditingContext theContext = ERXEC.newEditingContext();
    	ListPageInterface lpi = (ListPageInterface)D2W.factory().pageForConfigurationNamed("ListExcelEventPerson", session());
    	EOArrayDataSource ds = new EOArrayDataSource(null, theContext);
     	
//    	EOQualifier eventsQual = com.eltek.model.Event.EVENT_DATE.between(
//    			new NSTimestamp(this.asDate(now.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)))), 
//    			new NSTimestamp(this.asDate(now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)))), 
//    			true);
//    	
//    	NSArray<com.eltek.model.Event> eventArray =  com.eltek.model.Event.fetchEvents(theContext, eventsQual, null);

    	NSArray<EventPerson> eventPersons = EventPerson.fetchEventPersons(theContext, 
    			EventPerson.EVENT.dot(com.eltek.model.Event.EVENT_DATE.between(
    					new NSTimestamp(this.asDate(now.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)))), 
    					new NSTimestamp(this.asDate(now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)))))), 
    			null
    			);
    	
    	System.out.println(eventPersons);
    	
    	ds.setArray(eventPersons);
    	lpi.setDataSource(ds);
    	lpi.setNextPage(this.context().page());
   	
    	return (WOActionResults)lpi;
    }
	
    
    
   
	public WOActionResults exportAllToExcel() {
		
		NSArray<Person> allPersons = Person.fetchAllPersons(ERXEC.newEditingContext());
		
		ExportExcelPerson nextPage = pageWithName(ExportExcelPerson.class);
		nextPage.enabled = true;

		nextPage.setThePersonArray(allPersons);
		
		return nextPage;
    
	}
	
	
	public WOActionResults exportShowData() {
		LocalDate now = LocalDate.now();
		
		EOQualifier eventQual = com.eltek.model.Event.EVENT_DATE.between(
				new NSTimestamp(this.asDate(now.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)))), 
				new NSTimestamp(this.asDate(now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)))), true);
		
		NSArray<com.eltek.model.Event> events = com.eltek.model.Event.fetchEvents(ERXEC.newEditingContext(), eventQual, 
				com.eltek.model.Event.EVENT_DATE.ascs());

		System.out.println("eventQual: " + eventQual);
		
		ExcelForEvents nextPage = pageWithName(ExcelForEvents.class);
		nextPage.enabled = true;
		nextPage.setTheShowName("Lion King");
		nextPage.setTheEvents(events);
		
		return nextPage;
	}
    	
    	
    	
	private WOActionResults makePDF() {

		// Create the task
		//System.out.println("itemValue " + _itemValue);
		Callable<File> reportTask = Reports.createReportForShow(selectedShow(), _selectedItems.toString());

		// Create the long response page
		CCAjaxLongResponsePage nextPage = pageWithName(CCAjaxLongResponsePage.class);

		// Push the task into the long response page
		nextPage.setTask(reportTask);

		// Controller for handling the Callable result in the long response page
		FileTaskDownloadController nextPageController = new FileTaskDownloadController();

		// Hyperlink text on the "Your file is downloaded page" to get back here
		nextPageController.setReturnLinkText("Reports Menu");

		// The filename for the download
		nextPageController.setDownloadFileNameForClient("Payroll For This Week.pdf");

		nextPage.setNextPageForResultController(nextPageController);

		return nextPage;
	}

	public NSArray<Show> showList() {
		return _showList;
	}

	public void setShowList(NSArray<Show> aShowList) {
		this._showList = aShowList;
	}

	public Show theShow() {
		return theShow;
	}

	public void setTheShow(Show theShow) {
		this.theShow = theShow;
	}

	public Show selectedShow() {
		System.out.println("selectedShow: " + selectedShow);
		return selectedShow;
	}

	public void setSelectedShow(Show selectedShow) {
		this.selectedShow = selectedShow;
	}
    
    public NSArray<String> buttonList() {
    	return _buttonList;
    }
    
	public void setButtonLabel(String itemValue) {
		//System.out.println("setting _itemValue " + itemValue);
		_buttonLabel = itemValue;
	}

	public String buttonLabel() {
		return _buttonLabel;
	}
    
	/*
	 * make sure it is javascript named ok!
	 */
	public String buttonID () {
		return ERXStringUtilities.safeIdentifierName("button_" + _buttonLabel);
	}
	
	public void setSelected(boolean selected) {
		System.out.println("setSelected:" + selected + "   " + _buttonLabel);
		
		if (selected) {
			//_selectedItems.removeAllObjects();
			
			_selectedItems.addObject(_buttonLabel);
		} else {
			_selectedItems.removeObject(_buttonLabel);
		}
	}

	public boolean isSelected() {
		//System.out.println("isSelected: " + _buttonLabel);
		//System.out.println("_selectedItems.containsObject(_buttonLabel): " + _selectedItems.containsObject(_buttonLabel));
		return _selectedItems.containsObject(_buttonLabel);
	}

	public NSArray<String> reportTypeButtonList() {
		return _reportTypeButtonList;
	}

	public void setReportTypeButtonList(NSArray<String> _theButtonList) {
		this._reportTypeButtonList = _theButtonList;
	}

	public String reportTypeExtension() {
		return _reportTypeExtension;
	}

	public void setReportTypeExtension(String RTE) {
		this._reportTypeExtension = RTE;
	}

	
	public LocalDate localdateFromString(String s) {
		return LocalDate.parse(s, formatter);		
	}
	
	public Date asDate(LocalDate localDate) {
	    return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
	}

/*
 * just so I can save the original method
 * 
 *     public WOActionResults exportAssetXLS() {
    	System.out.println("exportAssetXLS:");
    	DateFormat df = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
    	
    	Date now = new Date();
    	Calendar c = GregorianCalendar.getInstance();
    	c.setTime(now);
    	
        System.out.println("Current week = " + Calendar.DAY_OF_WEEK + "      " + Calendar.DAY_OF_YEAR+ "      " + c);

        // Set the calendar to monday of the current week
       // c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        System.out.println("Current week = " + Calendar.DAY_OF_WEEK);

        // Print dates of the current week starting on Monday
        
        String startDate = "", endDate = "";

        startDate = df.format(c.getTime());
        c.add(Calendar.DATE, 6);
        endDate = df.format(c.getTime());

        System.out.println("Start Date = " + startDate);
        System.out.println("End Date = " + endDate);
    	
    	NSTimestamp today = ERXTimestampUtilities.today();
    	System.out.println("today: " + today);
    	
    	Integer numOfWeekToday = ERXTimestampUtilities.dayOfWeek(today);
    	    	
    	System.out.println("thisWeekSunday: " + numOfWeekToday);
    	System.out.println("thisWeekSunday: " + ERXTimestampUtilities.firstDateInSameWeek(today));
    	
    	Date date = new Date();
    	Calendar cal = GregorianCalendar.getInstance();
    	cal.setTime( date );
    	cal.add(Calendar.DAY_OF_MONTH, 7);
    	
    	NSTimestamp endOfWeek = new NSTimestamp(cal.getTime());

    	//System.out.println("the dates: " + thisWeekSunday + "   " + endOfWeek);

       	EOEditingContext theContext = ERXEC.newEditingContext();
    	ListPageInterface lpi = (ListPageInterface)D2W.factory().pageForConfigurationNamed("ListExcelAssets", session());
    	EOArrayDataSource ds = new EOArrayDataSource(null, theContext);
     	
    	EOQualifier eventsQual = com.eltek.model.Event.EVENT_DATE.between(endOfWeek, endOfWeek, true);
    	NSArray<com.eltek.model.Event> eventArray =  com.eltek.model.Event.fetchEvents(theContext, eventsQual, null);

    	ds.setArray(eventArray);
    	lpi.setDataSource(ds);
    	lpi.setNextPage(this.context().page());
   	
    	return (WOActionResults)lpi;
    }
    
    	TemporalField fieldUS = WeekFields.of(Locale.US).dayOfWeek();	
		
		LocalDate thisSunday = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
		LocalDate thisSaturday = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
		
		Date thisSundayDate = this.asDate(thisSunday);
		Date thisSaturdayDate = this.asDate(thisSaturday);


 */
	
}