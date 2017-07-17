package servlet;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.DaoManager;
import sessionManager.SessionIDGen;
import sessionManager.SessionManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

/**
 * Created by THU73 on 17/7/14.
 */
public class Login extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	DaoManager daoManager;
    	try {
    		daoManager = DaoManager.getInstance();
    		String username = req.getParameter("username");
    		String password = req.getParameter("password");
   
    		boolean success = daoManager.getUserDao().logIn(username, password);
    		if(success) {
    			String sessionID = SessionIDGen.gen(username);
    			Cookie cookie = new Cookie("sessionID", sessionID);
    			resp.addCookie(cookie);
    			cookie = new Cookie("username", username);
    			resp.addCookie(cookie);
    			SessionManager.addSession(username, sessionID,
    					System.currentTimeMillis() + ServletConstants.SESSION_EXPANSION);
    			//TODO send the user to home page
    			resp.sendRedirect("link to the home page");
    		}	else {
    			resp.sendError(ServletConstants.CODE_LOGIN_FAIL);
    		}
		} catch (Exception e) {
			e.printStackTrace();
			CommonProcess.dataBaseFailure(resp, e);
		} 
    }
}
