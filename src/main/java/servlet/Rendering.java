package servlet;

import bean.Task;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

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
        session.setAttribute("username", username);
        req.getRequestDispatcher("views/rendering.jsp").forward(req, res);
    }

    public static ArrayList<Task> getRenderingTasks(String username) {

        // return the on-going tasks of this user
        ArrayList<Task> taskList = new ArrayList<Task>();

        Task task = new Task();
        task.setId(1234);
        task.setName("图形学大作业");
        task.setState(Task.ONGOING);
        task.setType(Task.PICTURE);
        task.setDate(new Date(117, 6, 25));
        task.setMinutes(50);
        task.setPercent(75);
        taskList.add(task);

        task = new Task();
        task.setId(5678);
        task.setName("宣传片剪辑");
        task.setState(Task.ONGOING);
        task.setType(Task.VIDEO);
        task.setDate(new Date(117, 5, 30));
        task.setMinutes(700);
        task.setPercent(30);
        taskList.add(task);
        return taskList;
    }
}
