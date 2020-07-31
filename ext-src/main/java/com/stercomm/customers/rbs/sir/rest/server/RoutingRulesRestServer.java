package com.stercomm.customers.rbs.sir.rest.server;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.stercomm.customers.rbs.sir.rest.util.RuleSearchBy;
import com.stercomm.customers.rbs.sir.rest.util.SRRLogs;
import com.stercomm.customers.rbs.sir.rest.util.SRRUpdateLog;
import com.stercomm.customers.rbs.sir.rest.util.SRRValidator;
import com.stercomm.customers.rbs.sir.rest.util.Utils;
import com.sterlingcommerce.woodstock.ui.SWIFTNetRoutingRuleObj;
import com.sterlingcommerce.woodstock.util.frame.Manager;

@Path("/rules")
public class RoutingRulesRestServer extends BaseRestServer {

	private static Logger LOGGER = Logger.getLogger(RoutingRulesRestServer.class.getName());

	@PostConstruct
	private void init() {

		try {
			boolean logToConsole = true;
			String logPath=Manager.getProperties("bfgui").getProperty("log.path.dir");
			String logName=Manager.getProperties("bfgui").getProperty("log.path.routingrules.filename");
			String fullPath = logPath + File.separator + logName;
			LOGGER = setupLogging(logToConsole, fullPath);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSWIFTRulesForEntity(@QueryParam("entity-name") String entityName) {
		LOGGER.info("Got request for GET on rules for entity : " + entityName);

		@SuppressWarnings("rawtypes")
		ArrayList<HashMap> rulesList = null;
		List<String> namesOnly = new ArrayList<String>();
		Status retstat = Response.Status.OK;

		// Get the current list of Routing Rules using the entity name
		SWIFTNetRoutingRuleObj srroList = new SWIFTNetRoutingRuleObj();
		String ruleQuery = "%_" + entityName + "_%";
		LOGGER.info("Get request - looking for rules with query : " + ruleQuery);

		try {
			rulesList = srroList.listByName(ruleQuery, 0, 200, true);
			int noOfRules = rulesList.size();

			if (noOfRules > 0) {

				LOGGER.info("Found " + rulesList.size() + " matching rules.");
				Iterator<HashMap> itrExisting = rulesList.iterator();

				// the old collections classes are so ugly to use

				while (itrExisting.hasNext()) {
					HashMap o = (HashMap) itrExisting.next();
					namesOnly.add((String) o.get("route_name"));
				}

			} else {
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
		SRRLogs createLogs = new SRRLogs();

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

	/**
	 * PUT - given the same params as post
	 * 
	 * 1. validate the post 2. if ok, first search for all rules for the entity, add
	 * to a HashSet on RR name (rrs=a,c,e, old=a,c,e, req = a,b,d) 3. then create a
	 * new collection of RRs, based on the new rule, into a HashSet on RR name
	 * (rrs=a,c,e, old=a,c,e, (new=d,a,b)) 4. foreach rule in the "old" set not in
	 * the new set, delete it (rrs=a, old=a,c,e), (new=a,b,d)) 5. foreach rule in
	 * the "new" set not in the old set, add it (rrs=a,b,d) 6. foreach rule in the
	 * "new" set, if the sub-dir does not exist, create it
	 * 
	 * 
	 * @param rule
	 * @return
	 */
	@PUT
	@Path("/update")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({ "application/json" })
	public Response updateSWIFTRulesFromRule(RoutingRule rule) {

		
		Validator bodyValidator = Validation.buildDefaultValidatorFactory().getValidator();

		// what we return if the post failed to validate
		final Errors list = new Errors();
		final List<Error> errs = new ArrayList<Error>();
		validatePost(bodyValidator, errs, rule);

		// if we have any validation errors, get out right now, and send a 400
		if (errs.size() > 0) {

			list.setErrors(errs);
			return Response.status(Status.BAD_REQUEST).entity(errs).build();
		}

		
		
		SRRLogs updateLogs = new SRRLogs();


		// now create the new list (which also creates the rules as candidates)
		List<SWIFTNetRoutingRuleObj> newRules = populateRules(rule);

		// Get the current list of Routing Rules using the properties of the put
		// we get routing rule name and Object ID into the map

		Map<String, String> currentRuleset = createMapOfIDs(newRules, RuleSearchBy.PROPERTIES);

		// add the old names to a set
		Set<String> currentRRNames = new HashSet<String>();
		currentRuleset.keySet().forEach(s -> currentRRNames.add(s));

		// add the new rules to a hashMap
		Map<String, SWIFTNetRoutingRuleObj> newRRules = new HashMap<String, SWIFTNetRoutingRuleObj>();
		newRules.forEach(o -> newRRules.put(o.getRouteName(), o));

		// create a set of all the ones we want to add
		Set<String> add = new HashSet<String>(newRRules.keySet());
		// and the ones we *might* remove.. we need
		
		
		Set<String> remove = new HashSet<String>(currentRRNames);
		LOGGER.info("Rules to remove 0 (might be all existing) : " + remove);

		// anything in both add and current is an update
		// create a set of all on both add and remove
		Set<String> update = new HashSet<String>(add);
		remove.forEach(s -> update.add(s));

		// example
		// exist rules a,b,c
		// newrules : b,d
		// update should = b, remove=a,c, add = d

		// we dont want to add instead of update, so remove from add list any current
		// that also
		// exist, or that are in the update list

		add.removeAll(currentRRNames);
		update.removeAll(add);

		// we dont want to remove any that would try to be created or updated in the new rules so
		// remove those
		LOGGER.info("Rules to remove 1 : " + remove);
		remove.removeAll(newRRules.keySet());
		update.removeAll(remove);

		LOGGER.info("current rules : " + currentRRNames);
		LOGGER.info("new rules : " + newRRules.keySet());
		LOGGER.info("Rules to add : " + add);
		LOGGER.info("Rules to remove 2: " + remove);
		LOGGER.info("Rules to update : " + update);

		// now iterate the remove set and delete

		remove.forEach(s -> {
			final SRRUpdateLog updateLog = new SRRUpdateLog();
			updateLog.setRoutingRuleName(s);
			updateLog.setUpdateAction("delete");
			try {
				if (deleteFromBI(currentRuleset.get(s))) {
					updateLog.setCode(201);
					updateLog.setFailCause("");
					LOGGER.info("Removed : " + s);
				} else {
					LOGGER.severe("No Delete for : " + s);
				}
			} catch (Exception e) {
				updateLog.setCode(404);
				updateLog.setFailCause(e.getMessage());
			} finally {
				updateLogs.appendLog(updateLog);
			}
		});

		// now iterate the add map, get search one by key and add where

		add.forEach(s -> {
			final SRRUpdateLog updateLog = new SRRUpdateLog();
			SRRValidator validator = new SRRValidator();
			updateLog.setRoutingRuleName(s);
			updateLog.setUpdateAction("create");
			SWIFTNetRoutingRuleObj thisSRRO = newRRules.get(s);
			// wrap in a list to pass to the validator
			List<SWIFTNetRoutingRuleObj> srroList = new ArrayList<SWIFTNetRoutingRuleObj>();
			srroList.add(thisSRRO);
			try {
				List<SWIFTNetRoutingRuleObj> validated = validator.validatedList(srroList, updateLogs);
				// if is
				if (validated.size()==0) {
					LOGGER.severe("Validation failure : " + s);
					
				}else if (addRuleToBI(thisSRRO, updateLog)) {
					updateLog.setCode(201);
					updateLogs.appendLog(updateLog);
					LOGGER.info("Added : " + s);
				} else {
	
					LOGGER.severe("No Add for : " + s);
				}

			} catch (Exception e) {
				updateLog.setCode(404);
				updateLog.setFailCause(e.getMessage());
				updateLogs.appendLog(updateLog);
			} 
		});

		// when we update we get the srro from the new rules, and set its obj id
		// so that we are updating the existing rule with the new rules props
		update.forEach(s -> {
			final SRRUpdateLog updateLog = new SRRUpdateLog();
			updateLog.setRoutingRuleName(s);
			updateLog.setUpdateAction("update");
			try {
				SWIFTNetRoutingRuleObj srro = newRRules.get(s);
				srro.setobjectID(currentRuleset.get(s));
				if (updateRuleInBI(srro, updateLog)) {
					updateLog.setCode(201);
					LOGGER.info("Updated : " + s);
				} else {

					LOGGER.severe("No update to : " + s);
				}

			} catch (Exception e) {
				updateLog.setCode(404);
				updateLog.setFailCause(e.getMessage());
			} finally {
				updateLogs.appendLog(updateLog);
			}
		});

		return Response.status(Status.OK).entity(updateLogs.getLogs()).build();
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
				deleteFromBI(id);
			} else {
				// we didnt find it, return 404
				retstat = Status.NOT_FOUND;
				notFound = new Error();
				notFound.setAttribute("routingrule");
				notFound.setMessage("Rule not found with name " + ruleName);

				LOGGER.severe("No such rule to delete with name :  " + ruleName);
			}

		} catch (Exception e) {
			LOGGER.severe("Caught exception deleting the SWIFTNet Routing Rule: " + e.getMessage());

		}

		return Response.status(retstat).entity(notFound != null ? notFound : null).build();

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

			LOGGER.info("Creating SRR for " + rule.getEntityName() + ", request type : " + rule.getRequestType()[i]);
			SWIFTNetRoutingRuleObj swiftRule = new SWIFTRoutingRule.Builder().withActionType("BP")
					.withService(rule.getService()).withInvokeMode("SYNC").withRequestType(rule.getRequestType()[i])
					.withWorkflowName(rule.getRequestType()[i]).withRequestorDN(rule.getRequestorDN())
					.withResponderDN(rule.getResponderDN())
					.toRouteName(rule.getEntityName(), rule.getRequestType()[i], rule.getEntityType()).withPriority(0)
					.build();
			retval.add(swiftRule);
		}
		LOGGER.info("Created " + retval.size() + " candidate Routing rules.");
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
	private int saveToBI(List<SWIFTNetRoutingRuleObj> validatedSRRs, SRRLogs actionLogs) {

		int failCount = 0;

		for (SWIFTNetRoutingRuleObj swiftNetRoutingRuleObj : validatedSRRs) {

			SRRUpdateLog log = new SRRUpdateLog();
			log.setUpdateAction("create");
			String rn = swiftNetRoutingRuleObj.getRouteName();
			log.setRoutingRuleName(rn);
			LOGGER.info("Trying to save SRR : " + rn);
			if (!addRuleToBI(swiftNetRoutingRuleObj, log)) {

				failCount++;
			}
			;
			actionLogs.appendLog(log);
		}

		return failCount;
	}

	private boolean updateRuleInBI(SWIFTNetRoutingRuleObj srro, SRRUpdateLog log) {

		boolean res = false;

		LOGGER.info("Updating SRR with object ID : " + srro.getobjectID());
		try {

			res = srro.saveSWIFTNetRoutingRule(SWIFTNetRoutingRuleObj.UPDATE_ACTION);
			// we'd expect b to be true here, so let's set it in the log
			if (res) {
				log.setCode(201);
				log.setFailCause(""); // to do..can we suppress this attribute in the JSON where b==true?
				LOGGER.info("Updated SRR with object ID : " + srro.getobjectID());
			}
		} catch (Exception e) {

			// something unfortunate happened here
			LOGGER.severe("Exception trying to save rule : " + srro.getRouteName());
			LOGGER.severe(e.getMessage());
			log.setFailCause(e.getMessage());
			log.setCode(400);

		}

		return res;

	}

	private boolean addRuleToBI(SWIFTNetRoutingRuleObj srr, SRRUpdateLog log) {

		boolean res = false;
		try {
			res = srr.saveSWIFTNetRoutingRule(SWIFTNetRoutingRuleObj.INSERT_ACTION);
			// we'd expect b to be true here, so let's set it in the log
			if (res) {
				log.setCode(201);
				log.setFailCause(""); // to do..can we suppress this attribute in the JSON where b==true?
				LOGGER.info("Created SRR with rule name : " + srr.getRouteName());
			}
		} catch (Exception e) {

			// something unfortunate happened here
			LOGGER.severe("Exception trying to save rule : " + srr.getRouteName());
			LOGGER.severe(e.getMessage());
			log.setFailCause(e.getMessage());
			log.setCode(400);

		}
		return res;
	}

	/**
	 * Delete by object ID
	 * 
	 * @param ruleObjID
	 * @throws Exception
	 */
	private boolean deleteFromBI(String ruleObjID) throws Exception {

		boolean res = false;
		SWIFTNetRoutingRuleObj ruleToDel = new SWIFTNetRoutingRuleObj();
		ruleToDel.setobjectID(ruleObjID);
		res = ruleToDel.saveSWIFTNetRoutingRule(SWIFTNetRoutingRuleObj.DELETE_ACTION);
		return res;
	}

	/**
	 * create a HashMap with existing rules : name and ID
	 * 
	 */
	private HashMap<String, String> createMapOfIDs(List<SWIFTNetRoutingRuleObj> newRules, RuleSearchBy by) {

		HashMap<String, String> retval = new HashMap<String, String>();
		
		for (SWIFTNetRoutingRuleObj srro : newRules) {
		
			String ruleID = null;
			String srroRouteName=srro.getRouteName();
			
			if (by==RuleSearchBy.PROPERTIES) {
			
				ruleID=srro.exists(srro.getRequestor(),srro.getResponder(),srro.getService(),srro.getRequestType());
			}else if (by==RuleSearchBy.NAME) {
				
				ruleID=srro.checkExistsByName(srroRouteName);
			}
			if (null!=ruleID) {		
				retval.put(srro.getRouteName(), ruleID);
			
				LOGGER.info("Mapped existing rule for " + srroRouteName + " to " + ruleID);
			}
			else {
				LOGGER.info("No existing rule match for new : " + srro.getRouteName());
				
			}
		}
		return retval;
	}

}
