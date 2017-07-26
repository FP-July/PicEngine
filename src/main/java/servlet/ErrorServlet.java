package servlet;

import dao.DBConstants;
import session.Session;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created by THU73 on 17/7/19.
 */
public class ErrorServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        Integer statusCode = (Integer)
                req.getAttribute("javax.servlet.error.status_code");
        String errorMessage = DBConstants.codeToString(statusCode);

        HttpSession session = req.getSession();
        session.setAttribute("errorMessage", errorMessage);

        req.getRequestDispatcher("views/error.jsp").forward(req, res);
    }
}
