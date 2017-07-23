package raytracing.load;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import raytracing.model.Primitive;
import raytracing.model.Sphere;

public class ModelLoader {
	
	private static Logger logger = LoggerFactory.getLogger(ModelLoader.class);
	
	private Path modelPath;
	private BufferedReader br = null;

	public static enum Mod {
		_camera,
		_sphere,
		_plane
	}
	
	public ModelLoader(String filePath) throws IOException {
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(conf);
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
	
	public void close() {
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private int lineCount = 0;
	public boolean parse() {
		boolean hasCamera = false;

		HashMap<String, String> opts = new HashMap<String, String>();
		try {
			String line = br.readLine();
			while (line != null) {
				if (line.equals("") || line.startsWith("#")) continue;

				if (!line.startsWith("name=")) return error("property should called 'name'");
				
				String[] conf = line.trim().split("=");
				if (conf.length != 2) return error("name must be written");
				line = conf[1];

				opts.clear();
				/* 相机配置 */
				if (line.equals(Mod._camera.name())) {
					if (hasCamera) return error("multi cameras");
					
					line = br.readLine();
					while (line != null && (line.equals("") || line.startsWith("#"))) {
						line = br.readLine();
					}
					if (line == null) break;
					while (!line.startsWith("name=")) {
						String[] prop = line.trim().split("=");
						if (prop.length != 2) continue;
						opts.put(prop[0], prop[1]);
					}
				} 
				/* 模型配置 */
				else {
					
				}
				lineCount ++;
			}
		} catch (IOException e) {
			return error(e.toString());
		}
		
		if (!hasCamera) return error("no camera");
		
		return true;
	}
	
	private boolean error(String cause) {
		logger.error("failed to load models from path [{}] because {}, line count: {}",
					modelPath.toString(), cause, lineCount);
		return false;
	}
}
