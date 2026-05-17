package com.eltek.utils;

import com.webobjects.eoaccess.EODatabaseDataSource;
import com.webobjects.eocontrol.EODataSource;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.eltek.model.Book;

import er.directtoweb.delegates.ERDQueryDataSourceDelegateInterface;
import er.directtoweb.pages.ERD2WQueryPage;
import er.extensions.eof.ERXQ;

public class LimitBooksDataSourceDelegate implements ERDQueryDataSourceDelegateInterface {

/*
 * (non-Javadoc)
 * @see er.directtoweb.delegates.ERDQueryDataSourceDelegateInterface#queryDataSource(er.directtoweb.pages.ERD2WQueryPage)
 * 
 * this allows you to add more qualifiers to the Query page.
 * 
 * It only effects query (search) pages
 */
	
	public EODataSource queryDataSource(ERD2WQueryPage sender) {
		
		EODataSource ds = sender.dataSource();
		if (ds == null || !(ds instanceof EODatabaseDataSource)) {
			ds = new EODatabaseDataSource(sender.session().defaultEditingContext(), sender.entity().name());
			
			sender.setDataSource(ds);
			
		}

		EOFetchSpecification fs = ((EODatabaseDataSource) ds).fetchSpecification();
		fs.setQualifier(qualifierFromSender(sender));
		fs.setIsDeep(sender.isDeep());
		fs.setUsesDistinct(sender.usesDistinct());
		fs.setRefreshesRefetchedObjects(sender.refreshRefetchedObjects());

		int limit = sender.fetchLimit();
		if (limit != 0) {
			fs.setFetchLimit(limit);
		}

		NSArray<String> prefetchingRelationshipKeyPaths = sender.prefetchingRelationshipKeyPaths();
		if (prefetchingRelationshipKeyPaths != null && prefetchingRelationshipKeyPaths.count() > 0) {
			fs.setPrefetchingRelationshipKeyPaths(prefetchingRelationshipKeyPaths);
		}
		return ds;
	}

	
	private EOQualifier qualifierFromSender(ERD2WQueryPage sender) {
		/*
		 * current sender.qualifier() is null so we can 'and' the qualifier
		 * with our limiter
		 * because each sender will have a key 'person'
		 * we can use that.
		 */
		System.out.println("qualifier   ");
	    return ERXQ.and(sender.qualifier(), ERXQ.equals("bookTitle", "Horn 1"));
	    
		//return sender.qualifier();
	}
	

	
}

