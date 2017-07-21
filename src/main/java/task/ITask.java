package task;

import org.apache.hadoop.mapreduce.Job;

public interface ITask {
	public void run(String[] args) throws Exception;
	public Job getJob();
}
