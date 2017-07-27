package video;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.mapreduce.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import raytracing.mapreduce.RayTracerDriver;
import task.ITask;
import task.TaskUtils;

public class VideoTask implements ITask {
	private static Logger logger = LoggerFactory.getLogger(VideoTask.class);
	private static final String DEFAULT_VIDEO_DIR = "video";
	private static final String DEFAULT_CONF_NAME = "video_conf.xml";
	private static final String SUCCESS_FLAG = "_SUCCESS";
	private RayTracerDriver renderTask = new RayTracerDriver();

	private Map<String, String> confMap = new HashMap<>();
	private int width;
	private int height;
	private int fps;
	
	@Override
	public void run(String[] args) throws Exception {
		String username = args[0];
		String taskID = args[2];
		String workingDir = TaskUtils.getWorkingDir(username, taskID);
		String srcDir = TaskUtils.getSrcDir(workingDir);
		String resultDir = TaskUtils.getResultDir(workingDir);
		if(!loadConfig(srcDir))
			return;
		renderTask.run(args);
		cleanSuccessFlag(resultDir);
		if(genVideo(resultDir))
			setSuccessFlag(resultDir);
	}

	@Override
	public Job getJob() {
		return renderTask.getJob();
	}

	@Override
	public float getProgress() {
		return renderTask.getProgress();
	}
	
	private void cleanSuccessFlag(String resultDir) {
		try {
			FileSystem fSystem = FileSystem.get(TaskUtils.HDFS_URI, new Configuration());
			fSystem.delete(new Path(resultDir, SUCCESS_FLAG), false);
		} catch (Exception e) {
			logger.error("cannot clean flag in {} because {}", resultDir, e.toString());
		}
	}
	
	private void setSuccessFlag(String resultDir) {
		try {
			FileSystem fSystem = FileSystem.get(TaskUtils.HDFS_URI, new Configuration());
			fSystem.create(new Path(resultDir, SUCCESS_FLAG), false);
		} catch (Exception e) {
			logger.error("cannot set flag in {} because {}", resultDir, e.toString());
		}
	}

	private boolean loadConfig(String confPath) {
		FileSystem fSystem = null;
		try {
			fSystem = FileSystem.get(TaskUtils.HDFS_URI, new Configuration());
		} catch (IOException e) {
			logger.error("cannot load conf due to {}", e.toString());
			return false;
		}
		FSDataInputStream iStream = null;
		try {
			 iStream = fSystem.open(new Path(confPath, DEFAULT_CONF_NAME));
		} catch (IOException e) {
			logger.error("cannot load conf due to {}", e.toString());
			return false;
		}
		DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = dFactory.newDocumentBuilder();
			Document doc = builder.parse(iStream.getWrappedStream());
			Node conf = doc.getElementsByTagName("configuration").item(0);
			NamedNodeMap attrs = conf.getAttributes();
			NodeList nodeList = conf.getChildNodes();
			for(int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				confMap.put(node.getNodeName(), node.getTextContent());
			}
			width = Integer.parseInt(confMap.get("width"));
			height = Integer.parseInt(confMap.get("height"));
			fps = Integer.parseInt(confMap.get("fps"));
		} catch (Exception e) {
			logger.error("cannot load conf due to {}", e.toString());
			e.printStackTrace();
			return false;
		}
		logger.info("Video configuration: width {}, height {}, fps {}",width, height, fps);
		return true;
	}
	
	private boolean genVideo(String resultPath) {
		FileSystem fSystem = null;
		try {
			fSystem = FileSystem.get(TaskUtils.HDFS_URI, new Configuration());
		} catch (IOException e) {
			logger.error("cannot create video due to {}", e.toString());
			return false;
		}
		// get all files with name 'image'
		List<BufferedImage> images = new ArrayList<>();
		RemoteIterator<LocatedFileStatus> iterator;
		try {
			iterator = fSystem.listFiles(new Path(resultPath), false);
			while(iterator.hasNext()) {
				LocatedFileStatus fileStatus = iterator.next();
				Path path = fileStatus.getPath();
				String filename = path.getName();
				if(! filename.contains("image")) 
					continue;
				FSDataInputStream iStream = fSystem.open(path);
				BufferedImage img = ImageIO.read(iStream.getWrappedStream());
				images.add(img);
				iStream.close();
			}
		} catch (Exception e) {
			logger.error("cannot create video due to {}", e.toString());
			return false;
		}
		// generate a video with the imgs
		BufferedImage[] imgArray = images.toArray(new BufferedImage[0]);
		try {
			Path remotePath = new Path(resultPath, DEFAULT_VIDEO_DIR);
			fSystem.mkdirs(remotePath);
			File tempFile = new File("result_" + System.currentTimeMillis() + "_" + (int) (Math.random()*100000) + ".avi");
			PicToAviUtil.convertPicToAvi(imgArray, tempFile.getAbsolutePath(), fps, width, height);
			fSystem.copyFromLocalFile(true, true, new Path(tempFile.getAbsolutePath()), remotePath);
		} catch (Exception e) {
			logger.error("cannot create video due to {}", e.toString());
			return false;
		}
		return true;
	}
}
