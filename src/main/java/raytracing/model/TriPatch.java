package raytracing.model;

import java.util.List;

import raytracing.Ray;
import raytracing.Vec3d;

public class TriPatch extends Primitive {
	
	private Vec3d v0, v1, v2;
	private Vec3d surfaceColor, emissionColor;
	
	private Vec3d E1, E2;
	
	public TriPatch(Vec3d v0, Vec3d v1, Vec3d v2,
					Vec3d sc, Vec3d ec) {
		this.v0 = v0;
		this.v1 = v1;
		this.v2 = v2;
		this.surfaceColor = sc;
		this.emissionColor = ec;
		
		this.E1 = this.v1.sub(this.v0);
		this.E2 = this.v2.sub(this.v0);
	}

	@Override
	public boolean intersect(Ray ray, List<Double> pHits) {
		return false;
	}

	@Override
	public Vec3d getNormal(Vec3d pHit) {
		return null;
	}

	@Override
	public Vec3d getSurfaceColor(Vec3d pHit) {
		return null;
	}

	@Override
	public Double getTransparency(Vec3d pHit) {
		return null;
	}

	@Override
	public Double getReflection(Vec3d pHit) {
		return null;
	}

	@Override
	public boolean isLight() {
		return false;
	}

	@Override
	public Vec3d getEmissionColor(Vec3d point) {
		return null;
	}

	@Override
	public Vec3d getLightDirection(Vec3d point) {
		return null;
	}

	@Override
	public Double getDiffusion(Vec3d pHit) {
		return null;
	}

}
