package com.eltek;

import java.io.File;

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOComponent;
import com.webobjects.directtoweb.D2W;

import er.extensions.appserver.ERXNextPageForResultWOAction;
import er.extensions.appserver.ERXWOContext;
import er.extensions.components.ERXDownloadResponse;
import com.eltek.components.DownloadIsCompletePage;
import com.eltek.Session;

/**
 * A generic controller for handling a long response that generates a file to download.
 * 
 * The responsibility of this class is to pass control from the long response page at the end of
 * the execution of the long response task to the next page, which in this case is the "Your File is being downloaded" page.
 * 	
 * @author kieran
 *
 */
public class FileTaskDownloadController extends ERXNextPageForResultWOAction {
	
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(FileTaskDownloadController.class);
	
	private String _returnLinkText;
	private final WOComponent _senderPage;
	private Session _session;

	
	public FileTaskDownloadController() {
		super();
		_senderPage = ERXWOContext.currentContext().page();
		
		_session = (Session) _senderPage.session();		
	}
	
	/** @return the link text for the link/button that brings the user back to the original page that initiated the long response file download */
	public String returnLinkText() {
		return _returnLinkText;
	}
	
	/** @param returnLinkText the link text for the link/button that brings the user back to the original page that initiated the long response file download */
	public void setReturnLinkText(String returnLinkText){
		_returnLinkText = returnLinkText;
	}
	
	@Override
	public WOActionResults performAction() {
		if (_result instanceof File) {
			File file = (File) _result;
			
			ERXDownloadResponse dl = pageWithName(ERXDownloadResponse.class);
			dl.setFileToDownload(file);
			dl.setDownloadFilename(downloadFileNameForClient());
	/*
	 * 
	 * We fix that by replacing 

    HelloWorld aPage = (HelloWorld) pageWithName("HelloWorld");

with:
  HelloWorld aPage = (HelloWorld)  D2W.factory().pageForConfigurationNamed("HelloWorld", session());
DownloadIsCompletePage nextPage = (DownloadIsCompletePage)  D2W.factory().pageForConfigurationNamed("DownloadIsCompletePage", session());

	 */
			
			//DownloadIsCompletePage nextPage = pageWithName(DownloadIsCompletePage.class);
			
			DownloadIsCompletePage nextPage = (DownloadIsCompletePage)  
					D2W.factory().pageForConfigurationNamed("DownloadIsCompletePage", session() );
			nextPage.setDownloadResponseComponent(dl);
			nextPage.setReturnLinkText(returnLinkText());
			nextPage.setReferringPage(_senderPage);
			
		//	NSLog.out.appendln("***the nextPage  " + nextPage.template());
			
			return nextPage;

		} else {
			throw new NestableRuntimeException("Unknown result type: " + (_result == null ? "null" : _result.toString()));
		}
	}


	private String _downloadFileNameForClient;
	
	/** @return what */
	public String downloadFileNameForClient() {
		return _downloadFileNameForClient;
	}
	
	/** @param downloadFileNameForClient what */
	public void setDownloadFileNameForClient(String downloadFileNameForClient){
		_downloadFileNameForClient = downloadFileNameForClient;
	}
	
    public Session session() {
		return _session;
	}

	public void setSession(Session s) {
		_session = s;
	}
	
}
