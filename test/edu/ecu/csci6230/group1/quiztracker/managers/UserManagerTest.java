package edu.ecu.csci6230.group1.quiztracker.managers;

import static org.junit.Assert.*;

import java.io.FileReader;
import org.h2.tools.RunScript;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.ecu.csci6230.group1.quiztracker.dataconnection.DatabaseConnection;

public class UserManagerTest {
	private static String testDatabase = "jdbc:h2:./H2DBforTesting/QUIZTRACKER;MODE=MySQL;DATABASE_TO_UPPER=false;TRACE_LEVEL_SYSTEM_OUT=0;";
	private static String defaultUser = "quizuser";
	private static String defaultPassword = "quizuser";
	private static String testDatabaseClass = "org.h2.Driver";
	private static DatabaseConnection SQLConnection;

	public static final String invalidName = "jjdoe";
	public static final String validPassword = "jdoe123";
	public static final String validName = "jdoe";
	public static final String invalidPassword = "jdoe1234";

	public static final String userList[] = {
			String.format("%-20s%-15s%-20s%s", "wbee\t", "Worker\t", "Bee\t", "TA\t"),
			String.format("%-20s%-15s%-20s%s", "jdoe\t", "John\t", "Doe\t", "STUDENT\t"),
			String.format("%-20s%-15s%-20s%s", "dhead\t", "Dept\t", "Head\t", "DEPT_HEAD\t"),
			String.format("%-20s%-15s%-20s%s", "djohnson\t", "Don\t", "Johnson\t", "STUDENT\t"),
			String.format("%-20s%-15s%-20s%s", "tsmith\t", "Tom\t", "Smith\t", "STUDENT\t"),
			String.format("%-20s%-15s%-20s%s", "svilkomir\t", "Sergiey\t", "Vilkomir\t", "INSTRUCTOR\t"),
			String.format("%-20s%-15s%-20s%s", "dwhite\t", "David\t", "White\t", "ADMIN\t")
			};
	
	public static final String studentList[] = {
			String.format("%-20s%-15s%s", "jdoe\t", "John\t", "Doe"),
			String.format("%-20s%-15s%s", "djohnson\t", "Don\t", "Johnson"),
			String.format("%-20s%-15s%s", "tsmith\t", "Tom\t", "Smith")
			};
	
	@Before
	public void setUp() throws Exception {
		SQLConnection = DatabaseConnection.getInstance(testDatabase, defaultUser, defaultPassword, testDatabaseClass);
		RunScript.execute(SQLConnection.GetConnection(), new FileReader("TestScripts/InstructorTestSetup.txt"));
	}

	@After
	public void tearDown() throws Exception {
		RunScript.execute(SQLConnection.GetConnection(), new FileReader("TestScripts/TearDownScript.txt"));
	}

	@Test
	public void testVerifyUser(){
		try{
			UserManager.verifyUser("", validPassword);
			fail("missing username");
		} catch (IllegalArgumentException e){
			assertEquals("Please enter username and password",e.getMessage());
		}
		try{
			UserManager.verifyUser(validName, "");
			fail("missing password");
		} catch (IllegalArgumentException e){
			assertEquals("Please enter username and password",e.getMessage());
		}
		try{
			UserManager.verifyUser(invalidName, validPassword);
			fail("invalid username");
		} catch (IllegalArgumentException e){
			assertEquals("Username or password is incorrect.",e.getMessage());
		}
		try{
			UserManager.verifyUser(validName, invalidPassword);
			fail("Invalid password");
		} catch (IllegalArgumentException e){
			assertEquals("Username or password is incorrect.",e.getMessage());
		}
	}
	
	@Test
	public void testGetStudent(){
		try{
			UserManager.getStudent("");
		} catch (Exception e){
			assertEquals("SQLException", e.getClass().getName());
		}
	}
	
	@Test
	public void testGetFullUserListasString() {
		String list[] = UserManager.getFullUserListasString();
		assertArrayEquals(userList, list);
	}

	@Test
	public void testGetStudentListasString() {
		String list[] = UserManager.getStudentListasString();
		assertArrayEquals(studentList, list);
	}
}
