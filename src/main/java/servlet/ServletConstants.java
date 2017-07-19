package servlet;

import dao.DBConstants;

public class ServletConstants {
	
	public static final int SUCCESS = 1000;
	// response codes 
	public static final int NO_COOKIE = 701;
	public static final int SESSION_EXPIRE = 702;
	public static final int DB_INIT_FAILURE = 703;
	public static final int LACK_ARG = 704;
	public static final int NO_USERINFO = 705;
	
	public static final int HADOOP_FS_CRASH = 706;
	public static final int UPLOAD_FAIL = 707;
	// response string
	public static final String STR_SUCCESS = "success";
	public static final String STR_NO_COOKIE = "no cookie";
	public static final String STR_SESSION_EXPIRE = "session expire, login first";
	public static final String STR_DB_INIT_FAILURE = "database fatal error";
	public static final String STR_LACK_ARG = "not enough arguments";
	public static final String STR_NO_USERINFO = "cannot find the user";
	
	public static final String STR_HADOOP_FS_CRASH = "cannot init hadoop filesystem";
	public static final String STR_UPLOAD_FAIL = "file cannot be uploaded to HDFS";
	// session lifetime
	public static final long SESSION_EXPANSION = 1000 * 60 * 15; 
	
	
	public static String codeToString(int status) {
		switch (status) {
		case SUCCESS:
			return STR_SUCCESS;
		case SESSION_EXPIRE:
			return STR_SESSION_EXPIRE;
		case DB_INIT_FAILURE:
			return STR_DB_INIT_FAILURE;
		case LACK_ARG:
			return STR_LACK_ARG;
		case NO_USERINFO:
			return STR_NO_USERINFO;
		case HADOOP_FS_CRASH:
			return STR_HADOOP_FS_CRASH;
		case UPLOAD_FAIL:
			return STR_UPLOAD_FAIL;
		default:
			return DBConstants.codeToString(status);
		}
	}
}
