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
	 * @return status code
	 */
	public int createUser(String userName, String password) {
		if(userExist(userName) != DBConstants.NO_SUCH_USER)
			return DBConstants.USER_ALREADY_EXIST;
		
		String sql = "INSERT INTO " + DBConstants.USER_TABLE + "(username,password,permission)" +
					"VALUES ('" + userName + "','" + password + "','" + DBConstants.DB_PREVILIGE.user + "');";
		int status = DBConstants.SQL_EXCUTION_ERROR;
		try {
			status = statement.executeUpdate(sql) > 0 ? DBConstants.SUCCESS : status;
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("error occured when executing " + sql + "\n" + e.toString());
		}
		return status;
	}
	
	/** test if a user exists
	 * @param userName
	 * @return status code
	 */
	public int userExist(String userName) {
		String sql = "SELECT * FROM " + DBConstants.USER_TABLE +
				" where username='" + userName + "';";
		ResultSet resultSet;
		try {
			resultSet = statement.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("error occured when executing " + sql + "\n" + e.toString());
			return DBConstants.SQL_EXCUTION_ERROR;
		}
		
		int status = DBConstants.SQL_EXCUTION_ERROR;
		try {
			status = resultSet.next() ? DBConstants.USER_ALREADY_EXIST : DBConstants.NO_SUCH_USER;
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("error occured when itering result " + sql + "\n" + e.toString());
		}
		
		return status;
	}

	/** delete a user
	 * @param userName
	 * @return status code
	 */
	public int deleteUser(String userName) {
		String sql = "DELETE FROM " + DBConstants.USER_TABLE +
				" WHERE username=" + "'" + userName + "';";
		int status = DBConstants.SQL_EXCUTION_ERROR;
		try {
			status = statement.executeUpdate(sql) > 0 ? DBConstants.SUCCESS : status;
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("error occured when executing " + sql + "\n" + e.toString());
		}
		return status;
	}

	/**  check given username and password exist
	 * @param userName
	 * @param password
	 * @return status code
	 */
	public int logIn(String userName, String password) {
		if(userExist(userName) != DBConstants.USER_ALREADY_EXIST)
			return DBConstants.NO_SUCH_USER;
		String sql = "SELECT * FROM " + DBConstants.USER_TABLE +
				" where username='" + userName + "' AND password='" + password + "';";
		ResultSet resultSet;
		try {
			resultSet = statement.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("error occured when executing " + sql + "\n" + e.toString());
			return DBConstants.SQL_EXCUTION_ERROR;
		}
		
		int status = DBConstants.SQL_EXCUTION_ERROR;
		try {
			status = resultSet.next() ? DBConstants.SUCCESS : DBConstants.WRONG_PW;
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("error occured when itering result " + sql + "\n" + e.toString());
		}
		
		return status;
	}

	
	/** modify password
	 * @param userName
	 * @param oldPW
	 * @param newPW
	 * @return status code
	 */
	public int modifyPassword(String userName, String oldPW, String newPW) {
		if(userExist(userName) != DBConstants.USER_ALREADY_EXIST)
			return DBConstants.NO_SUCH_USER;
		String sql = "UPDATE " + DBConstants.USER_TABLE +
				" SET password='" + newPW + "'" +
				" where username='" + userName + "' AND password='" + oldPW + "';";
		int status = DBConstants.SQL_EXCUTION_ERROR;
		try {
			status = statement.executeUpdate(sql) > 0 ? DBConstants.SUCCESS : DBConstants.WRONG_PW;
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("error occured when executing " + sql + "\n" + e.toString());
		}
		return status;
	}
}
