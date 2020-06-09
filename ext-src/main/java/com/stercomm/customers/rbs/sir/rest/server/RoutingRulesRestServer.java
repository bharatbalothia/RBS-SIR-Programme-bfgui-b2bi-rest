package com.stercomm.customers.rbs.sir.rest.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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
public class RoutingRulesRestServer extends BaseRestServer {

	// we can construct 1..n SWIFT routing rules from a single POST
	private static List<RoutingRule> rules = new ArrayList<RoutingRule>();
	private static Logger LOGGER = Logger.getLogger(RoutingRulesRestServer.class.getName());

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
	public Response getSWIFTRulesForEntity(@QueryParam("entity-name") String entityName) {
		LOGGER.info("Got request for GET on rules for entity : " +entityName);

		@SuppressWarnings("rawtypes")
		ArrayList<HashMap> rulesList = null;
		List<String> namesOnly = new ArrayList<String>();
		Status retstat = Response.Status.OK;
		
		// Get the current list of Routing Rules using the entity name
		SWIFTNetRoutingRuleObj srroList = new SWIFTNetRoutingRuleObj();
		String ruleQuery = "GPL_" + entityName + "_%";
		LOGGER.info("Get request - looking for rules with query : " + ruleQuery);
		
		try {
			rulesList = srroList.listByName(ruleQuery, 0, 200, true);
			int noOfRules = rulesList.size();
			
			if (noOfRules>0) {
				
				LOGGER.info("Found " + rulesList.size() + " matching rules.");
				Iterator<HashMap> itrExisting = rulesList.iterator();
				
				// the old collections classes are so ugly to use
				
				while(itrExisting.hasNext()){
					HashMap o = (HashMap)itrExisting.next();
					namesOnly.add((String)o.get("route_name"));
				}
		
			}
			else {
				LOGGER.info("Found no matching rules.");
			}
		} catch (Exception e) {

			LOGGER.severe(e.getMessage());
		}

		return Response.status(retstat).entity(namesOnly).build();
	

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

		// create SRRs from this rule
		List<SWIFTNetRoutingRuleObj> candidateRules = populateRules(rule);

		// iterate and parse out the SRRs that can't be created for business logic
		// reasons
		// reasons - e.g the rule already exists
		// pass in the logs collection by ref in case the validation wants to contribute
		List<SWIFTNetRoutingRuleObj> validatedSRRs = new SRRValidator().validatedList(candidateRules, createLogs);

		// now we can try to save to BI the ones that survived validation
		int failCount = saveToBI(validatedSRRs, createLogs);
		// any errors?
		LOGGER.info("Errors in create log : " + failCount + " errors of " + validatedSRRs.size() + " candidates.");

		// now, try to create the dir *once* for the rule

		try {
			Utils.createSWIFTDirectory(rule);
		} catch (CreateDirectoryException cde) {

			LOGGER.severe(cde.getMessage());
		}

		// we always return 200 - the caller should check the codes of each section
		return Response.status(Status.OK).entity(createLogs.getLogs()).build();

	}

	@PUT
	@Path("/update")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({ "application/json" })
	public Response updateSWIFTRulesFromRule(RoutingRule rule) {

		List<SWIFTNetRoutingRuleObj> rulesList = null;

		// Get the current list of Routing Rules using the entity name
		SWIFTNetRoutingRuleObj srroList = new SWIFTNetRoutingRuleObj();
		String ruleQuery = "GPL_" + rule.getEntityName() + "_%";
		LOGGER.info("Update request - looking for rules for " + ruleQuery);

		try {
			rulesList = srroList.listByName(ruleQuery, 0, 200, true);
			LOGGER.info("Found " + rulesList.size() + " matching rules.");

			for (SWIFTNetRoutingRuleObj swiftNetRoutingRuleObjName : rulesList) {
				LOGGER.info("Found " + swiftNetRoutingRuleObjName.getRouteName());
			}
		} catch (Exception e) {

			LOGGER.severe(e.getMessage());
		}

		return Response.status(Status.OK).entity(rulesList).build();
	}

	@DELETE
	@Path("/delete/{routingrule}")
	@Produces({ "application/json" })
	public Response deleteSWIFTRulesFromRule(@PathParam("routingrule") String ruleName) {

		// in case of a 404
		Error notFound = null;

		// Get the current list of Routing Rules using the entity name
		SWIFTNetRoutingRuleObj rule = new SWIFTNetRoutingRuleObj();

		LOGGER.info("Delete request - looking for rule : " + ruleName);

		Status retstat = Response.Status.OK;

		try {
			
			// get the object ID of the rule with the name
			String id = rule.exists(ruleName);
			// if we found it, delete it
			if ((null != id) && (id != "")) {
				LOGGER.info("Found " + id);
				rule.setobjectID(id);
				rule.saveSWIFTNetRoutingRule(SWIFTNetRoutingRuleObj.DELETE_ACTION);

			} else {
				// we didnt find it, return 404
				retstat = Status.NOT_FOUND;
				notFound = new Error();
				notFound.setAttribute("routingrule");
				notFound.setMessage("Rule not found with name "+ ruleName);
				
				LOGGER.severe("No such rule to delete with name :  " + ruleName);
			}

		} catch (Exception e) {
			LOGGER.severe("Caught exception deleting the SWIFTNet Routing Rule: " + e.getMessage());

		}

		return Response.status(retstat).entity(notFound!=null?notFound:null).build();

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

	/**
	 * iterate the list create the BI SRR objects and add them to the candidate list
	 * 
	 * @param rule the routing rule
	 * @return List of the candidates
	 */
	private List<SWIFTNetRoutingRuleObj> populateRules(RoutingRule rule) {

		List<SWIFTNetRoutingRuleObj> retval = new ArrayList<SWIFTNetRoutingRuleObj>();

		final int numOfSRRs = rule.getRequestType().length;
		LOGGER.info("Looks like " + numOfSRRs + " in this rule, starting to build it/them.");

		for (int i = 0; i < numOfSRRs; i++) {

			LOGGER.info("Creating SSR for " + rule.getEntityName() + ", request type : " + rule.getRequestType()[i]);
			SWIFTNetRoutingRuleObj swiftRule = new SWIFTRoutingRule.Builder().withActionType("BP")
					.withService(rule.getService()).withInvokeMode("SYNC").withRequestType(rule.getRequestType()[i])
					.withWorkflowName(rule.getRequestType()[i]).withRequestorDN(rule.getRequestorDN())
					.withResponderDN(rule.getResponderDN()).toRouteName(rule.getEntityName(), rule.getRequestType()[i])
					.withPriority(0).build();
			retval.add(swiftRule);
		}
		return retval;
	}

	/**
	 * Save the list to BI, update success or failure into the createLogs object,
	 * and return a count of fails.
	 * 
	 * 
	 * @param validatedSRRs
	 * @param createLogs
	 * @return
	 */
	private int saveToBI(List<SWIFTNetRoutingRuleObj> validatedSRRs, SRRCreateLogs createLogs) {

		int failCount = 0;

		for (SWIFTNetRoutingRuleObj swiftNetRoutingRuleObj : validatedSRRs) {

			String rn = swiftNetRoutingRuleObj.getRouteName();
			LOGGER.info("Trying to save SRR : " + swiftNetRoutingRuleObj.getRouteName());
			SRRCreateLog log = new SRRCreateLog();
			log.setRoutingRuleName(rn);
			try {
				boolean b = swiftNetRoutingRuleObj.saveSWIFTNetRoutingRule(SWIFTNetRoutingRuleObj.INSERT_ACTION);
				// we'd expect b to be true here, so let's set it in the log
				log.setSuccessOnCreate(b);
				log.setCode(201);
				log.setFailCause(""); // to do..can we suppress this attribute in the JSON where b==true?
				LOGGER.info("Created SRR with object ID : " + swiftNetRoutingRuleObj.getRouteName());
			} catch (Exception e) {

				// something unfortunate happened here
				LOGGER.severe("Exception trying to save rule : " + swiftNetRoutingRuleObj.getRouteName());
				LOGGER.severe(e.getMessage());

				// ..and false here, so we can set a fail message
				log.setSuccessOnCreate(false);
				log.setFailCause(e.getMessage());
				log.setCode(400);

				failCount++;
			} finally {
				createLogs.appendLog(log);
			}

		}

		return failCount;
	}

}
