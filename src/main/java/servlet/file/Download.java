package servlet.file;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import servlet.CommonProcess;
import servlet.ServletConstants;
import userfiles.FileUtils;

public class Download extends HttpServlet {
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		boolean cookieValid = CommonProcess.checkSession(req, resp);
		if(!cookieValid)
			return;
		
		String username = req.getParameter("username"),
				taskID = req.getParameter("taskID"),
				filename = req.getParameter("filename");
		if(username == null || taskID == null) {
			String argLack = "";
			if(username == null)
				argLack += "username ";
			if(taskID == null)
				argLack += "taskID ";
			if(filename == null)
				argLack += "filename ";
			resp.sendError(ServletConstants.LACK_ARG, argLack);
		}
		
		resp.setCharacterEncoding("utf-8");  
        resp.setHeader("Content-Disposition","attachment; filename="+filename+"");  
        //获取响应报文输出流对象  
        ServletOutputStream  out =resp.getOutputStream();  
		int status = FileUtils.userDownload(username, taskID, filename, out);
		
		if(status == ServletConstants.SUCCESS) {
			// TODO send client a success
		} else {
			resp.sendError(status);
		}
	}
}
