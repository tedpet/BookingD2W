package com.eltek;
/*
 * Booking App
 */
import java.util.NoSuchElementException;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.eocontrol.EOSortOrdering.Comparison;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSTimestamp;

import er.corebusinesslogic.ERCoreBusinessLogic;
import er.directtoweb.ERD2WDirectAction;
import er.extensions.appserver.ERXApplication;
import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEOControlUtilities;
import er.extensions.foundation.ERXStringUtilities;

import com.eltek.model.Book;
import com.eltek.model.Event;
import com.eltek.model.Instrument;
import com.eltek.model.Person;
import com.eltek.model.PersonInstrument;
import com.eltek.model.PersonSecurity;
import com.eltek.model.Show;
import com.fasterxml.jackson.databind.JsonNode;
import com.eltek.components.Main;

import java.time.Instant;
import java.util.Base64;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DirectAction extends ERD2WDirectAction {

	private static final ObjectMapper MAPPER = new ObjectMapper(); 
	
	public DirectAction(WORequest request) {
		super(request);
	}

	@Override
	public WOActionResults defaultAction() {
		return pageWithName(Main.class.getName());
	}
	
	public WOActionResults pingAction() {
	    NSDictionary<String, Object> payload = new NSDictionary<>(
	        new Object[] { "ok", "WebObjects/Wonder", System.currentTimeMillis() },
	        new String[] { "status", "server", "timestamp" }
	    );

	    // If ERXJSONUtilities is available:
	    // String json = ERXJSONUtilities.toJSONString(payload);
	    // Else, quick inline JSON:
	    String json = "{"
	            + "\"status\":\"" + payload.objectForKey("status") + "\","
	            + "\"server\":\"" + payload.objectForKey("server") + "\","
	            + "\"timestamp\":" + payload.objectForKey("timestamp")
	            + "}";

	    WOResponse response =
	        WOApplication.application().createResponseInContext(context());
	    response.setStatus(200);
	    response.setHeader("application/json; charset=utf-8", "Content-Type");
	    response.setHeader("no-cache", "Cache-Control");
	    response.appendContentString(json);
	    return response;
	}

	
    /**
     * Checks if a page configuration is allowed to render.
     * Provide a more intelligent access scheme as the default just returns false. And
     * be sure to read the javadoc to the super class.
     * @param pageConfiguration
     * @return
     */
    protected boolean allowPageConfiguration(String pageConfiguration) {
        return false;
    }
    
	public WOActionResults loginAction() {
		
		WOComponent nextPage = null;

		String username = request().stringFormValueForKey("username");
		String password = request().stringFormValueForKey("password");
		
		boolean authFailed = true;

		String errorMessage = null;

		if (ERXStringUtilities.stringIsNullOrEmpty(username) || ERXStringUtilities.stringIsNullOrEmpty(password)){
			//there is something wrong so set the errorMessage
			errorMessage = "Please enter a username and password.";
		}
		else 
		{
			try {

				authFailed = false;
							
				Person user = Person.validateLogin(ERXEC.newEditingContext(), username, password);
				((Session) session()).setUser(user);
				nextPage = ((Session) session()).navController().queryMainEvents();
			
			}
			catch (NoSuchElementException e) {
				errorMessage = "No user found for that combination of username and password.";
				authFailed = true;
				
			}
			catch (Exception e) {
			  errorMessage = "Exception e)  Some Error other than bad username password combination: " + e;
			  authFailed = true;

			}
		}
		if (authFailed) {
			NSLog.out.appendln("authFailed ");
			nextPage = pageWithName(Main.class.getName());
			nextPage.takeValueForKey(errorMessage, "errorMessage");
			nextPage.takeValueForKey(username, "username");
			nextPage.takeValueForKey(password, "password");
		}

		return nextPage;
	}
	
    // GET .../MyBook.woa/wa/items?limit=20&offset=0&q=name='Foo'&sort=name&order=asc
    public WOActionResults itemsAction() {
        // Optional: short-circuit auth if needed
        // if (!isAuthorized()) return errorJSON(401, "unauthorized");

        final String entityName = "Book"; // <-- replace with your entity name

        NSLog.out.appendln("entityName = " + entityName);
        
        EOEditingContext ec = ERXEC.newEditingContext();
        
        EOEntity entity = EOUtilities.entityNamed(ec, "Book");

        NSLog.out.appendln("attributeNamed booktitle = " + entity.attributeNamed("bookTitle"));
        NSLog.out.appendln("classProperties = " + entity.classProperties());
        NSLog.out.appendln("attributes = " + entity.attributes());
        
        try {
            // Parse query params
            int limit = intParam("limit", 20, 1, 200); // cap to avoid huge payloads
            int offset = intParam("offset", 0, 0, Integer.MAX_VALUE);

            EOQualifier qualifier = qualifierFromRequest(request(), entityName);
            NSArray<EOSortOrdering> sortOrderings = sortOrderingsFromRequest(request(), "bookTitle"); // default sort key

            NSLog.out.appendln("qualifier = " + qualifier);
            NSLog.out.appendln("sortOrderings = " + sortOrderings
            		);
            
            EOFetchSpecification fs = new EOFetchSpecification(entityName, qualifier, sortOrderings);
            fs.setFetchLimit(limit);
           // fs.setFetchOffset(offset);
            fs.setFetchLimit(offset + limit);

            @SuppressWarnings("unchecked")
            //NSArray<EOEnterpriseObject> results = (NSArray<EOEnterpriseObject>) ec.objectsWithFetchSpecification(fs);
            NSArray<EOEnterpriseObject> results =
            (NSArray<EOEnterpriseObject>) ERXEOControlUtilities.objectsInRange(
                ec,
                fs,
                offset,
                offset + limit
            );
            
            // Whitelist attributes to expose
            NSArray<String> allowed = new NSArray<>(new String[] { "id", "bookTitle" });

            // Map EOs to dictionaries
            NSMutableArray<NSDictionary<String, Object>> out = new NSMutableArray<>();
            for (EOEnterpriseObject eo : results) {
                out.addObject(safeDict(eo, allowed));
            }

            // Wrap with pagination metadata (optional)
            NSDictionary<String, Object> payload = new NSDictionary<>(
                new Object[] { limit, offset, out },
                new String[] { "limit", "offset", "data" }
            );

            return jsonResponse(200, payload);
        } catch (Exception e) {
            return errorJSON(500, e.getMessage());
        } finally {
            ec.dispose();
        }
    }
	
    
    @Override
    public WOActionResults performActionNamed(String actionName) {
        try {
            if (isAuthorized()) {
                // Decode Basic Auth header to get loginName
                String auth = request().headerForKey("authorization");
                if (auth != null && auth.startsWith("Basic ")) {
                    String decoded = new String(
                        java.util.Base64.getDecoder().decode(auth.substring(6)));
                    String loginName = decoded.split(":", 2)[0];
                    EOEditingContext ec = ERXEC.newEditingContext();
                    Person actor = (Person) EOUtilities.objectMatchingKeyAndValue(
                        ec, "Person", "loginName", loginName);
                    ERCoreBusinessLogic.setActor(actor);
                }
            }
            return super.performActionNamed(actionName);
        } finally {
            ERCoreBusinessLogic.setActor(null);
        }
    }
    
 // ===== Instrument CRUD =====

 // GET /wa/listInstruments
 public WOActionResults listInstrumentsAction() {
     if (!isAuthorized()) return unauthorized();
     try {
         EOEditingContext ec = ERXEC.newEditingContext();
         @SuppressWarnings("unchecked")
         NSArray<Instrument> rows = (NSArray<Instrument>) ec.objectsWithFetchSpecification(
             new EOFetchSpecification("Instrument", null, null));
         return json(instrumentsToJsonArray(rows));
     } catch (Exception e) {
         return error(500, e.toString());
     }
 }

 // GET /wa/listAvailableInstruments  — uses the named fetchSpec from the EOModel
 public WOActionResults listAvailableInstrumentsAction() {
     if (!isAuthorized()) return unauthorized();
     try {
         EOEditingContext ec = ERXEC.newEditingContext();
         @SuppressWarnings("unchecked")
         NSArray<Instrument> rows = (NSArray<Instrument>)
             EOUtilities.objectsWithFetchSpecificationAndBindings(
                 ec, "Instrument", "AvailableInstruments", null);
         return json(instrumentsToJsonArray(rows));
     } catch (Exception e) {
         return error(500, e.toString());
     }
 }

 // POST /wa/createInstrument  body: { "instrumentName":"…", "available":true }
 public WOActionResults createInstrumentAction() {
     if (!isAuthorized()) return unauthorized();
     try {
         JsonNode root = readJsonBody();
         if (root == null) return error(400, "Missing JSON body");
         EOEditingContext ec = ERXEC.newEditingContext();
         Instrument i = (Instrument) EOUtilities.createAndInsertInstance(ec, "Instrument");
         if (root.has("instrumentName")) i.setInstrumentName(root.path("instrumentName").asText(null));
         if (root.has("available"))      i.setAvailable(root.path("available").asBoolean(true));
         ec.saveChanges();
         return json(instrumentToJson(i));
     } catch (Exception e) {
         return error(500, e.toString());
     }
 }

 // POST /wa/updateInstrument  body: { "id": N, ... }
 public WOActionResults updateInstrumentAction() {
     if (!isAuthorized()) return unauthorized();
     try {
         JsonNode root = readJsonBody();
         if (root == null) return error(400, "Missing JSON body");
         JsonNode idNode = root.path("id");
         if (idNode.isMissingNode() || idNode.isNull()) return error(400, "Missing id");

         EOEditingContext ec = ERXEC.newEditingContext();
         Instrument i = (Instrument) EOUtilities.objectWithPrimaryKeyValue(ec, "Instrument", idNode.numberValue());
         if (i == null) return status(404);

         if (root.has("instrumentName")) i.setInstrumentName(root.path("instrumentName").asText(null));
         if (root.has("available"))      i.setAvailable(root.path("available").asBoolean(true));
         ec.saveChanges();
         return json(instrumentToJson(i));
     } catch (Exception e) {
         return error(500, e.toString());
     }
 }

 // POST /wa/deleteInstrument?id=N
 public WOActionResults deleteInstrumentAction() {
     if (!isAuthorized()) return unauthorized();
     try {
         String idStr = request().stringFormValueForKey("id");
         if (idStr == null) return error(400, "Missing id");
         EOEditingContext ec = ERXEC.newEditingContext();
         Instrument i = (Instrument) EOUtilities.objectWithPrimaryKeyValue(ec, "Instrument", Integer.valueOf(idStr));
         if (i == null) return status(404);
         ec.deleteObject(i);
         ec.saveChanges();
         return status(204);
     } catch (NumberFormatException e) {
         return error(400, "id must be an integer");
     } catch (Exception e) {
         return error(500, e.toString());
     }
 }

 
 // ===== PersonSecurity CRUD =====

 // GET /wa/listPersonSecurities
 public WOActionResults listPersonSecuritiesAction() {
     if (!isAuthorized()) return unauthorized();
     try {
         EOEditingContext ec = ERXEC.newEditingContext();
         @SuppressWarnings("unchecked")
         NSArray<PersonSecurity> rows = (NSArray<PersonSecurity>) ec.objectsWithFetchSpecification(
             new EOFetchSpecification("PersonSecurity", null, null));
         StringBuilder sb = new StringBuilder("[");
         for (int i = 0; i < rows.count(); i++) {
             if (i > 0) sb.append(',');
             sb.append(securityToJson(rows.objectAtIndex(i)));
         }
         sb.append(']');
         return json(sb.toString());
     } catch (Exception e) {
         return error(500, e.toString());
     }
 }

 // POST /wa/createPersonSecurity  body: { "canManageBook":true, ... }
 public WOActionResults createPersonSecurityAction() {
     if (!isAuthorized()) return unauthorized();
     try {
         JsonNode root = readJsonBody();
         if (root == null) return error(400, "Missing JSON body");
         EOEditingContext ec = ERXEC.newEditingContext();
         PersonSecurity s = (PersonSecurity) EOUtilities.createAndInsertInstance(ec, "PersonSecurity");
         applySecurity(s, root);
         ec.saveChanges();
         return json(securityToJson(s));
     } catch (Exception e) {
         return error(500, e.toString());
     }
 }

 // POST /wa/updatePersonSecurity  body: { "id": N, ... fields to change ... }
 public WOActionResults updatePersonSecurityAction() {
     if (!isAuthorized()) return unauthorized();
     try {
         JsonNode root = readJsonBody();
         if (root == null) return error(400, "Missing JSON body");
         JsonNode idNode = root.path("id");
         if (idNode.isMissingNode() || idNode.isNull()) return error(400, "Missing id");

         EOEditingContext ec = ERXEC.newEditingContext();
         PersonSecurity s = (PersonSecurity) EOUtilities.objectWithPrimaryKeyValue(
             ec, "PersonSecurity", idNode.numberValue());
         if (s == null) return status(404);

         applySecurity(s, root);
         ec.saveChanges();
         return json(securityToJson(s));
     } catch (Exception e) {
         return error(500, e.toString());
     }
 }

 // POST /wa/deletePersonSecurity?id=N
 public WOActionResults deletePersonSecurityAction() {
     if (!isAuthorized()) return unauthorized();
     try {
         String idStr = request().stringFormValueForKey("id");
         if (idStr == null) return error(400, "Missing id");

         EOEditingContext ec = ERXEC.newEditingContext();
         PersonSecurity s = (PersonSecurity) EOUtilities.objectWithPrimaryKeyValue(
             ec, "PersonSecurity", Integer.valueOf(idStr));
         if (s == null) return status(404);

         ec.deleteObject(s);
         ec.saveChanges();
         return status(204);
     } catch (NumberFormatException e) {
         return error(400, "id must be an integer");
     } catch (Exception e) {
         return error(500, e.toString());
     }
 }


// the People section
 // ===== Person CRUD =====

 // GET /wa/listPersons
 public WOActionResults listPersonsAction() {
     if (!isAuthorized()) return unauthorized();
     try {
         EOEditingContext ec = ERXEC.newEditingContext();
         @SuppressWarnings("unchecked")
         NSArray<Person> people = (NSArray<Person>) ec.objectsWithFetchSpecification(
             new EOFetchSpecification("Person", null, null));
         StringBuilder sb = new StringBuilder("[");
         for (int i = 0; i < people.count(); i++) {
             if (i > 0) sb.append(',');
             sb.append(personToJson(people.objectAtIndex(i)));
         }
         sb.append(']');
         return json(sb.toString());
     } catch (Exception e) {
         return error(500, e.toString());
     }
 }

 // POST /wa/createPerson  body: { "firstName":"…", "lastName":"…", "loginName":"…",
//                                 "password":"…", "current":true, "administrator":false,
//                                 "personSecurity": null | { "id": N } }
 public WOActionResults createPersonAction() {
     if (!isAuthorized()) return unauthorized();
     try {
         JsonNode root = readJsonBody();
         if (root == null) return error(400, "Missing JSON body");

         EOEditingContext ec = ERXEC.newEditingContext();
         // init() has already set current=true, administrator=false,
         // creationDate=now, and created a default PersonSecurity for this person.
         Person p = (Person) EOUtilities.createAndInsertInstance(ec, "Person");

         // Only override what the client sent.
         if (root.has("firstName"))     p.setFirstName(root.path("firstName").asText(null));
         if (root.has("lastName"))      p.setLastName(root.path("lastName").asText(null));
         if (root.has("loginName"))     p.setLoginName(root.path("loginName").asText(null));
         if (root.has("password"))      p.setPassword(EltekUtilities.SHABase64String(root.path("password").asText(null)));
         if (root.has("current"))       p.setCurrent(root.path("current").asBoolean(true));
         if (root.has("administrator")) p.setAdministrator(root.path("administrator").asBoolean(false));

         // If client picked an EXISTING PersonSecurity, swap to it and delete the
         // auto-created one to avoid orphan rows.
			/*
			 * JsonNode psNode = root.path("personSecurity"); if (!psNode.isMissingNode() &&
			 * !psNode.isNull()) { JsonNode psId = psNode.path("id"); if
			 * (!psId.isMissingNode() && !psId.isNull()) { PersonSecurity picked =
			 * (PersonSecurity) EOUtilities.objectWithPrimaryKeyValue(ec, "PersonSecurity",
			 * psId.numberValue()); if (picked != null && picked != p.personSecurity()) {
			 * PersonSecurity autoCreated = p.personSecurity(); p.setPersonSecurity(picked);
			 * if (autoCreated != null) ec.deleteObject(autoCreated); } } }
			 */
         if (root.has("instruments")) syncInstruments(ec, p, root.path("instruments"));
         
         JsonNode psNode = root.path("personSecurity");
         if (!psNode.isMissingNode() && !psNode.isNull() && p.personSecurity() != null) {
             applySecurity(p.personSecurity(), psNode);
         }

         ec.saveChanges();
         return json(personToJson(p));
     } catch (Exception e) {
         return error(500, e.toString());
     }
 }


 // POST /wa/updatePerson  body: { "id": N, ... fields to change ... }
 public WOActionResults updatePersonAction() {
	    if (!isAuthorized()) return unauthorized();
	    try {
	        // Resolve actor from Basic Auth credentials for audit trail
	        String auth = request().headerForKey("authorization");
	        if (auth != null && auth.startsWith("Basic ")) {
	            String decoded = new String(
	                java.util.Base64.getDecoder().decode(auth.substring(6)));
	            String loginName = decoded.split(":", 2)[0];
	            EOEditingContext actorEc = ERXEC.newEditingContext();
	            Person actor = (Person) EOUtilities.objectMatchingKeyAndValue(
	                actorEc, "Person", "loginName", loginName);
	            ERCoreBusinessLogic.setActor(actor);
	        }

	        JsonNode root = readJsonBody();
	        if (root == null) return error(400, "Missing JSON body");
	        JsonNode idNode = root.path("id");
	        if (idNode.isMissingNode() || idNode.isNull()) return error(400, "Missing id");

	        EOEditingContext ec = ERXEC.newEditingContext();
	        Person p = (Person) EOUtilities.objectWithPrimaryKeyValue(ec, "Person", idNode.numberValue());
	        if (p == null) return status(404);

	        if (root.has("firstName"))     p.setFirstName(root.path("firstName").asText(null));
	        if (root.has("lastName"))      p.setLastName(root.path("lastName").asText(null));
	        if (root.has("loginName"))     p.setLoginName(root.path("loginName").asText(null));
	        if (root.has("current"))       p.setCurrent(root.path("current").asBoolean(true));
	        if (root.has("administrator")) p.setAdministrator(root.path("administrator").asBoolean(false));

	        if (root.has("instruments")) syncInstruments(ec, p, root.path("instruments"));

	        JsonNode pw = root.path("password");
	        if (!pw.isMissingNode() && !pw.isNull()) {
	            String s = pw.asText("");
	            if (!s.isEmpty()) {
	                p.setPassword(EltekUtilities.SHABase64String(s));
	            }
	        }

	        JsonNode psNode = root.path("personSecurity");
	        if (!psNode.isMissingNode() && !psNode.isNull()) {
	            if (p.personSecurity() == null) {
	                p.setPersonSecurity((PersonSecurity)
	                    EOUtilities.createAndInsertInstance(ec, "PersonSecurity"));
	            }
	            applySecurity(p.personSecurity(), psNode);
	        }

	        ec.saveChanges();
	        return json(personToJson(p));
	    } catch (Exception e) {
	        return error(500, e.toString());
	    } finally {
	        ERCoreBusinessLogic.setActor(null);
	    }
	}

 // POST /wa/deletePerson?id=N
 public WOActionResults deletePersonAction() {
     if (!isAuthorized()) return unauthorized();
     try {
         String idStr = request().stringFormValueForKey("id");
         if (idStr == null) return error(400, "Missing id");

         EOEditingContext ec = ERXEC.newEditingContext();
         Person p = (Person) EOUtilities.objectWithPrimaryKeyValue(ec, "Person", Integer.valueOf(idStr));
         if (p == null) return status(404);

         // If your EOModel doesn't cascade Person → PersonSecurity, delete the owned one too:
         PersonSecurity owned = p.personSecurity();
         if (owned != null) ec.deleteObject(owned);

         ec.deleteObject(p);
         ec.saveChanges();
         return status(204);
     } catch (NumberFormatException e) {
         return error(400, "id must be an integer");
     } catch (Exception e) {
         return error(500, e.toString());
     }
 }


    // And while you're at it, do the same to listEventsAction.
    // POST /wa/createBook  body: { "bookTitle": "...", "show": { "id": N } | null }
    public WOActionResults createBookAction() {
        if (!isAuthorized()) return unauthorized();
        try {
            JsonNode root = readJsonBody();
            if (root == null) return errorJSON(400, "Missing JSON body");

            EOEditingContext ec = ERXEC.newEditingContext();
            Book book = (Book) EOUtilities.createAndInsertInstance(ec, "Book");
            book.setBookTitle(root.path("bookTitle").asText(null));
            book.setShow(resolveShow(ec, root.path("show")));
            ec.saveChanges();

            return json(bookToJson(book));
        } catch (Exception e) {
            return errorJSON(500, e.getMessage());
        }
    }

    // POST /wa/updateBook  body: { "id": N, "bookTitle": "...", "show": { "id": N } | null }
    public WOActionResults updateBookAction() {
        if (!isAuthorized()) return unauthorized();
        try {
            JsonNode root = readJsonBody();
            if (root == null) return error(400, "Missing JSON body");
            JsonNode idNode = root.path("id");
            if (idNode.isMissingNode() || idNode.isNull()) return error(400, "Missing id");

            EOEditingContext ec = ERXEC.newEditingContext();
            Book book = (Book) EOUtilities.objectWithPrimaryKeyValue(ec, "Book", idNode.numberValue());
            if (book == null) return status(404);

            if (root.has("bookTitle")) book.setBookTitle(root.path("bookTitle").asText(null));
            if (root.has("show"))      book.setShow(resolveShow(ec, root.path("show")));

            ec.saveChanges();
            return json(bookToJson(book));
        } catch (Exception e) {
            return errorJSON(500, e.getMessage());
        }
    }

    // POST /wa/deleteBook?id=N
    public WOActionResults deleteBookAction() {
        if (!isAuthorized()) return unauthorized();
        try {
            String idStr = request().stringFormValueForKey("id");
            if (idStr == null) return errorJSON(400, "Missing id");

            EOEditingContext ec = ERXEC.newEditingContext();
            Book book = (Book) EOUtilities.objectWithPrimaryKeyValue(ec, "Book", Integer.valueOf(idStr));
            if (book == null) return status(404);

            ec.deleteObject(book);
            ec.saveChanges();
            return status(204);
        } catch (NumberFormatException e) {
            return errorJSON(400, "id must be an integer");
        } catch (Exception e) {
            return errorJSON(500, e.getMessage());
        }
    }

 // GET /wa/listEvents
    public WOActionResults listEventsAction() {
        if (!isAuthorized()) return unauthorized();
        EOEditingContext ec = ERXEC.newEditingContext();
        @SuppressWarnings("unchecked")
        NSArray<Event> events = (NSArray<Event>) ec.objectsWithFetchSpecification(
            new EOFetchSpecification("Event", null, null));

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < events.count(); i++) {
            if (i > 0) sb.append(',');
            sb.append(eventToJson(events.objectAtIndex(i)));
        }
        sb.append(']');
        return json(sb.toString());
    }

    // POST /wa/createEvent  body: { "eventDate": "2026-05-15T10:30:00Z" }
    public WOActionResults createEventAction() {
        if (!isAuthorized()) return unauthorized();
        try {
            JsonNode root = readJsonBody();
            if (root == null) return error(400, "Missing JSON body");

            EOEditingContext ec = ERXEC.newEditingContext();
            Event event = (Event) EOUtilities.createAndInsertInstance(ec, "Event");
            event.setEventDate(parseISODate(root.path("eventDate").asText(null)));
            ec.saveChanges();
            return json(eventToJson(event));
        } catch (Exception e) {
            return error(500, e.getMessage());
        }
    }

 // POST /wa/updateEvent  body: { "id": N, "eventDate": "..." }
    public WOActionResults updateEventAction() {
        if (!isAuthorized()) return unauthorized();
        try {
            setActorFromRequest();

            JsonNode root = readJsonBody();
            if (root == null) return error(400, "Missing JSON body");
            JsonNode idNode = root.path("id");
            if (idNode.isMissingNode() || idNode.isNull()) return error(400, "Missing id");

            EOEditingContext ec = ERXEC.newEditingContext();
            Event event = (Event) EOUtilities.objectWithPrimaryKeyValue(ec, "Event", idNode.numberValue());
            if (event == null) return status(404);

            if (root.has("eventDate")) event.setEventDate(parseISODate(root.path("eventDate").asText(null)));
            ec.saveChanges();
            return json(eventToJson(event));
        } catch (Exception e) {
            return error(500, e.getMessage());
        } finally {
            ERCoreBusinessLogic.setActor(null);
        }
    }

    // POST /wa/deleteEvent?id=N
    public WOActionResults deleteEventAction() {
        if (!isAuthorized()) return unauthorized();
        try {
            String idStr = request().stringFormValueForKey("id");
            if (idStr == null) return error(400, "Missing id");
            EOEditingContext ec = ERXEC.newEditingContext();
            Event event = (Event) EOUtilities.objectWithPrimaryKeyValue(ec, "Event", Integer.valueOf(idStr));
            if (event == null) return status(404);
            ec.deleteObject(event);
            ec.saveChanges();
            return status(204);
        } catch (NumberFormatException e) {
            return error(400, "id must be an integer");
        } catch (Exception e) {
            return error(500, e.getMessage());
        }
    }
    
 // GET /wa/listShows
    public WOActionResults listShowsAction() {
        if (!isAuthorized()) return unauthorized();
        EOEditingContext ec = ERXEC.newEditingContext();
        @SuppressWarnings("unchecked")
        NSArray<Show> shows = (NSArray<Show>) ec.objectsWithFetchSpecification(
            new EOFetchSpecification("Show", null, null));

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < shows.count(); i++) {
            if (i > 0) sb.append(',');
            sb.append(showToJson(shows.objectAtIndex(i)));
        }
        sb.append(']');
        return json(sb.toString());
    }

    // POST /wa/createShow  body: { "showName": "...", "events": [ { "id": N }, ... ] }
    public WOActionResults createShowAction() {
        if (!isAuthorized()) return unauthorized();
        try {
            JsonNode root = readJsonBody();
            if (root == null) return error(400, "Missing JSON body");

            EOEditingContext ec = ERXEC.newEditingContext();
            Show show = (Show) EOUtilities.createAndInsertInstance(ec, "Show");
            show.setShowName(root.path("showName").asText(null));
            show.setRunning(root.path("running").asBoolean(false));
            syncEvents(ec, show, root.path("events"));
            ec.saveChanges();
            return json(showToJson(show));
        } catch (Exception e) {
            return error(500, e.getMessage());
        }
    }

    // POST /wa/updateShow  body: { "id": N, "showName": "...", "events": [...] }
    public WOActionResults updateShowAction() {
        if (!isAuthorized()) return unauthorized();
        try {
            setActorFromRequest();
            JsonNode root = readJsonBody();
            if (root == null) return error(400, "Missing JSON body");
            JsonNode idNode = root.path("id");
            if (idNode.isMissingNode() || idNode.isNull()) return error(400, "Missing id");
            EOEditingContext ec = ERXEC.newEditingContext();
            Show show = (Show) EOUtilities.objectWithPrimaryKeyValue(ec, "Show", idNode.numberValue());
            if (show == null) return status(404);
            if (root.has("showName")) show.setShowName(root.path("showName").asText(null));
            if (root.has("running"))  show.setRunning(root.path("running").asBoolean(false));
            if (root.has("events"))   syncEvents(ec, show, root.path("events"));
            ec.saveChanges();
            return json(showToJson(show));
        } catch (Exception e) {
            return error(500, e.getMessage());
        } finally {
            ERCoreBusinessLogic.setActor(null);
        }
    }

    // POST /wa/deleteShow?id=N
    public WOActionResults deleteShowAction() {
        if (!isAuthorized()) return unauthorized();
        try {
            String idStr = request().stringFormValueForKey("id");
            if (idStr == null) return error(400, "Missing id");
            EOEditingContext ec = ERXEC.newEditingContext();
            Show show = (Show) EOUtilities.objectWithPrimaryKeyValue(ec, "Show", Integer.valueOf(idStr));
            if (show == null) return status(404);
            ec.deleteObject(show);
            ec.saveChanges();
            return status(204);
        } catch (NumberFormatException e) {
            return error(400, "id must be an integer");
        } catch (Exception e) {
            return error(500, e.getMessage());
        }
    }

    private void syncEvents(EOEditingContext ec, Show show, JsonNode eventsNode) {
        if (eventsNode == null || eventsNode.isMissingNode() || eventsNode.isNull() || !eventsNode.isArray()) {
            return;   // leave events alone if the key is missing/null
        }

        // Collect desired ids
        java.util.Set<Object> wanted = new java.util.HashSet<>();
        for (JsonNode en : eventsNode) {
            JsonNode idNode = en.path("id");
            if (!idNode.isMissingNode() && !idNode.isNull()) wanted.add(idNode.numberValue());
        }

        // Remove events that are no longer wanted (snapshot to avoid concurrent modification)
        NSMutableArray<Event> current = new NSMutableArray<>(show.events());
        for (Event e : current) {
            Object pk = ERXEOControlUtilities.primaryKeyObjectForObject(e);
            if (!wanted.contains(pk)) {
                show.removeObjectFromBothSidesOfRelationshipWithKey(e, "events");
            }
        }

        // Add newly wanted events
        java.util.Set<Object> currentIds = new java.util.HashSet<>();
        for (Event e : show.events()) {
            currentIds.add(ERXEOControlUtilities.primaryKeyObjectForObject(e));
        }
        for (Object id : wanted) {
            if (currentIds.contains(id)) continue;
            Event event = (Event) EOUtilities.objectWithPrimaryKeyValue(ec, "Event", id);
            if (event != null) {
                show.addObjectToBothSidesOfRelationshipWithKey(event, "events");
            }
        }
    }

    private String showToJson(Show s) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"id\":").append(ERXEOControlUtilities.primaryKeyObjectForObject(s))
          .append(",\"showName\":").append(jsonString(s.showName()))
          .append(",\"running\":").append(Boolean.TRUE.equals(s.running()))
          .append(",\"events\":[");
        NSArray<Event> events = s.events();
        if (events != null) {
            for (int i = 0; i < events.count(); i++) {
                if (i > 0) sb.append(',');
                sb.append(eventToJson(events.objectAtIndex(i)));
            }
        }
        sb.append("]}");
        return sb.toString();
    }
    
    // ===== Helpers =====

    private JsonNode readJsonBody() throws Exception {
        String body = request().contentString();
        if (body == null || body.isEmpty()) return null;
        return MAPPER.readTree(body);
    }

    private Show resolveShow(EOEditingContext ec, JsonNode showNode) {
        if (showNode == null || showNode.isNull() || showNode.isMissingNode()) return null;
        JsonNode idNode = showNode.path("id");
        if (idNode.isMissingNode() || idNode.isNull()) return null;
        return (Show) EOUtilities.objectWithPrimaryKeyValue(ec, "Show", idNode.numberValue());
    }

    private String bookToJson(Book b) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"id\":").append(ERXEOControlUtilities.primaryKeyObjectForObject(b))
          .append(",\"bookTitle\":").append(jsonString(b.bookTitle()))
          .append(",\"show\":");
        Show s = b.show();
        if (s != null) {
            sb.append("{")
              .append("\"id\":").append(ERXEOControlUtilities.primaryKeyObjectForObject(s))
              .append(",\"showName\":").append(jsonString(s.showName()))
              .append("}");
        } else {
            sb.append("null");
        }
        sb.append("}");
        return sb.toString();
    }

    private static String jsonString(String s) {
        if (s == null) return "null";
        StringBuilder out = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
            }
        }
        out.append('"');
        return out.toString();
    }
    
    private String securityToJson(PersonSecurity s) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"id\":").append(ERXEOControlUtilities.primaryKeyObjectForObject(s))
          .append(",\"canAssignPlayer\":")    .append(Boolean.TRUE.equals(s.canAssignPlayer()))
          .append(",\"canManageBook\":")      .append(Boolean.TRUE.equals(s.canManageBook()))
          .append(",\"canManageEvent\":")     .append(Boolean.TRUE.equals(s.canManageEvent()))
          .append(",\"canManageInstrument\":").append(Boolean.TRUE.equals(s.canManageInstrument()))
          .append(",\"canManagePeople\":")    .append(Boolean.TRUE.equals(s.canManagePeople()))
          .append(",\"canManageShow\":")      .append(Boolean.TRUE.equals(s.canManageShow()))
          .append("}");
        return sb.toString();
    }
    
    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
    

    private WOResponse status(int code) {
        WOResponse r = new WOResponse();
        r.setStatus(code);
        return r;
    }

    private WOResponse error(int code, String msg) {
        WOResponse r = new WOResponse();
        r.setStatus(code);
        r.setHeader("application/json; charset=utf-8", "content-type");
        r.setContent("{\"error\":" + jsonString(msg) + "}");
        return r;
    }

    private static NSTimestamp parseISODate(String s) {
        if (s == null) return null;
        return new NSTimestamp(Instant.parse(s).toEpochMilli());
    }

    private static String isoDate(java.util.Date d) {
        if (d == null) return null;
        return Instant.ofEpochMilli(d.getTime()).toString();   // "2026-05-15T10:30:00Z"
    }
    
    // ----- helpers -----

    private String instrumentsToJsonArray(NSArray<Instrument> rows) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < rows.count(); i++) {
            if (i > 0) sb.append(',');
            sb.append(instrumentToJson(rows.objectAtIndex(i)));
        }
        sb.append(']');
        return sb.toString();
    }

    private String instrumentToJson(Instrument i) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"id\":").append(ERXEOControlUtilities.primaryKeyObjectForObject(i))
          .append(",\"instrumentName\":").append(jsonString(i.instrumentName()))
          .append(",\"available\":").append(Boolean.TRUE.equals(i.available()))
          .append("}");
        return sb.toString();
    }

    private int intParam(String name, int def, int min, int max) {
        try {
            int v = Integer.parseInt(request().stringFormValueForKey(name));
            if (v < min) v = min;
            if (v > max) v = max;
            return v;
        } catch (Exception e) {
            return def;
        }
    }

    private EOQualifier qualifierFromRequest(WORequest req, String entityName) {
        // Option A: accept raw qualifier format in q param (use with caution)
        String q = req.stringFormValueForKey("q");
        if (q != null && !q.isEmpty()) {
            return EOQualifier.qualifierWithQualifierFormat(q, null);
        }
        // Option B: build qualifier from well-known params (safe)
        // Example: ?name=Foo&status=Active
        NSMutableArray<EOQualifier> parts = new NSMutableArray<>();
        String name = req.stringFormValueForKey("name");
        if (name != null && !name.isEmpty()) {
            parts.addObject(EOQualifier.qualifierWithQualifierFormat("name = %@", new NSArray<>(name)));
        }
        String status = req.stringFormValueForKey("status");
        if (status != null && !status.isEmpty()) {
            parts.addObject(EOQualifier.qualifierWithQualifierFormat("status = %@", new NSArray<>(status)));
        }
        if (parts.count() == 0) return null;
        return new EOAndQualifier(parts);
    }

    private NSArray<EOSortOrdering> sortOrderingsFromRequest(WORequest req, String defaultKey) {
        String sortKey = req.stringFormValueForKey("sort");
        if (sortKey == null || sortKey.isEmpty()) sortKey = defaultKey;

        String order = req.stringFormValueForKey("order");
        boolean descending = "desc".equalsIgnoreCase(order);

        EOSortOrdering sortOrdering = EOSortOrdering.sortOrderingWithKey(
            sortKey,
            descending
                ? EOSortOrdering.CompareCaseInsensitiveDescending
                : EOSortOrdering.CompareCaseInsensitiveAscending
        );
        NSLog.out.appendln("sortOrderings = " + sortOrdering);
        return new NSArray<>(sortOrdering);
    }

    private NSDictionary<String, Object> safeDict(EOEnterpriseObject eo, NSArray<String> keys) {
        NSMutableDictionary<String, Object> d = new NSMutableDictionary<>();
        for (String k : keys) {
            d.setObjectForKey(eo.valueForKey(k), k);
        }
        return d.immutableClone();
    }

    private WOActionResults jsonResponse(int status, Object obj) {
        // Prefer ERXJSONUtilities if available:
        // String json = ERXJSONUtilities.toJSONString(obj);
        String json = toJSON(obj); // fallback minimal encoder

        WOResponse response = ERXApplication.erxApplication().createResponseInContext(context());
        response.setStatus(status);
        response.setHeader("application/json; charset=utf-8", "Content-Type");
        response.setHeader("no-cache", "Cache-Control");
        response.appendContentString(json);
        return response;
    }

    private WOActionResults errorJSON(int status, String message) {
        NSDictionary<String, Object> err = new NSDictionary<>(
            new Object[] { status, message },
            new String[] { "status", "message" }
        );
        return jsonResponse(status, err);
    }

    // Minimal JSON encoder for NSDictionary/NSArray/String/Number/Boolean/null
    @SuppressWarnings("unchecked")
    private String toJSON(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof String) return "\"" + escape((String) obj) + "\"";
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();
        if (obj instanceof NSDictionary) {
            NSDictionary<Object, Object> d = (NSDictionary<Object, Object>) obj;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Object key : d.allKeys()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(escape(String.valueOf(key))).append("\":")
                  .append(toJSON(d.objectForKey(key)));
            }
            return sb.append("}").toString();
        }
        if (obj instanceof NSArray) {
            NSArray<Object> a = (NSArray<Object>) obj;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < a.count(); i++) {
                if (i > 0) sb.append(",");
                sb.append(toJSON(a.objectAtIndex(i)));
            }
            return sb.append("]").toString();
        }
        return "\"" + escape(String.valueOf(obj)) + "\"";
    }


    private boolean isAuthorized() {
        String header = request().headerForKey("authorization");
        if (header == null || !header.toLowerCase().startsWith("basic ")) return false;
        try {
            String decoded = new String(Base64.getDecoder().decode(header.substring(6).trim()));
            int colon = decoded.indexOf(':');
            if (colon < 0) return false;
            String user = decoded.substring(0, colon);
            String pass = decoded.substring(colon + 1);
            EOEditingContext ec = ERXEC.newEditingContext();
            try {
                Person.validateLogin(ec, user, pass);
                return true;
            } catch (Exception e) {
                return false;
            } finally {
                ec.dispose();
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private WOResponse unauthorized() {
        WOResponse r = new WOResponse();
        r.setStatus(401);
        r.setHeader("Basic realm=\"MyBook\"", "WWW-Authenticate");
        r.setContent("Unauthorized");
        return r;
    }

    private WOResponse json(String body) {
        WOResponse r = new WOResponse();
        r.setHeader("application/json; charset=utf-8", "content-type");
        r.setContent(body);
        return r;
    }
    
    protected void setActorFromRequest() {
        String auth = request().headerForKey("authorization");
        if (auth != null && auth.startsWith("Basic ")) {
            String decoded = new String(
                java.util.Base64.getDecoder().decode(auth.substring(6)));
            String loginName = decoded.split(":", 2)[0];
            EOEditingContext actorEc = ERXEC.newEditingContext();
            Person actor = (Person) EOUtilities.objectMatchingKeyAndValue(
                actorEc, "Person", "loginName", loginName);
            ERCoreBusinessLogic.setActor(actor);
        }
    }
    // Helper — applies the six boolean fields when present in the JSON.
    private void applySecurity(PersonSecurity s, JsonNode root) {
        if (root.has("canAssignPlayer"))     s.setCanAssignPlayer(root.path("canAssignPlayer").asBoolean(false));
        if (root.has("canManageBook"))       s.setCanManageBook(root.path("canManageBook").asBoolean(false));
        if (root.has("canManageEvent"))      s.setCanManageEvent(root.path("canManageEvent").asBoolean(false));
        if (root.has("canManageInstrument")) s.setCanManageInstrument(root.path("canManageInstrument").asBoolean(false));
        if (root.has("canManagePeople"))     s.setCanManagePeople(root.path("canManagePeople").asBoolean(false));
        if (root.has("canManageShow"))       s.setCanManageShow(root.path("canManageShow").asBoolean(false));
    }
       public WOActionResults listBooksAction() {
           if (!isAuthorized()) return unauthorized();
           try {
               EOEditingContext ec = ERXEC.newEditingContext();
               @SuppressWarnings("unchecked")
               NSArray<Book> books = (NSArray<Book>) ec.objectsWithFetchSpecification(
                   new EOFetchSpecification("Book", null, null));
               StringBuilder sb = new StringBuilder("[");
               for (int i = 0; i < books.count(); i++) {
                   if (i > 0) sb.append(',');
                   sb.append(bookToJson(books.objectAtIndex(i)));
               }
               sb.append(']');
               return json(sb.toString());
           } catch (Exception e) {
               return error(500, e.toString());      // include class name + message
           }
       }
       private void syncInstruments(EOEditingContext ec, Person p, JsonNode instrumentsNode) {
   	    if (instrumentsNode == null || !instrumentsNode.isArray()) return;

   	    // Desired Instrument ids
   	    java.util.Set<Object> wanted = new java.util.HashSet<>();
   	    for (JsonNode n : instrumentsNode) {
   	        JsonNode idNode = n.path("id");
   	        if (!idNode.isMissingNode() && !idNode.isNull()) wanted.add(idNode.numberValue());
   	    }

   	    // Existing PersonInstrument rows on this Person
   	    NSArray<PersonInstrument> current = p.personInstruments();   // adjust if relationship is named differently
   	    NSMutableArray<PersonInstrument> toRemove = new NSMutableArray<>();
   	    java.util.Set<Object> currentInstrumentIds = new java.util.HashSet<>();

   	    for (PersonInstrument pi : current) {
   	        Instrument inst = pi.instrument();
   	        if (inst == null) { toRemove.add(pi); continue; }
   	        Object instId = ERXEOControlUtilities.primaryKeyObjectForObject(inst);
   	        if (!wanted.contains(instId)) {
   	            toRemove.add(pi);
   	        } else {
   	            currentInstrumentIds.add(instId);
   	        }
   	    }
   	    for (PersonInstrument pi : toRemove) {
   	        p.removeObjectFromBothSidesOfRelationshipWithKey(pi, "personInstruments");
   	        ec.deleteObject(pi);
   	    }

   	    // Add new PersonInstrument rows for instruments not yet linked
   	    for (Object instId : wanted) {
   	        if (currentInstrumentIds.contains(instId)) continue;
   	        Instrument inst = (Instrument) EOUtilities.objectWithPrimaryKeyValue(ec, "Instrument", instId);
   	        if (inst == null) continue;
   	        PersonInstrument pi = (PersonInstrument) EOUtilities.createAndInsertInstance(ec, "PersonInstrument");
   	        pi.setPerson(p);
   	        pi.setInstrument(inst);
   	    }
   	}
       // ===== Helpers (add once if not already present) =====

       private PersonSecurity resolveSecurity(EOEditingContext ec, JsonNode node) {
           if (node == null || node.isNull() || node.isMissingNode()) return null;
           JsonNode idNode = node.path("id");
           if (idNode.isMissingNode() || idNode.isNull()) return null;
           return (PersonSecurity) EOUtilities.objectWithPrimaryKeyValue(
               ec, "PersonSecurity", idNode.numberValue());
       }
       
       private String personToJson(Person p) {
      	    StringBuilder sb = new StringBuilder("{");
      	    sb.append("\"id\":").append(ERXEOControlUtilities.primaryKeyObjectForObject(p))
      	      .append(",\"firstName\":").append(jsonString(p.firstName()))
      	      .append(",\"lastName\":").append(jsonString(p.lastName()))
      	      .append(",\"loginName\":").append(jsonString(p.loginName()))
      	      .append(",\"current\":").append(Boolean.TRUE.equals(p.current()))
      	      .append(",\"administrator\":").append(Boolean.TRUE.equals(p.administrator()))
      	      .append(",\"creationDate\":").append(jsonString(isoDate(p.creationDate())))
      	      .append(",\"personSecurity\":");
      	    PersonSecurity s = p.personSecurity();
      	    if (s != null) sb.append(securityToJson(s)); else sb.append("null");

      	    sb.append(",\"instruments\":[");
      	    NSArray<PersonInstrument> pis = p.personInstruments();
      	    if (pis != null) {
      	        boolean first = true;
      	        for (PersonInstrument pi : pis) {
      	            Instrument inst = pi.instrument();
      	            if (inst == null) continue;
      	            if (!first) sb.append(',');
      	            sb.append(instrumentToJson(inst));
      	            first = false;
      	        }
      	    }
      	    sb.append("]}");
      	    return sb.toString();
      	}
     
       private String eventToJson(Event e) {
           StringBuilder sb = new StringBuilder("{");
           sb.append("\"id\":").append(ERXEOControlUtilities.primaryKeyObjectForObject(e))
             .append(",\"eventDate\":").append(jsonString(isoDate(e.eventDate())))
             .append("}");
           return sb.toString();
       } 
    
       
}
