package task;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.mapreduce.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dao.DBConstants;
import dao.DaoManager;
import dao.ProjDao;
import model.ProjInfo;

/** this thread is created for running a specific task
 * @author jt
 *
 */
public class TaskThread extends Thread {

	Logger logger = LoggerFactory.getLogger(TaskThread.class);
	
	private ITask task;
	private String taskName;
	private String username;
	private String taskID;
	private String[] args;
	
	private static final int PROGRESS_UPDATE_INTERVAL = 5 * 1000;  // in ms
	private Timer progressTimer = new Timer();
	private TimerTask progressTask = new TimerTask() {
		
		@Override
		public void run() {
			updateProgress();
		}
	};
	
	public void setup(ITask task, String username, String taskID, String taskName, String[] args) {
		this.task = task;
		this.username = username;
		this.taskName = taskName;
		this.taskID = taskID;
		this.args = args;
	}
	
	private void handleFailure() {
		try {
			DaoManager daoManager = DaoManager.getInstance();
			ProjDao projDao = daoManager.getProjDao();
			int status = projDao.updateProjStatus(username, taskName, ProjInfo.statusEnum.error.ordinal());
			if(status != DBConstants.SUCCESS) {
				logger.error(DBConstants.codeToString(status));
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			logger.error("Task {} of {} failed but unhandled because {}",
					taskName, username, e.toString());
		}
	}
	
	private void examineResult() throws IOException, ClassNotFoundException, SQLException {
		String workingDir = TaskUtils.getWorkingDir(username, taskID);
		String resultDir = TaskUtils.getResultDir(workingDir);
		FileSystem fSystem = FileSystem.get(TaskUtils.HDFS_URI, new Configuration());
		RemoteIterator<LocatedFileStatus> iterator = fSystem.listFiles(new Path(resultDir), false);
		boolean findSuccess = false;
		while(iterator.hasNext()){
			LocatedFileStatus fileStatus = iterator.next();
			if(fileStatus.getPath().getName().contains("_SUCCESS")) {
				findSuccess = true;
				break;
			}
		}
		DaoManager daoManager = DaoManager.getInstance();
		ProjDao projDao = daoManager.getProjDao();
		if(findSuccess) {
			projDao.updateProjStatus(username, taskName, ProjInfo.statusEnum.finished.ordinal());
			projDao.updateProjLong(username, taskName, "finishedTime", System.currentTimeMillis());
		} else {
			projDao.updateProjStatus(username, taskName, ProjInfo.statusEnum.error.ordinal());
		}
	}
	
	private void updateProgress() {
		Job job = task.getJob();
		try {
			float progress = task.getProgress();
			String workingDir = TaskUtils.getWorkingDir(username, taskID);
			String progressDir = workingDir + File.separator + TaskUtils.PROGRESS_FILE;
			FileSystem fSystem = FileSystem.get(TaskUtils.HDFS_URI, new Configuration());
			FSDataOutputStream fOutputStream = fSystem.create(new Path(progressDir), true);
			fOutputStream.writeFloat(progress);
			fOutputStream.write('\n');
			fOutputStream.flush();
			fOutputStream.close();
		} catch (Exception e) {
			logger.error("update progress for {} of {} failed, because {}", taskName, username, e.toString());
			if(!(e instanceof IllegalStateException))
				e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			progressTimer.schedule(progressTask, 0, PROGRESS_UPDATE_INTERVAL);
			this.task.run(args);
			progressTimer.cancel();
			examineResult();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Task {} of {} failed because {}",
					taskName, username, e.toString());
			handleFailure();
		}
	}

}
