package security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

public class Security {
	public static Map<String, Object> createKeys() {
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			
			KeyPair keyPair = keyPairGenerator.generateKeyPair();
			PrivateKey privateKey = keyPair.getPrivate();
			PublicKey publicKey = keyPair.getPublic();

			Map<String, Object> keys = new HashMap<String, Object>();
			keys.put("private", privateKey);
			keys.put("public", publicKey);
			return keys;
		} catch (Exception e) {
			System.out.println("Could not get keys!");
			return null;
		}
	}

	public static String decryptMessage(String encryptedText, PublicKey publicKey){
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, publicKey);
			return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedText)));
		}catch(Exception e) {
			System.out.println("Could not decrypt the message.");
			return "";
		}
		
	}

	public static String encryptMessage(String plainText, PrivateKey privateKey){
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, privateKey);
			return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes()));
		}catch(Exception e) {
			System.out.println("Could not encrypt the message.");
			return "";
		}
		
	}
}
