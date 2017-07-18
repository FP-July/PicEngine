package servlet.user;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import dao.DBConstants;
import dao.DaoManager;
import servlet.CommonProcess;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by THU73 on 17/7/17.
 */
public class Register extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        
        try {
			DaoManager daoManager = DaoManager.getInstance();
			int status = daoManager.getUserDao().createUser(username, password);
			if(status == DBConstants.SUCCESS) {
	            req.getRequestDispatcher("views/rendering.jsp").forward(req, res);
	        } else {
	            session.setAttribute("failType", "register");
	            req.getRequestDispatcher("views/failed.jsp").forward(req, res);
	        }
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			CommonProcess.dataBaseFailure(res, e);
			return;
		}
    }
}
