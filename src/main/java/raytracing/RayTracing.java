package raytracing;

import java.util.ArrayList;
import java.util.List;

import raytracing.model.Primitive;

public class RayTracing {
	
	private List<Primitive> scene = new ArrayList<Primitive>();
	
	private int MAX_RAY_DEPTH = 5;
	
	public List<Primitive> getScene() {
		return scene;
	}
	public void setMaxRayDepth(String depth) {
		MAX_RAY_DEPTH = Integer.parseInt(depth);
	}
	
	public Primitive trace(Ray ray, Vec3d color, int depth) {
		if (depth >= MAX_RAY_DEPTH) return null;
		
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
	    if ((obj.getTransparency(phit) > 0 || obj.getReflection(phit) > 0)) { 
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
	    
	    if (obj.getDiffusion(phit) > 0) { 
	        for (Primitive sc : scene) { 
	            if (sc.isLight()) { 
	                double transmission = obj.getDiffusion(phit); 
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
	                if (obj.getDiffusion(phit) > 0.6) {
	                	System.out.println(transmission + ", " + nhit.dot(lightDirection.inv()));
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
