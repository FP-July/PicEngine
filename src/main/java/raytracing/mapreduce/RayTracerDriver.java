package raytracing.mapreduce;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import main.JobRegister;
import raytracing.Camera;
import raytracing.Vec3d;
import raytracing.hadoop.PixelInputFormat;
import raytracing.load.CameraLoader;
import raytracing.load.ConfLoader;
import raytracing.trace.CameraTrace;
import task.ITask;
import task.TaskUtils;

public class RayTracerDriver implements JobRegister, ITask {

	private Job job;
	
	public static enum PARAMS {
		INPUT_PATH,
		OUTPUT_PATH,
		MAX_RAY_DEPTH,
		SUPER_SAMPLING_TIMES
	}
	
	@Override
	public void execute(String[] args) {
		Configuration conf = new Configuration();
		conf.set("mr.job.name", "raytracing");
		
		try {
			Camera origCamera = new Camera(new Vec3d(), new Vec3d(), new Vec3d(), 60.0, 480, 640);
			ArrayList<CameraTrace> cats = new ArrayList<CameraTrace>();
			/** camera settings file input path <*.camera> */
			CameraLoader cl = new CameraLoader("tmp.camera", false);
			cl.parse(origCamera, cats);
			
			HashMap<String, String> opts = new HashMap<String, String>();
			ConfLoader confLoader = new ConfLoader("tmp.conf", true);
			confLoader.parse(opts);
			
			/** models settings file input path <*.mods> */
			conf.set(PARAMS.INPUT_PATH.name(), "tmp.mods");
			conf.set(PARAMS.MAX_RAY_DEPTH.name(), opts.getOrDefault("MAX_RAY_DEPTH", "5"));
			conf.set(PARAMS.SUPER_SAMPLING_TIMES.name(), opts.getOrDefault("SUPER_SAMPLING_TIMES", "3"));
			
			Camera camera = origCamera;
			int i = 0;
		    render(camera, conf, "image", i);
		    for (CameraTrace cat : cats) {
		    	cat.setInitCameraLocation(camera);
		    	while ((camera = cat.getNextCameraFrame()) != null) {
				    i ++;
			    	render(camera, conf, "image", i);
			    } 
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void render(String[] args) throws IOException {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length != 4) {
			System.err.println("Usage: raytracer <username> <taskname> <taskID> <rootPath>");
			return ;
		}

		String username = otherArgs[0];
		String taskName = otherArgs[1];
		String taskID = otherArgs[2];
		String workingDir = TaskUtils.getWorkingDir(username, taskID);
		String inputPath = TaskUtils.getSrcDir(workingDir);
		String outputPath = TaskUtils.getResultDir(workingDir);
		String logDir = TaskUtils.getMRLogPath(workingDir);
		String rootPath = otherArgs[3];
		
		FileSystem fs = FileSystem.get(conf);
		if (fs.exists(new Path(outputPath)))
			fs.delete(new Path(outputPath), true);

		conf.addResource(new Path(rootPath + "yarn-site.xml"));
 		conf.addResource(new Path(rootPath + "hdfs-site.xml"));
		conf.addResource(new Path(rootPath + "mapred-site.xml"));
		// TODO make this flexible
		conf.set("mapreduce.job.jar", System.getProperty("catalina.home") + File.separator +
														"wtpwebapps/PicEngine/" + "RayTracerDriver.jar");
		conf.set("username", username);
		conf.set("taskID", taskID);
		conf.set("mr.job.name", username + "_" + taskID);
		
		try {
			Camera origCamera = new Camera(new Vec3d(), new Vec3d(), new Vec3d(), 60.0, 480, 640);
			ArrayList<CameraTrace> cats = new ArrayList<CameraTrace>();
			/** camera settings file input path <*.camera> */
			CameraLoader cl = new CameraLoader(inputPath, false);
			cl.parse(origCamera, cats);
			
			HashMap<String, String> opts = new HashMap<String, String>();
			ConfLoader confLoader = new ConfLoader(inputPath, false);
			confLoader.parse(opts);
			
			/** models settings file input path <*.mods> */
			conf.set(PARAMS.INPUT_PATH.name(), inputPath);
			conf.set(PARAMS.MAX_RAY_DEPTH.name(), opts.getOrDefault("MAX_RAY_DEPTH", "5"));
			conf.set(PARAMS.SUPER_SAMPLING_TIMES.name(), opts.getOrDefault("SUPER_SAMPLING_TIMES", "3"));
			
			Camera camera = origCamera;
			int i = 0;
		    render(camera, conf, outputPath, i);
		    for (CameraTrace cat : cats) {
		    	cat.setInitCameraLocation(camera);
		    	while ((camera = cat.getNextCameraFrame()) != null) {
				    i ++;
			    	render(camera, conf, outputPath, i);
			    } 
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void render(Camera camera, Configuration conf, String outputPath, int id) 
		throws IOException, ClassNotFoundException, InterruptedException {
		conf.set(PARAMS.OUTPUT_PATH.name(), outputPath + "/image" + id + ".jpg");
		
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
		
		job.waitForCompletion(true);
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
		return 0.0f;
	}
}
