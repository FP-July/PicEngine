package raytracing.load;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;

import raytracing.log.ILog;
import raytracing.log.LogFactory;
import raytracing.mapreduce.RayTracerDriver;

public class ConfLoader implements Closeable {
	
	private ILog logger = LogFactory.getInstance(RayTracerDriver.PARAMS.ILOG.name());
	
	private Path confPath;
	private BufferedReader br = null;
	
	public static void main(String[] args) throws IOException {
		ConfLoader cl = new ConfLoader("tmp.conf", null, BasicLoader.ENV.NATIVE);
		HashMap<String, String> opts = new HashMap<String, String>();
		cl.parse(opts);
		cl.close();
		for (Entry<String, String> entry : opts.entrySet()) {
			System.out.println(entry.getKey() + " : " + entry.getValue());
		}
	}
	
	public ConfLoader(String filePath, URI hdfs_uri, BasicLoader.ENV env) throws IOException {
		switch (env) {
		case NATIVE:
			initLfs(filePath);
			break;
		case HDFS:
			initHdfs(hdfs_uri, filePath);
			break;
		}
	}
	
	private void initLfs(String filePath) throws FileNotFoundException {
		File file = new File(filePath);
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				if (f.getName().endsWith(".conf")) {
					confPath = new Path(f.getPath());
				}
			}
		} else {
			confPath = new Path(filePath);
		}
		br = new BufferedReader(new InputStreamReader(new FileInputStream(confPath.toString())));
	}
	
	private void initHdfs(URI hdfs_uri, String filePath) throws IOException {
		Configuration conf = new Configuration();
		FileSystem fs;
		if (hdfs_uri != null) fs= FileSystem.get(hdfs_uri, conf);
		else fs = FileSystem.get(conf);
		Path path = new Path(filePath);
		if (fs.isDirectory(path)) {
			RemoteIterator<LocatedFileStatus> it = fs.listFiles(path, false);
			while (it.hasNext()) {
				LocatedFileStatus lfs = it.next();
				if (lfs.isDirectory()) continue;
				
				path = lfs.getPath();
				if (path.getName().endsWith(".conf")) {
					confPath = path;
					break;
				}
			}
		}
		else {
			confPath = path;
		}
		br = new BufferedReader(new InputStreamReader(fs.open(confPath)));
	}
	
	@Override
	public void close() {
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int lineCount = -1;
	public void parse(HashMap<String, String> opts) {
		try {
			String line;
			while ((line = br.readLine()) != null) {
				lineCount ++;
				if (line.equals("") || line.startsWith("#")) continue;
				
				line = line.trim();
				
				try {
					Property prop = Property.getProperty(line);
					if (prop != null) {
						opts.put(prop.key, prop.value);
					} 
				} catch (NullPointerException e) {
					error("property format error");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		logger.info("parse done global configration settings file");
	}

	
	private boolean error(String cause) {
		logger.error("failed to load models from path [" + confPath.toString() + 
				"] because " + cause + ", line count: " + lineCount);
		return false;
	}
}
