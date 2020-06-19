package com.stercomm.customers.rbs.sir.rest.server;

import java.io.File;
import com.stercomm.customers.rbs.sir.rest.domain.Error;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.stercomm.customers.rbs.sir.rest.domain.AuthChainMember;
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
}
