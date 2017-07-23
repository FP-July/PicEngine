package raytracing.model;

import java.util.List;

import raytracing.Ray;
import raytracing.Vec3d;

/**
 * 平面模型,为漫反射属性。
 * @author wanglt
 * Jul 22, 2017
 */
public class Plane extends Primitive {
	
	//  [x, y, z, 1] * [A, B, C, D]^T = 0
	
	public Vec3d center;
	public Vec3d norm;
	public Vec3d surfaceColor, emissionColor;
	
	public Plane(Vec3d center,
				  Vec3d norm,
				  Vec3d sc,
				  Vec3d ec) {
		this.center = center; 
		this.norm = norm;
		this.surfaceColor = sc; 
		this.emissionColor = ec;
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
		return 0.0;
	}
	public Double getReflection(Vec3d pHit) {
		return 0.0;
	}
	public Vec3d getEmissionColor(Vec3d point) {
		return emissionColor;
	}
	public Vec3d getLightDirection(Vec3d point) {
		return point.sub(center);
	}
	
	/**
	 * 光线源节点在平面上的情况不考虑，当做没有交点。
	 */
	@Override
	public boolean intersect(Ray ray, List<Double> pHits) {
		Double l_n = ray.raydir.dot(norm);
		// 直线与平面平行，没有交点，不考虑直线在平面上的情况。
		if (l_n == 0.0) return false;
		Double t = center.sub(ray.rayorig).dot(norm) / l_n;
		if (t <= 0) return false;
		pHits.add(t);
		return true;
	}
	
	@Override
	public Vec3d getNormal(Vec3d pHit) {
		return norm;
	}
	
	@Override
	public String toString() {
		return "Plane";
	}
}
