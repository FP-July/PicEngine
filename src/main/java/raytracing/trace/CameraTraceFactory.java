package raytracing.trace;

import java.util.HashMap;

import raytracing.Camera;
import raytracing.Vec3d;
import raytracing.load.BasicLoader;

public class CameraTraceFactory {
	
	public static enum MOD {
		_rotation_with_in_horizontal_plane_init,
		_translation_with_in_line_init
	}

	public static CameraTrace loadInstanceByProperties(MOD mod, HashMap<String, String> opts, StringBuffer err) {
		CameraTrace cat = null;
		switch (mod) {
		case _rotation_with_in_horizontal_plane_init:
		{
			Double dis = BasicLoader.parseDoubleProperty(RotationWithInHorizontalPlane.Property.dis.name(), opts.get(RotationWithInHorizontalPlane.Property.dis.name()), err);
			Double range = BasicLoader.parseDoubleProperty(RotationWithInHorizontalPlane.Property.range.name(), opts.get(RotationWithInHorizontalPlane.Property.range.name()), err);
			Integer frame = BasicLoader.parseIntegerProperty(RotationWithInHorizontalPlane.Property.frame.name(), opts.get(RotationWithInHorizontalPlane.Property.frame.name()), err);
			Camera ca = null;
			try {
				cat = RotationWithInHorizontalPlane.getInstance(ca, dis, range, frame);
			} catch (IllegalArgumentException e) {
				cat = null;
			}
		}
			break;
		case _translation_with_in_line_init:
		{
			Vec3d dir = BasicLoader.parseVectorProperty(TranslationWithInLine.Property.direction.name(), opts.get(TranslationWithInLine.Property.direction.name()), err);
			Double range = BasicLoader.parseDoubleProperty(TranslationWithInLine.Property.scope.name(), opts.get(TranslationWithInLine.Property.scope.name()), err);
			Integer frame = BasicLoader.parseIntegerProperty(TranslationWithInLine.Property.frame.name(), opts.get(TranslationWithInLine.Property.frame.name()), err);
			Camera ca = null;
			try {
				cat = TranslationWithInLine.getInstance(ca, dir, range, frame);
			} catch (IllegalArgumentException e) {
				cat = null;
			}
		}
			break;
		}
		return cat;
	}
}
