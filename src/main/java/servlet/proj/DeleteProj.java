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
import servlet.CommonProcess;
import servlet.ServletConstants;

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
				projName = req.getParameter("taskName");
		if(username == null || projName == null) {
			String argLack = "";
			if(username == null)
				argLack += "taskname ";
			if(projName == null)
				argLack += "taskName ";
			resp.sendError(ServletConstants.LACK_ARG, argLack);
		}
		
		try {
			DaoManager daoManager = DaoManager.getInstance();
			ProjDao projDao = daoManager.getProjDao();
			int status = projDao.deleteProj(username, projName);
			if(status == DBConstants.SUCCESS) {
				//TODO send client a success msg
				resp.sendRedirect("...");
			} else {
				//TODO add error pages
				resp.sendError(status, ServletConstants.codeToString(status));
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			CommonProcess.dataBaseFailure(resp, e);
		}
	}
}
