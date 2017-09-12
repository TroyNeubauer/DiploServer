package com.troy.diplo.server.secutiry;

import java.security.NoSuchAlgorithmException;
import java.security.spec.*;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.logging.log4j.*;

import com.troyberry.util.ArrayUtil;

public class Security {

	private static final Logger logger = LogManager.getLogger(Security.class);
	
	private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
	private static final SecretKeyFactory FACTORY = getAlgorithm();
	
	public static byte[] getHashedPassword(char[] password, byte[] salt,  byte[] pepper, int iterations,  int derivedKeyLength) {
		byte[] saltPlusPepper = ArrayUtil.concat(salt, pepper);
	    KeySpec spec = new PBEKeySpec(password, saltPlusPepper, iterations, derivedKeyLength * 8);

	    try {
			return FACTORY.generateSecret(spec).getEncoded();
		} catch (InvalidKeySpecException e) {
			logger.catching(e);
			return null;
		}
	}
	
	
	private static SecretKeyFactory getAlgorithm() {
		try {
			return SecretKeyFactory.getInstance(ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			logger.fatal("Unable to find the required algorithm for hashing a password, " + ALGORITHM);
			logger.catching(e);
		}
		return null;
	}


	private Security() {
	}

}
