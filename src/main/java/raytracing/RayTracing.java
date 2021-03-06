package raytracing;

import java.util.ArrayList;
import java.util.List;

import raytracing.model.Primitive;
import raytracing.photon.Photon;
import raytracing.photon.PhotonLoader;

public class RayTracing {
	
	private List<Primitive> scene = new ArrayList<Primitive>();
	private PhotonLoader pl = null;
	private double alpha = 0.0;
	
	private int MAX_RAY_DEPTH = 5;
	private boolean isOnSoftShadow = false;
	private int softShadowNumber = 10;
	
	public List<Primitive> getScene() {
		return scene;
	}
	public void setMaxRayDepth(String depth) {
		MAX_RAY_DEPTH = Integer.parseInt(depth);
	}
	public void setPhotonLoader(PhotonLoader pl, double alpha) {
		this.pl = pl;
		this.alpha = alpha;
	}
	public void setSoftShadow(String num) {
		isOnSoftShadow = true;
		softShadowNumber = Integer.parseInt(num);
	}
	public void setSoftShadow(boolean flag) {
		isOnSoftShadow = flag;
	}
	
	private double getPhotons(Vec3d phit) {
		double strength = 0.0;
		
		double interval = 0.05;
		for (int i = -1; i < 2; ++i) {
			for (int j = -1; j < 2; ++j) {
				for (int k = -1; k < 2; ++k) {
					Photon pho = pl.getNearest(phit.add(new Vec3d(1.0 * i, 1.0 * j, 1.0 * k).mul(interval)));
					strength += pho.getStrength();
				}
			}
		}
		strength /= 27;
		return strength;
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
	                ArrayList<Ray> rays = new ArrayList<Ray>();
	            	if (isOnSoftShadow) {
	            		rays = sc.renderSoftShadowRays(phit, softShadowNumber);
	            	} else {
	            		rays.add(sc.getLightRay(phit));
	            	}
	                int intersectRaysNum = rays.size();
	                for (Ray lightRay : rays) {
	                	Vec3d lightDirection = lightRay.raydir;
		                double disFromLight = lightDirection.length();
		                lightDirection.normalize(); 
		                for (Primitive block : scene) { 
		                    if (block != sc) { 
		                    	List<Double> phits = new ArrayList<Double>();
		                    	boolean intersect = block.intersect(new Ray(phit.add(nhit.mul(bias)), lightDirection.inv()), phits);
		                        if (intersect && (phits.get(0) < disFromLight)) { 
		                        	intersectRaysNum --;
		                            break; 
		                        } 
		                    } 
		                } 
	                }
	                double transmission  = obj.getDiffusion(phit) * intersectRaysNum / rays.size(); 
	                
	                surfaceColor.addToThis(
	                	obj.getSurfaceColor(phit).mul(
	                	transmission).mul( 
	                	Math.max(0.0, nhit.dot(sc.getLightRay(phit).raydir.normalize().inv()))
	                	).mul(sc.getEmissionColor(phit))
	                ); 
	            } 
	        } 
	    } 
	    
	    surfaceColor.addToThis(obj.getEmissionColor(phit));
	    
//	    color.set(surfaceColor.mul(getPhotons(phit)));
	    color.set(surfaceColor);

	    return obj;

	}
	
	private double mix(double a, double b, double mix) {
		return b * mix + a * (1 - mix);
	}
}
