package task;

import bean.Task;
import raytracing.mapreduce.RayTracerDriver;
import video.VideoTask;

public class TaskFactory {
	public static ITask create(String taskType) {
		if(taskType.equals(Task.debug)) {
			return new TaskFrame();
		} else if (taskType.equals(Task.picture)) {
			return new RayTracerDriver();
		} else if (taskType.equals(Task.video)) {
			return new VideoTask();
		}
		return null;
	}
}
