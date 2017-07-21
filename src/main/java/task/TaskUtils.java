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
	public static final String MAPREDUCE_LOG_FILE = "hadoop_log";
	public static final String HADOOP_LOG_DIR = "/tmp/hadoop-yarn/staging/history/done";
	
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

	/** search the log dir, find the log file corresponding to username and taskName
	 *  and return its content
	 * @param username
	 * @param taskName
	 * @return a list of logs
	 * @throws IOException
	 */
	public static List<String> getLogs(String username, String taskName) throws IOException {
		String logName = username + "_" + taskName + "-";
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
}
