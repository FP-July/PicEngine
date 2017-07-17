package servlet;

import bean.Task;

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
public class Rendering extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String username = req.getParameter("username");

        ArrayList<Task> taskList = getRenderingTasks(username);

        session.setAttribute("taskList", taskList);
        req.getRequestDispatcher("views/rendering.jsp").forward(req, res);
    }

    ArrayList<Task> getRenderingTasks(String username) {

        // return the on-going tasks of this user

        return new ArrayList<Task>();
    }
}
