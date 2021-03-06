package servlet.proj;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.DaoManager;
import dao.ProjDao;
import servlet.CommonProcess;
import servlet.ServletConstants;

public class RenameProj extends HttpServlet {
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
				projName = req.getParameter("taskName"),
				newProjName = req.getParameter("newTaskName");
		if(username == null || projName == null || newProjName == null) {
			String argLack = "";
			if(username == null)
				argLack += "username ";
			if(projName == null)
				argLack += "taskName ";
			if(newProjName == null)
				argLack += "newTaskName";
			resp.sendError(ServletConstants.LACK_ARG, argLack);
		}
		
		try {
			DaoManager daoManager = DaoManager.getInstance();
			ProjDao projDao = daoManager.getProjDao();
			int status = projDao.renameProj(username, projName, newProjName);
			PrintWriter writer = resp.getWriter();
			writer.write(status + "\n");
			writer.flush();
			writer.close();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			CommonProcess.dataBaseFailure(resp, e);
		}
	}
}
