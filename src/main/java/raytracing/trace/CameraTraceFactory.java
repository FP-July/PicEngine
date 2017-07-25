package raytracing.trace;

import java.util.HashMap;

import raytracing.Camera;
import raytracing.load.BasicFunc;

public class CameraTraceFactory {
	
	public static enum MOD {
		_rotation_with_in_horizontal_plane_init
	}

	public static CameraTrace loadInstanceByProperties(MOD mod, HashMap<String, String> opts, StringBuffer err) {
		CameraTrace cat = null;
		switch (mod) {
		case _rotation_with_in_horizontal_plane_init:
		{
			Double dis = BasicFunc.parseDoubleProperty(RotationWithInHorizontalPlane.Property.dis.name(), opts.get(RotationWithInHorizontalPlane.Property.dis.name()), err);
			Double range = BasicFunc.parseDoubleProperty(RotationWithInHorizontalPlane.Property.range.name(), opts.get(RotationWithInHorizontalPlane.Property.range.name()), err);
			Integer frame = BasicFunc.parseIntegerProperty(RotationWithInHorizontalPlane.Property.frame.name(), opts.get(RotationWithInHorizontalPlane.Property.frame.name()), err);
			Camera ca = null;
			try {
				cat = RotationWithInHorizontalPlane.getInstance(ca, dis, range, frame);
			} catch (IllegalArgumentException e) {
				cat = null;
			}
		}
			break;
		}
		return cat;
	}
}