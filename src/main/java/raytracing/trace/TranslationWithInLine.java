package raytracing.trace;

import raytracing.Camera;
import raytracing.Vec3d;

public class TranslationWithInLine implements CameraTrace {
	
	public static enum Property {
		direction,
		scope,
		frame
	}
	
	private Vec3d dir;
	private double scope = 0.0;
	private int frame = 0;
	private double dis = 0.0;
	private double step = 0.0;
	private Camera origCamera;
	
	public TranslationWithInLine(Vec3d dir) {
		this.dir = dir.normalize();
	}
	
	public static TranslationWithInLine getInstance(Camera ca, Vec3d dir,  Double range, Integer frame) 
		throws IllegalArgumentException {
		if (dir == null || range == null || frame == null) {
			throw new IllegalArgumentException();
		}
		
		TranslationWithInLine twl = new TranslationWithInLine(dir);
		twl.setInitCameraLocation(ca);
		twl.setTraceScope(range);
		twl.setFrames(frame);
		return twl;
	}

	@Override
	public boolean setInitCameraLocation(Camera ca) {
		if (ca == null) return false;
		
		this.origCamera = ca;
		return true;
	}

	@Override
	public void setTraceScope(double range) {
		this.scope = range;
	}

	@Override
	public void setFrames(int frame) {
		this.frame = frame;
		this.step = scope / frame;
	}

	@Override
	public int getFrames() {
		return frame;
	}

	@Override
	public Camera skipToFrame(int num) {
		dis = step * num;
		if ((dis - scope) * dis <= 0) {
			return getSpecificDistenceCameraFrame(dis);
		}
		return null;
	}

	@Override
	public Camera getNextCameraFrame() {
		dis += step;
		if ((dis - scope) * dis <= 0) {
			return getSpecificDistenceCameraFrame(dis);
		}
		return null;
	}

	@Override
	public Camera getPrevCameraFrame() {
		dis -= step;
		if ((dis - scope) * dis <= 0) {
			return getSpecificDistenceCameraFrame(dis);
		}
		return null;
	}
	
	private Camera getSpecificDistenceCameraFrame(double dis) {
		Vec3d center = origCamera.getCenter().add(this.dir.mul(dis));
		Vec3d eye = origCamera.getEye().add(this.dir.mul(dis));
		Vec3d up = origCamera.getUp();
		double fov = origCamera.getFov();
		int rows = origCamera.getRows();
		int cols = origCamera.getCols();
		Camera ca = new Camera(eye, center, up, fov, rows, cols);
		return ca;
	}

}
