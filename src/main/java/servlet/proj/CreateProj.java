package servlet.proj;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.security.interfaces.RSAKey;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import dao.DBConstants;
import dao.DaoManager;
import dao.ProjDao;
import model.ProjInfo;
import servlet.CommonProcess;
import servlet.ServletConstants;

public class CreateProj extends HttpServlet {

	// 上传文件存储目录
	private static final String UPLOAD_DIRECTORY = "hdfs://localhost:9000/userfiles";
	private static final String USER_SRC_FOLDER = "src";

	// 上传配置
	private static final int MEMORY_THRESHOLD = 1024 * 1024 * 3; // 3MB
	private static final int MAX_FILE_SIZE = 1024 * 1024 * 40; // 40MB
	private static final int MAX_REQUEST_SIZE = 1024 * 1024 * 50; // 50MB

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		boolean cookieValid = CommonProcess.checkSession(req, res);
		if (!cookieValid)
			return;

		// 检测是否为多媒体上传
		if (!ServletFileUpload.isMultipartContent(req)) {
			// 如果不是则停止
			PrintWriter writer = res.getWriter();
			writer.println("Error: 表单必须包含 enctype=multipart/form-data");
			writer.flush();
			return;
		}

		// 配置上传参数
		DiskFileItemFactory factory = new DiskFileItemFactory();
		// 设置内存临界值 - 超过后将产生临时文件并存储于临时目录中
		factory.setSizeThreshold(MEMORY_THRESHOLD);
		// 设置临时存储目录
		factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

		ServletFileUpload upload = new ServletFileUpload(factory);

		// 设置最大文件上传值
		upload.setFileSizeMax(MAX_FILE_SIZE);

		// 设置最大请求值 (包含文件和表单数据)
		upload.setSizeMax(MAX_REQUEST_SIZE);

		// 构造临时路径来存储上传的文件
		// 这个路径相对当前应用的目录
		String uploadPath = getServletContext().getContextPath() + File.separator + UPLOAD_DIRECTORY;

		// 如果目录不存在则创建
		File uploadDir = new File(uploadPath);
		if (!uploadDir.exists()) {
			uploadDir.mkdir();
		}
		String filePath = null;
		List<FileItem> fileItems = new ArrayList<>();
		HashMap<String, String> parameters = new HashMap<String, String>();
		try {
			// 解析请求的内容提取文件数据
			List<FileItem> formItems = upload.parseRequest(req);

			if (formItems != null && formItems.size() > 0) {
				// 迭代表单数据
				for (FileItem item : formItems) {
					// 处理不在表单中的字段
					if (!item.isFormField()) {
						// 稍后再处理文件上传
						fileItems.add(item);
					} else {
						String name = item.getFieldName();
						String value = null;
						try {
							value = item.getString("utf-8");
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						parameters.put(name, value);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		HttpSession session = req.getSession();

		String username = parameters.get("username");
		String taskType = parameters.get("task_type");
		String taskName = parameters.get("task_name");

		if (username == null || taskName == null || taskType == null) {
			String argLack = "";
			if (username == null)
				argLack += "username ";
			if (taskName == null)
				argLack += "taskName ";
			if (taskType == null)
				argLack += "taskType ";
			res.sendError(ServletConstants.LACK_ARG, argLack);
			return;
		}
		//System.out.println("[create] " + username + " " + taskName + " " + taskType + " " + fileItems.size() + " files");
		try {
			DaoManager daoManager = DaoManager.getInstance();
			ProjDao projDao = daoManager.getProjDao();
			int status = projDao.createProj(taskName, username, taskType);
			if (status == DBConstants.SUCCESS) {
				session.setAttribute("username", username);
				status = uploadToHDFS(username, taskName, fileItems, projDao);
				if(status == ServletConstants.SUCCESS)
					req.getRequestDispatcher("views/create_success.jsp").forward(req, res);
				else
					res.sendError(status, ServletConstants.codeToString(status));
			} else {
				res.sendError(status, DBConstants.codeToString(status));
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			CommonProcess.dataBaseFailure(res, e);
		}
	}

	/**
	 * upload user file onto HDFS
	 * 
	 * @param username
	 * @param taskName
	 * @param fileItems
	 * @param projDao
	 * @return status code
	 */
	private int uploadToHDFS(String username, String taskName, List<FileItem> fileItems, ProjDao projDao) {
		// find taskID
		ProjInfo projInfo = projDao.findProj(username, taskName);
		if (projInfo == null) {
			return ServletConstants.NO_USERINFO;
		}
		/*
		 * String fileName = new File(item.getName()).getName(); filePath =
		 * uploadPath + File.separator + fileName; File storeFile = new
		 * File(filePath); // 在控制台输出文件的上传路径 System.out.println(filePath); //
		 * 保存文件到硬盘 item.write(storeFile); req.setAttribute("message",
		 * "文件上传成功!");
		 */
		FileSystem fSystem = null;
		Configuration conf = new Configuration();
		conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");

		try {
			fSystem = FileSystem.get(URI.create(UPLOAD_DIRECTORY), conf);
		} catch (IOException e) {
			e.printStackTrace();
			return ServletConstants.HADOOP_FS_CRASH;
		}
		String userFolder = UPLOAD_DIRECTORY + File.separator + username + File.separator + projInfo.projID
				+ File.separator + USER_SRC_FOLDER + File.separator;
		Path path = null;
		for (FileItem fileItem : fileItems) {
			try {
				String fileName = new File(fileItem.getName()).getName();
				path = new Path(userFolder + fileName);
				// save locally
				File tempFile = new File("temp_" + System.currentTimeMillis());
				tempFile.createNewFile();
				fileItem.write(tempFile);
				// transfer to HDFS
				fSystem.copyFromLocalFile(new Path(tempFile.getAbsolutePath()), path);
				tempFile.delete();
			} catch (Exception e) {
				e.printStackTrace();
				// clean file
				try {
					if (fSystem.exists(path))
						fSystem.delete(path, true);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				return ServletConstants.UPLOAD_FAIL;
			}
		}
		return ServletConstants.SUCCESS;
	}
}
