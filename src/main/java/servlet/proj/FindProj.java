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
import model.ProjInfo;
import servlet.CommonProcess;
import servlet.ServletConstants;

public class FindProj extends HttpServlet {
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
			ProjInfo info = projDao.findProj(username, projName);
			if(info == null) {
				resp.sendError(DBConstants.NO_SUCH_PROJ);
				return;
			}
			sendProjToClient(resp, info);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			CommonProcess.dataBaseFailure(resp, e);
		}
	}
	
	private void sendProjToClient(HttpServletResponse response, ProjInfo info) throws IOException {
		PrintWriter writer = response.getWriter();
		writer.write(info.toJSON().toString());
		writer.flush();
		writer.close();
	}
}
