package com.eltek;

public enum ReportName {
  SHOWREPORT("ShowReport.jasper"),
  SHOWREPORTFORWEEK("ThisWeekForShow.jasper")
  ;
  
  private String nameAsString;
  
  private ReportName(String nameAsString) {
    this.nameAsString = nameAsString;
  }
  
  @Override
  public String toString() {
    return  this.nameAsString;
  }
  
}
