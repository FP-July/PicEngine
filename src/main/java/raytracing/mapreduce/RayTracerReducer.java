package raytracing.mapreduce;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import raytracing.Camera;
import raytracing.Vec3d;
import raytracing.mapreduce.RayTracerDriver.PARAMS;

public class RayTracerReducer
	extends Reducer<Text, Text, Text, Text> {

	public int width = 0, height = 0;
	private BufferedImage image;
	
	@Override
	protected void setup(Reducer<Text, Text, Text, Text>.Context context) 
		throws IOException, InterruptedException {
		Configuration conf = context.getConfiguration();
	    height = Integer.parseInt(   conf.get(Camera.Property.CAMERA_HEIGHT.name(), "480"));
	    width  = Integer.parseInt(   conf.get(Camera.Property.CAMERA_WIDTH.name(),  "640"));
		image  = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	}
	
	public void reduce(Text key, Iterable<Text> values, Context context)
		throws IOException, InterruptedException {
		String[] coord = key.toString().split(",");
		
		Text text = values.iterator().next();
		if (text == null || text.toString().equals("")) return ;
		
		int x = Integer.parseInt(coord[0]);
		int y = Integer.parseInt(coord[1]);
		Vec3d rgb = Vec3d.deSerialize(text.toString());
		image.setRGB(x, y, rgb.getRGB());
	}
	
	public void cleanup(Context context) 
		throws IOException, InterruptedException {
		Configuration conf = context.getConfiguration();
		FileSystem fs = FileSystem.get(conf);
		
		Path path = new Path(conf.get(PARAMS.OUTPUT_PATH.name()));
		Path filePath = new Path(path, conf.get(PARAMS.OUTPUT_FILE_NAME.name()));
		OutputStream fos = fs.create(filePath, true);
	    
	    ImageOutputStream stream = null;
        try {
            stream = ImageIO.createImageOutputStream(fos);
        } catch (IOException e) {
            throw new IIOException("Can't create output stream!", e);
        }
		
		JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
	    jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
	    jpegParams.setCompressionQuality(1f);
	    
	    try {
		    ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
			writer.setOutput(stream);
		
			writer.write(null, new IIOImage(image, null, null), jpegParams);
			writer.reset();
	    } catch (IOException e) {
	    	e.printStackTrace();
	    }
	    fos.flush();
		fos.close();
	}
}
