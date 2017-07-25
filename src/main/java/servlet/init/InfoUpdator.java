package servlet.init;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import task.LogTransferThread;
import task.TaskGuardThread;

public class InfoUpdator extends HttpServlet {

	@Override
	public void init() throws ServletException {
		Thread taskGuradThread = new TaskGuardThread();
		taskGuradThread.start();
		Thread logTransferThread = new LogTransferThread();
		logTransferThread.start();
	}

}
