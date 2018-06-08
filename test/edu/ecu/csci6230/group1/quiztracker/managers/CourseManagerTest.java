package edu.ecu.csci6230.group1.quiztracker.managers;

import static org.junit.Assert.*;

import java.io.FileReader;

import org.h2.tools.RunScript;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.ecu.csci6230.group1.quiztracker.dataconnection.DatabaseConnection;

public class CourseManagerTest {
	private static String testDatabase = "jdbc:h2:./H2DBforTesting/QUIZTRACKER;MODE=MySQL;DATABASE_TO_UPPER=false;TRACE_LEVEL_SYSTEM_OUT=0;";
	private static String defaultUser = "quizuser";
	private static String defaultPassword = "quizuser";
	private static String testDatabaseClass = "org.h2.Driver";
	private static DatabaseConnection SQLConnection;

	public static final String course = "SENG\t  6240\t  101\t  fall\t  2016";
	public static final String studentEntry = "jdoe";
	public static final String instructorEntry = "svilkomir";

	private static final String expectedCourseList[] = {
			String.format("%-6s%-6s%-5s%-8s%s", "CSCI\t", "6250\t", "100\t", "fall\t", "2016"),
			String.format("%-6s%-6s%-5s%-8s%s", "CSCI\t", "6840\t", "100\t", "spring\t", "2017"),
			String.format("%-6s%-6s%-5s%-8s%s", "GOLF\t", "9999\t", "100\t", "fall\t", "2016"),
			String.format("%-6s%-6s%-5s%-8s%s", "SENG\t", "6230\t", "601\t", "fall\t", "2016"),
			String.format("%-6s%-6s%-5s%-8s%s", "SENG\t", "6240\t", "101\t", "fall\t", "2016") };

	private static final String enrolledCourses1[] = {
			String.format("%-6s%-6s%-5s%-8s%s", "SENG\t", "6240\t", "101\t", "fall\t", "2016") };

	private static final String enrolledCourses2[] = {
			String.format("%-6s%-6s%-5s%-8s%s", "CSCI\t", "6840\t", "100\t", "spring\t", "2017"),
			String.format("%-6s%-6s%-5s%-8s%s", "SENG\t", "6230\t", "601\t", "fall\t", "2016"),
			String.format("%-6s%-6s%-5s%-8s%s", "SENG\t", "6240\t", "101\t", "fall\t", "2016") };

	private static final String expectedCourseListAdmin1[][] = {
			{ String.format("%-6s%-6s%-5s%-8s%s", "CSCI\t", "6250\t", "100\t", "fall\t", "2016"), "false" },
			{ String.format("%-6s%-6s%-5s%-8s%s", "CSCI\t", "6840\t", "100\t", "spring\t", "2017"), "true"},
			{ String.format("%-6s%-6s%-5s%-8s%s", "GOLF\t", "9999\t", "100\t", "fall\t", "2016"), "false"},
			{ String.format("%-6s%-6s%-5s%-8s%s", "SENG\t", "6230\t", "601\t", "fall\t", "2016"), "true" },
			{ String.format("%-6s%-6s%-5s%-8s%s", "SENG\t", "6240\t", "101\t", "fall\t", "2016"), "true" } };

	private static final String expectedCourseListAdmin2[][] = {
			{ String.format("%-6s%-6s%-5s%-8s%s", "CSCI\t", "6250\t", "100\t", "fall\t", "2016"), "false" },
			{ String.format("%-6s%-6s%-5s%-8s%s", "CSCI\t", "6840\t", "100\t", "spring\t", "2017"), "false"},
			{ String.format("%-6s%-6s%-5s%-8s%s", "GOLF\t", "9999\t", "100\t", "fall\t", "2016"), "false"},
			{ String.format("%-6s%-6s%-5s%-8s%s", "SENG\t", "6230\t", "601\t", "fall\t", "2016"), "false" },
			{ String.format("%-6s%-6s%-5s%-8s%s", "SENG\t", "6240\t", "101\t", "fall\t", "2016"), "true" } };
	
	private static final String deptList[] = {"CSCI", "GOLF", "SENG"};
	private static final String deptHead = "dhead";
	private static final String deptHeadList[] = {"CSCI", "SENG"};
	
	private static final String deptHeadCourses[] = {
			String.format("%-6s%-6s%-5s%-8s%s", "CSCI\t", "6250\t", "100\t", "fall\t", "2016"),
			String.format("%-6s%-6s%-5s%-8s%s", "CSCI\t", "6840\t", "100\t", "spring\t", "2017"),
			String.format("%-6s%-6s%-5s%-8s%s", "SENG\t", "6230\t", "601\t", "fall\t", "2016"),
			String.format("%-6s%-6s%-5s%-8s%s", "SENG\t", "6240\t", "101\t", "fall\t", "2016") };
	
	private static final String deptListForAdmin[][] = {{"CSCI", "true"}, {"GOLF", "false"}, {"SENG", "true"}};
	
	private static final String courseDates[] = {"2016-10-14", "2016-10-25", "2016-10-27", "2016-11-01", "2016-11-03"};

	private static final String validDept = "GOLF";
	private static final String validCourse = "8888";
	private static final String term = "spring";
	private static final String validYear = "2016";
	private static final String invalidDept = "GOLFER";
	private static final String invalidCourse1 = "88888";
	private static final String invalidCourse2 = "888D";
	private static final String invalidYear = "2015";
	private static final String invalidYear2 = "B";
	
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
	public void testGetCurrentCourseList() {
		String list[] = CourseManager.getCurrentCourseList();
		assertArrayEquals(expectedCourseList, list);
	}

	@Test
	public void testGetUsersEnrolledCourses() {
		String list[] = CourseManager.getUsersEnrolledCourses(studentEntry);
		assertArrayEquals(enrolledCourses2, list);
		
		list = CourseManager.getUsersEnrolledCourses(instructorEntry);
		assertArrayEquals(enrolledCourses1, list);
	}

	@Test
	public void testGetUsersCourseListForAdmin() {
		String list[][] = CourseManager.getUsersCourseListForAdmin(studentEntry);
		for (int i = 0; i < list.length; i++) {
			assertArrayEquals(expectedCourseListAdmin1[i], list[i]);
		}
		for (int i = 0; i < list.length; i++) {
			list = CourseManager.getUsersCourseListForAdmin(instructorEntry);
			assertArrayEquals(expectedCourseListAdmin2[i], list[i]);
		}
	}

	@Test
	public void testGetCompleteDeptList() {
		String list[] = CourseManager.getCompleteDeptList();
		assertArrayEquals(deptList, list);
	}

	@Test
	public void testGetDeptListForDeptHead() {
		String list[] = CourseManager.getDeptListForDeptHead(deptHead);
		assertArrayEquals(deptHeadList, list);
	}

	@Test
	public void testAddNewCourse() {
		try{
			CourseManager.addNewCourse(validDept, validCourse, "100", "Course", term, invalidYear);
			fail("invalid year");
		} catch (Exception e){
			assertEquals("Addition of historical courses not supported", e.getMessage());
		}
		try{
			CourseManager.addNewCourse(validDept, validCourse, "100", "Course", term, invalidYear2);
			fail("invalid year");
		} catch (Exception e){
			assertEquals("Invalid Year", e.getMessage());
		}
		try{
			CourseManager.addNewCourse(validDept, invalidCourse1, "100", "Course", term, validYear);
			fail("invalid course");
		} catch (Exception e){
			assertEquals("Course Number must not exceed 9999", e.getMessage());
		}
		try{
			CourseManager.addNewCourse(validDept, invalidCourse2, "100", "Course", term, validYear);
			fail("invalid course");
		} catch (Exception e){
			assertEquals("Course Number must be a number", e.getMessage());
		}
		try{
			CourseManager.addNewCourse(invalidDept, validCourse, "100", "Course", term, validYear);
			fail("invalid department");
		} catch (Exception e){
			assertEquals("Department Code must not exceed 4 characters", e.getMessage());
		}
		try{
			CourseManager.addNewCourse(validDept, validCourse, "100", "Course", term, validYear);
		} catch (Exception e){
			fail("course should have been added");
		}
		try{
			CourseManager.addNewCourse("", validCourse, "100", "Course", term, validYear);
			fail("course should not be added");
		} catch (Exception e){
			assertEquals("Fields can not be empty. Please try again", e.getMessage());
		}
		try{
			CourseManager.addNewCourse(validDept, "", "100", "Course", term, validYear);
			fail("course should not be added");
		} catch (Exception e){
			assertEquals("Fields can not be empty. Please try again", e.getMessage());
		}
		try{
			CourseManager.addNewCourse(validDept, validCourse, "", "Course", term, validYear);
			fail("course should not be added");
		} catch (Exception e){
			assertEquals("Fields can not be empty. Please try again", e.getMessage());
		}
		try{
			CourseManager.addNewCourse(validDept, validCourse, "100", "Course", "", validYear);
			fail("course should not be added");
		} catch (Exception e){
			assertEquals("Fields can not be empty. Please try again", e.getMessage());
		}
		try{
			CourseManager.addNewCourse(validDept, validCourse, "100", "Course", term, "");
			fail("course should not be added");
		} catch (Exception e){
			assertEquals("Fields can not be empty. Please try again", e.getMessage());
		}
	}

	@Test
	public void testGetCourseSchedule() {
		String list[] = CourseManager.getCourseSchedule(course);
		assertArrayEquals(courseDates, list);
	}

	@Test
	public void testGetUsersDeptListForAdmin() {
		String list[][] = CourseManager.getUsersDeptListForAdmin(deptHead);
		for (int i = 0; i < list.length; i++) {
			assertArrayEquals(deptListForAdmin[i], list[i]);
		}
	}

	@Test
	public void testGetDeptHeadCourseList() {
		String list[] = CourseManager.getDeptHeadCourseList(deptHead);
		assertArrayEquals(deptHeadCourses, list);
	}

}
