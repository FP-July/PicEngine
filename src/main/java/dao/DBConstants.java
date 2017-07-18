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
			"progress INTEGER NOT NULL," +
			"createTime LONG NOT NULL);"
	};
}
