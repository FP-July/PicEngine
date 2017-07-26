package servlet.file;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import servlet.CommonProcess;
import servlet.ServletConstants;
import task.TaskUtils;
import userfiles.FileUtils;

public class DownloadResult extends HttpServlet {
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		boolean cookieValid = CommonProcess.checkSession(req, resp);
		if(!cookieValid)
			return;
		
		String username = req.getParameter("username"),
				taskID = req.getParameter("taskID");
		if(username == null || taskID == null) {
			String argLack = "";
			if(username == null)
				argLack += "username ";
			if(taskID == null)
				argLack += "taskID ";
			resp.sendError(ServletConstants.LACK_ARG, argLack);
		}
		
		resp.setCharacterEncoding("utf-8");  
        resp.setHeader("Content-Disposition","attachment; filename="+FileUtils.ZIPPED_RESULT_NAME+"");  
        //获取响应报文输出流对象  
        ServletOutputStream  out =resp.getOutputStream();  
		FileUtils.userDownloadResult(username, taskID, out);
	}
}
