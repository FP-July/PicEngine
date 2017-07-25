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

/**
 * read by coords with pixel, return with <key : col[x], value : row[y]>
 * @author wanglt
 * @version Jul 23, 2017
 */
public class PixelInputFormat extends InputFormat<IntWritable, IntWritable> {

	@Override
	public RecordReader<IntWritable, IntWritable> createRecordReader(InputSplit inputSplit, TaskAttemptContext context)
			throws IOException, InterruptedException {
		return new PixelRecordReader(context.getConfiguration());
	}

	@Override
	public List<InputSplit> getSplits(JobContext arg0) throws IOException, InterruptedException {
		ArrayList<InputSplit> splits = new ArrayList<>();
        splits.add(new EmptySplits());
        return splits;
	}
	
	public class PixelRecordReader extends RecordReader<IntWritable, IntWritable> {

		private int width = 0, height = 0;
		private int x = 0, y = 0;
		private IntWritable currentKey, currentValue; 
		
		public PixelRecordReader(Configuration conf) {
			width = Integer.parseInt(conf.get(Camera.Property.CAMERA_WIDTH.name(), "640"));
			height = Integer.parseInt(conf.get(Camera.Property.CAMERA_HEIGHT.name(), "480"));
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
			return 1.0f * (y * width + x) / (width * height);
		}

		@Override
		public void initialize(InputSplit arg0, TaskAttemptContext arg1) 
				throws IOException, InterruptedException {}

		@Override
		public boolean nextKeyValue() throws IOException, InterruptedException {
			x ++;
			if (x < width) return true;
			x = 0; y ++;
			if (y < height) return true;
			return false;
		}
		
	}

	public static class EmptySplits extends InputSplit implements Writable {

        @Override
        public long getLength() throws IOException, InterruptedException {
            return 0L;
        }

        @Override
        public String[] getLocations() throws IOException, InterruptedException {
            return new String[0];
        }

        @Override
        public void write(DataOutput out) throws IOException {

        }

        @Override
        public void readFields(DataInput in) throws IOException {

        }
    }
}
