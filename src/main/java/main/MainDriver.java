package main;

public class MainDriver {

	static {
		Registry.registerJobAlias("rtprep", "raytracing.mapreduce.RTPrep");
		Registry.registerJobAlias("raytracing", "raytracing.mapreduce.RayTracer");
	}
	
	public static void printUsage() {
		System.out.println("> Usage : ");
		System.out.println(Registry.printUsage(1));
	}

	public static void main(String[] args) {
		String job = Registry.getJob("raytracing");
		if (args.length > 0) {
			job = Registry.getJob(args[0]);
		} else {
			printUsage();
			System.exit(0);
		}
		String[] otherArgs = new String[args.length - 1];
		for (int i = 1; i < args.length; ++i) {
			otherArgs[i - 1] = args[i];
		}
		
		try {
			System.out.println(job);
			Object t = Class.forName(job).newInstance();
			JobRegister jr = null;
			if (t instanceof JobRegister) {
				jr = (JobRegister) t;
			}
			jr.execute(otherArgs);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
