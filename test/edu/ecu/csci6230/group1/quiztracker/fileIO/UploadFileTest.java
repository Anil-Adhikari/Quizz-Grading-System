package edu.ecu.csci6230.group1.quiztracker.fileIO;

import static org.junit.Assert.*;

import java.io.FileReader;
import org.h2.tools.RunScript;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.ecu.csci6230.group1.quiztracker.dataconnection.DatabaseConnection;
import edu.ecu.csci6230.group1.quiztracker.managers.UserManager;
import edu.ecu.csci6230.group1.quiztracker.users.Student;
import edu.ecu.csci6230.group1.quiztracker.users.UserType;

public class UploadFileTest {
	private static String testDatabase = "jdbc:h2:./H2DBforTesting/QUIZTRACKER;MODE=MySQL;DATABASE_TO_UPPER=false;TRACE_LEVEL_SYSTEM_OUT=0;";
	private static String defaultUser = "quizuser";
	private static String defaultPassword = "quizuser";
	private static String testDatabaseClass = "org.h2.Driver";
	private static DatabaseConnection SQLConnection;
	
	public static final String firstName = "John";
	public static final String lastName = "Doe";
	public static final String userId = "jdoe";
	public static final String password = "jdoe123";
	public static final UserType role = UserType.STUDENT;
	public static final String fullFilePath = "Quizzes/quiz4.txt";
	public static final String quizName = "Quiz 4";

	public static final String course = "SENG\t  6230\t  601\t  fall\t  2016";
	public static final String quizList[] = { "Quiz 1\t        5/15   ",
	   		  "Quiz 2\t       12/15   ",
	   		  "Quiz 3\t     null/10   Not Graded      Due: 2016-12-25",
	   		  "Quiz 4\t     null/10   Not Graded      Due: 2016-11-28" };
	
	private Student student;
	
	@Before
	public void setUp() throws Exception {
		SQLConnection = DatabaseConnection.getInstance(testDatabase, defaultUser, defaultPassword, testDatabaseClass);
		RunScript.execute(SQLConnection.GetConnection(), new FileReader("TestScripts/InstructorTestSetup.txt"));
		student = (Student)UserManager.verifyUser(userId, password);
	}

	@After
	public void tearDown() throws Exception {
		RunScript.execute(SQLConnection.GetConnection(), new FileReader("TestScripts/TearDownScript.txt"));
	}

	@Test
	public void testPutStudentSubmission() {
		UploadFile.putStudentSubmission(course, fullFilePath, quizName, userId);
		String list[] = student.getStudentCourseQuizListing(course);
		assertArrayEquals(quizList, list);
	}
}
