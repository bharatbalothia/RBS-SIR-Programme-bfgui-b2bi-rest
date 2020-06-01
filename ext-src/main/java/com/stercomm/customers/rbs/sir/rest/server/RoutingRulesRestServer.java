package com.stercomm.customers.rbs.sir.rest.server;

import java.io.IOException;
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
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.map.ObjectMapper;

import com.stercomm.customers.rbs.sir.rest.domain.Error;
import com.stercomm.customers.rbs.sir.rest.domain.Errors;
import com.stercomm.customers.rbs.sir.rest.domain.RoutingRule;
import com.stercomm.customers.rbs.sir.rest.domain.SWIFTRoutingRule;
import com.stercomm.customers.rbs.sir.rest.exception.CreateDirectoryException;
import com.stercomm.customers.rbs.sir.rest.util.SRRCreateLog;
import com.stercomm.customers.rbs.sir.rest.util.SRRCreateLogs;
import com.stercomm.customers.rbs.sir.rest.util.SRRValidator;
import com.stercomm.customers.rbs.sir.rest.util.Utils;
import com.sterlingcommerce.woodstock.ui.SWIFTNetRoutingRuleObj;

@Path("/rules")
public class RoutingRulesRestServer {

	// we can construct 1..n SWIFT routing rules from a single POST
	private static List<RoutingRule> rules = new ArrayList<RoutingRule>();

	private static Logger LOGGER = Logger.getLogger(RoutingRulesRestServer.class.getName());
	private static final String FORMAT_STRING = "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %2$s %5$s%6$s%n";

	@PostConstruct
	private void init() {

		try {
			boolean logToConsole = true;
			LOGGER = setupLogging(logToConsole, System.getProperty("user.home") + "/bfgui-rest-routingrule.log");
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
	public Response createSWIFTRulesFromRule(RoutingRule rule) {

		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

		// what we return if the post failed to validate
		final Errors list = new Errors();
		final List<Error> errs = new ArrayList<Error>();
		validatePost(validator, errs, rule);

		// if we have any validation errors, get out right now, and send a 400
		if (errs.size() > 0) {

			list.setErrors(errs);
			return Response.status(Status.BAD_REQUEST).entity(errs).build();
		}

		// we got here, so we were able to validate the rule against its type
		LOGGER.info("Rule passed validation.");
		
		// what we return to the client if the post validated,
		// with the result of our attempt to create the SRRs in BI
		SRRCreateLogs createLogs = new SRRCreateLogs();

		// ok, so how many possible SRRs are there to be created from this rule?
		// the same number as the array size of the rule request

		int numOfSRRs = rule.getRequestType().length;
		LOGGER.info("Looks like " + numOfSRRs + " in this rule, starting to build it/them.");

		List<SWIFTNetRoutingRuleObj> candidateRules = new ArrayList<SWIFTNetRoutingRuleObj>(numOfSRRs);

		// iterate the list create the BI SRR objects and add them to the candidate list

		for (int i = 0; i < numOfSRRs; i++) {

			LOGGER.info("Creating SSR for " + rule.getEntityName() + ", request type : " + rule.getRequestType()[i]);
			SWIFTNetRoutingRuleObj swiftRule = new SWIFTRoutingRule.Builder()
					.withActionType("BP")
					.withService(rule.getService())
					.withInvokeMode("SYNC")
					.withRequestType(rule.getRequestType()[i])
					.withWorkflowName(rule.getRequestType()[i])
					.withRequestorDN(rule.getRequestorDN())
					.withResponderDN(rule.getResponderDN())
					.toRouteName(rule.getEntityName(), rule.getRequestType()[i])
					.build();
			candidateRules.add(swiftRule);
		}

		// iterate and parse out the SRRs  that can't be created for business logic
		// reasons - e.g the rule already exists
		// pass in the logs collection by ref in case the validation wants to contribute
		List<SWIFTNetRoutingRuleObj> validatedSRRs = new SRRValidator().validatedList(candidateRules, createLogs);

		// now we can try to save the ones that survived...
		
		int failCount = 0;
		
		for (SWIFTNetRoutingRuleObj swiftNetRoutingRuleObj : validatedSRRs) {
			
			String rn = swiftNetRoutingRuleObj.getRouteName();
			LOGGER.info("Trying to save SRR : " + swiftNetRoutingRuleObj.getRouteName());
			SRRCreateLog log = new SRRCreateLog();		
			log.setRouteName(rn);
			try {
				boolean b = swiftNetRoutingRuleObj.saveSWIFTNetRoutingRule(SWIFTNetRoutingRuleObj.INSERT_ACTION);
				// we'd expect b to be true here, so let's set it in the log
				log.setSuccessOnCreate(b);
				log.setCode(201);
				log.setFailCause(""); // to do..can we suppress this attribute in the JSON where b==true?
				//LOGGER.info("Adding the log 1 in iterate cands");
				
				LOGGER.info("Created SRR with object ID : " + swiftNetRoutingRuleObj.getRouteName());
			} catch (Exception e) {
				
				// something unfortunate happened here
				LOGGER.severe("Exception trying to save rule : " + swiftNetRoutingRuleObj.getRouteName());
				LOGGER.severe(e.getMessage());
				
				//..and false here, so we can set a fail message
				log.setSuccessOnCreate(false);
				log.setFailCause(e.getMessage());
				log.setCode(400);
				//LOGGER.info("Adding the log 2 in iterate cands");
				
				failCount++;
			}
			finally {
				createLogs.appendLog(log);
			}
			// append the log if we havent seen it before, whatever it looked like
			
			
		}

		// any errors?
		
	//	int status=(0==failCount)?Response.Status.
		
		LOGGER.info("Errors in create log : " + failCount + " errors of " +validatedSRRs.size() +" candidates.");
		
		// now, try to create the dir once for the rule
	
		try {
			Utils.createSWIFTDirectory(rule);
		} catch (CreateDirectoryException cde) {

			LOGGER.severe(cde.getMessage());
		}
	
		// we always return 200 - the caller should check the codes of each section
		return Response.status(Status.OK).entity(createLogs.getLogs()).build();

	}



	/**
	 * Validate the rule using its annotated constraints
	 * 
	 * For each violation, add an Error to the List<Error> passed in *
	 * 
	 * @param val  The validator
	 * @param errs A List <Err - we add to for any error
	 * @param rule The rule instance to be validated
	 * @return Void
	 */
	private void validatePost(Validator val, List<Error> errs, RoutingRule rule) {

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

}
