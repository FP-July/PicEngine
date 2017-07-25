package model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bean.Task;

public class ProjInfo {
	private static Logger logger = LoggerFactory.getLogger(ProjInfo.class);
	
	static public enum statusEnum {
		init, ready, ongoing, stop, finished, error
	};
	
	public int projID = -1;
	public String username;
	public String projName;
	public String type;
	public int status;
	public int runtime;
	public int progress;
	public long createTime;
	
	public static ProjInfo fromSQLResult(ResultSet set) {
		try {
			if(set == null)
				 return null;
			ProjInfo info = new ProjInfo();
			info.projID = set.getInt("projID");
			info.username = set.getString("username");
			info.projName = set.getString("projName");
			info.status = set.getInt("status");
			info.runtime = set.getInt("runtime");
			info.progress = set.getInt("progress");
			info.createTime = set.getLong("createTime");
			info.type = set.getString("type");
			return info;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public JSONObject toJSON() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("projID", projID);
		jsonObject.put("username", username);
		jsonObject.put("status", status);
		jsonObject.put("runtime", runtime);
		jsonObject.put("progress", progress);
		jsonObject.put("createTime", createTime);
		jsonObject.put("type", type);
	
		return jsonObject;
	}
	
	public static String statusString(int status) {
		if(status == statusEnum.init.ordinal())
			return Task.init;
		if(status == statusEnum.finished.ordinal())
			return Task.finished;
		if(status == statusEnum.error.ordinal())
			return Task.error;
		if(status == statusEnum.ready.ordinal())
			return Task.ready;
		if(status == statusEnum.ongoing.ordinal())
			return Task.ongoing;
		if(status == statusEnum.stop.ordinal())
			return Task.stop;
		return "unknown";
	}
	
	public Task convertToTask() {
		Task task = new Task();
		task.setDate(new Date(createTime));
		task.setId(projID);
		task.setType(type);
		task.setState(statusString(status));
		task.setFileLocation(username + "/" + projID);
		task.setName(projName);
		task.setPercent(progress);
		task.setMinutes(runtime);
		task.setUsername(username);
		
		return task;
	}
}
