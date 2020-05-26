package com.stercomm.customers.rbs.sir.rest.util;

public class TestDirectory {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

			TestDirectory td = new TestDirectory();
			String reqDN = "cn=bar.com,ou=abc,o=bank02,o=swift";
			String respDN = "cn=foo.com,ou=abc,o=bank01,o=swift";
			
			System.out.println(Utils.createSWIFTDirectoryPath(reqDN, respDN));
			
			
	}
	/*
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
*/
}
