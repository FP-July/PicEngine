package servlet.user;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import bean.UserInfo;
import dao.DBConstants;
import dao.DaoManager;
import servlet.CommonProcess;
import servlet.ServletConstants;
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
    	HttpSession session = req.getSession();
    	try {
    		daoManager = DaoManager.getInstance();
    		String username = req.getParameter("username");
    		String password = req.getParameter("password");
   
    		int status = daoManager.getUserDao().logIn(username, password);
    		if(status == DBConstants.SUCCESS) {
    			String sessionID = SessionIDGen.gen(username);
    			Cookie cookie = new Cookie("sessionID", sessionID);
    			resp.addCookie(cookie);
    			cookie = new Cookie("username", username);
    			resp.addCookie(cookie);
    			SessionManager.addSession(username, sessionID,
    					System.currentTimeMillis() + ServletConstants.SESSION_EXPANSION);
    			
    			session.setAttribute("username", username);
    			
    			UserInfo userInfo = daoManager.gatherUserInfo(username);
    			if(userInfo == null) {
    				resp.sendError(ServletConstants.NO_USERINFO);
    				return;
    			}
                session.setAttribute("userInfo", userInfo);
                
                req.getRequestDispatcher("views/info.jsp").forward(req, resp);
    		}	else {
    			session.setAttribute("failType", "login");
                req.getRequestDispatcher("views/failed.jsp").forward(req, resp);
    		}
		} catch (Exception e) {
			e.printStackTrace();
			CommonProcess.dataBaseFailure(resp, e);
		} 
    }
}
