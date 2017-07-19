package raytracing.mapreduce;

import java.io.IOException;

import org.apache.commons.cli.Option;

import main.JobRegister;
import main.MainDriver;
import main.Registry;

public class Main extends JobRegister {

	public void execute(String[] args) {
		try {
			String path = "./finalPro/loc";
			RTPrep.rtPrep(path);
			RayTracer.rayTracing(path);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
