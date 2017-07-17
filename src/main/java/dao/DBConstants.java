package dao;

public class DBConstants {
	
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
			"(projectID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
			"username TEXT NOT NULL," +
			"projectName TEXT NOT NULL," +
			"status TEXT NOT NULL," +
			"runtime INTEGER NOT NULL," +
			"progress INTEGER NOT NULL);"
	};
}
