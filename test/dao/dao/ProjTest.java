package dao;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import dao.DBConstants;
import dao.DaoManager;
import dao.ProjDao;
import model.ProjInfo;

public class ProjTest {

	DaoManager daoManager;

	@Before
	public void beforTest() {
		try {
			daoManager = DaoManager.getInstance();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void createAndDeleteTest() {
		String projName = "firstProj", username = "newuser";
		int status = DBConstants.SQL_EXCUTION_ERROR;
		ProjDao projDao = daoManager.getProjDao();
		projDao.deleteProj(username, projName);
		
		status = projDao.createProj(projName, username,"none");
		assertTrue(status == DBConstants.SUCCESS);
		status = projDao.createProj(projName, username,"none");
		assertTrue(status == DBConstants.PROJ_ALREADY_EXIST);
		
		ProjInfo info = projDao.findProj(username, projName);
		assertFalse(info == null);
		assertTrue(info.username.equals(username));
		assertTrue(info.projName.equals(projName));
		assertTrue(info.status == ProjInfo.statusEnum.init.ordinal());
		
		status = projDao.deleteProj(username, projName);
		assertTrue(status == DBConstants.SUCCESS);
		info = projDao.findProj(username, projName);
		assertFalse(info != null);
	}
	
	@Test
	public void listTest() {
		String projName = "firstProj", username = "newuser5";
		ProjDao projDao = daoManager.getProjDao();
		for(int i = 0; i < 5; i++) {
			projDao.deleteProj(username + i, projName);
			projDao.createProj(projName + i, username, "none");
		}
		
		List<ProjInfo> list = projDao.listUserProj(username);
		assertTrue(list != null);
		assertTrue(list.size() == 5);
		assertTrue(list.get(4).projName.equals(projName + 4));
	}

	@Test
	public void updateTest() {
		String projName = "secondProj", username = "newuser2";
		int status = DBConstants.SQL_EXCUTION_ERROR;
		ProjDao projDao = daoManager.getProjDao();
		status = projDao.createProj(projName, username,"none");
		
		status = projDao.updateProjStatus(username, projName, ProjInfo.statusEnum.finished.ordinal());
		assertTrue(status == DBConstants.SUCCESS);
		ProjInfo info = projDao.findProj(username, projName);
		assertTrue(info.status == ProjInfo.statusEnum.finished.ordinal());
		
		status = projDao.updateProjInt(username, projName, "progress", 100);
		assertTrue(status == DBConstants.SUCCESS);
		info = projDao.findProj(username, projName);
		assertTrue(info.progress == 100);
	}

	@Test
	public void renameTest() {
		String projName = "seconsdProj2", username = "newuser4", newName = "ssssdddd";
		int status = DBConstants.SQL_EXCUTION_ERROR;
		ProjDao projDao = daoManager.getProjDao();
		projDao.deleteProj(username, projName);
		projDao.deleteProj(username,newName);
		projDao.createProj(projName, username,"none");
		
		status = projDao.renameProj(username, projName, newName);
		assertTrue(status == DBConstants.SUCCESS);
		ProjInfo info = projDao.findProj(username, newName);
		assertTrue(info != null);
		info = projDao.findProj(username, projName);
		assertTrue(info == null);
	}
	
	@Test
	public void findTest() {
		String projName = "findTest", username = "newuserfindtest";
		ProjDao projDao = daoManager.getProjDao();
		for(int i = 0; i < 10; i++) {
			projDao.deleteProj(username, projName + i);
			projDao.createProj(projName + i, username,"none");
		}
		
		for(int i = 0; i < 3; i++) {
			projDao.updateProjInt(username, projName + i, "status", ProjInfo.statusEnum.finished.ordinal());
		}
		List<ProjInfo> list = projDao.findProjsByInt(username, "status", ProjInfo.statusEnum.finished.ordinal());
		assertTrue(list.size() == 3);
		list = projDao.findProjsByInt(username, "status", ProjInfo.statusEnum.init.ordinal());
		assertTrue(list.size() == 7);
	}
}
