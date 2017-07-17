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
		
		boolean success = false; 
		success = daoManager.getUserDao().createUser(username, password);
		assertTrue(success);
		
		success = daoManager.getUserDao().userExist(username);
		assertTrue(success);
	}
	
	@Test
	public void testDelete() {
		String username = "user2", password = "123456";
		
		boolean success = false; 
		success = daoManager.getUserDao().createUser(username, password);
		assertTrue(success);
		
		daoManager.getUserDao().deleteUser(username);
		
		success = daoManager.getUserDao().userExist(username);
		assertTrue(!success);
	}

	@Test
	public void testLogin() {
		String username = "user3", password = "123456";
		
		daoManager.getUserDao().deleteUser(username);
		
		boolean success = false; 
		success = daoManager.getUserDao().createUser(username, password);
		assertTrue(success);
		
		success = daoManager.getUserDao().logIn(username, password + "1");
		assertFalse(success);
		success = daoManager.getUserDao().logIn(username, password);
		assertTrue(success);
	}

	@Test
	public void testModifyPW() {
		String username = "user3", password = "123456";
		
		daoManager.getUserDao().deleteUser(username);
		
		boolean success = false; 
		success = daoManager.getUserDao().createUser(username, password);
		assertTrue(success);
		
		String newPW = "654321";
		success = daoManager.getUserDao().modifyPassword(username, password, newPW);
		assertTrue(success);
		
		success = daoManager.getUserDao().logIn(username, password);
		assertFalse(success);
		success = daoManager.getUserDao().logIn(username, newPW);
		assertTrue(success);
	}
}
