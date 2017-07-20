package task;

import bean.Task;

public class TaskFactory {
	public static ITask create(String taskType) {
		if(taskType.equals(Task.debug)) {
			return new TaskFrame();
		}
		return null;
	}
}
