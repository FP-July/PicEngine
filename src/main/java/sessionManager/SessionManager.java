package sessionManager;

import java.util.HashMap;

import servlet.ServletConstants;

public class SessionManager {
	
	
	
	public static class Session {
		public Session(String sessionID2, long expireTime2) {
			this.SessionID = sessionID2;
			this.expireTime = expireTime2;
		}
		private String SessionID;
		private long expireTime;
	}
	
	private static HashMap<String,Session> sessionMap = new HashMap<>();
	
	/** add the given session
	 * @param username
	 * @param sessionID
	 * @param expireTime
	 */
	static public void addSession(String username, String sessionID, long expireTime) {
		sessionMap.put(username, new Session(sessionID, expireTime));
	}
	
	/** to test if the given session is valid, if true, expand the session
	 * @param username
	 * @param sessionID
	 * @return false if session does not exist or expire
	 */
	static public boolean testSession(String username, String sessionID) {
		Session session = sessionMap.get(username);
		if(session == null)
			return false;
		boolean valid = session.expireTime > System.currentTimeMillis() && session.SessionID.equals(sessionID);
		if(valid)
			session.expireTime = Math.max(System.currentTimeMillis() + 
					ServletConstants.SESSION_EXPANSION, session.expireTime);
		return valid;
	}

	/** abort a session, usually when a user log out
	 * @param username
	 */
	static public void abortSession(String username) {
		sessionMap.remove(username);
	}
}
