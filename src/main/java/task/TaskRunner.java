package task;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	public void runTask(String username, String taskName, String taskID, String taskType) {
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
