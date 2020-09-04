package com.stercomm.customers.rbs.sir.rest.server;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.stercomm.customers.rbs.sir.rest.domain.TransmitRequest;
import com.stercomm.customers.rbs.sir.rest.domain.TransmitResponse;
import com.stercomm.customers.rbs.sir.rest.error.Error;
import com.stercomm.customers.rbs.sir.rest.error.Errors;
import com.sterlingcommerce.woodstock.util.frame.Manager;
import com.sterlingcommerce.woodstock.workflow.InitialWorkFlowContext;
import com.sterlingcommerce.woodstock.workflow.InitialWorkFlowContextException;
import com.sterlingcommerce.woodstock.workflow.WorkFlowContextCookie;

@Path("/transmit")
public class TransmitRestServer extends BaseRestServer {

	private static Logger LOGGER = Logger.getLogger(TransmitRestServer.class.getName());

	@PostConstruct
	private void init() {
		try {
			boolean logToConsole = true;
			String logPath = Manager.getProperties("bfgui").getProperty("log.path.dir");
			String logName = Manager.getProperties("bfgui").getProperty("log.path.transmit.filename");
			String fullPath = logPath + File.separator + logName;
			LOGGER = setupLogging(logToConsole, fullPath);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response postTransmitRequest(TransmitRequest req) {

		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

		String bpName = null;
		TransmitResponse resp = null;

		// what we return if the post failed to validate
		final Errors list = new Errors();
		final List<Error> errs = new ArrayList<Error>();
		validatePost(validator, errs, req);

		// if we have any validation errors, get out right now, and send a 400
		if (errs.size() > 0) {
			list.setErrors(errs);
			return Response.status(Status.BAD_REQUEST).entity(errs).build();
		}
		// we have a valid transmit object
		
		// find the name of the bp to execute
		bpName = getBPName(req);

		InitialWorkFlowContext iwfc = new InitialWorkFlowContext();
		iwfc.setWorkFlowName(bpName);
		
		//create the process data
		createDocContent(req, bpName, iwfc);
	
		// in case there is an Error transmitting
		Error err = null;

		try {

			WorkFlowContextCookie cookie = iwfc.start();
			resp = new TransmitResponse();
			resp.setWfcID(cookie.getWorkFlowContextId());
		} catch (InitialWorkFlowContextException e) {

			e.printStackTrace();
			err = new Error();
			err.setAttribute("Execution");
			err.setMessage("Failed to exec BP : " + e.getMessage() + ", check logs");
		}

		if (null == err) {
			LOGGER.info("Transmitted request : " + req + "to BP : " + bpName + " wfcid=" + resp.getWfcID());
			return Response.status(Status.OK).entity(resp).build();
		} else {
			LOGGER.severe("Error transmitting request : " + req + " to BP : " + bpName);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build();
		}
	}

	private void validatePost(Validator val, List<Error> errs, TransmitRequest e) {

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

	private String getBPName(TransmitRequest req) {

		String retval = null;

		switch (req.getFileType().toUpperCase()) {

		case "IQF":
			retval = Manager.getProperties("sct").getProperty("entity.iqf.transmitNow.bpname");
			break;

		default:
			retval = Manager.getProperties("sct").getProperty("entity.icf.transmitNow.bpname");
			break;
		}
		return retval;
	}

	private Element parseXmlString(String xml) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
			ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes("UTF-8"));
			org.w3c.dom.Document documentEl = documentBuilder.parse(bais);
			Element root = documentEl.getDocumentElement();
			return root;
		} catch (ParserConfigurationException pce) {
			throw new RuntimeException("Error setting up XML parser", pce);
		} catch (UnsupportedEncodingException uee) {
			throw new RuntimeException("Error setting up XML parser", uee);
		} catch (IOException ie) {
			throw new RuntimeException("Error parsing XML.", ie);
		} catch (SAXException se) {
			throw new RuntimeException("Error parsing XML.", se);
		}
	}
	
	private void createDocContent(TransmitRequest req, String bpName, InitialWorkFlowContext context) {
		
		StringBuffer message = new StringBuffer("<Root>\n");
		message.append("<system-account-user-id>" + req.getUsername() + "</system-account-user-id>");
		message.append("<username>" + req.getUsername() + "</username>");
		message.append("<" + bpName + "><EntityID>" + req.getEntityID() + "</EntityID></" + bpName + ">");
		message.append("</Root>\n");
		Element root = parseXmlString(message.toString());
		NodeList nodes = root.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE)
				context.addContentElement("", node, true);
		}

		
	}
}
