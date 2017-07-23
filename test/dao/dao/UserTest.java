package dao;

import static org.junit.Assert.*;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

public class UserTest {

DaoManager daoManager;
	
	@Before
	public void beforTest(){
		try {
			daoManager = DaoManager.getInstance();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testCreate() {
		String username = "user", password = "123456";
		
		daoManager.getUserDao().deleteUser(username);
		
		int status = DBConstants.SQL_EXCUTION_ERROR; 
		status = daoManager.getUserDao().createUser(username, password);
		assertTrue(status == DBConstants.SUCCESS);
		
		status = daoManager.getUserDao().userExist(username);
		assertTrue(status == DBConstants.USER_ALREADY_EXIST);
	}
	
	@Test
	public void testDelete() {
		String username = "user2", password = "123456";
		
		int status = DBConstants.SQL_EXCUTION_ERROR; 
		status = daoManager.getUserDao().createUser(username, password);
		assertTrue(status == DBConstants.SUCCESS);
		
		daoManager.getUserDao().deleteUser(username);
		
		status = daoManager.getUserDao().userExist(username);
		assertTrue(status == DBConstants.NO_SUCH_USER);
	}

	@Test
	public void testLogin() {
		String username = "user3", password = "123456";
		
		daoManager.getUserDao().deleteUser(username);
		
		int status = DBConstants.SQL_EXCUTION_ERROR; 
		status = daoManager.getUserDao().createUser(username, password);
		assertTrue(status == DBConstants.SUCCESS);
		
		status = daoManager.getUserDao().logIn(username, password + "1");
		assertTrue(status == DBConstants.WRONG_PW);
		status = daoManager.getUserDao().logIn(username, password);
		assertTrue(status == DBConstants.SUCCESS);
	}

	@Test
	public void testModifyPW() {
		String username = "user3", password = "123456";
		
		daoManager.getUserDao().deleteUser(username);
		
		int status = DBConstants.SQL_EXCUTION_ERROR; 
		status = daoManager.getUserDao().createUser(username, password);
		assertTrue(status == DBConstants.SUCCESS);
		
		String newPW = "654321";
		status = daoManager.getUserDao().modifyPassword(username, password, newPW);
		assertTrue(status == DBConstants.SUCCESS);
		
		status = daoManager.getUserDao().logIn(username, password);
		assertTrue(status == DBConstants.WRONG_PW);
		status = daoManager.getUserDao().logIn(username, newPW);
		assertTrue(status == DBConstants.SUCCESS);
	}
}
