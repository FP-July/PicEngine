package session;

public class Session {
	public Session(String sessionID2, long expireTime2) {
		this.setSessionID(sessionID2);
		this.setExpireTime(expireTime2);
	}

	public String getSessionID() {
		return SessionID;
	}

	public void setSessionID(String sessionID) {
		SessionID = sessionID;
	}

	public long getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}

	private String SessionID;
	private long expireTime;
	
}