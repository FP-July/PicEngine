package servlet.proj;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
				projName = req.getParameter("projName");
		if(username == null || projName == null) {
			String argLack = "";
			if(username == null)
				argLack += "username ";
			if(projName == null)
				argLack += "projName ";
			resp.sendError(ServletConstants.CODE_LACK_ARG, argLack);
		}
		
		try {
			DaoManager daoManager = DaoManager.getInstance();
			ProjDao projDao = daoManager.getProjDao();
			int status = projDao.createProj(projName, username);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			CommonProcess.dataBaseFailure(resp, e);
		}
	}

}
