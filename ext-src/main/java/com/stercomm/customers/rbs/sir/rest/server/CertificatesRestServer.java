package com.stercomm.customers.rbs.sir.rest.server;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import javax.security.cert.X509Certificate;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.stercomm.customers.rbs.sir.rest.domain.AuthChainMember;
import com.stercomm.customers.rbs.sir.rest.domain.Certificate;
import com.stercomm.customers.rbs.sir.rest.domain.CertificateValidationResponse;
import com.stercomm.customers.rbs.sir.rest.domain.Error;
import com.stercomm.customers.rbs.sir.rest.domain.Errors;
import com.stercomm.customers.rbs.sir.rest.domain.RoutingRule;
import com.sterlingcommerce.security.kcapi.CACertificateInfo;
import com.sterlingcommerce.security.kcapi.CertificateHeldException;
import com.sterlingcommerce.security.kcapi.CertificateInfoBase;
import com.sterlingcommerce.security.kcapi.CertificateRevokedException;
import com.sterlingcommerce.security.kcapi.IssuerNotFoundException;
import com.sterlingcommerce.security.kcapi.TrustedCertificateInfo;
import com.sterlingcommerce.woodstock.util.frame.Manager;
import com.sterlingcommerce.woodstock.util.frame.jdbc.Conn;

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

	@GET
	@Path("/chain")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createAuthChain(@QueryParam("issuerdn") String issuerDN) {

		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		byte[] decodedIssuerDNBytes = Base64.getDecoder().decode(issuerDN);
		String decodedIssuerDN = new String(decodedIssuerDNBytes, StandardCharsets.UTF_8);

		List<AuthChainMember> chain = new ArrayList<AuthChainMember>();
		String query = "select name, norm_subj_rdn, norm_issuer_rdn from ca_cert_info where "
				+ "not_before < trunc(sysdate) and not_after > trunc(sysdate) and " + "norm_subj_rdn = ? ";
		/*
		 * String queryForRoot = "select name, norm_subj_rdn from ca_cert_info where "+
		 * "not_before < trunc(sysdate) and not_after > trunc(sysdate) and "+
		 * "norm_issuer_rdn = ? ";
		 */
		LOGGER.info("decodedIssuerDN : " + decodedIssuerDN);
		try {
			conn = Conn.getConnection();

			// first time for the intermediate

			ps = conn.prepareStatement(query);
			ps.setString(1, decodedIssuerDN);
			rs = ps.executeQuery();
			String rootIssuerDN = null;
			while (rs.next()) {
				rootIssuerDN = rs.getString(3);
				chain.add(toResult(rs));
			}

			// now again for the root
			ps.close();
			ps = conn.prepareStatement(query);
			ps.setString(1, rootIssuerDN);

			rs = ps.executeQuery();

			while (rs.next()) {
				chain.add(toResult(rs));
			}

		}

		catch (Exception e) {
			LOGGER.severe("SQL Error searching the CA_cert_info table : " + e.getMessage());

		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
				if (conn != null) {
					Conn.freeConnection(conn);
				}
			} catch (SQLException se) {
				LOGGER.severe("SQL exception : " + se.getMessage());
			}
		}

		// if we have two in the chain, we are ok, right?
		if (chain.size() == 2) {
			return Response.status(Status.OK).entity(chain).build();
		} else {
			Error e = new Error();
			e.setAttribute("chain");
			e.setMessage("Could not construct a cert chain for the issuer DN.");
			return Response.status(Status.NOT_FOUND).entity(e).build();
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

		CertificateValidationResponse resp = new CertificateValidationResponse();
		byte[] decoded = Base64.getDecoder().decode(certificate.getCertBody());
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
			e=toError("Certificate Auth Chain incomplete. The certificate issuer is not trusted :" + infe.getMessage(), "issuer.notfound");
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
			resp.setValid(false);
		} catch (SQLException sqle) {
			e=toError("Error checking the validity of the certificate " + sqle.getMessage(), "certificate.sqlexception");
			resp.setError(e);
			resp.setValid(false);
		} catch (Exception ex) {
			e=toError("Error checking the validity of the certificate " + ex.getMessage(), "certificate.exception");
			resp.setError(e);
			resp.setValid(false);
		}
		if (e == null && resp.isValid()) {
			LOGGER.info("No errors, returning chain of size : " + chain.size());
			return Response.status(Status.OK).entity(resp).build();
		} else {
			return Response.status(Status.NOT_FOUND).entity(e).build();
		}
	}

	/**
	 * Create a FileSearchResult object from a Row
	 * 
	 * @param row
	 * @return
	 * @throws SQLException
	 */
	private AuthChainMember toResult(ResultSet row) throws SQLException {

		String name = row.getString(1);
		String dn = row.getString(2);
		AuthChainMember m = new AuthChainMember();
		m.setCertificateName(name);
		m.setSubjectDN(dn);

		LOGGER.info("Created : " + m);
		return m;

	}

	private List<AuthChainMember> toAuthChainMemberList(Vector<CertificateInfoBase> authChain) {

		Iterator<CertificateInfoBase> acit = authChain.iterator();
		StringBuffer acr = new StringBuffer();

		List<AuthChainMember> retval = new ArrayList<AuthChainMember>(authChain.size());
		
		while (acit.hasNext()) {
			AuthChainMember member = new AuthChainMember();
			CertificateInfoBase ctemp = acit.next();
			member.setSubjectDN(ctemp.getSubjectRDN());
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

		LOGGER.info("Validating : " + cert.getCertName());
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
