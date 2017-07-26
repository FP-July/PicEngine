package raytracing.log;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import raytracing.load.BasicLoader;

public class ILog implements Closeable {
	
	private Path path;
	private BufferedWriter bw = null;

	public ILog() {}
	public ILog(URI hdfs_uri, String logName, String logPath, BasicLoader.ENV env) throws IOException {
		path = new Path(logPath, logName + ".log");
		switch (env) {
		case NATIVE:
			initLfs(); break;
		case HDFS:
			initHdfs(hdfs_uri); break;
		}
			
	}
	
	private void initLfs() throws IOException {
		File file = new File(path.toString());
		bw = new BufferedWriter(new FileWriter(file, true));
	}
	
	private void initHdfs(URI hdfs_uri) throws IOException {
		Configuration conf = new Configuration();
		FileSystem fs;
		if (hdfs_uri != null) fs= FileSystem.get(hdfs_uri, conf);
		else fs = FileSystem.get(conf);
		
		if (fs.exists(path)) {
			bw = new BufferedWriter(new OutputStreamWriter(fs.append(path)));
		} else {
			bw = new BufferedWriter(new OutputStreamWriter(fs.create(path)));
		}
	}
	
	public String getCurrentDateTime() {
		LocalDateTime ldt = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		return ldt.format(formatter);
	}

	public void info(String msg) {
		if (bw == null) return ;
		try {
			bw.write(getCurrentDateTime() + " INFO : " + msg + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void warn(String msg) {
		if (bw == null) return ;
		try {
			bw.write(getCurrentDateTime() + " WARN : " + msg + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void error(String msg) {
		if (bw == null) return ;
		try {
			bw.write(getCurrentDateTime() + " ERROR : " + msg + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void close() {
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
