package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.ProjInfo;

public class ProjDao {
	private static Logger logger = LoggerFactory.getLogger(ProjDao.class);
	private Statement statement;

	public ProjDao(Statement statement) {
		this.statement = statement;
	}

	/**
	 * create a proj using given info
	 * 
	 * @param projName
	 * @param username
	 * @return status code
	 */
	public int createProj(String projName, String username, String type) {
		if(findProj(username, projName) != null)
			return DBConstants.PROJ_ALREADY_EXIST;
		String sql = "INSERT OR IGNORE INTO " + DBConstants.PROJ_TABLE
				+ "(username,projName,status,runtime,progress,createTime,type)" + "VALUES ('" + username + "','"
				+ projName + "','" + ProjInfo.statusEnum.init.ordinal() + "',0,0," + System.currentTimeMillis() 
				+  ",'" + type + "');";
		int status = DBConstants.SQL_EXCUTION_ERROR;
		try {
			status = statement.executeUpdate(sql) > 0 ? DBConstants.SUCCESS : DBConstants.SQL_EXCUTION_ERROR;
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("error occured when creating proj " + sql + "\n" + e.toString());
		}
		return status;
	}

	/**
	 * list all projs of a user
	 * 
	 * @param username
	 * @return list of projs, null when failed
	 */
	public List<ProjInfo> listUserProj(String username) {
		String sql = "SELECT * FROM " + DBConstants.PROJ_TABLE + " WHERE " + "username='" + username + "';";
		try {
			ResultSet set = statement.executeQuery(sql);
			List<ProjInfo> list = new ArrayList<>();
			while (set.next()) {
				list.add(ProjInfo.fromSQLResult(set));
			}
			return list;
		} catch (SQLException e) {
			logger.error("error occured when listing proj " + sql + "\n" + e.toString());
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * update status of a given proj
	 * 
	 * @param username
	 * @param projName
	 * @param newStatus
	 * @return status code
	 */
	public int updateProjStatus(String username, String projName, int newStatus) {
		String sql = "UPDATE " + DBConstants.PROJ_TABLE + " SET status =" + newStatus + " where username='" + username
				+ "' AND projName='" + projName + "';";
		int status = DBConstants.SQL_EXCUTION_ERROR;
		try {
			status = statement.executeUpdate(sql) > 0 ? DBConstants.SUCCESS : status;
		} catch (SQLException e) {
			logger.error("error occured when updating proj status" + sql + "\n" + e.toString());
			e.printStackTrace();
		}
		return status;
	}

	/** set value for an int type field in a given proj
	 * @param username
	 * @param projName
	 * @param fieldName
	 * @param newValue
	 * @return status code
	 */
	public int updateProjInt(String username, String projName, String fieldName, int newValue) {
		String sql = "UPDATE " + DBConstants.PROJ_TABLE + " SET " + fieldName + " =" + newValue 
				+ " where username='" + username
				+ "' AND projName='" + projName + "';";
		int status = DBConstants.SQL_EXCUTION_ERROR;
		try {
			status = statement.executeUpdate(sql) > 0 ? DBConstants.SUCCESS : status;
		} catch (SQLException e) {
			logger.error("error occured when updating proj status" + sql + "\n" + e.toString());
			e.printStackTrace();
		}
		return status;
	}
	
	/** set value for a string type field in a given proj
	 * @param username
	 * @param projName
	 * @param fieldName
	 * @param newValue
	 * @return status code
	 */
	public int updateProjString(String username, String projName, String fieldName, String newValue) {
		String sql = "UPDATE " + DBConstants.PROJ_TABLE + " SET " + fieldName + " ='" + newValue 
				+ "' where username='" + username
				+ "' AND projName='" + projName + "';";
		int status = DBConstants.SQL_EXCUTION_ERROR;
		try {
			status = statement.executeUpdate(sql) > 0 ? DBConstants.SUCCESS : status;
		} catch (SQLException e) {
			logger.error("error occured when updating proj status" + sql + "\n" + e.toString());
			e.printStackTrace();
		}
		return status;
	}
	
	/** set value for a long type field in a given proj
	 * @param username
	 * @param projName
	 * @param fieldName
	 * @param newValue
	 * @return status code
	 */
	public boolean updateProjLong(String username, String projName, String fieldName, Long newValue) {
		String sql = "UPDATE " + DBConstants.PROJ_TABLE + " SET " + fieldName + " =" + newValue 
				+ " where username='" + username
				+ "' AND projName='" + projName + "';";
		boolean success = false;
		try {
			success = statement.executeUpdate(sql) > 0;
		} catch (SQLException e) {
			logger.error("error occured when updating proj status" + sql + "\n" + e.toString());
			e.printStackTrace();
		}
		return success;
	}
	
	/**
	 * rename a given proj
	 * 
	 * @param username
	 * @param oldProjName
	 * @param newProjName
	 * @return status code
	 */
	public int renameProj(String username, String oldProjName, String newProjName) {
		if(findProj(username, newProjName) != null)
			return DBConstants.PROJ_ALREADY_EXIST;
		String sql = "UPDATE " + DBConstants.PROJ_TABLE + " SET projName='" + newProjName + "' " + " WHERE username='"
				+ username + "' AND projName='" + oldProjName + "';";
		int status = DBConstants.SQL_EXCUTION_ERROR;
		try {
			status = statement.executeUpdate(sql) > 0 ? DBConstants.SUCCESS : status;
		} catch (SQLException e) {
			logger.error("error occured when renaming proj" + sql + "\n" + e.toString());
			e.printStackTrace();
		}
		return status;
	}

	/**
	 * find a proj by username and projname
	 * 
	 * @param username
	 * @param projName
	 * @return
	 */
	public ProjInfo findProj(String username, String projName) {
		String sql = "SELECT * FROM " + DBConstants.PROJ_TABLE + " WHERE " + "username='" + username
				+ "' AND projName='" + projName + "';";
		try {
			ResultSet set = statement.executeQuery(sql);
			if (set.next())
				return ProjInfo.fromSQLResult(set);
		} catch (SQLException e) {
			logger.error("error occured when listing proj " + sql + "\n" + e.toString());
			e.printStackTrace();
		}
		return null;
	}
	
	/** deletet a proj by username and projName
	 * @param username
	 * @param projName
	 * @return status code
	 */
	public int deleteProj(String username, String projName) {
		String sql = "DELETE FROM " + DBConstants.PROJ_TABLE + 
				" where username='" + username
				+ "' AND projName='" + projName + "';";
		int status = DBConstants.SQL_EXCUTION_ERROR;
		try {
			status = statement.executeUpdate(sql) > 0 ? DBConstants.SUCCESS : DBConstants.NO_SUCH_PROJ;
		} catch (SQLException e) {
			logger.error("error occured when updating proj status" + sql + "\n" + e.toString());
			e.printStackTrace();
			status = DBConstants.SQL_EXCUTION_ERROR;
		}
		return status;
	}
	
	
	/** find projs of a user by given int filed
	 * @param username
	 * @param fieldName
	 * @param value
	 * @return list of projs
	 */
	public List<ProjInfo> findProjsByInt(String username, String fieldName, int value) {
		String sql = "SELECT * FROM " + DBConstants.PROJ_TABLE + " WHERE " + "username='" + username + "' "
				+ "AND " + fieldName + "=" + value + ";";
		try {
			ResultSet set = statement.executeQuery(sql);
			List<ProjInfo> list = new ArrayList<>();
			while (set.next()) {
				list.add(ProjInfo.fromSQLResult(set));
			}
			return list;
		} catch (SQLException e) {
			logger.error("error occured when listing proj " + sql + "\n" + e.toString());
			e.printStackTrace();
		}
		return null;
	}
	
	/** find projs of a user by given string filed
	 * @param username
	 * @param fieldName
	 * @param value
	 * @return list of projs
	 */
	public List<ProjInfo> findProjsByString(String username, String fieldName, String value) {
		String sql = "SELECT * FROM " + DBConstants.PROJ_TABLE + " WHERE " + "username='" + username + "' "
				+ "AND " + fieldName + "='" + value + "';";
		try {
			ResultSet set = statement.executeQuery(sql);
			List<ProjInfo> list = new ArrayList<>();
			while (set.next()) {
				list.add(ProjInfo.fromSQLResult(set));
			}
			return list;
		} catch (SQLException e) {
			logger.error("error occured when listing proj " + sql + "\n" + e.toString());
			e.printStackTrace();
		}
		return null;
	}
	
	
}
