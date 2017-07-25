package raytracing.model;

import java.util.HashMap;

import raytracing.Vec3d;
import raytracing.load.BasicLoader;

public class PrimFactory {

	public static enum MOD {
		_sphere_init,
		_plane_init
	}

	public static Primitive loadInstanceByProperties(MOD mod, HashMap<String, String> opts, StringBuffer err) {
		Primitive prim = null;
		switch (mod) {
		case _sphere_init:
		{
			Vec3d center = BasicLoader.parseVectorProperty(Sphere.Property.center.name(), opts.get(Sphere.Property.center.name()), err);
			Double radius = BasicLoader.parseDoubleProperty(Sphere.Property.radius.name(), opts.get(Sphere.Property.radius.name()), err);
			Vec3d surfaceColor = BasicLoader.parseVectorProperty(Sphere.Property.surfaceColor.name(), opts.get(Sphere.Property.surfaceColor.name()), err);
			Vec3d emissionColor = BasicLoader.parseVectorProperty(Sphere.Property.emissionColor.name(), opts.get(Sphere.Property.emissionColor.name()), err);
			Double transparency = BasicLoader.parseDoubleProperty(Sphere.Property.transparency.name(), opts.get(Sphere.Property.transparency.name()), err);
			Double reflection = BasicLoader.parseDoubleProperty(Sphere.Property.reflection.name(), opts.get(Sphere.Property.reflection.name()), err);
			try {
				prim = new Sphere(center, radius, surfaceColor, reflection, transparency, emissionColor);
			} catch (IllegalArgumentException e) {
				prim = null;
			}
		}
			break;
		case _plane_init:
		{
			Vec3d center = BasicLoader.parseVectorProperty(Plane.Property.center.name(), opts.get(Plane.Property.center.name()), err);
			Vec3d norm = BasicLoader.parseVectorProperty(Plane.Property.norm.name(), opts.get(Plane.Property.norm.name()), err);
			Vec3d surfaceColor = BasicLoader.parseVectorProperty(Plane.Property.surfaceColor.name(), opts.get(Plane.Property.surfaceColor.name()), err);
			Vec3d emissionColor = BasicLoader.parseVectorProperty(Plane.Property.emissionColor.name(), opts.get(Plane.Property.emissionColor.name()), err);
			try {
				prim = new Plane(center, norm, surfaceColor, emissionColor);
			} catch (IllegalArgumentException e) {
				prim = null;
			}
		}
			break;
		}
		return prim;
	}
}
