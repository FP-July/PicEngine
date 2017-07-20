package task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapred.lib.MultipleOutputs;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;

public class TaskFrame implements ITask {

	private static String WORK_DIR = "/user/jt/";
	private static final float STOP_PERC = 0.01f; // percentage of stop words

	public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {

		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			StringTokenizer itr = new StringTokenizer(value.toString(), " .\\:;'\"+*/()!@#$%^&*`~|[]{}<>,?	");
			while (itr.hasMoreTokens()) {
				word.set(itr.nextToken());
				if (word.getLength() == 0)
					continue;
				context.write(word, one);
			}
		}
	}

	public static class IntSumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
		private IntWritable result = new IntWritable();

		public void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			result.set(sum);
			context.write(key, result);
		}
	}

	public static void collectWordCount(String output, Configuration conf) throws IOException {
		FileSystem fs = FileSystem.get(conf);
		RemoteIterator<LocatedFileStatus> itr = fs.listFiles(new Path(output), true);
		HashMap<String, Integer> wordMap = new HashMap<>();
		BufferedReader reader;

		// read MR results
		while (itr.hasNext()) {
			LocatedFileStatus fileStatus = itr.next();
			if (fileStatus.getPath().toString().contains("part-")) {
				FSDataInputStream iStream = fs.open(fileStatus.getPath());
				reader = new BufferedReader(new InputStreamReader(iStream.getWrappedStream()));

				String line;
				while ((line = reader.readLine()) != null) {
					String[] parts = line.split("	");
					String word = parts[0];
					int count = Integer.parseInt(parts[1]);
					Integer preCount = wordMap.get(word);
					if (preCount == null) {
						wordMap.put(word, count);
					} else {
						wordMap.put(word, count + preCount);
					}
				}
				reader.close();
			}
		}
		// sort by count
		Object[] entries = wordMap.entrySet().toArray();
		Arrays.sort(entries, new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				if (((Entry<String, Integer>) o1).getValue() == ((Entry<String, Integer>) o2).getValue())
					return 0;
				return ((Entry<String, Integer>) o1).getValue() > ((Entry<String, Integer>) o2).getValue() ? -1 : 1;
			}
		});
		writeStopList(entries, fs);
		writeFullList(entries, fs);
		fs.close();
	}

	public static void writeFullList(Object[] entries, FileSystem fs) throws IllegalArgumentException, IOException {
		FSDataOutputStream oStream = fs.create(new Path("lab1/fullList"), true);
		int wordListSize = entries.length;
		for (int i = 0; i < wordListSize; i++) {
			Entry<String, Integer> entry = (Entry<String, Integer>) entries[i];
			oStream.writeBytes(entry.getKey() + " " + entry.getValue() + "\n");
		}
		oStream.close();
	}

	public static void writeStopList(Object[] entries, FileSystem fs) throws IllegalArgumentException, IOException {
		FSDataOutputStream oStream = fs.create(new Path("lab1/stopList"), true);
		int wordListSize = entries.length;
		int stopListSize = new Double(Math.ceil(wordListSize * STOP_PERC)).intValue();
		for (int i = 0; i < stopListSize; i++) {
			Entry<String, Integer> entry = (Entry<String, Integer>) entries[i];
			oStream.writeBytes(entry.getKey() + " " + entry.getValue() + "\n");
		}
		oStream.close();
	}

	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length != 4) {
			System.err.println("Usage: wordcount <in> <out> <log> <confPath>");
			String dString = null;
			dString.length();
		}
		// ------------------------------------
		String inputPath = otherArgs[0];
		String outputPath = otherArgs[1];
		String logPath = otherArgs[2];
		String rootPath = otherArgs[3];
		FileSystem fs = FileSystem.get(new Configuration());
		FSDataOutputStream oStream = fs.create(new Path(logPath), true);

		WriterAppender writerAppender = new WriterAppender(new SimpleLayout(), oStream);
		BasicConfigurator.configure(writerAppender);

		if (fs.exists(new Path(outputPath)))
			fs.delete(new Path(outputPath), true);

		conf.addResource(new Path(rootPath + "yarn-site.xml"));
		conf.addResource(new Path(rootPath + "core-site.xml"));
		conf.addResource(new Path(rootPath + "hdfs-site.xml"));
		conf.addResource(new Path(rootPath + "mapred-site.xml"));
		conf.set("mapred.jar", "TaskFrame.jar");
		// --------------------------------------

		Job job = new Job(conf, "word count");
		job.setJarByClass(TaskFrame.class);
		job.setMapperClass(TokenizerMapper.class);
		job.setCombinerClass(IntSumReducer.class);
		job.setReducerClass(IntSumReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		MultipleOutputs.addNamedOutput(new JobConf(conf), "wordCounts", TextOutputFormat.class, Text.class,
				IntWritable.class);

		int exitCode = job.waitForCompletion(true) ? 0 : 1;

		collectWordCount(otherArgs[1], conf);

	}

	@Override
	public void run(String[] args) throws ClassNotFoundException, IOException, InterruptedException {
		main(args);
	}
}