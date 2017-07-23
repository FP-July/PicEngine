package main;

public class MainDriver {

	static {
//		Registry.registerJobAlias("rtprep", "raytracing.mapreduce.RTPrep");
		Registry.registerJobAlias("raytracing", "raytracing.mapreduce.Main");
	}

	public static void main(String[] args) {
		String job = Registry.getJob("raytracing");
		if (args.length > 1) {
			job = Registry.getJob(args[1]);
		}
		
		try {
			System.out.println(job);
			Object t = Class.forName(job).newInstance();
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
