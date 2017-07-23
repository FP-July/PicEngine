package raytracing.mapreduce;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.ImageIO;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;

import main.JobRegister;
import raytracing.Camera;
import raytracing.Ray;
import raytracing.RayTracing;
import raytracing.Vec3d;
import raytracing.hadoop.PixelInputFormat;
import raytracing.hadoop.PixelInputFormat.PixelRecordReader;
import raytracing.model.Plane;
import raytracing.model.Sphere;
import utils.StaticValue;

public class RayTracer extends JobRegister {

	public static class RayTracerMapper
		extends Mapper<IntWritable, IntWritable, Text, Text> {
		
		RayTracing rayTracing = new RayTracing();
	    Camera camera = null;
		
		public void setup(Context context) 
			throws IOException, InterruptedException {
			// position, radius, surface color, reflectivity, transparency, emission color
			RayTracing.scene.add(new Sphere(new Vec3d(10.0, -4.0, 0.0),     2.0, new Vec3d(0.40, 0.57, 0.74), 0.0, 0.3, new Vec3d())); 
		    RayTracing.scene.add(new Sphere(new Vec3d(20.0, 5.0, 0.0),     4.0, new Vec3d(1.00, 0.32, 0.36), 1.0, 0.5, new Vec3d())); 
		    RayTracing.scene.add(new Sphere(new Vec3d(15.0, -1.0, -2.0),     2.0, new Vec3d(0.90, 0.76, 0.46), 1.0, 0.7, new Vec3d())); 
		    RayTracing.scene.add(new Sphere(new Vec3d(25.0, 0.0, 10.0),     3.0, new Vec3d(0.65, 0.77, 0.57), 1.0, 0.0, new Vec3d())); 
		    RayTracing.scene.add(new Sphere(new Vec3d(15.0, 2.0, 0.0),     3.0, new Vec3d(0.90, 0.90, 0.90), 1.0, 0.3, new Vec3d()));
		    
//		    RayTracing.scene.add(new Sphere(new Vec3d(30.0, -30.0, 10.0), 2.0, new Vec3d(0.20, 0.20, 0.20), 0.3, 0.0, new Vec3d(1.0)));
		    RayTracing.scene.add(new Sphere(new Vec3d(-1000.0, 0.0, 11000.0), 10000.0, new Vec3d(0.20, 0.20, 0.20), 0.3, 0.0, new Vec3d(1.0)));
		    
		    RayTracing.scene.add(new Plane(new Vec3d(0.0, 0.0, -4.0), new Vec3d(0.0, 0.0, 1.0), new Vec3d(0.3, 0.3, 0.3), new Vec3d()));
		    
		    Configuration conf = context.getConfiguration();
		    Vec3d eye    = Vec3d.deSerialize(  conf.get(Camera.Property.CAMERA_EYE.name(),    new Vec3d(-1.0, 0.0, 0.0).serialize()));
		    Vec3d center = Vec3d.deSerialize(  conf.get(Camera.Property.CAMERA_CENTER.name(), new Vec3d().serialize()));
		    Vec3d up     = Vec3d.deSerialize(  conf.get(Camera.Property.CAMERA_UP.name(),     new Vec3d(0.0, 0.0, 1.0).serialize()));
		    Double fov   = Double.parseDouble( conf.get(Camera.Property.CAMERA_FOV.name(),    "60.0"));
		    Integer rows = Integer.parseInt(   conf.get(Camera.Property.CAMERA_HEIGHT.name(), "480"));
		    Integer cols = Integer.parseInt(   conf.get(Camera.Property.CAMERA_WIDTH.name(),  "640"));
		    camera = new Camera(eye, center, up, fov, rows, cols);
		}
		
		public void map(IntWritable key, IntWritable value, Context context) 
			throws IOException, InterruptedException {
			Integer x = key.get();
			Integer y = value.get();
			Ray ray = camera.getRay(x, y);
	        Vec3d rgb = new Vec3d();
            
            ArrayList<Ray> rays = new ArrayList<Ray>();
            int times = 10;
            camera.getSuperSamplingRays(x, y, times, rays);
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
	
	public static class RayTracerReducer
		extends Reducer<Text, Text, Text, Text> {

		public int width = 0, height = 0;
		private BufferedImage image;
		
		@Override
		protected void setup(Reducer<Text, Text, Text, Text>.Context context) 
			throws IOException, InterruptedException {
			Configuration conf = context.getConfiguration();
		    height = Integer.parseInt(   conf.get(Camera.Property.CAMERA_HEIGHT.name(), "480"));
		    width  = Integer.parseInt(   conf.get(Camera.Property.CAMERA_WIDTH.name(),  "640"));
			image  = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		}
		
		public void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {
			String[] coord = key.toString().split(",");
			
			Text text = values.iterator().next();
			if (text == null || text.toString().equals("")) return ;
			
			int x = Integer.parseInt(coord[0]);
			int y = Integer.parseInt(coord[1]);
			Vec3d rgb = Vec3d.deSerialize(text.toString());
			image.setRGB(x, y, rgb.getRGB());
		}
		
		public void cleanup(Context context) 
			throws IOException, InterruptedException {
			Configuration conf = context.getConfiguration();
			FileSystem fs = FileSystem.get(conf);
			
			Path path = new Path(conf.get("IMAGE_OUT_PATH"));
			OutputStream os = fs.create(path, true);
			ImageIO.write(image, "bmp", os);
			os.close();
		}
	}
	
	public void execute(String[] args) {
		if (args.length == 0) {
			System.out.println("RayTracer with jobId.");
			System.exit(0);
		}
		
		String jobId = args[0];
		String outPath = StaticValue.BASR_OUT_PATH + "/image" + jobId + ".bmp";

		Configuration conf = new Configuration();
		conf.set("IMAGE_OUT_PATH", outPath);
		System.out.println(conf.get("IMAGE_OUT_PATH"));
		
		try {
			RayTracer.rayTracing(conf);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void rayTracing(Configuration conf) 
		throws IOException, InterruptedException, ClassNotFoundException {
		
		Job job = Job.getInstance(conf, "raytracing");
		job.setJarByClass(RayTracer.class);
		job.setMapperClass(RayTracerMapper.class);
		job.setReducerClass(RayTracerReducer.class);
		
		job.setInputFormatClass(PixelInputFormat.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputFormatClass(NullOutputFormat.class);
		
//		String srcPath = StaticValue.LOC_PATH + "/" + width + "_" + height;
//		FileInputFormat.addInputPath(job, new Path(srcPath));
		job.waitForCompletion(true);
	}
}
