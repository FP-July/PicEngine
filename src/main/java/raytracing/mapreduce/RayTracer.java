package raytracing.mapreduce;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
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
import raytracing.load.CameraLoader;
import raytracing.load.ModelLoader;
import raytracing.model.Plane;
import raytracing.model.Sphere;
import raytracing.trace.CameraTrace;
import utils.StaticValue;

public class RayTracer extends JobRegister {

	public static class RayTracerMapper
		extends Mapper<IntWritable, IntWritable, Text, Text> {
		
		RayTracing rayTracing = new RayTracing();
	    Camera camera = null;
		
		public void setup(Context context) 
			throws IOException, InterruptedException {
		    Configuration conf = context.getConfiguration();
		    String modelPath = conf.get("MODEL_PATH");
		    ModelLoader ml = new ModelLoader(modelPath, false);
		    ml.parse(rayTracing.scene);
		    
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
            int times = 3;
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
			OutputStream fos = fs.create(path, true);
		    
		    ImageOutputStream stream = null;
	        try {
	            stream = ImageIO.createImageOutputStream(fos);
	        } catch (IOException e) {
	            throw new IIOException("Can't create output stream!", e);
	        }
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
		    jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		    jpegParams.setCompressionQuality(1f);
		    
		    try {
			    ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
				writer.setOutput(stream);
			
				writer.write(null, new IIOImage(image, null, null), jpegParams);
				writer.reset();
		    } catch (IOException e) {
		    	e.printStackTrace();
		    }
			fos.close();
		}
	}
	
	public void execute(String[] args) {

		Configuration conf = new Configuration();
		conf.set("MODEL_PATH", "tmp.mods");
		
		try {
			Camera origCamera = new Camera(new Vec3d(), new Vec3d(), new Vec3d(), 60.0, 480, 640);
			ArrayList<CameraTrace> cats = new ArrayList<CameraTrace>();
			CameraLoader cl = new CameraLoader("tmp.camera", false);
			cl.parse(origCamera, cats);
			
			Camera camera = origCamera;
			int i = 0;
		    render(camera, conf, i);
		    for (CameraTrace cat : cats) {
		    	cat.setInitCameraLocation(camera);
		    	while ((camera = cat.getNextCameraFrame()) != null) {
				    i ++;
			    	render(camera, conf, i);
			    } 
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void render(Camera camera, Configuration conf, int id) {
		conf.set("IMAGE_OUT_PATH", "image/image" + id + ".jpg");
		
		conf.set(Camera.Property.CAMERA_EYE.name(), camera.getEye().serialize());
		conf.set(Camera.Property.CAMERA_CENTER.name(), camera.getCenter().serialize());
		conf.set(Camera.Property.CAMERA_UP.name(), camera.getVy().inv().serialize());
		conf.set(Camera.Property.CAMERA_FOV.name(), camera.getFov().toString());
		conf.set(Camera.Property.CAMERA_HEIGHT.name(), camera.getRows().toString());
		conf.set(Camera.Property.CAMERA_WIDTH.name(), camera.getCols().toString());
		
		camera.viewFrame(id);
		
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
		job.setNumReduceTasks(1);
		
		job.setInputFormatClass(PixelInputFormat.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputFormatClass(NullOutputFormat.class);
		
//		String srcPath = StaticValue.LOC_PATH + "/" + width + "_" + height;
//		FileInputFormat.addInputPath(job, new Path(srcPath));
		job.waitForCompletion(true);
	}
}
