package edu.ecu.csci6230.group1.quiztracker.managers;

import static org.junit.Assert.*;

import java.io.FileReader;
import java.util.ArrayList;

import org.h2.tools.RunScript;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.ecu.csci6230.group1.quiztracker.dataconnection.DatabaseConnection;
import edu.ecu.csci6230.group1.quiztracker.users.Student;

public class QuizManagerTest {
	private static String testDatabase = "jdbc:h2:./H2DBforTesting/QUIZTRACKER;MODE=MySQL;DATABASE_TO_UPPER=false;TRACE_LEVEL_SYSTEM_OUT=0;";
	private static String defaultUser = "quizuser";
	private static String defaultPassword = "quizuser";
	private static String testDatabaseClass = "org.h2.Driver";
	private static DatabaseConnection SQLConnection;
	public static final String course1 = "CSCI\t  6840\t  100\t  spring\t  2017";
	public static final String course2 = "SENG\t  6230\t  601\t  fall\t  2016";
	public static final String course3 = "CSCI\t  6250\t  100\t  fall\t  2016";
	public static final String course4 = "SENG\t  6240\t  101\t  fall\t  2016";
	
	public static final double aggregateMean = 58.9;
	public static final int expectedValue = 10;
	public static final int expectedId = 3;
	
	public static final String studentEntry = "jdoe\t  SENG\t  6230\t  601\t  fall\t  2016";
	public static final String detailStudent = "jdoe";
	public static final String quizEntry = "Quiz 3\t     null/10   Not Submitted   Due: 2016-11-25";
	
	
	private static final String expectedList[] = {
			"Quiz Name      Average         Min         Max",
			String.format("%-15s%6.0f%%%12s%12s", "Quiz 1", 66.7, "5", "15"),
			String.format("%-15s%6.0f%%%12s%12s", "Quiz 2", 60.0, "6", "12"),
			String.format("%-15s%6.0f%%%12s%12s", "Quiz 3", 50.0, "5", "5"),
			String.format("%-15s%6.0f%%%12s%12s", "Quiz 4", 0.0, "null", "null")
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
	public void testGetAggregateQuizMean() {
		double mean = QuizManager.getAggregateQuizMean(course2);
		assertEquals(aggregateMean, mean, 0.1);
		
		mean = QuizManager.getAggregateQuizMean(course4);
		assertEquals(0, mean, 0.1);
	}

	@Test
	public void testGetGradedQuizzesList() {
		ArrayList<String> list = QuizManager.getGradedQuizzesList(course2);
		String list2[] = list.toArray(new String[list.size()]);
		assertArrayEquals(expectedList, list2);
	}

	@Test
	public void testGetQuizValue() {
		int value = QuizManager.getQuizValue(quizEntry, studentEntry, course2);
		assertEquals(expectedValue, value);
	}

	@Test
	public void testAddQuizGrade() {
		QuizManager.addQuizGrade(quizEntry, detailStudent, course2, 8, "Excellent work");
		Student stu = UserManager.getStudent(detailStudent);
		String list[] = stu.getStudentCourseQuizListing(course2);
		int value = Integer.parseInt(list[2].split("\\t *")[1].split("/")[0]);
		assertEquals(8, value);
		assertEquals("Excellent work", stu.getQuizResultComments(quizEntry, course2));
	}

	@Test
	public void testGetQuizId() {
		int id = QuizManager.getQuizId(course2, "Quiz 3", detailStudent);
		assertEquals(expectedId, id);
	}

	@Test
	public void testCheckDueDate() {
		assertTrue(QuizManager.checkDueDate(course2, "Quiz 3", detailStudent));
		assertFalse(QuizManager.checkDueDate(course2, "Quiz 1", detailStudent));
	}

}
