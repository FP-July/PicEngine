package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.DBConstants;
import sessionManager.SessionManager;

public class CommonProcess {
	
	/** inform client in case of database failure
	 * @param response
	 * @param e
	 */
	public static void dataBaseFailure(HttpServletResponse response, Exception e) {
		try {
			response.sendError(ServletConstants.CODE_DB_INIT_FAILURE, e.toString());
			return;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	/** extract username([0]) and sessionID([1]) from cookies
	 * @param cookies
	 * @return username([0]) and sessionID([1])
	 */
	public static String[] cookies2Session(Cookie[] cookies) {
		String sessionID = null, username = null;
		for(Cookie cookie : cookies) {
			if(cookie.getName().equals("sessionID"))
				sessionID = cookie.getValue();
			else if(cookie.getName().equals("username"))
				username = cookie.getValue();
		}
		if(sessionID != null && username != null)
			return new String[]{username, sessionID};
		else
			return null;
	}
	
	/** to test if a request has valid cookie, when false, this method will
	 * 	send error code to client
	 * @param req
	 * @param resp
	 * @return true if the request's cookie is valid
	 */
	public static boolean checkSession(HttpServletRequest req, HttpServletResponse resp){
		String[] userSession = CommonProcess.cookies2Session(req.getCookies());
		if(userSession == null) {
			try {
				resp.sendError(ServletConstants.CODE_NO_COOKIE);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}
		if(!SessionManager.testSession(userSession[0], userSession[1])) {
			try {
				resp.sendError(ServletConstants.CODE_SESSION_EXPIRE);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}
		return true;
	}
}
