package servlet;

public class ServletConstants {
	
	public static final int SUCCESS = 1000;
	// response codes 
	public static final int NO_COOKIE = 701;
	public static final int SESSION_EXPIRE = 702;
	public static final int DB_INIT_FAILURE = 703;
	public static final int LACK_ARG = 704;
	public static final int NO_USERINFO = 705;
	// session lifetime
	public static final long SESSION_EXPANSION = 1000 * 60 * 15; 
}
