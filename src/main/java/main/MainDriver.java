package main;

public class MainDriver {

	static {
//		Registry.registerJobAlias("rtprep", "raytracing.mapreduce.RTPrep");
		Registry.registerJobAlias("raytracing", "raytracing.mapreduce.Main");
	}

	public static void main(String[] args) {
		try {
			System.out.println(Registry.getJob("raytracing"));
			Object t = Class.forName(Registry.getJob("raytracing")).newInstance();
			JobRegister jr = null;
			if (t instanceof JobRegister) {
				jr = (JobRegister) t;
			}
			jr.execute(args);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
