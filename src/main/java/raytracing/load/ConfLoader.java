package raytracing.load;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import raytracing.Camera;
import raytracing.trace.CameraTrace;
import raytracing.trace.CameraTraceFactory;

public class ConfLoader {
	
	private Logger logger = LoggerFactory.getLogger(ConfLoader.class);
	
	private Path confPath;
	private BufferedReader br = null;
	
	public static void main(String[] args) throws IOException {
		ConfLoader cl = new ConfLoader("tmp.conf", true);
		HashMap<String, String> opts = new HashMap<String, String>();
		cl.parse(opts);
		for (Entry<String, String> entry : opts.entrySet()) {
			System.out.println(entry.getKey() + " : " + entry.getValue());
		}
	}
	
	public ConfLoader(String filePath, boolean locate) throws IOException {
		if (locate) initLfs(filePath);
		else initHdfs(filePath);
	}
	
	private void initLfs(String filePath) throws FileNotFoundException {
		File file = new File(filePath);
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				if (f.getName().endsWith(".mods")) {
					confPath = new Path(f.getPath());
				}
			}
		} else {
			confPath = new Path(filePath);
		}
		br = new BufferedReader(new InputStreamReader(new FileInputStream(confPath.toString())));
	}
	
	private void initHdfs(String filePath) throws IOException {
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(conf);
		Path path = new Path(filePath);
		if (fs.isDirectory(path)) {
			RemoteIterator<LocatedFileStatus> it = fs.listFiles(path, false);
			while (it.hasNext()) {
				LocatedFileStatus lfs = it.next();
				if (lfs.isDirectory()) continue;
				
				path = lfs.getPath();
				if (path.getName().endsWith(".camera")) {
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
	
	public void close() {
		try {
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
	}

	
	private boolean error(String cause) {
		logger.error("failed to load models from path [{}] because {}, line count: {}",
					confPath.toString(), cause, lineCount);
		return false;
	}
}