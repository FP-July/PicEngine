package raytracing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import raytracing.model.PhyObject;
import raytracing.model.Sphere;

public class RayTracing {
	
	public static List<PhyObject> scene = new ArrayList<PhyObject>();
//	public static Scene scene = new Scene();
	
	public final int MAX_RAY_DEPTH = 5;
	
	public static void main(String[] args) {
		// position, radius, surface color, reflectivity, transparency, emission color
		RayTracing.scene.add(new Sphere(new Vec3d( 0.0,      2.0, -10.0),     2.0, new Vec3d(0.40, 0.57, 0.74), 0.0, 0.3, new Vec3d(0.0))); 
	    RayTracing.scene.add(new Sphere(new Vec3d( 0.0,      0.0, -20.0),     4.0, new Vec3d(1.00, 0.32, 0.36), 1.0, 0.5, new Vec3d(0.0))); 
	    RayTracing.scene.add(new Sphere(new Vec3d( 5.0,     -1.0, -15.0),     2.0, new Vec3d(0.90, 0.76, 0.46), 1.0, 0.7, new Vec3d(0.0))); 
	    RayTracing.scene.add(new Sphere(new Vec3d( 5.0,      0.0, -25.0),     3.0, new Vec3d(0.65, 0.77, 0.97), 1.0, 0.0, new Vec3d(0.0))); 
	    RayTracing.scene.add(new Sphere(new Vec3d(-5.5,      0.0, -15.0),     3.0, new Vec3d(0.90, 0.90, 0.90), 1.0, 0.3, new Vec3d(0.0))); 
	    // light
	    
	    Sphere light = new Sphere(new Vec3d( 0.0, -20.0, -10040.0), 10000.0, new Vec3d(0.20, 0.20, 0.20), 0.0, 0.0, new Vec3d(1.0));
	    RayTracing.scene.add(light); 
	    RayTracing rt = new RayTracing();
	    rt.render();
	}

	public void render() {
	    int width = 640, height = 480; 
	    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    double invWidth = 1.0 / width, invHeight = 1.0 / height; 
	    double fov = -30, aspectratio = 1.0 * width / height; 
	    double angle = Math.tan(Math.PI * 0.5 * fov / 180); 
	    // Trace rays
	    for (int y = 0; y < height; ++y) { 
	        for (int x = 0; x < width; ++x) { 
	            double xx = (2 * ((x + 0.5) * invWidth) - 1) * angle * aspectratio; 
	            double yy = (1 - 2 * ((y + 0.5) * invHeight)) * angle; 
	            Vec3d raydir = new Vec3d(xx, yy, -1.0); 
	            raydir.normalize(); 
	            Vec3d rgb = trace(new Vec3d(0.0), raydir, 0); 
	            image.setRGB(x, y, rgb.getRGB());
	        } 
	    } 
	    
	    
	    try {
			ImageIO.write(image, "bmp", new File("image.bmp"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Vec3d trace(Vec3d rayorig, Vec3d raydir, int depth) {
		double tnear = Double.MAX_VALUE; 
	    PhyObject obj = null; 
	    // find intersection of this ray with the sphere in the scene
	    for (PhyObject sc : scene) { 
	        List<Double> pHits = new ArrayList<Double>();
	        if (sc.intersect(rayorig, raydir, pHits)) { 
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
	    if (obj == null) {
	    	return new Vec3d(0.2); 
	    }

//	    System.out.println(obj.isLight());
	    
	    Vec3d surfaceColor = new Vec3d(0.0); // color of the ray/surfaceof the object intersected by the ray 
	    Vec3d phit = rayorig.add(raydir.mul(tnear)); // point of intersection 
	    Vec3d nhit = obj.getNormal(phit); // normal at the intersection point 
	    nhit.normalize(); // normalize normal direction 
	    
	    // If the normal and the view direction are not opposite to each other
	    // reverse the normal direction. That also means we are inside the sphere so set
	    // the inside bool to true. Finally reverse the sign of IdotN which we want
	    // positive.
	    double bias = 1e-4; // add some bias to the point from which we will be tracing 
	    boolean inside = false; 
	    if (raydir.dot(nhit) > 0) {
	    	nhit = nhit.inv();
	    	inside = true; 
	    }
	    if ((obj.getTransparency(phit) > 0 || obj.getReflection(phit) > 0) && depth < MAX_RAY_DEPTH) { 
	        double facingratio = -raydir.dot(nhit); 
	        // change the mix value to tweak the effect
	        double fresneleffect = mix(Math.pow(1 - facingratio, 3), 1, 0.1); 
	        // compute reflection direction (not need to normalize because all vectors
	        // are already normalized)
	        Vec3d refldir = raydir.sub(nhit.mul(2.0).mul(raydir.dot(nhit))); 
	        refldir.normalize(); 
	        Vec3d reflection = trace(phit.add(nhit.mul(bias)), refldir, depth + 1); 
	        //Vec3f reflection = trace(phit, refldir, spheres, depth + 1); little change in the final effect
	        Vec3d refraction = new Vec3d(0.0); 
	        // if the sphere is also transparent compute refraction ray (transmission)
	        if (obj.getTransparency(phit) != 0) { 
	            double ior = 1.1, eta = (inside) ? ior : 1 / ior; // are we inside or outside the surface? 
	            double cosi = -nhit.dot(raydir); 
	            double k = 1 - eta * eta * (1 - cosi * cosi); 
	            Vec3d refrdir = raydir.mul(eta).add(nhit.mul((eta *  cosi - Math.sqrt(k)))); 
	            refrdir.normalize(); 
	            refraction = trace(phit.sub(nhit.mul(bias)), refrdir, depth + 1); 
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
	        for (PhyObject sc : scene) { 
	            if (sc.isLight()) { 
	                double transmission = 1.0; 
	                Vec3d lightDirection = sc.getLightDirection(phit); 
	                lightDirection.normalize(); 
	                for (PhyObject block : scene) { 
	                    if (block != sc) { 
	                    	List<Double> phits = new ArrayList<Double>();
	                        if (block.intersect(phit.add(nhit.mul(bias)), lightDirection, phits)) { 
	                            transmission = 0.0; 
	                            break; 
	                        } 
	                    } 
	                } 
	                surfaceColor.addToThis(
	                	obj.getSurfaceColor(phit).mul(
	                	transmission).mul( 
	                	Math.max(0.0, nhit.dot(lightDirection))
	                	).mul(obj.getEmissionColor(phit))
	                ); 
	            } 
	        } 
	    } 

//    	if (obj.isLight() && depth == 1) {
//    		System.out.println(surfaceColor.serialize());
//    	} 
	    return surfaceColor.add(obj.getEmissionColor(phit)); 

	}
	
	private double mix(double a, double b, double mix) {
		return b * mix + a * (1 - mix);
	}
}
