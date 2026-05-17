package com.eltek.components;

import java.io.File;
import java.util.concurrent.Callable;

import com.eltek.model.Show;
import com.eltek.FileTaskDownloadController;
import com.eltek.Reports;
import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

import er.coolcomponents.CCAjaxLongResponsePage;
import er.extensions.components.ERXStatelessComponent;

public class PrintShowReport extends ERXStatelessComponent  {
  
	private static final long serialVersionUID = 1L;

	public PrintShowReport(WOContext context) {
        super(context);
    }
    
    public EOEnterpriseObject object() {return (EOEnterpriseObject)valueForBinding("object");}
    public String key() {return (String)valueForBinding("key");}
    public String displayValue() {return (String)valueForBinding("key");}
    
    
    public WOActionResults printShowReport() {
    	
    	Show theShowReport = (Show)object();
		
		Callable<File> reportTask = Reports.createReportForShow(theShowReport, "");

		// Create the long response page
		CCAjaxLongResponsePage nextPage = pageWithName(CCAjaxLongResponsePage.class);
		
		// Push the task into the long response page
		nextPage.setTask(reportTask);
		
		// Controller for handling the Callable result in the long response page
		FileTaskDownloadController nextPageController = new FileTaskDownloadController();
		
		// Hyperlink text on the "Your file is downloaded page" to get back here
		nextPageController.setReturnLinkText("Reports Menu");
		
		// The filename for the download
		nextPageController.setDownloadFileNameForClient("ShowReport.pdf");
		
		nextPage.setNextPageForResultController(nextPageController);
			
		return nextPage;
    }
    
}