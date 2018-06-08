package edu.ecu.csci6230.group1.quiztracker.users.test;

import static org.junit.Assert.*;

import java.io.FileReader;

import org.h2.tools.RunScript;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.ecu.csci6230.group1.quiztracker.dataconnection.DatabaseConnection;
import edu.ecu.csci6230.group1.quiztracker.managers.UserManager;
import edu.ecu.csci6230.group1.quiztracker.users.DeptHead;

public class DeptHeadTest {

	private static String testDatabase = "jdbc:h2:./H2DBforTesting/QUIZTRACKER;MODE=MySQL;DATABASE_TO_UPPER=false;TRACE_LEVEL_SYSTEM_OUT=0;";
	private static String defaultUser = "quizuser";
	private static String defaultPassword = "quizuser";
	private static String testDatabaseClass = "org.h2.Driver";
	private static DatabaseConnection SQLConnection;
	public static final String fullName = "Dept Head";
	public static final String firstName = "Dept";
	public static final String lastName = "Head";
	public static final String userId = "dhead";
	public static final String password = "dhead1234";
	private DeptHead dhead;

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
	public void testAssistant() {
		dhead = (DeptHead) UserManager.verifyUser(userId, password);
		assertEquals(fullName, dhead.getFullname());
	}

}
