package raytracing.trace;

import raytracing.Camera;

public abstract class CameraTrace {

	public abstract boolean setInitCameraLocation(Camera ca);
	
	public abstract void setTraceScope(double range);
	
	public abstract void setFrames(int frame);
	
	/**
	 * 0 < num < frame
	 * @param num
	 */
	public abstract Camera skipToFrame(int num);
	
	public abstract Camera getNextCameraFrame();
	public abstract Camera getPrevCameraFrame();
	
}
