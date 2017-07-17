package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDao {
	private static Logger logger = LoggerFactory.getLogger(UserDao.class);
	private Statement statement;
	
	public UserDao(Statement statement) {
		this.statement = statement;
	}
	
	/** create a user
	 * @param userName
	 * @param password
	 * @return true if create successfully
	 */
	public boolean createUser(String userName, String password) {
		if(userExist(userName))
			return false;
		
		String sql = "INSERT INTO " + DBConstants.USER_TABLE + "(username,password,permission)" +
					"VALUES ('" + userName + "','" + password + "','" + DBConstants.DB_PREVILIGE.user + "');";
		boolean success = false;
		try {
			success = statement.executeUpdate(sql) > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("error occured when executing " + sql);
		}
		return success;
	}
	
	/** test if a user exists
	 * @param userName
	 * @return true if user exists
	 */
	public boolean userExist(String userName) {
		String sql = "SELECT * FROM " + DBConstants.USER_TABLE +
				" where username='" + userName + "';";
		ResultSet resultSet;
		try {
			resultSet = statement.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("error occured when executing " + sql + "\n" + e.toString());
			return false;
		}
		
		boolean found = false;
		try {
			found = resultSet.next();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("error occured when itering result " + sql + "\n" + e.toString());
		}
		
		return found;
	}

	/** delete a user
	 * @param userName
	 * @return true if success, false if failed or no such user
	 */
	public boolean deleteUser(String userName) {
		String sql = "DELETE FROM " + DBConstants.USER_TABLE +
				" WHERE username=" + "'" + userName + "';";
		boolean success = false;
		try {
			success = statement.executeUpdate(sql) > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("error occured when executing " + sql + "\n" + e.toString());
		}
		return success;
	}

	/**  check given username and password exist
	 * @param userName
	 * @param password
	 * @return true if username and password pair exist
	 */
	public boolean logIn(String userName, String password) {
		String sql = "SELECT * FROM " + DBConstants.USER_TABLE +
				" where username='" + userName + "' AND password='" + password + "';";
		ResultSet resultSet;
		try {
			resultSet = statement.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("error occured when executing " + sql + "\n" + e.toString());
			return false;
		}
		
		boolean found = false;
		try {
			found = resultSet.next();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("error occured when itering result " + sql + "\n" + e.toString());
		}
		
		return found;
	}

	
	public boolean modifyPassword(String userName, String oldPW, String newPW) {
		String sql = "UPDATE " + DBConstants.USER_TABLE +
				" SET password='" + newPW + "'" +
				" where username='" + userName + "' AND password='" + oldPW + "';";
		boolean success = false;
		try {
			success = statement.executeUpdate(sql) > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("error occured when executing " + sql + "\n" + e.toString());
		}
		return success;
	}
}
