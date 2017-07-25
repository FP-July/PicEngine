package raytracing.hadoop;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import raytracing.mapreduce.RayTracerDriver.PARAMS;

public class JobStateFlagCreator {

	public static void createSuccessFlag(Configuration conf) throws IOException {
		FileSystem fs = FileSystem.get(conf);
		Path path = new Path(conf.get(PARAMS.OUTPUT_PATH.name()));
		/* 创建_SUCCESS文件 */
		Path succPath = new Path(path, "_SUCCESS");
		OutputStream fos = fs.create(succPath, true);
		fos.close();
	}
	
	public static void createFailedFlag(Configuration conf) throws IOException {
		FileSystem fs = FileSystem.get(conf);
		Path path = new Path(conf.get(PARAMS.OUTPUT_PATH.name()));
		/* 创建_SUCCESS文件 */
		Path succPath = new Path(path, "_FAILED");
		OutputStream fos = fs.create(succPath, true);
		fos.close();
	}
}
