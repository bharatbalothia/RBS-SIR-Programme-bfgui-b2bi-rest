package com.stercomm.customers.rbs.sir.rest.server;

import java.io.IOException;

import com.sterlingcommerce.woodstock.workflow.WorkFlowContext;
import com.sterlingcommerce.woodstock.workflow.Document;
import com.sterlingcommerce.woodstock.workflow.InitialWorkFlowContext;
import com.sterlingcommerce.woodstock.workflow.InitialWorkFlowContextException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.annotation.PostConstruct;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.ObjectMapper;

import com.stercomm.customers.rbs.sir.rest.domain.Error;
import com.stercomm.customers.rbs.sir.rest.domain.Errors;
import com.stercomm.customers.rbs.sir.rest.domain.RoutingRule;
import com.stercomm.customers.rbs.sir.rest.exception.CreateDirectoryException;
import com.stercomm.customers.rbs.sir.rest.util.SRRCreateLog;
import com.stercomm.customers.rbs.sir.rest.util.SRRCreator;
import com.stercomm.customers.rbs.sir.rest.util.Utils;

@Path("/rules")
public class RoutingRulesRestServer {

	private static List<RoutingRule> rules = new ArrayList<RoutingRule>();

	private static Logger LOGGER = Logger.getLogger(RoutingRulesRestServer.class.getName());
	private static final String FORMAT_STRING = "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %2$s %5$s%6$s%n";

	@PostConstruct
	private void init() {

		try {
			boolean logToConsole = true;
			LOGGER = setupLogging(logToConsole, System.getProperty("user.home") + "/bfgui-rest.log");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<RoutingRule> getRules() {
		LOGGER.info("Got request for GET on rules");

		return rules;

	}
	// comment

	@POST
	@Path("/create")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({ "application/json" })
	public Response postRuleRecord(RoutingRule rule) {

		int statusOKSC = 201;
		int validationFailureSC = 404;
		int status = statusOKSC;

		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

		final Errors list = new Errors();
		final List<Error> errs = new ArrayList<Error>();
		validatePost(validator, errs, rule);

		// do we have any errors?
		if (errs.size() > 0) {
			list.setErrors(errs);
		}

		// no, just add the rule and return 201 and no body, the caller doesnt need it
		if (list.size() == 0) {
			LOGGER.info("Rule passed validation.");
			rules.add(rule);

			SRRCreator creator = new SRRCreator(rule);
			creator.execute();
			List<SRRCreateLog> logs = creator.getLogOfCreateAttempts();

			int finalStatus = getStatus(logs);
			
			// try to create the directory
			
			try {
				createSWIFTDirectory(rule);
			}
			catch (CreateDirectoryException cde) {
				
				LOGGER.severe(cde.getMessage());
			}
			
			if (finalStatus == statusOKSC) {
		
		
				return Response.status(finalStatus).entity(null).build();
			} else {
				
				// we got some errors back when we tried to create at least some of the rules
				Errors createErrs = new Errors();
				List<Error> eList = new ArrayList<Error>();
				logs.forEach(log -> {
					Error e=new Error();
					e.setMessage(log.getFailCause());
					e.setAttribute(log.getEntityName());
					eList.add(e);
					});
				createErrs.setErrors(eList);
				LOGGER.info("Returning some errors : " + eList);
				return Response.status(finalStatus).entity(createErrs).build();
			}

			// yes, there was at least one basic type validation error so create a JSON response back
		} else {
			status = validationFailureSC;
			Error[] ar = errs.toArray(new Error[list.size()]);
//			
			return Response.status(status).entity(toErrorResp(list)).build();
		}

	}

	private int getStatus(List<SRRCreateLog> logs) {

		int statusOKSC = 201;
		int createFailureSC = 404;

		int retval = statusOKSC;

		// do any of the create logs have an error?

		for (SRRCreateLog srrCreateLog : logs) {
			LOGGER.info("Log : " + srrCreateLog);
			retval = srrCreateLog.isSuccessOnCreate() ? statusOKSC : createFailureSC;

		}
		LOGGER.info("Returning status : " + retval);

		return retval;

	}

	private List<Error> validatePost(Validator val, List<Error> errs, RoutingRule rule) {

		LOGGER.info("Validating : " + rule);
		val.validate(rule).stream().forEach(violation -> {

			String message = violation.getMessage();
			String attr = violation.getPropertyPath().toString();
			LOGGER.severe(attr + " : " + message);
			Error e = new Error();
			e.setAttribute(attr);
			e.setMessage(message);
			errs.add(e);
		});

		return errs;
	}

	private String toErrorResp(Errors list) {

		String json = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			json = mapper.writeValueAsString(list);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;

	}

	private Logger setupLogging(boolean logToConsole, String logFile) throws Exception {
		// Setup the logging
		System.setProperty("java.util.logging.SimpleFormatter.format", FORMAT_STRING);
		LogManager.getLogManager().reset();
		Logger thisLogger = Logger.getLogger(this.getClass().getName());
		if (logFile != null) {
			FileHandler logHandler = new FileHandler(logFile, 8 * 1024 * 1024, 2, true);
			logHandler.setFormatter(new SimpleFormatter());
			logHandler.setLevel(Level.FINEST);
			thisLogger.addHandler(logHandler);
		}

		if (logToConsole) {
			ConsoleHandler consoleHandler = new ConsoleHandler();
			consoleHandler.setFormatter(new SimpleFormatter());
			consoleHandler.setLevel(Level.INFO);
			thisLogger.addHandler(consoleHandler);
		}

		thisLogger.setLevel(Level.INFO);
		return thisLogger;
	}
	
	public void createSWIFTDirectory(RoutingRule rule) throws CreateDirectoryException{
		
		String reqDN = rule.getRequestorDN();
		String respDN = rule.getResponderDN();
		
		String dirToCreate = Utils.createSWIFTDirectoryPath(reqDN, respDN);
		
		try {
			InitialWorkFlowContext iwfc = new InitialWorkFlowContext();
			WorkFlowContext wfc = new WorkFlowContext();
			Document primaryDocumentOutput = wfc.newDocument();
			
			// how do i set name/values. xml etc in the primary?
			// primaryDocumentOutput. ???
	
			 wfc.setWFContent("SWIFTDirectory",dirToCreate);
			 iwfc.putPrimaryDocument(primaryDocumentOutput);
		
			 iwfc.setWorkFlowName("HelloWorld"); 
			 iwfc.start();
		}
		catch (InitialWorkFlowContextException iwcf) {
			
			throw new CreateDirectoryException("Unable to create SWIFT Dir "+ dirToCreate+" : "+iwcf.getMessage());
		}
		
		
	}

}
