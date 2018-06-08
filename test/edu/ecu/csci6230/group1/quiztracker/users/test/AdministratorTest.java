package edu.ecu.csci6230.group1.quiztracker.users.test;

import static org.junit.Assert.*;

import java.io.FileReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JCheckBox;

import org.h2.tools.RunScript;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.ecu.csci6230.group1.quiztracker.dataconnection.DatabaseConnection;
import edu.ecu.csci6230.group1.quiztracker.managers.UserManager;
import edu.ecu.csci6230.group1.quiztracker.users.Administrator;
import edu.ecu.csci6230.group1.quiztracker.users.UserType;

public class AdministratorTest {

	private static String testDatabase = "jdbc:h2:./H2DBforTesting/QUIZTRACKER;MODE=MySQL;DATABASE_TO_UPPER=false;TRACE_LEVEL_SYSTEM_OUT=0;";
	private static String defaultUser = "quizuser";
	private static String defaultPassword = "quizuser";
	private static String testDatabaseClass = "org.h2.Driver";
	private static DatabaseConnection SQLConnection;
	public static final String fullName = "David White";
	public static final String firstName = "David";
	public static final String lastName = "White";
	public static final String userId = "dwhite";
	public static final String password = "dwhite1234";
	public static final UserType role = UserType.ADMIN;

	public static ArrayList<JCheckBox> courses;
	public static final String course1 = "CSCI\t  6840\t  100\t  spring\t  2017";
	public static final String course2 = "SENG\t  6230\t  601\t  fall\t  2016";
	public static final String course3 = "CSCI\t  6250\t  100\t  fall\t  2016";
	public static final String course4 = "SENG\t  6240\t  101\t  fall\t  2016";

	private Administrator admin;

	@Before
	public void setUp() throws Exception {
		SQLConnection = DatabaseConnection.getInstance(testDatabase, defaultUser, defaultPassword, testDatabaseClass);
		RunScript.execute(SQLConnection.GetConnection(), new FileReader("TestScripts/AdminTestSetup.txt"));

		courses = new ArrayList<JCheckBox>();
		courses.add(new JCheckBox(course1, false));
		courses.add(new JCheckBox(course2, true));
		courses.add(new JCheckBox(course3, false));
		courses.add(new JCheckBox(course4, true));

		admin = new Administrator(firstName, lastName, userId, true, role);

		// this ensures the default password for testing
		admin.resetUserPassword(userId);
	}

	@After
	public void tearDown() throws Exception {
		RunScript.execute(SQLConnection.GetConnection(), new FileReader("TestScripts/TearDownScript.txt"));
	}

	/**
	 * this tests the constructor, really isn't needed because we have a basic
	 * constructor and all the other tests will fail if the constructor fails.
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testAdministrator() {
		Administrator admin2 = (Administrator) UserManager.verifyUser(userId, "dwhite1234");
		assertEquals(userId, admin2.getUserId());
	}

	@Test
	public void testResetUserPassword() throws SQLException {
		// test initial parameters
		PreparedStatement stmt = SQLConnection.GetConnection()
				.prepareStatement("SELECT reset_password, password FROM USER WHERE user = 'jdoe'");
		ResultSet results = stmt.executeQuery();
		int passwordStatus = -1;
		String password = "";
		if (results.next()) {
			passwordStatus = results.getInt(1);
			password = results.getString(2);
		}
		assertEquals("Before reset password should have status of 0", 0, passwordStatus);
		assertEquals("Before reset password should be jdoe123", "jdoe123", password);

		// do something
		admin.resetUserPassword("jdoe");

		// verify the changes
		stmt = SQLConnection.GetConnection()
				.prepareStatement("SELECT reset_password, password FROM USER WHERE user = 'jdoe'");
		results = stmt.executeQuery();
		passwordStatus = -1;
		if (results.next()) {
			passwordStatus = results.getInt(1);
			password = results.getString(2);
		}
		assertEquals("Reset password should have status of 1", 1, passwordStatus);
		assertEquals("Reset password should be jdoe1234", "jdoe1234", password);
		stmt.close();
		results.close();
	}

	// this test effects quiz, attendance and tables, so test them all.
	@Test
	public void testRemoveUser() throws SQLException {
		try {
			admin.removeUser(userId);
			fail("Should not have deleted the only admin");
		} catch (IllegalArgumentException e) {
			assertEquals("Can not remove administrator", e.getMessage());
		}

		PreparedStatement stmt = SQLConnection.GetConnection()
				.prepareStatement("SELECT active, user FROM USER WHERE user='dhead'");
		ResultSet results = stmt.executeQuery();
		int status = -1;
		String user = "";
		if (results.next()) {
			status = results.getInt(1);
			user = results.getString(2);
		}
		assertEquals("Before delete user dhead active value is 1", 1, status);
		assertEquals("Before delete user is dhead", "dhead", user);

		stmt = SQLConnection.GetConnection().prepareStatement("SELECT count(user) FROM ENROLLMENT WHERE user='dhead'");
		results = stmt.executeQuery();
		int numCourses = -1;
		if (results.next()) {
			numCourses = results.getInt(1);
		}
		assertEquals("Before delete user dhead courses is 2", 2, numCourses);

		admin.removeUser("dhead");

		stmt = SQLConnection.GetConnection().prepareStatement("SELECT active, user FROM USER WHERE user='dhead'");
		results = stmt.executeQuery();
		user = "";
		status = -1;
		if (results.next()) {
			status = results.getInt(1);
			user = results.getString(2);
		}
		assertEquals("After delete active status is 0", 0, status);
		assertEquals("After delete user is dhead", "dhead", user);

		stmt = SQLConnection.GetConnection().prepareStatement("SELECT count(user) FROM ENROLLMENT WHERE user='dhead'");
		results = stmt.executeQuery();
		numCourses = -1;
		if (results.next()) {
			numCourses = results.getInt(1);
		}
		assertEquals("After delete user dhead courses is 0", 0, numCourses);

		stmt = SQLConnection.GetConnection().prepareStatement("SELECT active, user FROM USER WHERE user='jdoe'");
		results = stmt.executeQuery();
		status = -1;
		user = "";
		if (results.next()) {
			status = results.getInt(1);
			user = results.getString(2);
		}
		assertEquals("Before delete user jdoe active value is 1", 1, status);
		assertEquals("Before delete user is jdoe", "jdoe", user);

		stmt = SQLConnection.GetConnection().prepareStatement("SELECT count(user) FROM ENROLLMENT WHERE user='jdoe'");
		results = stmt.executeQuery();
		numCourses = -1;
		if (results.next()) {
			numCourses = results.getInt(1);
		}
		assertEquals("Before delete user jdoe courses is 3", 3, numCourses);

		admin.removeUser("jdoe");

		stmt = SQLConnection.GetConnection().prepareStatement("SELECT active, user FROM USER WHERE user='jdoe'");
		results = stmt.executeQuery();
		user = "";
		status = -1;
		if (results.next()) {
			status = results.getInt(1);
			user = results.getString(2);
		}
		assertEquals("After delete active status is 0", 0, status);
		assertEquals("After delete user is jdoe", "jdoe", user);

		stmt = SQLConnection.GetConnection().prepareStatement("SELECT count(user) FROM ENROLLMENT WHERE user='jdoe'");
		results = stmt.executeQuery();
		numCourses = -1;
		if (results.next()) {
			numCourses = results.getInt(1);
		}
		assertEquals("After delete user jdoe courses is 0", 0, numCourses);

		stmt.close();
		results.close();
	}

	/**
	 * This will test the add user function and test the constructor which
	 * should not allow duplicate userID's
	 * 
	 * @throws SQLException
	 */
	@Test
	public void testAddUser() throws SQLException {
		// Lets try and create a duplicate student, John Doe 2
		// first, make sure he isn't there.

		PreparedStatement stmt = SQLConnection.GetConnection()
				.prepareStatement("SELECT user FROM USER WHERE user='jdoe2'");
		ResultSet results = stmt.executeQuery();
		int count = -1;
		if (results.next()) {
			count = results.getInt(1);
		} else {
			count = 0;
		}
		assertEquals("Before add user jdoe2 count is 0", 0, count);

		String newUser = admin.addUser(UserType.STUDENT, "John", "Doe", "jdoe", courses);
		assertEquals("jdoe2", newUser);

		stmt = SQLConnection.GetConnection().prepareStatement("SELECT user FROM USER WHERE user='jdoe2'");
		results = stmt.executeQuery();
		String user = "";
		if (results.next()) {
			user = results.getString(1);
		}
		stmt = SQLConnection.GetConnection().prepareStatement("SELECT count(user) FROM ENROLLMENT WHERE user='jdoe2'");
		results = stmt.executeQuery();
		int numCourses = -1;
		if (results.next()) {
			numCourses = results.getInt(1);
		}
		assertEquals("After add user jdoe2 user is jdoe2", "jdoe2", user);
		assertEquals("After add user jdoe2 courses is 2", 2, numCourses);

		stmt.close();
		results.close();

		try {
			newUser = admin.addUser(UserType.STUDENT, "", "Doe", "jdoe", courses);
		} catch (IllegalArgumentException e) {
			assertEquals("Fields can not be empty. Please try again", e.getMessage());
		}
		try {
			newUser = admin.addUser(UserType.STUDENT, "John", "", "jdoe", courses);
		} catch (IllegalArgumentException e) {
			assertEquals("Fields can not be empty. Please try again", e.getMessage());
		}
		try {
			newUser = admin.addUser(UserType.STUDENT, "John", "Doe", "", courses);
		} catch (IllegalArgumentException e) {
			assertEquals("Fields can not be empty. Please try again", e.getMessage());
		}
	}

	@Test
	public void testUpdateUser() throws SQLException {
		PreparedStatement stmt = SQLConnection.GetConnection()
				.prepareStatement("SELECT dept FROM ENROLLMENT WHERE user='jdoe' " + "order by dept");
		ResultSet results = stmt.executeQuery();
		if (results.next()) {
			assertEquals("CSCI", "CSCI", results.getString(1));
			results.next();
			assertEquals("SENG", "SENG", results.getString(1));
			results.next();
			assertEquals("SENG", "SENG", results.getString(1));
			if (results.next()) {
				fail("Should have retrieved only 3 sets.");
			}
		} else {
			fail("Should have retrieved results.");
		}

		stmt = SQLConnection.GetConnection().prepareStatement(
				"SELECT dept, course, section, term, year, count(date) as ct FROM ATTENDANCE WHERE user='jdoe' and active=1 "
						+ "group by dept, course, section, term, year order by dept");
		results = stmt.executeQuery();
		if (results.next()) {
			assertEquals("CSCI", "CSCI", results.getString(1));
			assertEquals("6840", "6840", results.getString(2));
			assertEquals("attended 3 CSCI courses", 3, results.getInt("ct"));
			results.next();
			assertEquals("SENG", "SENG", results.getString(1));
			assertEquals("6240", "6240", results.getString(2));
			assertEquals("attended 5 SENG 6240 courses", 5, results.getInt("ct"));
			results.next();
			assertEquals("SENG", "SENG", results.getString(1));
			assertEquals("6230", "6230", results.getString(2));
			assertEquals("attended 9 SENG 6230 courses", 9, results.getInt("ct"));
			if (results.next()) {
				fail("Should have retrieved only 3 sets.");
			}
		} else {
			fail("Should have retrieved results.");
		}

		courses.get(0).setSelected(true);
		courses.get(2).setSelected(true);
		courses.get(3).setSelected(false);

		admin.updateUser(UserType.STUDENT, "John", "Doe", "jdoe", courses, "jdoe");

		stmt = SQLConnection.GetConnection()
				.prepareStatement("SELECT dept, course FROM ENROLLMENT WHERE user='jdoe' " + "order by dept, course");
		results = stmt.executeQuery();
		if (results.next()) {
			assertEquals("CSCI", "CSCI", results.getString(1));
			assertEquals("6250", "6250", results.getString(2));
			results.next();
			assertEquals("CSCI", "CSCI", results.getString(1));
			assertEquals("6840", "6840", results.getString(2));
			results.next();
			assertEquals("SENG", "SENG", results.getString(1));
			assertEquals("6230", "6230", results.getString(2));
			if (results.next()) {
				fail("Should have retrieved only 2 sets.");
			}
		} else {
			fail("Should have retrieved results.");
		}

		stmt = SQLConnection.GetConnection().prepareStatement(
				"SELECT dept, course, section, term, year, count(date) as ct FROM ATTENDANCE WHERE user='jdoe' and active=1 "
						+ "group by dept, course, section, term, year order by dept, course");
		results = stmt.executeQuery();
		if (results.next()) {
			assertEquals("CSCI", "CSCI", results.getString(1));
			assertEquals("attended 3 CSCI courses", 3, results.getInt("ct"));
			results.next();
			assertEquals("SENG", "SENG", results.getString(1));
			assertEquals("6230", "6230", results.getString(2));
			if (results.next()) {
				fail("Should have retrieved only 2 sets.");
			}
		} else {
			fail("Should have retrieved results.");
		}

		admin.updateUser(UserType.STUDENT, "John", "Doe", "jdoer", courses, "jdoe");

		stmt = SQLConnection.GetConnection()
				.prepareStatement("SELECT dept, course FROM ENROLLMENT WHERE user='jdoer' " + "order by dept, course");
		results = stmt.executeQuery();
		if (results.next()) {
			assertEquals("CSCI", "CSCI", results.getString(1));
			assertEquals("6250", "6250", results.getString(2));
			results.next();
			assertEquals("CSCI", "CSCI", results.getString(1));
			assertEquals("6840", "6840", results.getString(2));
			results.next();
			assertEquals("SENG", "SENG", results.getString(1));
			assertEquals("6230", "6230", results.getString(2));
			if (results.next()) {
				fail("Should have retrieved only 2 sets.");
			}
		} else {
			fail("Should have retrieved results.");
		}

		stmt = SQLConnection.GetConnection().prepareStatement(
				"SELECT dept, course, section, term, year, count(date) as ct FROM ATTENDANCE WHERE user='jdoer' and active=1 "
						+ "group by dept, course, section, term, year order by dept, course");
		results = stmt.executeQuery();
		if (results.next()) {
			assertEquals("CSCI", "CSCI", results.getString(1));
			assertEquals("attended 3 CSCI courses", 3, results.getInt("ct"));
			results.next();
			assertEquals("SENG", "SENG", results.getString(1));
			assertEquals("6230", "6230", results.getString(2));
			if (results.next()) {
				fail("Should have retrieved only 2 sets.");
			}
		} else {
			fail("Should have retrieved results.");
		}

		stmt.close();
		results.close();

		try {
			admin.updateUser(UserType.STUDENT, "John", "Doe", "dhead", courses, "jdoe");
		} catch (IllegalArgumentException e) {
			assertEquals("Username dhead is already taken.", e.getMessage());
		}
		try {
			admin.updateUser(UserType.STUDENT, "John", "Doe", "", courses, "jdoe");
		} catch (IllegalArgumentException e) {
			assertEquals("Username can not be blank", e.getMessage());
		}
	}

	@Test
	public void testAddDeptHead() throws SQLException {
		PreparedStatement stmt = SQLConnection.GetConnection()
				.prepareStatement("SELECT user FROM USER WHERE user='jdoe2'");
		ResultSet results = stmt.executeQuery();
		int count = -1;
		if (results.next()) {
			count = results.getInt(1);
		} else {
			count = 0;
		}
		assertEquals("Before add user jdoe2 count is 0", 0, count);

		String newUser = admin.addDeptHead(UserType.DEPT_HEAD, "John", "Doe", "jdoe", courses);
		assertEquals("jdoe2", newUser);

		stmt = SQLConnection.GetConnection().prepareStatement("SELECT user FROM USER WHERE user='jdoe2'");
		results = stmt.executeQuery();
		String user = "";
		if (results.next()) {
			user = results.getString(1);
		}
		stmt = SQLConnection.GetConnection().prepareStatement("SELECT count(user) FROM ENROLLMENT WHERE user='jdoe2'");
		results = stmt.executeQuery();
		int numCourses = -1;
		if (results.next()) {
			numCourses = results.getInt(1);
		}
		assertEquals("After add, user jdoe2 user is jdoe2", "jdoe2", user);
		assertEquals("After add, user jdoe2 departments is 2", 2, numCourses);

		stmt.close();
		results.close();
	}

	@Test
	public void testUpdateDeptHead() throws SQLException {
		PreparedStatement stmt = SQLConnection.GetConnection()
				.prepareStatement("SELECT dept FROM ENROLLMENT WHERE user='dhead' " + "order by dept");
		ResultSet results = stmt.executeQuery();
		int resultCount = 0;
		if (results.next()) {
			assertEquals("First dept is CSCI", "CSCI", results.getString("dept"));
			resultCount++;
			results.next();
			assertEquals("Second dept is SENG", "SENG", results.getString("dept"));
			resultCount++;
			if (results.next()) {
				resultCount++;
			}
			assertEquals("Count should be 2", 2, resultCount);
		}

		ArrayList<JCheckBox> depts = new ArrayList<JCheckBox>();
		depts.add(new JCheckBox("SENG", false));
		depts.add(new JCheckBox("CSCI", true));

		admin.updateDeptHead(UserType.DEPT_HEAD, "Dept", "Head", "dhead", depts, "dhead");

		stmt = SQLConnection.GetConnection()
				.prepareStatement("SELECT dept as count FROM ENROLLMENT WHERE user='dhead' " + "order by dept");
		results = stmt.executeQuery();
		if (results.next()) {
			assertEquals("Dept is CSCI", "CSCI", results.getString("dept"));
			if (results.next()) {
				fail("There should only be one result set.");
			}
		}

		depts.get(0).setSelected(true);
		admin.updateDeptHead(UserType.DEPT_HEAD, "Dept", "Head", "dhead", depts, "dhead");

		stmt = SQLConnection.GetConnection()
				.prepareStatement("SELECT dept as count FROM ENROLLMENT WHERE user='dhead' " + "order by dept");
		results = stmt.executeQuery();
		if (results.next()) {
			assertEquals("Dept is CSCI", "CSCI", results.getString("dept"));
			if (!results.next()) {
				fail("There should be two result sets.");
			}
			assertEquals("Dept is SENG", "SENG", results.getString("dept"));
			if (results.next()) {
				fail("There should only be two result sets.");
			}
		}

		admin.updateDeptHead(UserType.DEPT_HEAD, "Dept", "Head", "dheader", depts, "dhead");

		stmt = SQLConnection.GetConnection()
				.prepareStatement("SELECT dept as count FROM ENROLLMENT WHERE user='dheader' " + "order by dept");
		results = stmt.executeQuery();
		if (results.next()) {
			assertEquals("Dept is CSCI", "CSCI", results.getString("dept"));
			if (!results.next()) {
				fail("There should be two result sets.");
			}
			assertEquals("Dept is SENG", "SENG", results.getString("dept"));
			if (results.next()) {
				fail("There should only be two result sets.");
			}
		}

		stmt.close();
		results.close();
	}

	/**
	 * User is an abstract class, so its constructor can't be instantiated. It's
	 * tested via all the classes that extend it.
	 */
	// @Test
	// public void testUser() {
	// }

	@Test
	public void testGetUserId() {
		assertEquals(userId, admin.getUserId());
	}

	@Test
	public void testGetPassword() {
		assertEquals("This is the password stored by MYSQL", password, admin.getPassword());
	}

	@Test
	public void testGetNeedsPassword() {
		assertTrue(admin.getNeedsPassword());
		admin.setPasswordFlag();
		assertFalse(admin.getNeedsPassword());
	}

	@Test
	public void testChangePassword() {
		assertEquals(password, admin.getPassword());
		admin.setPasswordFlag();
		assertFalse(admin.getNeedsPassword());
		admin.changePassword(password, "superman");
		assertEquals("superman", admin.getPassword());
		assertFalse(admin.getNeedsPassword());
	}

	@Test
	public void testGetUserRole() {
		assertEquals(admin.getUserRole(), UserType.ADMIN);
	}

	@Test
	public void testGetFullname() {
		assertEquals(fullName, admin.getFullname());
	}
}
