package task;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dao.DaoManager;
import dao.ProjDao;
import model.ProjInfo;
import servlet.ServletConstants;

public class TaskRunner {
	private static Logger logger = LoggerFactory.getLogger(TaskRunner.class);
	private static TaskRunner instance;
	
	
	private TaskRunner() {
		
	}
	
	public static TaskRunner getInstance() {
		if(instance == null) {
			instance = new TaskRunner();
		}
		return instance;
	}
	
	public int runTask(String username, String taskName, String taskID, String taskType) {
		String workingDir = TaskUtils.getWorkingDir(username, taskID);
		String srcDir = TaskUtils.getSrcDir(workingDir);
		String resultDir = TaskUtils.getResultDir(workingDir);
		String logFile = TaskUtils.getHadoopLogPath(workingDir);
		String[] args = new String[] {srcDir, resultDir, logFile, TaskUtils.getHadoopConfPath()};
		ITask task = TaskFactory.create(taskType);
		try {
			TaskThread taskThread = new TaskThread();
			taskThread.setup(new TaskFrame(), username, taskID, taskName, args);
			taskThread.start();
			ProjDao projDao = DaoManager.getInstance().getProjDao();
			int status = projDao.updateProjStatus(username, taskName, ProjInfo.statusEnum.ongoing.ordinal());
			if(status != ServletConstants.SUCCESS) {
				task.getJob().killJob();
				logger.error("failed to run task {} of {} because {}"
						,taskName, username, ServletConstants.codeToString(status) );
				return status;
			}
		} catch (Exception e) {
			logger.error("failed to run task {} of {} because {}"
					,taskName, username, e.toString());
			return ServletConstants.TASK_RUN_FAIL;
		}
		return ServletConstants.SUCCESS;
	}
}
