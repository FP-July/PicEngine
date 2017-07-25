package task;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.log4j.pattern.LogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dao.DaoManager;
import dao.ProjDao;
import model.ProjInfo;

public class TaskUtils {
	
	private static Logger logger = LoggerFactory.getLogger(TaskUtils.class);
	
	public static final String HADOOP_CONF_PATH = "/hadoop_conf/";
	public static final String USER_FOLDER = "/userfiles";
	public static final String SRC_FOLDER = "src";
	public static final String RST_FOLDER = "result";
	public static final String PROGRESS_FILE = "PROGRESS";
	public static final String MAPREDUCE_LOG_FILE = "MR_log";  // this is for custom(our) log
	public static final String HADOOP_LOG_FILE = "Hadoop_log"; // this is for hadoop-generated log 
	public static final String HADOOP_LOG_DIR = "/tmp/hadoop-yarn/staging/history/done";  // where hadoop-generated logs naturally placed
	
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
	
	public static String getMRLogPath(String workingDir) {
		return workingDir + MAPREDUCE_LOG_FILE;
	}
	
	/** get the progress of a task, in the form of two floats
	 *  [0] is map progress and [1] is reduce progress
	 *  if the task has finished, both will be 1.0f
	 *  else if the task is not running, both will be -1.0f or null if error occurs
	 * @param username
	 * @param taskName
	 * @return {mapProgress, reduceProgress}
	 */
	public static float getProgress(String username, String taskName) {
		DaoManager daoManager = null;
		try {
			daoManager = DaoManager.getInstance();
		} catch (ClassNotFoundException | SQLException e) {
			logger.error("failed to get dao when getting progress, for {}",e.toString());
			return -1.0f;
		}
		ProjDao projDao = daoManager.getProjDao();
		
		ProjInfo info = projDao.findProj(username, taskName);
		if(info == null) {
			logger.error("can not find task {} of {}",taskName, username);
			return -1.0f;
		}
		if(info.status == ProjInfo.statusEnum.finished.ordinal()) 
			return -1.0f;
		else if(info.status != ProjInfo.statusEnum.ongoing.ordinal()) {
			return -1.0f;
		}
		
		String taskID = String.valueOf(info.projID);
		String workingDir = TaskUtils.getWorkingDir(username, taskID);
		String progressDir = workingDir + File.separator + TaskUtils.PROGRESS_FILE;
		try {
			FileSystem fSystem = FileSystem.get(TaskUtils.HDFS_URI, new Configuration());
			FSDataInputStream iStream = fSystem.open(new Path(progressDir));
			String line = iStream.readLine();
			if(line.length() > 0)
				return Float.parseFloat(line);
				
		} catch (Exception e) {
			logger.error("failed to get fs when getting progress, for {}",e.toString());
		}
		return -1.0f;
	}

	
	public static List<String> getLogs(String username, String taskName) throws IOException {
		ProjDao projDao = null;
		try {
			projDao = DaoManager.getInstance().getProjDao();
		} catch (ClassNotFoundException | SQLException e) {
			logger.error("fail to get dao, reason: {}", e.toString());
			e.printStackTrace();
			return null;
		}
		ProjInfo info = projDao.findProj(username, taskName);
		if(info == null) {
			logger.warn("find no info about {} of {}", taskName, username);
			return null;
		}
		return getLogs(username, info.projID);
	}
	
	public static List<String> getLogs(String username, int taskID) throws IOException {
		List<String> list = localGetLogs(username, taskID);
		if(list.size() != 0) {
			return list;
		} else {
			list = remoteGetLogs(username, taskID);
			return list;
		}
	}
	
	/** seek hadoop log in user folder
	 * @param username
	 * @param taskID
	 * @return a list of hadoop logs, null if the log does not exist
	 * @throws IOException
	 */
	private static List<String> localGetLogs(String username, int taskID) throws IOException {
		String workingDir = getWorkingDir(username, String.valueOf(taskID));
		String hadoop_log = getHadoopLogPath(workingDir);
		String MR_log = getMRLogPath(workingDir);
		FileSystem fSystem = FileSystem.get(HDFS_URI, new Configuration());
		Path logPath = new Path(hadoop_log);
		
		List<String> list = new ArrayList<>();
		if(fSystem.exists(logPath)) {
			FSDataInputStream iStream = fSystem.open(logPath);
			BufferedReader bReader = new BufferedReader(new InputStreamReader(iStream));
			String line;
			while((line = bReader.readLine()) != null) {
				if(line.length() > 0)
					list.add(line);
			}
			bReader.close();
		}
		logPath = new Path(MR_log);
		if(fSystem.exists(logPath)) {
			FSDataInputStream iStream = fSystem.open(logPath);
			BufferedReader bReader = new BufferedReader(new InputStreamReader(iStream));
			String line;
			while((line = bReader.readLine()) != null) {
				if(line.length() > 0)
					list.add(line);
			}
			bReader.close();
		}
		return list;
	}
	
	/** search the log dir, find the log file corresponding to username and taskName
	 *  and return its content
	 * @param username
	 * @param taskName
	 * @return a list of logs
	 * @throws IOException
	 */
	private static List<String> remoteGetLogs(String username, int taskID) throws IOException {
		String logName = username + "_" + taskID + "-";
		FileSystem fSystem = FileSystem.get(HDFS_URI, new Configuration());
		// TODO specify log path to speed up
		RemoteIterator<LocatedFileStatus> iterator = fSystem.listFiles(new Path(HADOOP_LOG_DIR), true);
		
		List<String> list = new ArrayList<>();
		while(iterator.hasNext()) {
			LocatedFileStatus fileStatus = iterator.next();
			Path path = fileStatus.getPath();
			if(path.getName().contains(logName)) {
				FSDataInputStream iStream = fSystem.open(path);
				BufferedReader bReader = new BufferedReader(new InputStreamReader(iStream));
				String line;
				while((line = bReader.readLine()) != null) {
					if(line.length() > 0)
						list.add(line);
				}
				break;
			}
		}
		return list;
	}

	/** check the status of a task by looking for _SUCCESS
	 *  notice : the task to be checked is assumed started, so if none of above are
	 *  found, the task will be marked ongoing
	 * @param username
	 * @param taskID
	 * @return new status
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	public static int checkStatus(String username, String taskID) throws IllegalArgumentException, IOException {
		String workingDir = getWorkingDir(username, taskID);
		String resultDir = getResultDir(workingDir);
		String successFile = resultDir + File.separator + "_SUCCESS";
		FileSystem fSystem = FileSystem.get(HDFS_URI, new Configuration());
		// TODO detect dead task
		if(fSystem.exists(new Path(successFile))) {
			return ProjInfo.statusEnum.finished.ordinal();
		} else {
			return ProjInfo.statusEnum.ongoing.ordinal();
		}
	}
	
	/** clean user files and log files of a task
	 * @param username
	 * @param taskName
	 * @param taskID
	 */
	public static void cleanTaskFile(String username, String taskName, String taskID) {
		String workingDir = TaskUtils.getWorkingDir(username, taskID);
		try {
			FileSystem fSystem = FileSystem.get(TaskUtils.HDFS_URI, new Configuration());
			// clean user files
			Path workingPath = new Path(workingDir);
			if(fSystem.exists(workingPath))
				fSystem.delete(workingPath, true);
			
		} catch (IOException e) {
			logger.error("failed to get fs when getting progress, for {}",e.toString());
		}
	}
	
}
