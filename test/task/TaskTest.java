package task;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.SQLException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bean.Task;
import dao.DaoManager;
import dao.ProjDao;
import model.ProjInfo;

public class TaskTest {
	private static Logger logger = LoggerFactory.getLogger(TaskTest.class);

	DaoManager daoManager;
	ProjDao projDao;
	
	@Before
	public void beforTest(){
		try {
			daoManager = DaoManager.getInstance();
			projDao = daoManager.getProjDao();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testSuccess() throws IOException, InterruptedException {
		String username = "admin";
		String taskName = "runTest";
		String taskType = Task.debug;
		projDao.createProj(taskName, username, Task.video);
		String taskID = String.valueOf(projDao.findProj(username, taskName).projID);
		String workingDir = TaskUtils.getWorkingDir(username, taskID);
		String srcDir = TaskUtils.getSrcDir(workingDir);
		FileSystem fSystem = FileSystem.get(TaskUtils.HDFS_URI, new Configuration());
		fSystem.mkdirs(new Path(srcDir));
		
		TaskRunner taskRunner = TaskRunner.getInstance();
		taskRunner.runTask(username, taskName, taskID, taskType);
		
		// give some time to run
		Thread.sleep(10000);
		assertTrue(TaskUtils.checkStatus(username, taskID) == ProjInfo.statusEnum.ongoing.ordinal());
		Thread.sleep(10000);
		
		ProjInfo projInfo = projDao.findProj(username, taskName);
		assertTrue(projInfo.status == ProjInfo.statusEnum.finished.ordinal());
		float[] progress = TaskUtils.getProgress(username, taskName);
		assertTrue(progress[0] == 1.0f && progress[1] == 1.0f);
		/*try {
			List<String> logs = TaskUtils.getLogs(username, taskName);
			for(String log : logs)
				System.out.println(log);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}*/
	}
	
	@Test
	public void testFail() throws IOException {
		String username = "admin";
		String taskName = "runTest2";
		String taskType = Task.debug;
		projDao.createProj(taskName, username, Task.video);
		String taskID = String.valueOf(projDao.findProj(username, taskName).projID);
		// do not make src dir to cause error
		/*String workingDir = TaskUtils.getWorkingDir(username, taskID);
		String srcDir = TaskUtils.getSrcDir(workingDir);
		FileSystem fSystem = FileSystem.get(TaskUtils.HDFS_URI, new Configuration());
		fSystem.mkdirs(new Path(srcDir));*/
		
		TaskRunner taskRunner = TaskRunner.getInstance();
		taskRunner.runTask(username, taskName, taskID, taskType);
		// give some time to run
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ProjInfo projInfo = projDao.findProj(username, taskName);
		assertTrue(projInfo.status == ProjInfo.statusEnum.error.ordinal());
	}

}
