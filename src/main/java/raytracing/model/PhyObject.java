package raytracing.model;

import java.util.List;

import raytracing.Vec3d;

public abstract class PhyObject {

//	public Vec3d surfaceColor, emissionColor;
//	public Vec3d emissionLoc;
	
//	Double transparency, reflection;
	
	public abstract boolean intersect(Vec3d rayorig, Vec3d raydir, List<Double> pHits);
	
	public abstract Vec3d getNormal(Vec3d pHit);
	
	public abstract Vec3d getSurfaceColor(Vec3d pHit);
	public abstract Double getTransparency(Vec3d pHit);
	public abstract Double getReflection(Vec3d pHit);
	
	public abstract boolean isLight();
	public abstract Vec3d getEmissionColor(Vec3d point);
	public abstract Vec3d getLightDirection(Vec3d point);
	
}
