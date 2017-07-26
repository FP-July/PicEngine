package raytracing.load;

import raytracing.Vec3d;

public class BasicLoader {
	
	public static enum ENV {
		NATIVE,
		HDFS
	}

	public static Double parseDoubleProperty(String key, String value, StringBuffer err) {
		if (value == null) {
			err.append("property " + key + " never be set;");
			return null;
		}
		try {
			double opt = Double.parseDouble(value);
			return opt;
		} catch (Exception e) {
			err.append("property " + key + " number format error;");
			return null;
		}
	}
	
	public static Integer parseIntegerProperty(String key, String value, StringBuffer err) {
		if (value == null) {
			err.append("property " + key + " never be set;");
			return null;
		}
		try {
			int opt = Integer.parseInt(value);
			return opt;
		} catch (Exception e) {
			err.append("property " + key + " number format error;");
			return null;
		}
	}
	
	public static Vec3d parseVectorProperty(String key, String value, StringBuffer err) {
		if (value == null) {
			err.append("property " + key + " never be set;");
			return null;
		}
		Vec3d vec = new Vec3d();
		String[] coords = value.split(",");
		if (coords.length != 3) {
			err.append("property " + key + ": coord error;");
			return null;
		}
		
		try {
			vec.x = Double.parseDouble(coords[0]);
			vec.y = Double.parseDouble(coords[1]);
			vec.z = Double.parseDouble(coords[2]);
		} catch (Exception e) {
			err.append("property " + key + ": number format error;");
			return null;
		}
		
		return vec;
	}
}
