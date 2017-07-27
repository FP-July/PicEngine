package raytracing.mapreduce;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.mortbay.log.Log;

import main.JobRegister;
import raytracing.Camera;
import raytracing.Vec3d;
import raytracing.hadoop.JobStateFlagCreator;
import raytracing.hadoop.PixelInputFormat;
import raytracing.load.BasicLoader;
import raytracing.load.CameraLoader;
import raytracing.load.ConfLoader;
import raytracing.load.ModelLoader;
import raytracing.log.ILog;
import raytracing.log.LogFactory;
import raytracing.mapreduce.RayTracerDriver.PARAMS;
import raytracing.model.Primitive;
import raytracing.trace.CameraTrace;
import task.ITask;
import task.TaskUtils;

public class RayTracerDriver implements JobRegister, ITask {

	private Job job;
	private int totalJobNum = 1;
	private int processedJobNum = 0;

	public static enum PARAMS {
		INPUT_PATH, OUTPUT_PATH, OUTPUT_FILE_NAME, HDFS_URI, 
		MAX_RAY_DEPTH, SUPER_SAMPLING_TIMES, ILOG,
		IS_ON_SOFT_SHADOW, SOFT_SHADOW_NUMBER
	}

	@Override
	public void execute(String[] args) {
		Configuration conf = new Configuration();
		
//		try {
//			new GenericOptionsParser(conf, args);
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
//		System.out.println(conf.get("tmpjars"));
		
		conf.set("mr.job.name", "raytracing");

		String outputPath = "image";

		try {
			String logDir = "log";
			LogFactory.setParams(logDir, BasicLoader.ENV.HDFS);
			
			FileSystem fs = FileSystem.get(conf);
			if (fs.exists(new Path(outputPath)))
				fs.delete(new Path(outputPath), true);
			if (fs.exists(new Path(logDir)))
				fs.delete(new Path(logDir), true);
			
			/**
			 * Load ray tracing configration file  
			 */
			
			/** camera settings file input path <*.camera> */
			Camera origCamera = new Camera(new Vec3d(), new Vec3d(), new Vec3d(), 60.0, 480, 640);
			ArrayList<CameraTrace> cats = new ArrayList<CameraTrace>();
			CameraLoader cl = new CameraLoader("tmp.camera", null, BasicLoader.ENV.HDFS);
			cl.parse(origCamera, cats);
			cl.close();

			/** global settings file input path <*.conf> */
			HashMap<String, String> opts = new HashMap<String, String>();
			ConfLoader confLoader = new ConfLoader("tmp.conf", null, BasicLoader.ENV.HDFS);
			confLoader.parse(opts);
			confLoader.close();

			/** models settings file input path <*.mods> */
		    ModelLoader ml = new ModelLoader("tmp.mods", null, BasicLoader.ENV.HDFS);
		    ml.parse(new ArrayList<Primitive>());
		    ml.close();
			
			/**
			 *  map reduce configration set 
			 */
			conf.set(PARAMS.INPUT_PATH.name(), "tmp.mods");
			conf.set(PARAMS.OUTPUT_PATH.name(), outputPath);
			conf.set(PARAMS.MAX_RAY_DEPTH.name(), opts.getOrDefault(PARAMS.MAX_RAY_DEPTH.name(), "5"));
			conf.set(PARAMS.IS_ON_SOFT_SHADOW.name(), opts.getOrDefault(PARAMS.IS_ON_SOFT_SHADOW.name(), "off"));
			conf.set(PARAMS.SOFT_SHADOW_NUMBER.name(), opts.getOrDefault(PARAMS.SOFT_SHADOW_NUMBER.name(), "10"));
			conf.set(PARAMS.SUPER_SAMPLING_TIMES.name(), opts.getOrDefault(PARAMS.SUPER_SAMPLING_TIMES.name(), "3"));

			boolean _SUCC = false;
			try {
				Camera camera = origCamera;
				_SUCC = render(camera, conf, processedJobNum);
				for (CameraTrace cat : cats) {
					cat.setInitCameraLocation(origCamera);
					while ((camera = cat.getNextCameraFrame()) != null && _SUCC) {
						processedJobNum++;
						origCamera = camera;
						_SUCC = render(camera, conf, processedJobNum);
					}
				}
			} catch (Exception e) {
				_SUCC = false;
			}

			if (_SUCC)
				JobStateFlagCreator.createSuccessFlag(conf);
			else
				JobStateFlagCreator.createFailedFlag(conf);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/* flush log */
		ILog log = LogFactory.getInstance(PARAMS.ILOG.name());
		log.close();
	}

	public void render(String[] args) throws IOException {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length != 4) {
			System.err.println("Usage: raytracer <username> <taskname> <taskID> <rootPath>");
			return;
		}

		String username = otherArgs[0];
		@SuppressWarnings("unused")
		String taskName = otherArgs[1];
		String taskID = otherArgs[2];
		String workingDir = TaskUtils.getWorkingDir(username, taskID);
		String inputPath = TaskUtils.getSrcDir(workingDir);
		String outputPath = TaskUtils.getResultDir(workingDir);
		String logDir = TaskUtils.getMRLogPath(workingDir);
		String rootPath = otherArgs[3];
		
		LogFactory.setHdfsUri(TaskUtils.HDFS_URI);
		LogFactory.setParams(logDir, BasicLoader.ENV.HDFS);
		
		FileSystem fs = FileSystem.get(conf);
		if (fs.exists(new Path(outputPath)))
			fs.delete(new Path(outputPath), true);

		conf.addResource(new Path(rootPath + "yarn-site.xml"));
		conf.addResource(new Path(rootPath + "hdfs-site.xml"));
		conf.addResource(new Path(rootPath + "mapred-site.xml"));

		conf.set("mapreduce.job.jar", TaskUtils.MR_JAR_PATH + "RayTracerDriver.jar");
		conf.set("username", username);
		conf.set("taskID", taskID);
		conf.set("mr.job.name", username + "_" + taskID);

		try {
			/**
			 * Load ray tracing configration file  
			 */
			
			/** camera settings file input path <*.camera> */
			Camera origCamera = new Camera(new Vec3d(), new Vec3d(), new Vec3d(), 60.0, 480, 640);
			ArrayList<CameraTrace> cats = new ArrayList<CameraTrace>();
			CameraLoader cl = new CameraLoader(inputPath, TaskUtils.HDFS_URI, BasicLoader.ENV.HDFS);
			cl.parse(origCamera, cats);
			cl.close();

			/** global settings file input path <*.conf> */
			HashMap<String, String> opts = new HashMap<String, String>();
			ConfLoader confLoader = new ConfLoader(inputPath, TaskUtils.HDFS_URI, BasicLoader.ENV.HDFS);
			confLoader.parse(opts);
			confLoader.close();

			/** models settings file input path <*.mods> */
		    ModelLoader ml = new ModelLoader(inputPath, TaskUtils.HDFS_URI, BasicLoader.ENV.HDFS);
		    ml.parse(new ArrayList<Primitive>());
		    ml.close();
			
			/**
			 *  map reduce configration set 
			 */
			conf.set(PARAMS.INPUT_PATH.name(), inputPath);
			conf.set(PARAMS.HDFS_URI.name(), TaskUtils.HDFS_URI.toString());
			
			for (CameraTrace cat : cats) {
				totalJobNum += cat.getFrames();
			}

			conf.set(PARAMS.OUTPUT_PATH.name(), outputPath);
			conf.set(PARAMS.MAX_RAY_DEPTH.name(), opts.getOrDefault(PARAMS.MAX_RAY_DEPTH.name(), "5"));
			conf.set(PARAMS.IS_ON_SOFT_SHADOW.name(), opts.getOrDefault(PARAMS.IS_ON_SOFT_SHADOW.name(), "off"));
			conf.set(PARAMS.SOFT_SHADOW_NUMBER.name(), opts.getOrDefault(PARAMS.SOFT_SHADOW_NUMBER.name(), "10"));
			conf.set(PARAMS.SUPER_SAMPLING_TIMES.name(), opts.getOrDefault(PARAMS.SUPER_SAMPLING_TIMES.name(), "3"));

			boolean _SUCC = false;
			try {
				Camera camera = origCamera;
				processedJobNum = 0;
				_SUCC = render(camera, conf, processedJobNum);
				for (CameraTrace cat : cats) {
					cat.setInitCameraLocation(origCamera);
					while ((camera = cat.getNextCameraFrame()) != null && _SUCC) {
						processedJobNum++;
						origCamera = camera;
						_SUCC = render(camera, conf, processedJobNum);
					}
				}
			} catch (Exception e) {
				_SUCC = false;
			}

			if (_SUCC)
				JobStateFlagCreator.createSuccessFlag(conf);
			else
				JobStateFlagCreator.createFailedFlag(conf);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/* flush log */
		LogFactory.close(PARAMS.ILOG.name());
	}

	public boolean render(Camera camera, Configuration conf, int id)
			throws IOException, ClassNotFoundException, InterruptedException {
		conf.set(PARAMS.OUTPUT_FILE_NAME.name(), "image" + id + ".jpg");

		conf.set(Camera.Property.CAMERA_EYE.name(), camera.getEye().serialize());
		conf.set(Camera.Property.CAMERA_CENTER.name(), camera.getCenter().serialize());
		conf.set(Camera.Property.CAMERA_UP.name(), camera.getUp().serialize());
		conf.set(Camera.Property.CAMERA_FOV.name(), camera.getFov().toString());
		conf.set(Camera.Property.CAMERA_HEIGHT.name(), camera.getRows().toString());
		conf.set(Camera.Property.CAMERA_WIDTH.name(), camera.getCols().toString());

		camera.viewFrame(id);

		job = Job.getInstance(conf, conf.get("mr.job.name", "raytracing"));
		job.setJarByClass(RayTracerDriver.class);
		job.setMapperClass(RayTracerMapper.class);
		job.setReducerClass(RayTracerReducer.class);
		job.setNumReduceTasks(1);

		job.setInputFormatClass(PixelInputFormat.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputFormatClass(NullOutputFormat.class);

		return job.waitForCompletion(true);
	}

	@Override
	public void run(String[] args) throws Exception {
		render(args);
	}

	@Override
	public Job getJob() {
		return job;
	}

	@Override
	public float getProgress() {
		float mapProgress = 0.0f;
		try {
			mapProgress = job.mapProgress();
		} catch (Exception e) {}
		
		return 1.0f * (processedJobNum + mapProgress) / totalJobNum;
	}
}
