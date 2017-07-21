package servlet.proj;

import java.io.IOException;
import java.sql.SQLException;

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
import task.TaskRunner;

public class RunProj extends HttpServlet {
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		boolean cookieValid = CommonProcess.checkSession(req, resp);
		if(!cookieValid)
			return;
		
		String username = req.getParameter("username"),
				projName = req.getParameter("taskName");
		if(username == null || projName == null) {
			String argLack = "";
			if(username == null)
				argLack += "username ";
			if(projName == null)
				argLack += "taskName ";
			resp.sendError(ServletConstants.LACK_ARG, argLack);
		}
		
		try {
			DaoManager daoManager = DaoManager.getInstance();
			ProjDao projDao = daoManager.getProjDao();
			ProjInfo info = projDao.findProj(username, projName);
			if(info == null) {
				resp.sendError(DBConstants.NO_SUCH_PROJ, DBConstants.codeToString(DBConstants.NO_SUCH_PROJ));
				return;
			}
			if(info.status == ProjInfo.statusEnum.ongoing.ordinal()) {	
				resp.sendError(ServletConstants.TASK_ALREADY_RUNNING, ServletConstants.STR_TASK_ALREADY_RUNNING);
				return;
			}
			if(info.status == ProjInfo.statusEnum.finished.ordinal()) {
				resp.sendError(ServletConstants.TASK_ALREADY_FINISHED, ServletConstants.STR_TASK_ALREADY_FINISHED);
				return;
			}
			int status = runProj(info);
			
			if(status == ServletConstants.SUCCESS) {
				// TODO send client success
				resp.sendRedirect("...");
			} else {
				resp.sendError(status, ServletConstants.codeToString(status));
			}
			
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			CommonProcess.dataBaseFailure(resp, e);
		}
	}
	
	private int runProj(ProjInfo info) {
		TaskRunner taskRunner = TaskRunner.getInstance();
		return taskRunner.runTask(info);
	}
}
