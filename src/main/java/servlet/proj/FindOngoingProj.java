package servlet.proj;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dao.DBConstants;
import dao.DaoManager;
import dao.ProjDao;
import model.ProjInfo;
import servlet.CommonProcess;
import servlet.ServletConstants;
import task.TaskUtils;

public class FindOngoingProj extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		boolean cookieValid = CommonProcess.checkSession(req, resp);
		if(!cookieValid)
			return;
		
		String username = req.getParameter("username");
		
		if(username == null ) {
			String argLack = "";
			if(username == null)
				argLack += "username ";
			resp.sendError(ServletConstants.LACK_ARG, argLack);
		}
		
		try {
			DaoManager daoManager = DaoManager.getInstance();
			ProjDao projDao = daoManager.getProjDao();
			List<ProjInfo> ongoingInfos = projDao.findProjsByInt(username, "status", ProjInfo.statusEnum.ongoing.ordinal());
			// get progress for ongoing tasks
			for(ProjInfo info : ongoingInfos) {
				float[] progress = TaskUtils.getProgress(username, info.projName);
				if(progress != null) {
					info.progress = (int)((progress[0] + progress[1]) / 2);
				} else {
					info.progress = -1;
				}
			}
			List<ProjInfo> initInfos = projDao.findProjsByInt(username, "status", ProjInfo.statusEnum.init.ordinal());
			List<ProjInfo> readyInfos = projDao.findProjsByInt(username, "status", ProjInfo.statusEnum.ready.ordinal());
			if(ongoingInfos == null || initInfos == null || readyInfos == null) {
				resp.sendError(DBConstants.NO_SUCH_PROJ);
				return;
			}
			ongoingInfos.addAll(initInfos);
			ongoingInfos.addAll(readyInfos);
			CommonProcess.sendProjsToClient(req, resp, ongoingInfos, "views/rendering.jsp");
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			CommonProcess.dataBaseFailure(resp, e);
		}
	}
	
	
}
