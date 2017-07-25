package task;

import bean.Task;
import raytracing.mapreduce.RayTracerDriver;

public class TaskFactory {
	public static ITask create(String taskType) {
		if(taskType.equals(Task.debug)) {
			return new TaskFrame();
		} else if (taskType.equals(Task.picture)) {
			return new RayTracerDriver();
		}
		return null;
	}
}
