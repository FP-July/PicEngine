package raytracing;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
 *
 */
public class EmptyInputFormat extends InputFormat<Object, Object> {

    boolean isReadingDone = false;

    @Override
    public List<InputSplit> getSplits(JobContext job) throws IOException {
        ArrayList<InputSplit> splits = new ArrayList<>();
        splits.add(new EmptySplits());
        return splits;
    }

    @Override
    public RecordReader<Object, Object> createRecordReader(InputSplit split,
                                                           TaskAttemptContext context) throws IOException, InterruptedException {
        return new RecordReader<Object, Object>() {
            @Override
            public void initialize(InputSplit split,
                                   TaskAttemptContext context) throws IOException, InterruptedException {

            }

            @Override
            public boolean nextKeyValue() throws IOException, InterruptedException {
                if (isReadingDone) {
                    return false;
                } else {
                    isReadingDone = true;
                    return true;
                }
            }

            @Override
            public Object getCurrentKey() throws IOException, InterruptedException {
                return new Object();
            }

            @Override
            public Object getCurrentValue() throws IOException, InterruptedException {
                return new Object();
            }

            @Override
            public float getProgress() throws IOException, InterruptedException {
                if (isReadingDone) {
                    return 1.0f;
                } else {
                    return 0.0f;
                }
            }

            @Override
            public void close() throws IOException {

            }
        };
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
