package task;

import java.io.IOException;
import java.net.URI;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** this thread periodically scan hadoop generated log dir
 *  and move the log to the folder of its user
 * @author jt
 *
 */
public class LogTransferThread extends Thread {
	private static Logger logger = LoggerFactory.getLogger(LogTransferThread.class);
	
	private static final String HADOOP_LOG_DIR = TaskUtils.HADOOP_LOG_DIR;
	private static final URI HDFS_URI = TaskUtils.HDFS_URI;
	private static final int SCAN_INTERVAL = 60 * 1000; // in ms
	
	@Override
	public void run() {
		FileSystem fSystem = null;
		try {
			fSystem = FileSystem.get(HDFS_URI, new Configuration());
		} catch (IOException e) {
			logger.error("cannot get file system due to {}", e.toString());
			e.printStackTrace();
			return;
		}
		while(true) {
			try {
				RemoteIterator<LocatedFileStatus> iterator = fSystem.listFiles(new Path(HADOOP_LOG_DIR), true);
				
				while(iterator.hasNext()) {
					LocatedFileStatus fileStatus = iterator.next();
					Path path = fileStatus.getPath();
					String logName = path.getName();
					if(logName.endsWith(".jhist")) {
						String[] names = parseLogName(logName);
						String username = names[0], taskID = names[1];
						String workingDir = TaskUtils.getWorkingDir(username, taskID);
						String logFile = TaskUtils.getHadoopLogPath(workingDir);
						Path newPath = new Path(logFile);
						fSystem.mkdirs(newPath.getParent());
						boolean renameResult = fSystem.rename(path, newPath);
						if(!renameResult) {
							logger.warn("rename {} to {} failed", path.toString(), newPath.toString());
						}
					}
					else if(fSystem.isFile(path)) {
						fSystem.delete(path, false);
					}
				}
			} catch (Exception e1) {
				logger.error("fail to scan due to {}", e1.toString());
				e1.printStackTrace();
			}
			
			try {
				Thread.sleep(SCAN_INTERVAL);
			} catch (InterruptedException e) {
				logger.error("unexpected interuption {}", e.toString());
				e.printStackTrace();
			}
		}
	}
	
	/** parse username([0]) and taskID([1]) from given log name
	 * @param logName
	 * @return username([0]) and taskID([1])
	 */
	private String[] parseLogName(String logName) {
		String[] sections = logName.split("-");
		sections = sections[3].split("_");
		return sections;
	}
}
