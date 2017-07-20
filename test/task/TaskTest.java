package task;

import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskTest {
	private static Logger logger = LoggerFactory.getLogger(TaskTest.class);

	@Test
	public void test() {
		TaskRunner taskRunner = TaskRunner.getInstance();
		taskRunner.runTask("admin", "564988982469", "27");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
