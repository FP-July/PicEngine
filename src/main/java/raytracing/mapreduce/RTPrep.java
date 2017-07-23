package raytracing.mapreduce;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import main.JobRegister;
import raytracing.hadoop.EmptyInputFormat;
import utils.StaticValue;

public class RTPrep extends JobRegister {
	
	public static int height = 480, width = 640;

	public static class RTPrepMapper
		extends Mapper<Object, Object, Text, Text> {
		
		@Override
		public void map(Object key, Object value, Context context)
			throws IOException, InterruptedException {
			context.write(new Text("1"), new Text("1"));
		}
	}
	
	public static class RTPrepReducer
		extends Reducer<Text, Text, Text, Text> {
		
		public void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {
			for (int y = 0; y < height; ++y) {
				for (int x = 0; x < width; ++x) {
					context.write(new Text("loc"), new Text(x + "," + y));
				}
			}
		}
	}
	
	public void execute(String[] args) {
		try {
			RTPrep.rtPrep(StaticValue.LOC_PATH + "/" + width + "_" + height);
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
	
	public static void rtPrep(String outPath) 
		throws IOException, ClassNotFoundException, InterruptedException {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "rtprep");
		job.setJarByClass(RTPrep.class);
		job.setMapperClass(RTPrepMapper.class);
		job.setReducerClass(RTPrepReducer.class);
		
		job.setNumReduceTasks(1);
		
		job.setInputFormatClass(EmptyInputFormat.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
//		FileInputFormat.addInputPath(job, new Path(srcPath));
		FileOutputFormat.setOutputPath(job, new Path(outPath));
		job.waitForCompletion(true);
	}
}
