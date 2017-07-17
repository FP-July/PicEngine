package sessionManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SessionIDGen {
	/** use username + currentTime and MD5 to generate sessionID 
	 * @param username
	 * @return sessionID
	 */
	public static String gen(String username) {
		String result = "";
		try {
			username += System.currentTimeMillis();
			MessageDigest mDigest = MessageDigest.getInstance("MD5");
			byte[] input = username.getBytes();
			byte[] buff = mDigest.digest(input);
			result = new String(buff);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return result;
	}
}
