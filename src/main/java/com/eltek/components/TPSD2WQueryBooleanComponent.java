package com.eltek.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;

import er.directtoweb.components.ERDCustomQueryComponent;

@SuppressWarnings("serial")
public class TPSD2WQueryBooleanComponent extends ERDCustomQueryComponent {  

  public TPSD2WQueryBooleanComponent(WOContext context) {
    super(context);

  }

  public NSArray<String> list() {

    NSMutableArray<String> theList = new NSMutableArray<String>();

    if (d2wContext().valueForKey("popupBooleanTrueValue") != null) {
      theList.add(d2wContext().valueForKey("popupBooleanTrueValue").toString());
    } else {
      d2wContext().takeValueForKey("True", "popupBooleanTrueValue");
      theList.add("True");
    }

    if (d2wContext().valueForKey("popupBooleanFalseValue") != null) {
      theList.add(d2wContext().valueForKey("popupBooleanFalseValue").toString());
    } else {
      d2wContext().takeValueForKey("False", "popupBooleanFalseValue");
      theList.add("False");
    }

    return (NSArray<String>)theList;
  }

  public Object value() {

    propertyKey = (String) valueForBinding("propertyKey");
    displayGroup = (WODisplayGroup)valueForBinding("displayGroup");        

    if(displayGroup != null && displayGroup.queryMatch() != null)
      return displayGroup.queryMatch().objectForKey(propertyKey);
    else
      return null;
  }

  public void setValue(String newValue) throws Exception {
    int returnValue = 0;

    propertyKey = (String) valueForBinding("propertyKey");        
    displayGroup = (WODisplayGroup)valueForBinding("displayGroup");
    
    NSLog.out.appendln("propertyKey = " + propertyKey );

    if(newValue == null || newValue == "don't care" || newValue == "-- Select --") {
      displayGroup.queryMatch().takeValueForKey(null, propertyKey);
      displayGroup.queryOperator().takeValueForKey(null, propertyKey);
    } else {

      if (newValue.equalsIgnoreCase(d2wContext().valueForKey("popupBooleanTrueValue").toString())) {
        returnValue = 1;
      } else if (newValue.equalsIgnoreCase(d2wContext().valueForKey("popupBooleanFalseValue").toString())){
        returnValue = 0; 
      } 

      displayGroup.queryMatch().takeValueForKey(returnValue, propertyKey);
      displayGroup.queryOperator().takeValueForKey("=", propertyKey);

      NSLog.out.appendln("displayGroup.allObjects() = " + displayGroup.toString() );
    }
  }

  /*
   * if emptyBooleanPopup is not set in a rule return the default answer
   */
  public String emptyString() {	   
    return (d2wContext().valueForKey("emptyBooleanPopup") != null) ?  (String) d2wContext().valueForKey("emptyBooleanPopup") : "-- Select --";
  }

  public EOEnterpriseObject object;
  public String propertyKey;
  public Object item;
  public WODisplayGroup displayGroup;
  //private static NSArray<String> list = new NSArray<String>("true", "false");

  //NSLog.out.appendln("setValue in the boolean popup " + returnValue );

}