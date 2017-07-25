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
import task.TaskUtils;

public class DeleteProj extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
				argLack += "taskname ";
			if(taskName == null)
				argLack += "taskName ";
			resp.sendError(ServletConstants.LACK_ARG, argLack);
		}
		
		try {
			DaoManager daoManager = DaoManager.getInstance();
			ProjDao projDao = daoManager.getProjDao();
			ProjInfo projInfo = projDao.findProj(username, taskName);
			if(projInfo == null) {
				resp.sendError(DBConstants.NO_SUCH_PROJ);
				return;
			}
			int status = projDao.deleteProj(username, taskName);
			if(status == DBConstants.SUCCESS) {
				TaskUtils.cleanTaskFile(username, taskName, String.valueOf(projInfo.projID));
				//TODO send client a success msg
				resp.sendRedirect("...");
			} else {
				resp.sendError(status, ServletConstants.codeToString(status));
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			CommonProcess.dataBaseFailure(resp, e);
		}
	}
}
