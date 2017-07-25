package raytracing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;

import org.apache.hadoop.util.hash.Hash;

import raytracing.load.CameraLoader;
import raytracing.load.ConfLoader;
import raytracing.load.ModelLoader;
import raytracing.trace.CameraTrace;
import utils.DirectoryChecker;

public class Main {
	
	private static RayTracing rayTracing = new RayTracing();
	private static int superSamplingTimes = 3;
	
	public static void main(String[] args) throws IOException {
		ModelLoader ml = new ModelLoader("tmp.mods", true);
		ml.parse(rayTracing.getScene());
		
		CameraLoader cl = new CameraLoader("tmp.camera", true);
		Camera origCamera = new Camera(new Vec3d(), new Vec3d(), new Vec3d(), 60.0, 480, 640);
		ArrayList<CameraTrace> cats = new ArrayList<CameraTrace>();
		cl.parse(origCamera, cats);

		HashMap<String, String> opts = new HashMap<String, String>();
		ConfLoader confLoader = new ConfLoader("tmp.conf", true);
		confLoader.parse(opts);
		
		rayTracing.setMaxRayDepth(opts.getOrDefault("MAX_RAY_DEPTH", "5"));
		superSamplingTimes = Integer.parseInt(opts.getOrDefault("SUPER_SAMPLING_TIMES", "3"));
		
	    render(origCamera, cats);
	}

	public static void render(Camera origCamera, ArrayList<CameraTrace> cats) {
	    Camera camera = origCamera;
	    DirectoryChecker.dirCheck("image", true);
	    
	    int i = 0;
	    render(camera, i);
	    for (CameraTrace cat : cats) {
	    	cat.setInitCameraLocation(camera);
	    	while ((camera = cat.getNextCameraFrame()) != null) {
			    i ++;
		    	render(camera, i);
		    } 
	    }
	    
	}
	
	public static void render(Camera camera, int id) {
		camera.viewFrame(id);
		
		int width = camera.getCols(), height = camera.getRows();
	    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    	
	    int times = superSamplingTimes;
	    for (int y = 0; y < height; ++y) { 
	        for (int x = 0; x < width; ++x) { 
            	Vec3d rgb = new Vec3d();
                ArrayList<Ray> rays = new ArrayList<Ray>();
                camera.getSuperSamplingRays(x, y, times, rays);
                for (Ray ssray : rays) {
                	Vec3d p = new Vec3d();
                	rayTracing.trace(ssray, p, 0);
                	rgb.addToThis(p);
                }
                double invTimes = 1.0 / (times * times);
                rgb.mulToThis(new Vec3d(invTimes));
	            
	            image.setRGB(x, y, rgb.getRGB());
	        } 
	    } 

	    JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
	    jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
	    jpegParams.setCompressionQuality(1f);
	    
	    try {
		    ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
		    // specifies where the jpg image has to be written
		    FileImageOutputStream fios = new FileImageOutputStream(new File("image/image" + id + ".jpg"));
			writer.setOutput(fios);
		
			// writes the file with given compression level 
			// from your JPEGImageWriteParam instance
			writer.write(null, new IIOImage(image, null, null), jpegParams);
			writer.reset();
			fios.close();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	}
}
