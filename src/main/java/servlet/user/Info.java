package servlet.user;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.UserInfo;
import dao.DaoManager;
import servlet.CommonProcess;
import servlet.ServletConstants;

public class Info extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		boolean cookieValid = CommonProcess.checkSession(req, resp);
		if(!cookieValid)
			return;
		HttpSession session = req.getSession();
		
		String username = req.getParameter("username");
		DaoManager daoManager = null;
		try {
			daoManager = DaoManager.getInstance();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			CommonProcess.dataBaseFailure(resp, e);
			return;
		}
		UserInfo userInfo = daoManager.gatherUserInfo(username);
		if(userInfo == null) {
			resp.sendError(ServletConstants.NO_USERINFO);
			return;
		}
		
		session.setAttribute("userInfo", userInfo);
        session.setAttribute("username", username);
        req.getRequestDispatcher("views/info.jsp").forward(req, resp);
	}
}
