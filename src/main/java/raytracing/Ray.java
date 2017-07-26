package raytracing;

/**
 * 光线类，包含源节点和光线方向
 * @author wanglt
 * @version Jul 23, 2017
 */
public class Ray {

	public Vec3d rayorig;
	public Vec3d raydir;
	
	public Ray(Vec3d orig, Vec3d dir) {
		rayorig = orig;
		raydir = dir;
	}
	
	public Vec3d cross(Double dis) {
		return rayorig.add(raydir.mul(dis));
	}
}
