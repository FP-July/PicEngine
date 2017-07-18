package raytracing.mapreduce;

import java.io.IOException;

import org.apache.commons.cli.Option;

import main.MainDriver;
import main.Registry;

public class Main {
	static {
//		Registry.registerJobAlias("rtprep", "raytracing.mapreduce.RTPrep");
		Registry.registerJobAlias("raytracing", "raytracing.mapreduce.Main");
	}

	public static void main(String[] args) {
		try {
			RTPrep.rtPrep("./loc.txt");
			RayTracer.rayTracing("./loc.txt");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
