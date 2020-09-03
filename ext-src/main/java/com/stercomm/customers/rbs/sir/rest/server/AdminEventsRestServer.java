package com.stercomm.customers.rbs.sir.rest.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.stercomm.customers.rbs.sir.rest.domain.Event;
import com.stercomm.customers.rbs.sir.rest.error.Error;
import com.stercomm.customers.rbs.sir.rest.error.Errors;
import com.sterlingcommerce.woodstock.dmi.visibility.event.DmiVisEventFactory;
import com.sterlingcommerce.woodstock.event.ExceptionLevel;
import com.sterlingcommerce.woodstock.event.InvalidEventException;
import com.sterlingcommerce.woodstock.util.frame.Manager;

import java.util.UUID;


@Path("/events")
public class AdminEventsRestServer extends BaseRestServer {

	private static Logger LOGGER = Logger.getLogger(TransactionsRestServer.class.getName());

	@PostConstruct
	private void init() {
		try {
			boolean logToConsole = true;
			String logPath = Manager.getProperties("bfgui").getProperty("log.path.dir");
			String logName = Manager.getProperties("bfgui").getProperty("log.path.events.filename");
			String fullPath = logPath + File.separator + logName;
			LOGGER = setupLogging(logToConsole, fullPath);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response postEvent(Event event) {

		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

		// what we return if the post failed to validate
		final Errors list = new Errors();
		final List<Error> errs = new ArrayList<Error>();
		validatePost(validator, errs, event);

		// if we have any validation errors, get out right now, and send a 400
		if (errs.size() > 0) {

			list.setErrors(errs);
			return Response.status(Status.BAD_REQUEST).entity(errs).build();
		}

		// we have a valid event object
		
		
		
		Error err = null;
		try{
			
			
			DmiVisEventFactory.fireAdminAuditEvent(6, //event id
				ExceptionLevel.NORMAL, //level
				UUID.randomUUID().toString(), //just an ID
				System.currentTimeMillis(),
				event.getActionBy(),	//who made the change
				event.getType() + ": "+event.getActionType().toUpperCase(),// to what e.g. Entity or TC
				"Change " + event.getEventType(),
				event.getChangeID(),	// the id for the record from the contributing system
				event.getActionValue()); // e.g. the entity id or the trusted cert name
		}
		catch(InvalidEventException ie) {
			
			err = new Error();
			err.setAttribute("event");
			err.setMessage("Invalid event : " + ie.getMessage());
			
		}
		
		if (null == err) {
			return Response.status(Status.OK).entity(null).build();
		}
		else {
			LOGGER.info("Event posted to BI : " + event);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build();
		}
		
	}

	private void validatePost(Validator val, List<Error> errs, Event e) {

		LOGGER.info("Validating : " + e);
		val.validate(e).stream().forEach(violation -> {

			String message = violation.getMessage();
			String attr = violation.getPropertyPath().toString();
			LOGGER.severe(attr + " : " + message);
			Error err = new Error();
			err.setAttribute(attr);
			err.setMessage(message);
			errs.add(err);
		});

	}

}
