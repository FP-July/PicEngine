package raytracing.hadoop;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

public class PixelRecordReader extends RecordReader<IntWritable, IntWritable> {

	private Pixel start;
	private Pixel end;
	
	private int width = 0, height = 0;

	private int x = 0, y = 0;
	private IntWritable currentKey, currentValue; 
	
	public PixelRecordReader(PixelSplit inputSplit) {
		start = inputSplit.getStart();
		end = inputSplit.getEnd();
		
		x = start.x; y = start.y;
		width = end.x - start.x;
		height = end.y - start.y;
	}
	
	@Override
	public void close() throws IOException {}

	@Override
	public IntWritable getCurrentKey() throws IOException, InterruptedException {
		currentKey = new IntWritable(x);
		return currentKey;
	}

	@Override
	public IntWritable getCurrentValue() throws IOException, InterruptedException {
		currentValue = new IntWritable(y);
		return currentValue;
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		return 1.0f * ((y - start.y) * width + (x - start.x)) / (width * height);
	}

	@Override
	public void initialize(InputSplit arg0, TaskAttemptContext arg1) 
			throws IOException, InterruptedException {}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		x ++;
		if (x < end.x) return true;
		x = start.x; y ++;
		if (y < end.y) return true;
		return false;
	}
	
}
