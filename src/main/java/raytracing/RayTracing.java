package raytracing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;

import raytracing.model.Plane;
import raytracing.model.Primitive;
import raytracing.model.Sphere;
import raytracing.track.RotationWithInHorizontalPlane;
import utils.DirectoryChecker;

public class RayTracing {
	
	public static List<Primitive> scene = new ArrayList<Primitive>();
	
	public final int MAX_RAY_DEPTH = 5;
	
	public static void main(String[] args) {
		// position, radius, surface color, reflectivity, transparency, emission color
		RayTracing.scene.add(new Sphere(new Vec3d(10.0, -4.0, 0.0),     2.0, new Vec3d(0.40, 0.57, 0.74), 0.0, 0.3, new Vec3d())); 
	    RayTracing.scene.add(new Sphere(new Vec3d(20.0, 5.0, 0.0),     4.0, new Vec3d(1.00, 0.32, 0.36), 1.0, 0.5, new Vec3d())); 
	    RayTracing.scene.add(new Sphere(new Vec3d(15.0, -1.0, -2.0),     2.0, new Vec3d(0.90, 0.76, 0.46), 1.0, 0.7, new Vec3d())); 
	    RayTracing.scene.add(new Sphere(new Vec3d(25.0, 0.0, 10.0),     3.0, new Vec3d(0.65, 0.77, 0.57), 1.0, 0.0, new Vec3d())); 
	    RayTracing.scene.add(new Sphere(new Vec3d(15.0, 2.0, 0.0),     3.0, new Vec3d(0.90, 0.90, 0.90), 1.0, 0.3, new Vec3d()));
	    
//	    RayTracing.scene.add(new Sphere(new Vec3d(30.0, -30.0, 10.0), 2.0, new Vec3d(0.20, 0.20, 0.20), 0.3, 0.0, new Vec3d(1.0)));
	    RayTracing.scene.add(new Sphere(new Vec3d(-1000.0, 0.0, 11000.0), 10000.0, new Vec3d(0.20, 0.20, 0.20), 0.3, 0.0, new Vec3d(1.0)));
	    
	    RayTracing.scene.add(new Plane(new Vec3d(0.0, 0.0, -4.0), new Vec3d(0.0, 0.0, 1.0), new Vec3d(0.3, 0.3, 0.3), new Vec3d()));
	    RayTracing rt = new RayTracing();
	    rt.render();
	}

	public void render() {
	    int width = 640, height = 480; 
	    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    Camera camera = new Camera(new Vec3d(-1.0, 0.0, 0.0), new Vec3d(), new Vec3d(0.0, 0.0, 1.0), 60.0, height, width);
	    
	    RotationWithInHorizontalPlane rhp = RotationWithInHorizontalPlane.getInstance(camera, 25.0, -180, 60);
	    camera = rhp.skipToFrame(30);
	    
	    DirectoryChecker.dirCheck("image", true);
	    int i = 30;
	    do {
	    	camera.viewFrame(i);
	    	
		    for (int y = 0; y < height; ++y) { 
//		    	System.out.println("rows : " + y);
		        for (int x = 0; x < width; ++x) { 
	            	Vec3d rgb = new Vec3d();
	                ArrayList<Ray> rays = new ArrayList<Ray>();
	                int times = 3;
	                camera.getSuperSamplingRays(x, y, times, rays);
	                for (Ray ssray : rays) {
	                	Vec3d p = new Vec3d();
	                	trace(ssray, p, 0);
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
			    final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
			    // specifies where the jpg image has to be written
				writer.setOutput(new FileImageOutputStream(
				   new File("image/image" + i + ".jpg")));
			
				// writes the file with given compression level 
				// from your JPEGImageWriteParam instance
				writer.write(null, new IIOImage(image, null, null), jpegParams);
		    } catch (IOException e) {
		    	e.printStackTrace();
		    }
		    i ++;
	    } while ((camera = rhp.getNextCameraFrame()) != null);
	    
	}
	
	public Primitive trace(Ray ray, Vec3d color, int depth) {
		double tnear = Double.MAX_VALUE; 
	    Primitive obj = null; 
	    // find intersection of this ray with the sphere in the scene
	    for (Primitive sc : scene) { 
	        List<Double> pHits = new ArrayList<Double>();
	        if (sc.intersect(ray, pHits)) { 
	        	for (Double phit : pHits) {
	        		if (phit < 0) continue;
	        		if (phit < tnear) {
	        			tnear = phit;
	        			obj = sc;
	        			break;
	        		}
	        	}
	        } else {
//	        	System.out.println(raydir.serialize());
	        }
	    } 
	    
	    // if there's no intersection return black or background color
	    if (obj == null) return obj; 

	    Vec3d phit = ray.cross(tnear); // point of intersection 
	    Vec3d surfaceColor = new Vec3d(); // color of the ray/surfaceof the object intersected by the ray 
	    Vec3d nhit = obj.getNormal(phit); // normal at the intersection point 
	    nhit.normalize(); // normalize normal direction 
	    
	    // If the normal and the view direction are not opposite to each other
	    // reverse the normal direction. That also means we are inside the sphere so set
	    // the inside bool to true. Finally reverse the sign of IdotN which we want
	    // positive.
	    double bias = 1e-4; // add some bias to the point from which we will be tracing 
	    boolean inside = false; 
	    if (ray.raydir.dot(nhit) > 0) {
	    	nhit = nhit.inv();
	    	inside = true; 
	    }
	    if ((obj.getTransparency(phit) > 0 || obj.getReflection(phit) > 0) && depth < MAX_RAY_DEPTH) { 
	        double facingratio = -ray.raydir.dot(nhit); 
	        // change the mix value to tweak the effect
	        double fresneleffect = mix(Math.pow(1 - facingratio, 3), 1, 0.1); 
	        // compute reflection direction (not need to normalize because all vectors
	        // are already normalized)
	        Vec3d refldir = ray.raydir.sub(nhit.mul(2.0).mul(ray.raydir.dot(nhit))); 
	        refldir.normalize(); 
	        Vec3d reflection = new Vec3d();
	        trace(new Ray(phit.add(nhit.mul(bias)), refldir), reflection, depth + 1); // little change in the final effect
	        
	        Vec3d refraction = new Vec3d(); 
	        // if the sphere is also transparent compute refraction ray (transmission)
	        if (obj.getTransparency(phit) != 0) { 
	            double ior = 1.1, eta = (inside) ? ior : 1 / ior; // are we inside or outside the surface? 
	            double cosi = -nhit.dot(ray.raydir); 
	            double k = 1 - eta * eta * (1 - cosi * cosi); 
	            Vec3d refrdir = ray.raydir.mul(eta).add(nhit.mul((eta *  cosi - Math.sqrt(k)))); 
	            refrdir.normalize(); 
	            trace(new Ray(phit.sub(nhit.mul(bias)), refrdir), refraction, depth + 1); 
	        } 
	        // the result is a mix of reflection and refraction (if the sphere is transparent)
	        surfaceColor = 
	        	reflection.mul(fresneleffect).add(
	        	refraction.mul(
	            	(1 - fresneleffect) * obj.getTransparency(phit))
	        	).mul(obj.getSurfaceColor(phit)); 
	    } 
	    else { 
	    	
	        // it's a diffuse object, no need to raytrace any further
	        for (Primitive sc : scene) { 
	            if (sc.isLight()) { 
	                double transmission = 1.0; 
	                Vec3d lightDirection = sc.getLightDirection(phit); 
	                lightDirection.normalize(); 
	                for (Primitive block : scene) { 
	                    if (block != sc) { 
	                    	List<Double> phits = new ArrayList<Double>();
	                        if (block.intersect(new Ray(phit.add(nhit.mul(bias)), lightDirection.inv()), phits)) { 
	                            transmission = 0.0; 
	                            break; 
	                        } 
	                    } 
	                } 
	                surfaceColor.addToThis(
	                	obj.getSurfaceColor(phit).mul(
	                	transmission).mul( 
	                	Math.max(0.0, nhit.dot(lightDirection.inv()))
	                	).mul(sc.getEmissionColor(phit))
	                ); 
	            } 
	        } 
	    } 
	    
	    color.set(surfaceColor.add(obj.getEmissionColor(phit)));

	    return obj;

	}
	
	private double mix(double a, double b, double mix) {
		return b * mix + a * (1 - mix);
	}
}
