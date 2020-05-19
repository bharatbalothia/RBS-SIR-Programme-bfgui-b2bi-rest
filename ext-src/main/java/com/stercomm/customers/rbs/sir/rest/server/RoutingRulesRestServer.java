package com.stercomm.customers.rbs.sir.rest.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

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
import com.stercomm.customers.rbs.sir.rest.util.SRRCreator;

@Path("/rules")
public class RoutingRulesRestServer {

	private static List<RoutingRule> rules = new ArrayList<RoutingRule>();

	private static Logger LOGGER = Logger.getLogger(RoutingRulesRestServer.class.getName());	
	private static final String LOGCONFIGPATH = "log.properties";
	
	@PostConstruct
	private void init() {

		try {
			InputStream logIS = this.getClass().getResourceAsStream(LOGCONFIGPATH);
			System.out.println(logIS.toString());
		
			LogManager.getLogManager().readConfiguration(logIS);
			System.out.println("Logging initialized from " + LOGCONFIGPATH);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<RoutingRule> getRules() {
		LOGGER.log(Level.ALL, "Got request for get on rules");

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

			rules.add(rule);
			if (rule.isForReal()) {
				System.out.println("(HERE)");

				SRRCreator creator = new SRRCreator(rule);
				boolean res = creator.execute();
			}
			return Response.status(status).entity(null).build();

			// yes, there was at least one error so create a JSON response back
		} else {
			status = validationFailureSC;
			Error[] ar = errs.toArray(new Error[list.size()]);
//			
			return Response.status(status).entity(toErrorResp(list)).build();
		}

	}

	private List<Error> validatePost(Validator val, List<Error> errs, RoutingRule rule) {

		val.validate(rule).stream().forEach(violation -> {

			String message = violation.getMessage();
			Error e = new Error();
			e.setAttribute(violation.getPropertyPath().toString());
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

}
