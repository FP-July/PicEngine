package task;

import org.apache.hadoop.mapreduce.Job;

public interface ITask {
	/** run the map-reduce job, args are
	 * 	username, taskName, taskID, hadoopConfPath
	 * @param args
	 * @throws Exception
	 */
	public void run(String[] args) throws Exception;
	/** 
	 * @return the job related to the task
	 */
	public Job getJob();
	
	public float getProgress();
}
