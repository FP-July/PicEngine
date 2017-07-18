package model;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.spi.DirStateFactory.Result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjInfo {
	private static Logger logger = LoggerFactory.getLogger(ProjInfo.class);
	
	static public enum statusEnum {
		init, ready, running, stop, done, error
	};
	
	public int projectID = -1;
	public String username;
	public String projName;
	public int status;
	public int runtime;
	public int progress;
	public long createTime;
	
	public static ProjInfo fromSQLResult(ResultSet set) {
		try {
			if(set == null)
				 return null;
			ProjInfo info = new ProjInfo();
			info.projectID = set.getInt("projectID");
			info.username = set.getString("username");
			info.projName = set.getString("projName");
			info.status = set.getInt("status");
			info.runtime = set.getInt("runtime");
			info.progress = set.getInt("progress");
			info.createTime = set.getLong("createTime");
			return info;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
