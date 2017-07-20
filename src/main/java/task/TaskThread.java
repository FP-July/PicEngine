package task;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		this.taskID = taskID;
		this.args = args;
	}
	
	private void handleFailure() {
		try {
			DaoManager daoManager = DaoManager.getInstance();
			ProjDao projDao = daoManager.getProjDao();
			projDao.updateProjStatus(username, taskName, ProjInfo.statusEnum.error.ordinal());
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			logger.error("Task {} of {} failed but unhandled because {}",
					taskName, username, e.toString());
		}
	}
	
	@Override
	public void run() {
		try {
			this.task.run(args);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Task {} of {} failed because {}",
					taskName, username, e.toString());
			handleFailure();
		}
	}

}
