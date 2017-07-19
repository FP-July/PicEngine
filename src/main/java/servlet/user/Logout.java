package servlet.user;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import servlet.CommonProcess;
import servlet.ServletConstants;
import sessionManager.SessionManager;

public class Logout extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String[] userSession = CommonProcess.cookies2Session(req.getCookies());
		if(userSession == null) {
			resp.sendError(ServletConstants.NO_COOKIE);
			return;
		}
		
		SessionManager.abortSession(userSession[0]);
		req.getRequestDispatcher("index.jsp").forward(req, resp);
	}

}
