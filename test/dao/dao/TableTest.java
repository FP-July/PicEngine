package dao;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

public class TableTest extends TestCase {
	
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
		try {
			daoManager = DaoManager.getInstance();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			fail();
		}
	}
	
}
