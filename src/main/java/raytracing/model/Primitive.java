package raytracing.model;

import java.util.List;

import raytracing.Ray;
import raytracing.Vec3d;

/**
 * 抽象物体类，所有的物体具体实现都继承于该类，提供对外必须的接口。
 * @author wanglt
 * Jul 22, 2017
 */
public abstract class Primitive {
	
	/*
	 * 返回光线是否与该物体有交点，如果有，将交点与光线源节点的距离信息存储在pHits中，。
	 */
	public abstract boolean intersect(Ray ray, List<Double> pHits);
	/*
	 * 返回物体在表面该点的法向量。
	 */
	public abstract Vec3d getNormal(Vec3d pHit);
	/*
	 * 返回物体表面该点的颜色。
	 */
	public abstract Vec3d getSurfaceColor(Vec3d pHit);
	
	/*
	 * 返回物体表面该点的透明度，为0时光线不折射。
	 */
	public abstract Double getTransparency(Vec3d pHit);
	/*
	 * 返回物体表面该点的反射系数，为0时表示不反射。
	 */
	public abstract Double getReflection(Vec3d pHit);
	/*
	 * 返回物体表面该点的漫反射系数，为0时表示不反射。
	 */
	public abstract Double getDiffusion(Vec3d pHit);
	
	/*
	 * 返回该物体是否为光源。
	 */
	public abstract boolean isLight();
	/*
	 * 返回该物体发出的光线到某点的颜色。
	 */
	public abstract Vec3d getEmissionColor(Vec3d point);
	/*
	 * 返回该物体发出的光线到某点的方向。
	 */
	public abstract Vec3d getLightDirection(Vec3d point);
	
	@Override
	public String toString() {
		return "Primitive";
	}
}
