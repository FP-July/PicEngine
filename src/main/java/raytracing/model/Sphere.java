package raytracing.model;

import java.util.List;

import raytracing.Vec3d;

public class Sphere extends PhyObject {
	
	public Vec3d center;
	public Vec3d emissionLoc;
	public Double radius, radius2;
	public Vec3d surfaceColor, emissionColor;
	public Double transparency = 0.0, reflection = 0.0;
	public boolean isLight = false;
	
	public Sphere(Vec3d c,
				  Double r,
				  Vec3d sc,
				  Double refl,
				  Double transp,
				  Vec3d ec) {
		center = c; emissionLoc = c;
		radius = r; radius2 = r*r;
		surfaceColor = sc; emissionColor = ec;
		transparency = transp; reflection = refl;
	}

	
	public void setLight(boolean flag) {
		this.isLight = flag;
	}
	public boolean isLight() {
		return isLight;
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
	public Vec3d getLightDirection(Vec3d point) {
		return point.sub(center);
	}
	
	@Override
	public boolean intersect(Vec3d rayorig, Vec3d raydir, List<Double> pHits) {
		Vec3d l = center.sub(rayorig);
		Double tca = l.dot(raydir);
		if (tca < 0) return false;
		Double d2 = l.dot(l) - tca * tca;
		if (d2 > radius) return false;
		Double thc = Math.sqrt(radius2 - d2);
		pHits.add(tca - thc);
		pHits.add(tca + thc);
		return true;
	}
	
	@Override
	public Vec3d getNormal(Vec3d pHit) {
		return pHit.sub(center);
	}
	
}
