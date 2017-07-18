package servlet.proj;

import java.io.IOException;
import java.io.PrintWriter;
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

public class CreateProj extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		boolean cookieValid = CommonProcess.checkSession(req, resp);
		if(!cookieValid)
			return;
		
		String username = req.getParameter("username"),
				projName = req.getParameter("projName"),
				type = req.getParameter("type");
		if(username == null || projName == null) {
			String argLack = "";
			if(username == null)
				argLack += "username ";
			if(projName == null)
				argLack += "projName ";
			if(type == null)
				argLack += "type ";
			resp.sendError(ServletConstants.LACK_ARG, argLack);
		}
		
		try {
			DaoManager daoManager = DaoManager.getInstance();
			ProjDao projDao = daoManager.getProjDao();
			int status = projDao.createProj(projName, username, type);
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
