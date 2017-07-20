package task;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dao.DBConstants;
import dao.DaoManager;
import dao.ProjDao;
import model.ProjInfo;

public class TaskThread extends Thread {

	Logger logger = LoggerFactory.getLogger(TaskThread.class);
	
	private ITask task;
	private String taskName;
	private String username;
	private String taskID;
	private String[] args;
	
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
		} else {
			projDao.updateProjStatus(username, taskName, ProjInfo.statusEnum.error.ordinal());
		}
	}
	
	@Override
	public void run() {
		try {
			this.task.run(args);
			examineResult();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Task {} of {} failed because {}",
					taskName, username, e.toString());
			handleFailure();
		}
	}

}
