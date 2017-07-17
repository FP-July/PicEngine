package servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created by THU73 on 17/7/14.
 */
public class Login extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        HttpSession session = req.getSession();
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        assert (username != null && password != null);

        boolean success = verifyLogin(username, password);

        if(success) {
            session.setAttribute("username", username);
            req.getRequestDispatcher("views/info.jsp").forward(req, res);
        } else {
            session.setAttribute("failType", "login");
            req.getRequestDispatcher("views/failed.jsp").forward(req, res);
        }

    }

    private boolean verifyLogin(String username, String password) {

        //verify...
        if (username.equals("admin") && password.equals("admin")) {
            return true;
        }


        return false;
    }
}
