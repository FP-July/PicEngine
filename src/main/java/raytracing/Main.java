package raytracing;

import java.io.IOException;

import raytracing.mapreduce.RTPrep;
import raytracing.mapreduce.RayTracer;
import utils.StaticValue;

public class Main {

	public static void main(String[] args) {
		try {
			RTPrep.rtPrep(StaticValue.LOC_PATH);
			RayTracer.rayTracing(StaticValue.LOC_PATH);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
