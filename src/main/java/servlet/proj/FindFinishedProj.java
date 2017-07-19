package servlet.proj;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.Task;
import dao.DBConstants;
import dao.DaoManager;
import dao.ProjDao;
import model.ProjInfo;
import servlet.CommonProcess;
import servlet.ServletConstants;

public class FindFinishedProj extends HttpServlet{
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		boolean cookieValid = CommonProcess.checkSession(req, resp);
		if(!cookieValid)
			return;
		
		String username = req.getParameter("username");
		
		if(username == null ) {
			String argLack = "";
			if(username == null)
				argLack += "username ";
			resp.sendError(ServletConstants.LACK_ARG, argLack);
		}
		
		try {
			DaoManager daoManager = DaoManager.getInstance();
			ProjDao projDao = daoManager.getProjDao();
			List<ProjInfo> finishedInfos = projDao.findProjsByInt(username, "status", ProjInfo.statusEnum.finished.ordinal());
			List<ProjInfo> errorInfos = projDao.findProjsByInt(username, "status", ProjInfo.statusEnum.error.ordinal());
			if(finishedInfos == null || errorInfos == null) {
				resp.sendError(DBConstants.NO_SUCH_PROJ);
				return;
			}
			finishedInfos.addAll(errorInfos);
			CommonProcess.sendProjsToClient(req, resp, finishedInfos, "views/finished.jsp");
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			CommonProcess.dataBaseFailure(resp, e);
		}
	}
}
