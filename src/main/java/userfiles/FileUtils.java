package userfiles;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import servlet.ServletConstants;
import task.TaskUtils;

public class FileUtils {
	private static Logger logger = LoggerFactory.getLogger(FileUtils.class);
	public static final String ZIPPED_RESULT_NAME = "result.zip"; 
	
	/** a download interface for user to download a single file in a specific path
	 * @param username
	 * @param taskID
	 * @param filename
	 * @param oStream
	 * @return
	 */
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

	/** 
     * 将存放在sourceFilePath目录下的源文件，打包成fileName名称的zip文件，并存放到zipFilePath路径下 
     * @param sourceFilePath :待压缩的文件路径 
     * @param zipFilePath :压缩后存放路径 
     * @return 
     */  
    public static void compressDir(String sourceFilePath,String zipFilePath){  
        try {
			boolean flag = false;  
			FileSystem fSystem = FileSystem.get(TaskUtils.HDFS_URI, new  Configuration());
			RemoteIterator<LocatedFileStatus> iterator = fSystem.listFiles(new Path(sourceFilePath), true);
			Path zipPath = new Path(zipFilePath);
			FSDataOutputStream oStream = fSystem.create(zipPath, true);
			ZipOutputStream zStream = new ZipOutputStream(new BufferedOutputStream(oStream.getWrappedStream()));
			FSDataInputStream iStream = null;
			while(iterator.hasNext()) {
				LocatedFileStatus fileStatus = iterator.next();
				Path path = fileStatus.getPath();
				byte[] buffer = new byte[4096];
				
				iStream = fSystem.open(path);
				ZipEntry zipEntry = new ZipEntry(path.getName());
				zStream.putNextEntry(zipEntry);
				
				int readCnt = 0;
				while((readCnt = iStream.read(buffer)) != -1) {
					zStream.write(buffer, 0, readCnt);
				}
				iStream.close();
			}
			zStream.flush();
			zStream.close();
			logger.info("compress {} finished", sourceFilePath);
		} catch (IllegalArgumentException | IOException e) {
			logger.error("compress failed for {} because", sourceFilePath, e.toString());
			e.printStackTrace();
		} 
    }  

    /** compress and send the result of a task specified by taskID to user
     *  through oStream
     * @param username
     * @param taskID
     * @param oStream
     * @throws IOException 
     */
    public static void userDownloadResult(String username, String taskID, OutputStream oStream) throws IOException {
    	String workingDir = TaskUtils.getWorkingDir(username, taskID);
    	String resultDir = TaskUtils.getResultDir(workingDir);
    	String zipDir = workingDir + ZIPPED_RESULT_NAME;
    	FileSystem fSystem = FileSystem.get(TaskUtils.HDFS_URI, new Configuration());
    	if(!(fSystem.exists(new Path(zipDir))))
    		compressDir(resultDir, zipDir);
    	userDownload(username, taskID, ZIPPED_RESULT_NAME, oStream);
    }
}
