package edu.ecu.csci6230.group1.quiztracker.users.test;

import static org.junit.Assert.*;

import java.io.FileReader;
import org.h2.tools.RunScript;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.ecu.csci6230.group1.quiztracker.dataconnection.DatabaseConnection;
import edu.ecu.csci6230.group1.quiztracker.managers.UserManager;
import edu.ecu.csci6230.group1.quiztracker.users.Instructor;
import edu.ecu.csci6230.group1.quiztracker.users.UserType;

public class InstructorTest {

	private static String testDatabase = "jdbc:h2:./H2DBforTesting/QUIZTRACKER;MODE=MySQL;DATABASE_TO_UPPER=false;TRACE_LEVEL_SYSTEM_OUT=0;";
	private static String defaultUser = "quizuser";
	private static String defaultPassword = "quizuser";
	private static String testDatabaseClass = "org.h2.Driver";
	private static DatabaseConnection SQLConnection;
	public static final String fullName = "Sergiey Vilkomir";
	public static final String firstName = "Sergiey";
	public static final String lastName = "Vilkomir";
	public static final String userId = "svilkomir";
	public static final String password = "svilkomir";
	public static final String badPassword = "sad";

	public static final UserType role = UserType.INSTRUCTOR;
	public static final String quizName = "Quiz 1";
	public static final int value = 10;
	public static final String fullFilePath = "Quizzes/quiz1.txt";
	public static final String dueDate = "11/18/2016";

	public static final String course1 = "CSCI\t  6840\t  100\t  spring\t  2017";
	public static final String course2 = "SENG\t  6230\t  601\t  fall\t  2016";
	public static final String course3 = "CSCI\t  6250\t  100\t  fall\t  2016";
	public static final String course4 = "SENG\t  6240\t  101\t  fall\t  2016";

	private Instructor inst;

	@Before
	public void setUp() throws Exception {
		SQLConnection = DatabaseConnection.getInstance(testDatabase, defaultUser, defaultPassword, testDatabaseClass);
		RunScript.execute(SQLConnection.GetConnection(), new FileReader("TestScripts/AdminTestSetup.txt"));
		
		inst = (Instructor) UserManager.verifyUser(userId, password);
	}

	@After
	public void tearDown() throws Exception {
		RunScript.execute(SQLConnection.GetConnection(), new FileReader("TestScripts/TearDownScript.txt"));
	}
	
	@Test
	public void testInstructor(){
		try {
			inst.changePassword(password, badPassword);
			fail("Password was too short");
		} catch (IllegalArgumentException e){
			assertEquals("Invalid password: Password must be at least 6 characters and not the same as the old password", e.getMessage());
		}
		try {
			inst.changePassword(password, password);
			fail("Password was not changed");
		} catch (IllegalArgumentException e){
			assertEquals("Invalid password: Password must be at least 6 characters and not the same as the old password", e.getMessage());
		}
		try {
			inst.changePassword(badPassword, password);
			fail("Old password incorrect");
		} catch (IllegalArgumentException e){
			assertEquals("Old password is incorrect", e.getMessage());
		}
	}

	@Test
	public void testAssignQuiz(){
		//check for current quizzes.
		String[] students = UserManager.getStudentListforCourse(course4);
		
		for (String string : students) {
			String quizList[] = UserManager.getStudent(string).getStudentCourseQuizListing(course4);
			assertEquals(0,quizList.length);
		}
		
		//upload quiz
		inst.assignQuiz(course4, quizName, value, fullFilePath, dueDate);
		
		//check for current quizzes.
		for (String string : students) {
			String quizList[] = UserManager.getStudent(string).getStudentCourseQuizListing(course4);
			assertEquals(1, quizList.length);
			assertEquals("Quiz 1\t     null/10   Not Submitted   Due: 2016-11-18", quizList[0]);
		}
		
		//do it again to test the Quiz validation function.
		inst.assignQuiz(course4, quizName, value, fullFilePath, dueDate);
		
		//check for current quizzes.
		for (String string : students) {
			String quizList[] = UserManager.getStudent(string).getStudentCourseQuizListing(course4);
			assertEquals(2, quizList.length);
			assertEquals("Quiz 1\t     null/10   Not Submitted   Due: 2016-11-18", quizList[0]);
			assertEquals("Quiz 1(2)\t  null/10   Not Submitted   Due: 2016-11-18", quizList[1]);
		}
		try {
			inst.assignQuiz(course4, "", value, fullFilePath, dueDate);
			fail("No blank names allowed");
		} catch (IllegalArgumentException e){
			assertEquals("Quiz name can not be blank", e.getMessage());
		}
	}
}
