package raytracing.hadoop;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import raytracing.Camera;
import raytracing.mapreduce.RayTracerDriver;

/**
 * read by coords with pixel, return with <key : col[x], value : row[y]>
 * @author wanglt
 * @version Jul 23, 2017
 */
public class PixelInputFormat extends InputFormat<IntWritable, IntWritable> {
	
	private final int STD_LEN = 200;
	private final int MIN_SPLIT_SIZE = 2, MAX_SPLIT_SIZE = 8;
	
	private final int STD_MAX_RAY_DAPTH = 5;
	private final int STD_SUPER_SAMPLING_TIMES = 5;

	@Override
	public RecordReader<IntWritable, IntWritable> createRecordReader(InputSplit inputSplit, TaskAttemptContext context)
			throws IOException, InterruptedException {
		return new PixelRecordReader((PixelSplit) inputSplit);
	}

	@Override
	public List<InputSplit> getSplits(JobContext context) throws IOException, InterruptedException {
		Configuration conf = context.getConfiguration();
		int width = Integer.parseInt(conf.get(Camera.Property.CAMERA_WIDTH.name(), "640"));
		int height = Integer.parseInt(conf.get(Camera.Property.CAMERA_HEIGHT.name(), "480"));
		int max_ray_depth = Integer.parseInt(conf.get(RayTracerDriver.PARAMS.MAX_RAY_DEPTH.name()));
		int super_sampling_times = Integer.parseInt(conf.get(RayTracerDriver.PARAMS.SUPER_SAMPLING_TIMES.name()));

		int splitLength = (int) (1.0f * STD_LEN 
				* STD_MAX_RAY_DAPTH * STD_SUPER_SAMPLING_TIMES * STD_SUPER_SAMPLING_TIMES  
				/ max_ray_depth / super_sampling_times  / super_sampling_times);
		
		int widthStep = getSuitableSplitLength(width, splitLength);
		int heightStep = getSuitableSplitLength(height, splitLength);
		System.out.println("width step : " + widthStep + ", height step : " + heightStep + ", split len : " + splitLength);

		ArrayList<InputSplit> splits = new ArrayList<>();
		int x = 0, y = 0;
		while (x < width) {
			int xBegin = x;
			int xEnd = Math.min(xBegin + widthStep, width);
			while (y < height) {
				int yBegin = y;
				int yEnd = Math.min(yBegin + heightStep, height);
				PixelSplit ps = new PixelSplit(xBegin, xEnd, yBegin, yEnd);
				splits.add(ps);
				
				y += heightStep;
			}
			y = 0;
			x += widthStep;
		}
        return splits;
	}
	
	private int getSuitableSplitLength(int len, int splitLength) {
		
		int minLen = (MIN_SPLIT_SIZE - 1) * splitLength;
		int maxLen = MAX_SPLIT_SIZE * splitLength;
		if (len <= minLen) return len / MIN_SPLIT_SIZE;
		if (len > maxLen) return len / MAX_SPLIT_SIZE;
		
		return splitLength;
	}
}
