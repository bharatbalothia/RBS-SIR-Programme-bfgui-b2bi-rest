package com.stercomm.customers.rbs.sir.rest.server;

import java.io.File;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.security.cert.CertificateException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.stercomm.customers.rbs.sir.rest.domain.AuthChainMember;
import com.stercomm.customers.rbs.sir.rest.domain.Certificate;
import com.stercomm.customers.rbs.sir.rest.domain.CertificateValidationResponse;
import com.stercomm.customers.rbs.sir.rest.error.Error;
import com.stercomm.customers.rbs.sir.rest.error.Errors;
import com.sterlingcommerce.security.kcapi.CACertificateInfo;
import com.sterlingcommerce.security.kcapi.CertificateHeldException;
import com.sterlingcommerce.security.kcapi.CertificateInfoBase;
import com.sterlingcommerce.security.kcapi.CertificateRevokedException;
import com.sterlingcommerce.security.kcapi.IssuerNotFoundException;
import com.sterlingcommerce.security.kcapi.TrustedCertificateInfo;
import com.sterlingcommerce.woodstock.util.frame.Manager;

@Path("/certificates")
public class CertificatesRestServer extends BaseRestServer {

	private static Logger LOGGER = Logger.getLogger(CertificatesRestServer.class.getName());

	@PostConstruct
	private void init() {
		try {
			boolean logToConsole = true;
			String logPath = Manager.getProperties("bfgui").getProperty("log.path.dir");
			String logName = Manager.getProperties("bfgui").getProperty("log.path.certificates.filename");
			String fullPath = logPath + File.separator + logName;
			LOGGER = setupLogging(logToConsole, fullPath);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	

	@POST
	@Path("/verify")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({ "application/json" })
	public Response createAuthChain(Certificate certificate) {
		
		
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

		// what we return if the post failed to validate
		final Errors list = new Errors();
		final List<Error> errs = new ArrayList<Error>();
		validatePost(validator, errs, certificate);

		// if we have any validation errors, get out right now, and send a 400
		if (errs.size() > 0) {

			list.setErrors(errs);
			return Response.status(Status.BAD_REQUEST).entity(errs).build();
		}

		List<AuthChainMember> chain = new ArrayList<AuthChainMember>();
		Error e = null;
		boolean isSystemException = false;

		CertificateValidationResponse resp = new CertificateValidationResponse();
		byte[] decoded = Base64.getDecoder().decode(certificate.getCertificateBody());
		try {
			// X509Certificate cert = X509Certificate.getInstance(decoded);
			LinkedList<TrustedCertificateInfo> certList = TrustedCertificateInfo.parseCertificateBlob(decoded);
			CertificateInfoBase c = (CertificateInfoBase)certList.getFirst();
			Vector<CertificateInfoBase> authChain = CACertificateInfo.verifyCert(c, certList, true, null);

			chain = toAuthChainMemberList(authChain);
			
			//must be valid but chain is empty
			if (chain.isEmpty()) {		
				resp.setSelfSigned(true);
			}
			else {
				resp.setChain(chain);
			}
			resp.setValid(true);
			
		}

		catch (CertificateHeldException che) {
			e=toError("Certificate is held :"  + che.getMessage(), "certificate.held");
			resp.setError(e);
			resp.setValid(false);
		} catch (CertificateRevokedException cre) {
			e=toError("Certificate revoked"  + cre.getMessage(), "certificate.revoked");
			resp.setError(e);
			resp.setValid(false);
		} catch (IssuerNotFoundException infe) {
			e=toError("Certificate Auth Chain incomplete. The certificate issuer is not trusted :" + infe.getMessage(), "issuer.nottrusted");
			resp.setError(e);
			resp.setValid(false);
		} catch (CertificateExpiredException cee) {
			e=toError("Certificate Auth Chain incomplete. The certificate has expired :" + cee.getMessage(), "certificate.expired");
			resp.setError(e);
			resp.setValid(false);
		} catch (CertificateNotYetValidException cnyve) {
			e=toError("Error checking the validity of the certificate " + cnyve.getMessage(), "certificate.notyetvalid");
			resp.setError(e);
			resp.setValid(false);
		} catch (NoSuchAlgorithmException nsuae) {
			e=toError("Error checking the validity of the certificate " + nsuae.getMessage(), "certificate.nosuchalgorithm");
			resp.setError(e);
			resp.setValid(false);
		} catch (InvalidKeyException ike) {
			e=toError("Error checking the validity of the certificate " + ike.getMessage(), "certificate.invalidkey");
			resp.setError(e);
			resp.setValid(false);
		} catch (NoSuchProviderException nspe) {
			e=toError("Error checking the validity of the certificate " + nspe.getMessage(), "certificate.nosuchprovider");
			resp.setError(e);
			resp.setValid(false);
		} catch (SignatureException se) {
			e=toError("Error checking the validity of the certificate " + se.getMessage(), "certificate.badsignature");
			resp.setError(e);
			resp.setValid(false);
		} catch (CertificateException ce) {
			e=toError("Error checking the validity of the certificate " + ce.getMessage(), "certificate.generalexception");
			resp.setError(e);
			isSystemException=true;
			resp.setValid(false);
		} catch (SQLException sqle) {
			e=toError("Error checking the validity of the certificate " + sqle.getMessage(), "certificate.sqlexception");
			resp.setError(e);
			isSystemException=true;
			resp.setValid(false);
		} catch (Exception ex) {
			e=toError("Error checking the validity of the certificate " + ex.getMessage(), "certificate.exception");
			resp.setError(e);
			isSystemException=true;
			resp.setValid(false);
		}
		if (e == null && resp.isValid()) {
			LOGGER.info("No errors, returning chain of size : " + chain.size());
			return Response.status(Status.OK).entity(resp).build();
		} else {
			return Response.status(isSystemException?Status.INTERNAL_SERVER_ERROR:Status.BAD_REQUEST).entity(e).build();
		}
	}

	

	private List<AuthChainMember> toAuthChainMemberList(Vector<CertificateInfoBase> authChain) {

		Iterator<CertificateInfoBase> acit = authChain.iterator();
		StringBuffer acr = new StringBuffer();

		List<AuthChainMember> retval = new ArrayList<AuthChainMember>(authChain.size());
		
		while (acit.hasNext()) {
			AuthChainMember member = new AuthChainMember();
			CertificateInfoBase ctemp = acit.next();
			member.setSubjectDN(ctemp.getNormalizedSubjectRDN());
			member.setCertificateName(ctemp.getName());
			LOGGER.info("Adding to chain : " + member);
			retval.add(member);
			}
		
		if (retval.size() > 0) {
			retval.remove(0);
		}
		return retval;
	}

	private Error toError(String message, String attribute) {

		Error e = new Error();
		e.setAttribute(attribute);
		e.setMessage(message);
		return e;
	}
	private void validatePost(Validator val, List<Error> errs, Certificate cert) {

		LOGGER.info("Validating a cert starting with > " + cert.getCertificateBody().substring(0, 20) + "...");
		val.validate(cert).stream().forEach(violation -> {

			String message = violation.getMessage();
			String attr = violation.getPropertyPath().toString();
			LOGGER.severe(attr + " : " + message);
			Error e = new Error();
			e.setAttribute(attr);
			e.setMessage(message);
			errs.add(e);
		});

	}
}
