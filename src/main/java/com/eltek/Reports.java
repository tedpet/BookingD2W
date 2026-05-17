package com.eltek;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Callable;

import tp.jasperreports.TPJRReportTaskFromEO;

import com.eltek.model.Person;
import com.eltek.model.Show;

import er.extensions.foundation.ERXThreadStorage;

public class Reports {

	/*
	 * pass in the show then print the report
	 * 
	 */
	
	public static Callable<File> createReportForShow(Show theShow, String dateFrame) {
		
		
		String reportDescription = "Payroll for " + theShow.showName() + ", for " + dateFrame;
		
		Person localPerson = ((Person) ERXThreadStorage.valueForKey("currentUser")).localInstanceIn(theShow.editingContext());
		
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("reportDescription", reportDescription);
		parameters.put("userName", localPerson.firstName() + " " + localPerson.lastName());
		
		TPJRReportTaskFromEO reportTask = new TPJRReportTaskFromEO(theShow, ReportName.SHOWREPORTFORWEEK.toString(), "app", parameters);
		
		return reportTask;			
	}
	
	
	
}
