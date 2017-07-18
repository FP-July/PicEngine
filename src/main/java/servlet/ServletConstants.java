package servlet;

public class ServletConstants {
	
	public static final int SUCCESS = 1000;
	// response codes 
	public static final int CODE_NO_COOKIE = 701;
	public static final int CODE_SESSION_EXPIRE = 702;
	public static final int CODE_DB_INIT_FAILURE = 703;
	public static final int CODE_LACK_ARG = 704;
	// session lifetime
	public static final long SESSION_EXPANSION = 1000 * 60 * 15; 
}
