package raytracing;

import java.util.ArrayList;

import org.apache.hadoop.yarn.webapp.hamlet.Hamlet.I;

/**
 * 相机类，能够发射光线。
 * @param 	eye 	: 眼睛所在的坐标;
 * @param	center	: 相机屏幕的中心点，屏幕垂直于eye和center的连线
 * @param	up		: 相机屏幕向上方向
 * @param	fov		: 眼睛视野大小，[0~180]
 * @param	cols, rows : 相机屏幕像素点的范围
 * @author wanglt
 * 
 * @version Jul 23, 2017
 */
public class Camera {
	
	public static enum Property {
		CAMERA_EYE,
		CAMERA_CENTER,
		CAMERA_UP,
		CAMERA_FOV,
		CAMERA_HEIGHT,
		CAMERA_WIDTH
	};

	private Vec3d eye;
	private Vec3d center;
	private Vec3d vx, vy, vz;
	private double windowDistance, windowHeight, windowWidth;
	private double fov;
	private int rows, cols;
	
	public Camera(	Vec3d eye, 
					Vec3d center,
					Vec3d up,
					Double fov,
					Integer rows,
					Integer cols) {
		this.eye = eye; 
		this.center = center;
		this.fov = fov;
		this.rows = rows;
		this.cols = cols;
		
		Vec3d v = center.sub(eye);
		windowDistance = v.length();
		
		this.vz = v.normalize();
		this.vx = up.cross(vz);
		this.vy = vx.cross(vz);
		
		double angle = Math.tan(Math.PI * 0.5 * fov / 180);
		double aspectratio = 1.0 * cols / rows;
		
		windowHeight = windowDistance * angle * 2.0;
		windowWidth = windowHeight * aspectratio;
	}
	public void set(Camera ca) {
		this.eye = ca.eye;
		this.center = ca.center;
		this.vx = ca.vx;
		this.vy = ca.vy;
		this.vz = ca.vz;
		this.windowDistance = ca.windowDistance;
		this.windowHeight = ca.windowHeight;
		this.windowWidth = ca.windowWidth;
		this.fov = ca.fov;
		this.rows = ca.rows;
		this.cols = ca.cols;
	}
	
	public void viewFrame(int i) {
		System.out.println("View Frame " + i + " : ");
		System.out.println(" Org : " + eye.serialize());
		System.out.println("   X : " + vx.serialize());
		System.out.println("   Y : " + vy.serialize());
		System.out.println("   Z : " + vz.serialize());
		
		System.out.println("   Window width : " + windowWidth);
		System.out.println("         height : " + windowHeight);
	}
	
	public Vec3d getEye() { return eye; }
	public Vec3d getCenter() { return center; }
	public Vec3d getVx()  { return vx;  }
	public Vec3d getVy()  { return vy;  }
	public Vec3d getVz()  { return vz;  }
	public Double getFov() { return fov; }
	public Integer getRows() { return rows; }
	public Integer getCols() { return cols; }
	
	public Ray getRay(int col, int row) {
		return getRay(col, row, 0.5, 0.5);
	}
	
	public void getSuperSamplingRays(int col, int row, int times, ArrayList<Ray> rays) {
		double interval = 1.0 / times;
		double offset = interval / 2;
		for (double x = offset; x < 1.0; x += interval) {
			for (double y = offset; y < 1.0; y += interval) {
				rays.add(getRay(col, row, x, y));
			}
		}
	}
	
	public Ray getRay(int col, int row, double pixelAdjustmentX, double pixelAdjustmentY) {
		double x = (((double)col + pixelAdjustmentX) / cols) * windowWidth - (windowWidth / 2.0);
		double y = (((double)row + pixelAdjustmentY) / rows) * windowHeight - (windowHeight / 2.0);
//		System.out.println(x + "," + y);

		Vec3d coord = convertCoords(new Vec3d(x, y, windowDistance));
		Vec3d raydir = coord.sub(eye).normalize();

		Ray ray = new Ray(eye, raydir);

		return ray;
	}

	public Vec3d convertCoords(Vec3d v) {
		Vec3d coord = new Vec3d();
		coord.x = vx.x * v.x + vy.x * v.y + vz.x * v.z;
		coord.y = vx.y * v.x + vy.y * v.y + vz.y * v.z;
		coord.z = vx.z * v.x + vy.z * v.y + vz.z * v.z;
		
		return coord.add(eye);
	}
}
