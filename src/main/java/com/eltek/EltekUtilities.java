package com.eltek;

import er.extensions.crypting.ERXCrypto;

public class EltekUtilities {

	
	
	/**
	 * Creates the UTF-8 String encrypted with SHA-512
	 * @param incoming aString
	 * @return
	 */
	public static String SHABase64String(String aString ) {
		String digestedString = "";
		try {			
			digestedString = ERXCrypto.base64HashedString(ERXCrypto.sha512Encode(aString));			
		}
		catch (Exception e) {
			System.out.println("exception e = " + e.toString());
		}
		return digestedString;
	}

}
