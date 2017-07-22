package userfiles;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import servlet.ServletConstants;
import task.TaskUtils;

public class FileUtils {
	private static Logger logger = LoggerFactory.getLogger(FileUtils.class);
	
	public static int userDownload(String username, String taskID, String filename, OutputStream oStream) {
		FileSystem fSystem = null;
		try {
			fSystem = FileSystem.get(TaskUtils.HDFS_URI, new Configuration());
		} catch (IOException e) {
			logger.error("cannot get file system when user {} download {} from {}, for {}",
					username, filename, taskID, e.toString());
			e.printStackTrace();
			return ServletConstants.HADOOP_FS_CRASH;
		}
		
		String workingDir = TaskUtils.getWorkingDir(username, taskID);
		String fPath = workingDir + filename;
		Path path = new Path(fPath);
		try {
			if(!fSystem.exists(path)) {
				return ServletConstants.NO_SUCH_FILE;
			}
			if(!fSystem.isFile(path)) {
				return ServletConstants.NOT_A_FILE;
			}
			FSDataInputStream iStream = fSystem.open(path);
			byte[] buffer = new byte[4096];
			int readCnt = 0;
			while((readCnt = iStream.read(buffer)) != -1 ) {
				oStream.write(buffer, 0, readCnt);
			}
			oStream.flush();
			oStream.close();
			iStream.close();
		} catch (IOException e) {
			logger.error("cannot get file when user {} download {} from {}, for {}",
					username, filename, taskID, e.toString());
			e.printStackTrace();
			return ServletConstants.HADOOP_FS_CRASH;
		}
		
		return ServletConstants.SUCCESS;
	}
}
