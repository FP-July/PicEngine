package raytracing.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.Validate;

import raytracing.Ray;
import raytracing.Vec3d;

public class Sphere extends Primitive {
	
	public static enum Property {
		center,
		radius,
		surfaceColor,
		emissionColor,
		transparency,
		reflection,
		diffuse
	};
	
	public Vec3d center;
	public Vec3d emissionLoc;
	public Double radius, radius2;
	public Vec3d surfaceColor, emissionColor;
	public Double transparency = 0.0, reflection = 0.0, diffuse = 0.0;
	
	public Sphere(Vec3d c,
				  Double r,
				  Vec3d sc,
				  Double refl,
				  Double transp,
				  Double diff,
				  Vec3d ec) throws IllegalArgumentException {
		if (c == null || r == null || sc == null || diff == null
			|| refl == null || transp == null || ec == null) {
			throw new IllegalArgumentException();
		}
		
		center = c; emissionLoc = c;
		radius = r; radius2 = r*r;
		surfaceColor = sc; emissionColor = ec;
		transparency = transp; reflection = refl;
		diffuse = diff;
	}
	
	public static Primitive loadProperties(HashMap<String, String> opts) {
		return null;
	}


	public boolean isLight() {
		if (emissionColor.x + emissionColor.y + emissionColor.z > 0) {
			return true;
		}
		return false;
	}

	public Vec3d getSurfaceColor(Vec3d pHit) {
		return surfaceColor;
	}
	public Double getTransparency(Vec3d pHit) {
		return transparency;
	}
	public Double getReflection(Vec3d pHit) {
		return reflection;
	}
	public Vec3d getEmissionColor(Vec3d point) {
		return emissionColor;
	}
	public Ray getLightRay(Vec3d point) {
		return new Ray(center, point.sub(center));
	}
	
	@Override
	public boolean intersect(Ray ray, List<Double> pHits) {
		Vec3d l = center.sub(ray.rayorig);
		Double tca = l.dot(ray.raydir);
		if (tca < 0) return false;
		Double d2 = l.dot(l) - tca * tca;
		if (d2 > radius2) return false;
		Double thc = Math.sqrt(radius2 - d2);
		if (tca > thc) {
			pHits.add(tca - thc);
		}
		pHits.add(tca + thc);
		return true;
	}
	
	@Override
	public Vec3d getNormal(Vec3d pHit) {
		return pHit.sub(center);
	}
	
	@Override
	public String toString() {
		return "Sphere";
	}

	@Override
	public Double getDiffusion(Vec3d pHit) {
		return diffuse;
	}

	@Override
	public ArrayList<Ray> renderSoftShadowRays(Vec3d point, int num) {
		Vec3d lightDirection = point.sub(center).normalize();
		Vec3d dir = new Vec3d(lightDirection.x, lightDirection.y, lightDirection.z + 1);
		Vec3d vx = lightDirection.cross(dir).normalize();
		Vec3d vy = vx.cross(lightDirection).normalize();
		Vec3d vz = lightDirection.normalize();
		
		ArrayList<Ray> rays = new ArrayList<Ray>();
		double angleStep = 360.0 / num;
		double angle = 0.0;
		do {
			Vec3d tmp = new Vec3d(radius * Math.cos(angle * Math.PI / 180), radius * Math.sin(angle * Math.PI / 180), 0.0);
			Vec3d coord = new Vec3d();
			coord.x = tmp.x * vx.x + tmp.y * vy.x + tmp.z * vz.x + center.x;
			coord.y = tmp.x * vx.y + tmp.y * vy.y + tmp.z * vz.y + center.y;
			coord.z = tmp.x * vx.z + tmp.y * vy.z + tmp.z * vz.z + center.z;
			rays.add(new Ray(coord, point.sub(coord)));
			angle += angleStep;
		} while (angle < 360);
		return rays;
	}
	
}
