package dao;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DaoManager {
	private static Logger logger = LoggerFactory.getLogger(DaoManager.class);
	private static DaoManager instance;
	private Connection connection;
	private Statement statement;
	
	private UserDao userDao;
	private ProjDao projDao;

	public static DaoManager getInstance() throws ClassNotFoundException, SQLException {
		if (instance == null) {
			instance = new DaoManager();
			instance.init();
		}
		return instance;
	}

	private void init() throws SQLException, ClassNotFoundException   {
		connect2DB();
		createTables();
		setUserDao(new UserDao(statement));
		setProjDao(new ProjDao(statement));
	}
	
	private void connect2DB() throws SQLException, ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");
		File dbDir = new File(DBConstants.DB_PATH);
		if(!dbDir.exists())
			dbDir.mkdirs();
		connection = DriverManager.getConnection("jdbc:sqlite:" + DBConstants.DB_PATH + "/test.db");
		statement = connection.createStatement();
	}
	
	private void createTables() throws SQLException {
		for(String sql : DBConstants.tableCreateSQL) {
			boolean success = statement.execute(sql);
			if(!success)
				logger.error("sql " + sql + " fails to excute");
		}
	}

	public UserDao getUserDao() {
		return userDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public ProjDao getProjDao() {
		return projDao;
	}

	public void setProjDao(ProjDao projDao) {
		this.projDao = projDao;
	}

}
