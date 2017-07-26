package raytracing.hadoop;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.InputSplit;

public class PixelSplit extends InputSplit implements Writable {
	
	private Pixel start;
	private Pixel end;

	public PixelSplit(int xBegin, int xEnd, int yBegin, int yEnd) {
		start = new Pixel(xBegin, yBegin);
		end = new Pixel(xEnd, yEnd);
	}
	public PixelSplit() {}
	
	public Pixel getStart() {
		return start;
	}
	public Pixel getEnd() {
		return end;
	}
	
    @Override
    public long getLength() throws IOException, InterruptedException {
        return (start.x - end.x) * (start.y - end.y);
    }

    @Override
    public String[] getLocations() throws IOException, InterruptedException {
        return new String[0];
    }

    @Override
    public void write(DataOutput out) throws IOException {
    	out.writeInt(start.x);
    	out.writeInt(start.y);
    	out.writeInt(end.x);
    	out.writeInt(end.y);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
    	int x = in.readInt();
    	int y = in.readInt();
    	start = new Pixel(x, y);
    	x = in.readInt();
    	y = in.readInt();
    	end = new Pixel(x, y);
    }
}