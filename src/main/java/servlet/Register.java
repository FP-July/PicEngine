package servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created by THU73 on 17/7/17.
 * use user/register instead
 */
public class Register extends HttpServlet {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        String info = register(username, password);

        if(info.equals("success")) {
            req.getRequestDispatcher("views/rendering.jsp").forward(req, res);
        } else {
            session.setAttribute("failType", "register");
            req.getRequestDispatcher("views/failed.jsp").forward(req, res);
        }

    }

    private String register(String username, String password) {

        boolean success = true;
        if(success) {
            //if register successfully
            return "success";
        } else {
            //else return the failure information like "user exists"...
            return "user exists";
        }
    }
}
