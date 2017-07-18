package servlet;

import bean.Task;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by THU73 on 17/7/17.
 */
public class Finished extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        HttpSession session = req.getSession();
        String username = req.getParameter("username");

        ArrayList<Task> taskList = getFinishedTasks(username);

        session.setAttribute("taskList", taskList);
        req.getRequestDispatcher("views/finished.jsp").forward(req, res);
    }

    ArrayList<Task> getFinishedTasks(String username) {

        ArrayList<Task> taskList = new ArrayList<Task>();
        // return the on-going tasks of this user
        Task task = new Task();
        task.setId(1234);
        task.setName("图形学大作业");
        task.setState(Task.FAILED);
        task.setType(Task.PICTURE);
        task.setDate(new Date(117, 6, 25));
        task.setMinutes(50);
        task.setPercent(75);
        taskList.add(task);

        task = new Task();
        task.setId(1234);
        task.setName("图形学大作业");
        task.setState(Task.FINISHED);
        task.setType(Task.PICTURE);
        task.setDate(new Date(117, 6, 25));
        task.setMinutes(50);
        task.setPercent(100);
        taskList.add(task);

        return taskList;
    }
}
