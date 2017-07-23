package servlet.proj;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.DBConstants;
import dao.DaoManager;
import dao.ProjDao;
import model.ProjInfo;
import servlet.CommonProcess;
import servlet.ServletConstants;
import task.TaskUtils;

public class GetLog extends HttpServlet {
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		boolean cookieValid = CommonProcess.checkSession(req, resp);
		if(!cookieValid)
			return;
		
		String username = req.getParameter("username"),
				taskName = req.getParameter("taskName");
		if(username == null || taskName == null) {
			String argLack = "";
			if(username == null)
				argLack += "username ";
			if(taskName == null)
				argLack += "taskName ";
			resp.sendError(ServletConstants.LACK_ARG, argLack);
		}
		
		List<String> logs = TaskUtils.getLogs(username, taskName);
		// TODO convert the log into a more understandable format and send to the user
		sendLogsToClient(req, resp, logs);
	}
	
	private void sendLogsToClient(HttpServletRequest req, HttpServletResponse resp, List<String> logs) throws IOException {
		PrintWriter pWriter = resp.getWriter();
		for(String log : logs) {
			pWriter.write(log);
		}
		pWriter.flush();
		pWriter.close();
	}
}
