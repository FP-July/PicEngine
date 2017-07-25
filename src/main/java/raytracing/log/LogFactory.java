package raytracing.log;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;

import raytracing.load.BasicLoader;

public class LogFactory {

	private static HashMap<String, ILog> logs = new HashMap<String, ILog>();
	private static URI HDFS_URI = null;
	private static String logPath = null;
	private static BasicLoader.ENV env;
	private static boolean ignore = false;
	
	public static void setHdfsUri(URI uri) {
		HDFS_URI = uri;
	}
	public static void setParams(String path, BasicLoader.ENV en) {
		logPath = path;
		env = en;
		ignore = false;
	}
	public static void setIgnore(boolean ig) {
		ignore = ig;
	}
	
	public static ILog getInstance(String logName) {
		if (logs.containsKey(logName)) return logs.get(logName);
		try {
			ILog log;
			if (ignore) log = new ILog();
			else log = new ILog(HDFS_URI, logName, logPath, env);
			logs.put(logName, log);
			return log;
		} catch (IOException e) {}
		return null;
	}
}
