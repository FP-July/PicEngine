package raytracing.mapreduce;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import raytracing.Camera;
import raytracing.Ray;
import raytracing.RayTracing;
import raytracing.Vec3d;
import raytracing.load.BasicLoader;
import raytracing.load.ModelLoader;
import raytracing.log.ILog;
import raytracing.log.LogFactory;
import raytracing.mapreduce.RayTracerDriver.PARAMS;

public class RayTracerMapper
	extends Mapper<IntWritable, IntWritable, Text, Text> {
	
	RayTracing rayTracing = new RayTracing();
	Camera camera = null;
	
	@Override
	public void setup(Context context) 
		throws IOException, InterruptedException {
		LogFactory.setIgnore(true);
		
	    Configuration conf = context.getConfiguration();
	    String modelPath = conf.get(PARAMS.INPUT_PATH.name());
	    URI hdfs_uri = null;
	    String hdfs = conf.get(PARAMS.HDFS_URI.name());
	    if (hdfs != null) hdfs_uri = URI.create(hdfs);
	    ModelLoader ml = new ModelLoader(modelPath, hdfs_uri, BasicLoader.ENV.HDFS);
	    ml.parse(rayTracing.getScene());
	    ml.close();
	    
	    rayTracing.setMaxRayDepth(conf.get(PARAMS.MAX_RAY_DEPTH.name()));
	    
	    Vec3d eye    = Vec3d.deSerialize(  conf.get(Camera.Property.CAMERA_EYE.name(),    new Vec3d(-1.0, 0.0, 0.0).serialize()));
	    Vec3d center = Vec3d.deSerialize(  conf.get(Camera.Property.CAMERA_CENTER.name(), new Vec3d().serialize()));
	    Vec3d up     = Vec3d.deSerialize(  conf.get(Camera.Property.CAMERA_UP.name(),     new Vec3d(0.0, 0.0, 1.0).serialize()));
	    Double fov   = Double.parseDouble( conf.get(Camera.Property.CAMERA_FOV.name(),    "60.0"));
	    Integer rows = Integer.parseInt(   conf.get(Camera.Property.CAMERA_HEIGHT.name(), "480"));
	    Integer cols = Integer.parseInt(   conf.get(Camera.Property.CAMERA_WIDTH.name(),  "640"));
	    camera = new Camera(eye, center, up, fov, rows, cols);
	}
	
	@Override
	public void map(IntWritable key, IntWritable value, Context context) 
		throws IOException, InterruptedException {
		Integer x = key.get();
		Integer y = value.get();
	    
	    Configuration conf = context.getConfiguration();
	    int times = Integer.parseInt(conf.get(PARAMS.SUPER_SAMPLING_TIMES.name()));
	    ArrayList<Ray> rays = new ArrayList<Ray>();
	    camera.getSuperSamplingRays(x, y, times, rays);
	    
	    Vec3d rgb = new Vec3d();
	    for (Ray ssray : rays) {
	    	Vec3d p = new Vec3d();
	    	rayTracing.trace(ssray, p, 0);
	    	rgb.addToThis(p);
	    }
	    double invTimes = 1.0 / (times * times);
	    rgb.mulToThis(new Vec3d(invTimes));
	    
	    context.write(new Text(key.get() + "," + value.get()), new Text(rgb.serialize()));
	}
}
