package task;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dao.DaoManager;
import dao.ProjDao;
import model.ProjInfo;

/** this thread periodically collect 
 * @author jt
 *
 */
public class TaskGuardThread extends Thread {

	private static Logger logger = LoggerFactory.getLogger(TaskGuardThread.class);
	private final int CHECK_INTERVAL = 60 * 1000; // in ms
	
	
	@Override
	public void run() {
		while(true) {
			ensureAllRunning();
			try {
				this.sleep(CHECK_INTERVAL);
			} catch (InterruptedException e) {
				logger.error("unexpected interruption {}", e.toString());
				e.printStackTrace();
			}
		}
	}

	/** make sure tasks in status ongoing are actually running
	 * 
	 */
	private void ensureAllRunning() {
		try {
			ProjDao projDao = DaoManager.getInstance().getProjDao();
			List<ProjInfo> list = projDao.findAllProjsByInt("status", ProjInfo.statusEnum.ongoing.ordinal());
			for(ProjInfo info : list) {
				try {
					info.status = TaskUtils.checkStatus(info.username, String.valueOf(info.projID));
					if(info.status != ProjInfo.statusEnum.ongoing.ordinal()) {
						projDao.updateProjStatus(info.username, info.projName, info.status);
					}
				} catch (IllegalArgumentException | IOException e) {
					logger.error("check status of {} of {} failed due to {}",
							info.projName, info.username, e.toString());
					e.printStackTrace();
				}
			}
			
		} catch (ClassNotFoundException | SQLException e) {
			logger.error("EnsureRunning failed to run due to {}", e.toString());
			e.printStackTrace();
		}
	}
}
