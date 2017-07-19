package raytracing.mapreduce;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.KeyValueTextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;

import raytracing.RayTracing;
import raytracing.Vec3d;
import raytracing.model.Sphere;
import utils.StaticValue;

public class RayTracer {
	
	public static int width = 640, height = 480;

	public static class RayTracerMapper
		extends Mapper<Text, Text, Text, Text> {
		
		RayTracing rayTracing = new RayTracing();

	    double invWidth = 1.0 / width, invHeight = 1.0 / height; 
	    double fov = -30, aspectratio = 1.0 * width / height; 
	    double angle = Math.tan(Math.PI * 0.5 * fov / 180); 
		
		public void setup(Context context) 
			throws IOException, InterruptedException {
			// position, radius, surface color, reflectivity, transparency, emission color
			RayTracing.scene.add(new Sphere(new Vec3d( 0.0,      2.0, -10.0),     2.0, new Vec3d(0.40, 0.57, 0.74), 0.0, 0.3, new Vec3d(0.0))); 
		    RayTracing.scene.add(new Sphere(new Vec3d( 0.0,      0.0, -20.0),     4.0, new Vec3d(1.00, 0.32, 0.36), 1.0, 0.5, new Vec3d(0.0))); 
		    RayTracing.scene.add(new Sphere(new Vec3d( 5.0,     -1.0, -15.0),     2.0, new Vec3d(0.90, 0.76, 0.46), 1.0, 0.7, new Vec3d(0.0))); 
		    RayTracing.scene.add(new Sphere(new Vec3d( 5.0,      0.0, -25.0),     3.0, new Vec3d(0.65, 0.77, 0.97), 1.0, 0.0, new Vec3d(0.0))); 
		    RayTracing.scene.add(new Sphere(new Vec3d(-5.5,      0.0, -15.0),     3.0, new Vec3d(0.90, 0.90, 0.90), 1.0, 0.3, new Vec3d(0.0))); 
		    // light
		    
		    Sphere light = new Sphere(new Vec3d( 0.0, -20.0, -10040.0), 10000.0, new Vec3d(0.20, 0.20, 0.20), 0.0, 0.0, new Vec3d(1.0));
		    RayTracing.scene.add(light); 
		    
		}
		
		public void map(Text key, Text value, Context context) 
			throws IOException, InterruptedException {
			String[] vas = value.toString().trim().split(",");
			Integer x = Integer.parseInt(vas[0]);
			Integer y = Integer.parseInt(vas[1]);
			double xx = (2 * ((x + 0.5) * invWidth) - 1) * angle * aspectratio; 
	        double yy = (1 - 2 * ((y + 0.5) * invHeight)) * angle; 
	        Vec3d raydir = new Vec3d(xx, yy, -1.0); 
	        raydir.normalize();
	        Vec3d rgb = rayTracing.trace(new Vec3d(0.0), raydir, 0);
//	        Integer.parseInt(rgb.serialize());
	        context.write(new Text("rgb"), new Text(value.toString() + "\t" + rgb.serialize()));
		}
	}
	
	public static class RayTracerReducer
		extends Reducer<Text, Text, Text, Text> {
		
		private BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		public void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {
			Iterator<Text> it = values.iterator();
			while (it.hasNext()) {
				Text value = it.next();
				if (value == null || value.toString().equals("")) continue;
				
				try{
					String[] kv = value.toString().split("\t");
					String[] loc = kv[0].split(",");
					Integer x = Integer.parseInt(loc[0]);
					Integer y = Integer.parseInt(loc[1]);
					Vec3d rgb = Vec3d.deSerialize(kv[1]);
					image.setRGB(x, y, rgb.getRGB());
				} catch (Exception e) {
					Integer.parseInt(value.toString());
				}
			}
		}
		
		public void cleanup(Context context) 
			throws IOException, InterruptedException {
			Configuration conf = context.getConfiguration();
			FileSystem fs = FileSystem.get(conf);
			
			Path path = new Path(StaticValue.IMAGE_OUT_PATH);
			OutputStream os = fs.create(path, true);
			ImageIO.write(image, "bmp", os);
			os.close();
		}
	}
	
	public static void rayTracing(String srcPath) 
		throws IOException, InterruptedException, ClassNotFoundException {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "raytraceing");
		job.setJarByClass(RayTracer.class);
		job.setMapperClass(RayTracerMapper.class);
		job.setReducerClass(RayTracerReducer.class);
		
		job.setInputFormatClass(KeyValueTextInputFormat.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputFormatClass(NullOutputFormat.class);
//		job.setOutputKeyClass(Text.class);
//		job.setOutputValueClass(Text.class);
		
		FileInputFormat.addInputPath(job, new Path(srcPath));
//		FileOutputFormat.setOutputPath(job, new Path(outPath));
		job.waitForCompletion(true);
	}
}
