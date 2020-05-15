package com.acme.swift.util;

public class TestDirectory {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

			TestDirectory td = new TestDirectory();
		//	String respDN = "cn=foo,ou=abc,o=bankbebb,o=swift";
		//	String reqDN = "cn=bar,ou=abc,o=bankbebb,o=swift";
			String respDN = "foo";
			String reqDN = "bar";
			System.out.println(td.createSWIFTDirectory(reqDN, respDN));
			
			
	}
	
	private  String createSWIFTDirectory(String reqDN, String respDN){

		String swiftDir = readSWIFTDN(respDN) + "/" + readSWIFTDN(reqDN);
		return swiftDir;
	}
	
	private String readSWIFTDN(String dn){

		final String SWIFT_DN_END = ",o=swift";

		String respstub = dn.substring(0, dn.indexOf( SWIFT_DN_END ));
		String[] tok = respstub.split(",");

		StringBuffer sb = new StringBuffer();
		for(int i=tok.length-1;i>=0;i--){
			String fld = tok[i].replaceAll("ou=", "").trim();
			fld = fld.replaceAll("o=", "").trim();
			fld = fld.replaceAll("cn=", "").trim();
			if(!fld.equalsIgnoreCase("")){
				sb.append(fld);
				if(i>0){
					sb.append("_");
				}
			}
		}

		return sb.toString();

	}

}
