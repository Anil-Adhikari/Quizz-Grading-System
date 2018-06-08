package edu.ecu.csci6230.group1.quiztracker.users.test;

import static org.junit.Assert.*;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JCheckBox;

import org.h2.tools.RunScript;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.ecu.csci6230.group1.quiztracker.dataconnection.DatabaseConnection;
import edu.ecu.csci6230.group1.quiztracker.managers.UserManager;
import edu.ecu.csci6230.group1.quiztracker.users.Student;
import edu.ecu.csci6230.group1.quiztracker.users.UserType;

public class StudentTest {
	private static String testDatabase = "jdbc:h2:./H2DBforTesting/QUIZTRACKER;MODE=MySQL;DATABASE_TO_UPPER=false;TRACE_LEVEL_SYSTEM_OUT=0;";
	private static String defaultUser = "quizuser";
	private static String defaultPassword = "quizuser";
	private static String testDatabaseClass = "org.h2.Driver";
	private static DatabaseConnection SQLConnection;
	public static final String fullName = "John Doe";
	public static final String firstName = "John";
	public static final String lastName = "Doe";
	public static final String userId = "jdoe";
	public static final String password = "jdoe123";
	public static final UserType role = UserType.STUDENT;
	
	public static ArrayList<JCheckBox> enrolledCourses;
	public static final String course1 = "CSCI\t  6840\t  100\t  spring\t  2017";
	public static final String course2 = "SENG\t  6230\t  601\t  fall\t  2016";
	public static final String course3 = "SENG\t  6240\t  101\t  fall\t  2016";
	
	public static final String quizList[] = { "Quiz 1\t        5/15   ",
									   		  "Quiz 2\t       12/15   ",
									   		  "Quiz 3\t     null/10   Not Graded      Due: 2016-11-25",
									   		  "Quiz 4\t     null/10   Not Submitted   Due: 2016-11-28" };
	
	public static final String quizInfo = "Quiz 1\t        5/15   ";
	public static final String quizInfo2 = "Quiz 3\t     null/10   Not Graded      Due: 2016-11-25";
	public static final String expectedAverage = "57%";
	public static final String expectedComment = "study more";
	public static final String course1Attendance[][] = {{"2017-01-05", "true"},{"2017-01-10", "true"},{"2017-01-31", "false"}};
	private Student student;
	
	@Before
	public void setUp() throws Exception {
		SQLConnection = DatabaseConnection.getInstance(testDatabase, defaultUser, defaultPassword, testDatabaseClass);
		RunScript.execute(SQLConnection.GetConnection(), new FileReader("TestScripts/StudentTestSetup.txt"));
		
		enrolledCourses = new ArrayList<JCheckBox>();
		enrolledCourses.add(new JCheckBox(course1, true));
		enrolledCourses.add(new JCheckBox(course2, true));
		enrolledCourses.add(new JCheckBox(course3, true));
		
		student = (Student)UserManager.verifyUser(userId, password);
	}

	@After
	public void tearDown() throws Exception {
		RunScript.execute(SQLConnection.GetConnection(), new FileReader("TestScripts/TearDownScript.txt"));
	}

	@Test
	public void testStudent() {
		assertEquals("jdoe", student.getUserId());
	}
	
	@Test
	public void testGetStudentCourseQuizListing(){
		String list[] = student.getStudentCourseQuizListing(course2);
		for(int i = 0; i < quizList.length; i++){
			assertEquals(quizList[i],list[i]);
		}
	}
	
	@Test
	public void testGetStudentQuizAverage(){
		String average = student.getStudentQuizAverage(course2);
		assertEquals(expectedAverage, average);
		average = student.getStudentQuizAverage(course3);
		assertEquals("NaN%", average);
	}
	
	@Test
	public void testGetQuizResultComments(){
		String comment = student.getQuizResultComments(quizInfo, course2);
		assertEquals(expectedComment,comment);
		comment = student.getQuizResultComments(quizInfo2, course1);
		assertEquals("", comment);
	}
	
	@Test
	public void testGetStudentCourseAttendance(){
		String attendance[][] = student.getStudentCourseAttendance(course1);
		assertEquals(Arrays.deepToString(course1Attendance),Arrays.deepToString(attendance));
	}

}
