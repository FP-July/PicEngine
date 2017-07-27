package dao;

public class DBConstants {
	//status codes
	public static final int SUCCESS = 1000;
	public static final int SQL_EXCUTION_ERROR = 1001;
	//proj 
	public static final int PROJ_ALREADY_EXIST = 1100;
	public static final int NO_SUCH_PROJ = 1101;
	//user
	public static final int USER_ALREADY_EXIST = 1200;
	public static final int NO_SUCH_USER = 1201;
	public static final int WRONG_PW = 1202;
	
	//status strings
	public static final String STR_SUCCESS = "success";
	public static final String STR_SQL_EXCUTION_ERROR = "error occured when excuting sql";
	//proj 
	public static final String STR_PROJ_ALREADY_EXIST = "task already exists";
	public static final String STR_NO_SUCH_PROJ = "no such task";
	//user
	public static final String STR_USER_ALREADY_EXIST = "user already exists";
	public static final String STR_NO_SUCH_USER = "no such user";
	public static final String STR_WRONG_PW = "wrong password";
	
	public static final String USER_TABLE = "userData";
	public static final String PROJ_TABLE = "projectData";
	
	public static final String DB_PATH = "/home/jt/sqlite";
	
	public static enum DB_PREVILIGE {admin, user};
	
	public static final String[] tableCreateSQL = {
			"CREATE TABLE IF NOT EXISTS " + USER_TABLE + " "+
			"(username TEXT PRIMARYKEY NOT NULL," +
			"password TEXT NOT NULL," +
			"permission INTEGER NOT NULL);",
			
			"CREATE TABLE IF NOT EXISTS " + PROJ_TABLE + " " +
			"(projID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
			"username TEXT NOT NULL," +
			"projName TEXT NOT NULL," +
			"status INTEGER NOT NULL," +
			"runtime INTEGER NOT NULL," +
			"finishedTime INTEGER," +
			"progress INTEGER NOT NULL," +
			"type TEXT NOT NULL," +
			"createTime LONG NOT NULL);"
	};
	
	public static String codeToString(int status) {
		switch (status) {
		case SUCCESS:
			return STR_SUCCESS;
		case SQL_EXCUTION_ERROR:
			return STR_SQL_EXCUTION_ERROR;
		case PROJ_ALREADY_EXIST:
			return STR_PROJ_ALREADY_EXIST;
		case NO_SUCH_PROJ:
			return STR_NO_SUCH_PROJ;
		case NO_SUCH_USER:
			return STR_NO_SUCH_USER;
		case USER_ALREADY_EXIST:
			return STR_USER_ALREADY_EXIST;
		case WRONG_PW:
			return STR_WRONG_PW;
		default:
			return "未知错误。";
		}
	}
}
