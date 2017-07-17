package servlet;

import bean.Task;
import bean.UserInfo;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by THU73 on 17/7/17.
 */
public class Info extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String username = req.getParameter("username");

        UserInfo userInfo = getUserInfo(username);

        session.setAttribute("userInfo", userInfo);
        req.getRequestDispatcher("views/info.jsp").forward(req, res);
    }

    UserInfo getUserInfo(String username) {

        // return the on-going tasks of this user
        UserInfo user = new UserInfo();
        user.setFailedCount(1);
        user.setFinishedCount(5);
        user.setRenderingCount(5);
        return user;
    }
}
