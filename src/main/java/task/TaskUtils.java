package task;

import java.io.File;
import java.net.URI;

public class TaskUtils {
	
	public static final String HADOOP_CONF_PATH = "hadoopPath/";
	public static final String USER_FOLDER = "/userfiles";
	public static final String SRC_FOLDER = "src";
	public static final String RST_FOLDER = "result";
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
}
