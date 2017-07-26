package raytracing.load;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;

import raytracing.model.PrimFactory.MOD;
import raytracing.log.ILog;
import raytracing.log.LogFactory;
import raytracing.mapreduce.RayTracerDriver;
import raytracing.model.PrimFactory;
import raytracing.model.Primitive;

public class ModelLoader implements Closeable {

	private ILog logger = LogFactory.getInstance(RayTracerDriver.PARAMS.ILOG.name());
	
	private Path modelPath;
	private BufferedReader br = null;
	
	public static void main(String[] args) throws IOException {
		ModelLoader ml = new ModelLoader("tmp.mods", null, BasicLoader.ENV.NATIVE);
		ArrayList<Primitive> scene = new ArrayList<Primitive>();
		ml.parse(scene);
		ml.close();
		System.out.println(scene.size());
	}
	
	public ModelLoader(String filePath, URI hdfs_uri, BasicLoader.ENV env) throws IOException {
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
				if (f.getName().endsWith(".mods")) {
					modelPath = new Path(f.getPath());
				}
			}
		} else {
			modelPath = new Path(filePath);
		}
		br = new BufferedReader(new InputStreamReader(new FileInputStream(modelPath.toString())));
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
				if (path.getName().endsWith(".mods")) {
					modelPath = path;
					break;
				}
			}
		}
		else {
			modelPath = path;
		}
		br = new BufferedReader(new InputStreamReader(fs.open(modelPath)));
	}
	
	@Override
	public void close() {
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private int lineCount = 0;
	public void parse(List<Primitive> scene) {
		try {
			String line;
			while ((line = br.readLine()) != null) {
				lineCount ++;
				if (line.equals("") || line.startsWith("#")) continue;
				
				line = line.trim();
				if (!line.endsWith(":")) error("mod name init must end with ':'");
				line = line.substring(0, line.length() - 1);
				
				MOD mod = MOD.valueOf(line.trim());
				if (mod == null) error("mod name error");
				
				line = br.readLine(); lineCount ++;
				while (line != null) {
					if (line.equals("") || line.startsWith("#")) {
						line = br.readLine(); lineCount ++;
						continue;
					}
					else break;
				}
				
				HashMap<String, String> opts = new HashMap<String, String>();
				while (line != null && !line.equals("end")) {
					if (!line.equals("") && !line.startsWith("#")) {
						try {
							Property prop = Property.getProperty(line);
							if (prop != null) {
								opts.put(prop.key, prop.value);
							}
						} catch (NullPointerException e) {
							error("property format error");
						}
					}
					line = br.readLine(); lineCount ++;
				}
				
				StringBuffer err = new StringBuffer();
				Primitive prim = PrimFactory.loadInstanceByProperties(mod, opts, err);
				if (prim == null) {
					System.out.println(err.toString());
					error("mod " + mod.name() + " properties format error");
				} else {
					scene.add(prim);
				}
			}
		} catch (IOException e) {
			error(e.toString());
		}
		
		logger.info("parse done model settings file");
	}
	
	private boolean error(String cause) {
		logger.error("failed to load models from path [" + modelPath.toString() + 
				"] because " + cause + ", line count: " + lineCount);
		return false;
	}
}
