package raytracing.trace;

import raytracing.Camera;
import raytracing.Ray;
import raytracing.Vec3d;
import raytracing.trace.CameraTrace;

public class RotationWithInHorizontalPlane implements CameraTrace {
	
	public static enum Property {
		dis,
		range,
		frame
	}
	
	private Vec3d center = null;
	private double dis = 0.0;
	
	private Camera origCamera = null;
	private double scope = 0.0;
	private int frame;
	private double step = 0.0;
	private double angle = 0.0;
	
	private double cenLength, eyeLength;
	private Vec3d vx, vy, vz;
	
	private Vec3d up = new Vec3d(0.0, 0.0, 1.0);
	
	public RotationWithInHorizontalPlane(Vec3d center) {
		this.center = center;
	}
	public RotationWithInHorizontalPlane(double dis) {
		this.dis = dis;
	}
	
	/**
	 * 获取以距离相机眼睛 dis 为中心点的水平面旋转轨迹
	 * @param ca
	 * @param dis
	 * @return RotationWithInHorizontalPlane
	 */
	public static RotationWithInHorizontalPlane getInstance(Camera ca, Double dis, Double range, Integer frame)
		throws IllegalArgumentException {
		if (dis == null || range == null || frame == null) {
			throw new IllegalArgumentException();
		}
		
		RotationWithInHorizontalPlane rhp = new RotationWithInHorizontalPlane(dis);
		rhp.setInitCameraLocation(ca);
		rhp.setTraceScope(range);
		rhp.setFrames(frame);
		return rhp;
	}

	@Override
	public boolean setInitCameraLocation(Camera ca) {
		if (ca == null) return false;

		origCamera = ca;
		if (center != null) {
			dis = origCamera.getEye().sub(center).length();
		} else {
			center = origCamera.getEye().add(origCamera.getVz().mul(dis));
		}
		
		Vec3d dir = origCamera.getEye().sub(center);
		
		if (!dir.isParallel(ca.getVz())) return false;
		if (!up.isParallel(ca.getVy())) return false;
		
		eyeLength = dir.length();
		cenLength = origCamera.getCenter().sub(center).length();
		
		vz = up.normalize();
		vx = dir.normalize();
		vy = vz.cross(vx);
		
		return true;
	}

	/** 从上向下俯视
	 * @param range : 旋转的角度，大于0时逆向旋转，小于0时正向旋转。
	 */
	@Override
	public void setTraceScope(double range) {
		scope = range;
	}

	@Override
	public void setFrames(int frame) {
		this.frame = frame;
		step = scope / frame;
	}
	
	@Override
	public int getFrames() {
		return frame;
	}
	
	@Override
	public Camera skipToFrame(int num) {
		angle = step * num;
		if ((angle - scope) * angle <= 0)
			return getSpecificAngleCameraFrame(angle);
		return null;
	}

	@Override
	public Camera getNextCameraFrame() {
		angle += step;
		if ((angle - scope) * angle <= 0)
			return getSpecificAngleCameraFrame(angle);
		return null;
	}
	
	@Override
	public Camera getPrevCameraFrame() {
		angle -= step;
		if ((angle - scope) * angle <= 0)
			return getSpecificAngleCameraFrame(angle);
		return null;
	}
	
	private Camera getSpecificAngleCameraFrame(double angle) {
		double rad = Math.PI * angle / 180;
		double x = Math.cos(rad);
		double y = Math.sin(rad);
		Vec3d centerCoord = new Vec3d(cenLength * x, cenLength * y, 0.0);
		Vec3d eyeCoord = new Vec3d(eyeLength * x, eyeLength * y, 0.0);
		
		Vec3d cen = convertCoords(centerCoord);
		Vec3d eye = convertCoords(eyeCoord);
		
		Camera camera = new Camera(eye, cen, up, origCamera.getFov(), origCamera.getRows(), origCamera.getCols());
		return camera;
	}
	
	public Vec3d convertCoords(Vec3d v) {
		Vec3d coord = new Vec3d();
		coord.x = vx.x * v.x + vy.x * v.y + vz.x * v.z;
		coord.y = vx.y * v.x + vy.y * v.y + vz.y * v.z;
		coord.z = vx.z * v.x + vy.z * v.y + vz.z * v.z;
		
		return coord.add(center);
	}

}
