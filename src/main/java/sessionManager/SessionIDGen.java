package sessionManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SessionIDGen {
	/** use username + currentTime and MD5 to generate sessionID 
	 * @param username
	 * @return sessionID
	 */
	public static String gen(String username) {
		String result = username + System.currentTimeMillis();
		return result;
	}
}
