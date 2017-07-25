package raytracing;

import java.awt.Color;
import java.util.regex.Pattern;

public class Vec3d {
	public double x, y, z;
	
	public Vec3d() {
		x = 0.0; y = 0.0; z = 0.0;
	}
	public Vec3d(Double v) {
		x = new Double(v);
		y = new Double(v);
		z = new Double(v);
	}
	public Vec3d(Double _x, Double _y, Double _z) {
		x = new Double(_x);
		y = new Double(_y);
		z = new Double(_z);
	}
	
	public void set(Vec3d v) {
		this.x = v.x;
		this.y = v.y;
		this.z = v.z;
	}
	public Vec3d normalize() {
		Double nor = length();
		if (nor > 0) {
			Double invNor = 1 / nor;
			x *= invNor; y *= invNor; z *= invNor;
		}
		return this;
	}
	
	public Vec3d cross(Vec3d v) {
		Vec3d cro = new Vec3d();
		cro.x = y * v.z - z * v.y;
		cro.y = z * v.x - x * v.z;
		cro.z = x * v.y - y * v.x;
		return cro.normalize();
	}
	
	public Vec3d mul(Double d) {
		return new Vec3d(x*d, y*d, z*d);
	}
	public Vec3d mul(Vec3d v) {
		return new Vec3d(x*v.x, y*v.y, z*v.z);
	}
	public Double dot(Vec3d v) {
		return x*v.x + y*v.y + z*v.z;
	}
	
	public Vec3d add(Double d) {
		return new Vec3d(x+d, y+d, z+d);
	}
	public Vec3d add(Vec3d v) {
		return new Vec3d(x+v.x, y+v.y, z+v.z);
	}
	public Vec3d sub(Double d) {
		return add(-d);
	}
	public Vec3d sub(Vec3d v) {
		return add(v.inv());
	}
	
	public Vec3d addToThis(Vec3d v) {
		x += v.x; y += v.y; z += v.z;
		return this;
	}
	public Vec3d mulToThis(Vec3d v) {
		x *= v.x; y *= v.y; z *= v.z;
		return this;
	}
	
	public Vec3d inv() {
		return new Vec3d(-x, -y, -z);
	}
	
	public String serialize() {
		return "[" + x + "," + y + "," + z + "]";
	}
	public static Vec3d deSerialize(String ser) {
		Vec3d vec = new Vec3d();
		String con = ser.substring(1, ser.length()-1);
		String[] ds = con.split(",");
		vec.x = Double.parseDouble(ds[0]);
		vec.y = Double.parseDouble(ds[1]);
		vec.z = Double.parseDouble(ds[2]);
		return vec;
	}
	
	public int getRGB() {
		int r = (int) (Math.min(1, x) * 255);
		int g = (int) (Math.min(1, y) * 255);
		int b = (int) (Math.min(1, z) * 255);
		Color color = new Color(r, g, b);
//		if (r != 255)
//		System.out.println(color.toString());
		return color.getRGB();
	}
	
	public boolean similiar(Vec3d v) {
		double bias = 0.5;
		return ((Math.abs(x - v.x) < bias) && (Math.abs(y - v.y) < bias) && (Math.abs(z - v.z) < bias));
	}
	
	public boolean isParallel(Vec3d v) {
		Vec3d cro = cross(v);
		Vec3d zero = new Vec3d();
		if (cro.equals(zero)) return true;
		return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Vec3d) {
			Vec3d v = (Vec3d) obj;
			return (x == v.x) && (y == v.y) && (z == v.z);
		}
		return false;
	}
	
	public Double length2() {
		return x*x + y*y + z*z;
	}
	public Double length() {
		return Math.sqrt(length2());
	}
}
