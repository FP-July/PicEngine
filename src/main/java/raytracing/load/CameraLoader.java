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
import raytracing.Vec3d;
import raytracing.model.PrimFactory;
import raytracing.model.Primitive;
import raytracing.trace.CameraTrace;
import raytracing.trace.CameraTraceFactory;
import task.TaskUtils;
import raytracing.model.PrimFactory.MOD;

public class CameraLoader {
	
	private Logger logger = LoggerFactory.getLogger(ModelLoader.class);
	
	private Path cameraPath;
	private BufferedReader br = null;
	
	public static enum MOD {
		_camera_init,
		_trace_init
	}
	
	public static void main(String[] args) throws IOException {
		CameraLoader cl = new CameraLoader("tmp.camera", true);
		HashMap<String, String> opts = new HashMap<String, String>();
		Camera ca = null;
		ArrayList<CameraTrace> cats = new ArrayList<CameraTrace>();
		cl.parse(ca, cats);
		for (Entry<String, String> entry : opts.entrySet()) {
			System.out.println(entry.getKey() + " : " + entry.getValue());
		}
		System.out.println(cats.size());
	}
	
	public CameraLoader(String filePath, boolean locate) throws IOException {
		if (locate) initLfs(filePath);
		else initHdfs(filePath);
	}
	
	private void initLfs(String filePath) throws FileNotFoundException {
		File file = new File(filePath);
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				if (f.getName().endsWith(".mods")) {
					cameraPath = new Path(f.getPath());
				}
			}
		} else {
			cameraPath = new Path(filePath);
		}
		br = new BufferedReader(new InputStreamReader(new FileInputStream(cameraPath.toString())));
	}
	
	private void initHdfs(String filePath) throws IOException {
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(TaskUtils.HDFS_URI, conf);
		Path path = new Path(filePath);
		if (fs.isDirectory(path)) {
			RemoteIterator<LocatedFileStatus> it = fs.listFiles(path, false);
			while (it.hasNext()) {
				LocatedFileStatus lfs = it.next();
				if (lfs.isDirectory()) continue;
				
				path = lfs.getPath();
				if (path.getName().endsWith(".camera")) {
					cameraPath = path;
					break;
				}
			}
		}
		else {
			cameraPath = path;
		}
		br = new BufferedReader(new InputStreamReader(fs.open(cameraPath)));
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
	public boolean parse(Camera ca, ArrayList<CameraTrace> cats) {
		boolean hasCamera = false;
		try {
			String line;
			while ((line = br.readLine()) != null) {
				lineCount ++;
				if (line.equals("") || line.startsWith("#")) continue;
				
				line = line.trim();
				if (!line.endsWith(":")) return error("camera name init must end with ':'");
				line = line.substring(0, line.length() - 1);

				MOD mod = MOD.valueOf(line.trim());
				if (mod == null) return error("mod name error");

				switch (mod) {
				/* 相机配置 */
				case _camera_init:
					if (hasCamera) return error("multi cameras");
					hasCamera = true;
					
					line = br.readLine(); lineCount ++;
					while (line != null) {
						if (line.equals("") || line.startsWith("#")) {
							line = br.readLine(); lineCount ++;
							continue;
						}
						else break;
					}
					
					HashMap<String, String> opts = new HashMap<String ,String>();
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
					Vec3d eye = BasicFunc.parseVectorProperty(Camera.Property.CAMERA_EYE.name(), opts.get(Camera.Property.CAMERA_EYE.name()), err);
					Vec3d center = BasicFunc.parseVectorProperty(Camera.Property.CAMERA_CENTER.name(), opts.get(Camera.Property.CAMERA_CENTER.name()), err);
					Vec3d up = BasicFunc.parseVectorProperty(Camera.Property.CAMERA_UP.name(), opts.get(Camera.Property.CAMERA_UP.name()), err);
					Double fov = BasicFunc.parseDoubleProperty(Camera.Property.CAMERA_FOV.name(), opts.get(Camera.Property.CAMERA_FOV.name()), err);
					Integer rows = BasicFunc.parseIntegerProperty(Camera.Property.CAMERA_HEIGHT.name(), opts.get(Camera.Property.CAMERA_HEIGHT.name()), err);
					Integer cols = BasicFunc.parseIntegerProperty(Camera.Property.CAMERA_WIDTH.name(), opts.get(Camera.Property.CAMERA_WIDTH.name()), err);
					ca.set(new Camera(eye, center, up, fov, rows, cols));
					break;
				/* 相机轨迹配置 */
				case _trace_init:
					while ((line = br.readLine()) != null && !line.equals("end")) {
						lineCount ++;
						if (line.equals("") || line.startsWith("#")) continue;
						
						line = line.trim();
						if (!line.endsWith(":")) error("trace name init must end with ':'");
						line = line.substring(0, line.length() - 1);
						
						raytracing.trace.CameraTraceFactory.MOD trace = raytracing.trace.CameraTraceFactory.MOD.valueOf(line.trim());
						if (trace == null) error("mod name error");
						
						line = br.readLine(); lineCount ++;
						while (line != null) {
							if (line.equals("") || line.startsWith("#")) {
								line = br.readLine(); lineCount ++;
								continue;
							}
							else break;
						}
						
						HashMap<String, String> confs = new HashMap<String, String>();
						while (line != null && !line.equals("end")) {
							if (!line.equals("") && !line.startsWith("#")) {
								try {
									Property prop = Property.getProperty(line);
									if (prop != null) {
										confs.put(prop.key, prop.value);
									}
								} catch (NullPointerException e) {
									error("property format error");
								}
							}
							line = br.readLine(); lineCount ++;
						}
						
						StringBuffer cause = new StringBuffer();
						CameraTrace cat = CameraTraceFactory.loadInstanceByProperties(trace, confs, cause);
						if (cat == null) {
							System.out.println(cause.toString());
							error("camera trace " + trace.name() + " properties format error");
						} else {
							cats.add(cat);
						}
					}
					break;
				}
				line = br.readLine();
			}
		} catch (IOException e) {
			return error(e.toString());
		}
		
		if (!hasCamera) return error("no camera");
		
		return true;
	}
	
	private boolean error(String cause) {
		logger.error("failed to load models from path [{}] because {}, line count: {}",
					cameraPath.toString(), cause, lineCount);
		return false;
	}
}
