package task;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.tools.doclint.Checker.Flag;

import dao.DaoManager;
import dao.ProjDao;
import model.ProjInfo;

public class TaskUtils {
	
	private static Logger logger = LoggerFactory.getLogger(TaskUtils.class);
	
	public static final String HADOOP_CONF_PATH = "hadoopPath/";
	public static final String USER_FOLDER = "/userfiles";
	public static final String SRC_FOLDER = "src";
	public static final String RST_FOLDER = "result";
	public static final String PROGRESS_FILE = "PROGRESS";
	public static final String HADOOP_LOG_FILE = "hadoop_log";
	
	public static final URI HDFS_URI = URI.create("hdfs://localhost:9000/");
	
	public static String getWorkingDir(String username, String taskID) {
		return USER_FOLDER + File.separator + username + File.separator + taskID + File.separator;
	}
	
	public static String getSrcDir(String workingDir) {
		return workingDir + SRC_FOLDER;
	}
	
	public static String getResultDir(String workingDir) {
		return workingDir + RST_FOLDER;
	}

	public static String getHadoopConfPath() {
		return HADOOP_CONF_PATH;
	}
	public static String getHadoopLogPath(String workingDir) {
		return workingDir + HADOOP_LOG_FILE;
	}
	
	public static float[] getProgress(String username, String taskName) {
		DaoManager daoManager = null;
		try {
			daoManager = DaoManager.getInstance();
		} catch (ClassNotFoundException | SQLException e) {
			logger.error("failed to get dao when getting progress, for {}",e.toString());
			return null;
		}
		ProjDao projDao = daoManager.getProjDao();
		
		ProjInfo info = projDao.findProj(username, taskName);
		if(info == null) {
			logger.error("can not find task {} of {}",taskName, username);
			return null;
		}
		if(info.status == ProjInfo.statusEnum.finished.ordinal()) 
			return new float[]{1.0f, 1.0f};
		else if(info.status != ProjInfo.statusEnum.ongoing.ordinal()) {
			return new float[]{-1.0f, -1.0f};
		}
		
		String taskID = String.valueOf(info.projID);
		String workingDir = TaskUtils.getWorkingDir(username, taskID);
		String progressDir = workingDir + File.separator + TaskUtils.PROGRESS_FILE;
		try {
			FileSystem fSystem = FileSystem.get(TaskUtils.HDFS_URI, new Configuration());
			FSDataInputStream iStream = fSystem.open(new Path(progressDir));
			String line = iStream.readLine();
			String[] sections = line.split(",");
			if(sections.length < 2) 
				return null;
			else {
				float mapProgress = Float.parseFloat(sections[0]);
				float reduceProgress = Float.parseFloat(sections[1]);
				return new float[] {mapProgress, reduceProgress};
			}
				
		} catch (IOException e) {
			logger.error("failed to get fs when getting progress, for {}",e.toString());
		}
		return null;
	}
}
