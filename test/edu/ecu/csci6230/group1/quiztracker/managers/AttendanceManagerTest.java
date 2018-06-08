package edu.ecu.csci6230.group1.quiztracker.managers;

import static org.junit.Assert.*;

import java.io.FileReader;
import javax.swing.JCheckBox;

import org.h2.tools.RunScript;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.ecu.csci6230.group1.quiztracker.dataconnection.DatabaseConnection;

public class AttendanceManagerTest {
	private static String testDatabase = "jdbc:h2:./H2DBforTesting/QUIZTRACKER;MODE=MySQL;DATABASE_TO_UPPER=false;TRACE_LEVEL_SYSTEM_OUT=0;";
	private static String defaultUser = "quizuser";
	private static String defaultPassword = "quizuser";
	private static String testDatabaseClass = "org.h2.Driver";
	private static DatabaseConnection SQLConnection;
	
	private static final String[][] expectedAttendance = {
			{"&nbsp&nbsp&nbsp&nbsp", "2016-10-14", "2016-10-25", "2016-10-27", "2016-11-01", "2016-11-03"},
			{String.format("%-20s%-15s%s", "jdoe\t", "John\t", "Doe"), "false", "true", "false", "true", "true"},
			{String.format("%-20s%-15s%s", "djohnson\t", "Don\t", "Johnson"), "false", "false", "false", "false", "false"},
			{String.format("%-20s%-15s%s", "tsmith\t", "Tom\t", "Smith"), "false", "false", "false", "false", "false"}};
	
	private static final String[][] expectedAttendance2 = {
			{"&nbsp&nbsp&nbsp&nbsp", "2016-10-14", "2016-10-25", "2016-10-27", "2016-11-01", "2016-11-03", "2016-11-05"},
			{String.format("%-20s%-15s%s", "jdoe\t", "John\t", "Doe"), "false", "true", "false", "true", "true", "true"},
			{String.format("%-20s%-15s%s", "djohnson\t", "Don\t", "Johnson"), "false", "false", "false", "false", "false", "false"},
			{String.format("%-20s%-15s%s", "tsmith\t", "Tom\t", "Smith"), "false", "false", "false", "false", "false", "true"}};
	
	public static final String course = "SENG\t  6240\t  101\t  fall\t  2016";
	public static final String date = "11/05/2016";
	JCheckBox marks[] = {new JCheckBox("jdoe\t", true), new JCheckBox("djohnson\t", false), new JCheckBox("tsmith\t", true)}; 
	
	public static final String averageAttendance = "20%";
	
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
	public void testMarkAttendance() {
		AttendanceManager.markAttendance(date, marks, course);
		String list[][] = AttendanceManager.getFullCourseAttendance(course);
		for (int i = 0; i < list.length; i++) {
			assertArrayEquals(expectedAttendance2[i], list[i]);
		}
		try{
			AttendanceManager.markAttendance(date, marks, course);
			fail("course already exists");
		} catch (IllegalArgumentException e){
			assertEquals("Attendance has already been taken for this class session.", e.getMessage());
		}
	}

	@Test
	public void testGetFullCourseAttendance() {
		String list[][] = AttendanceManager.getFullCourseAttendance(course);
		for (int i = 0; i < list.length; i++) {
			assertArrayEquals(expectedAttendance[i], list[i]);
		}
	}
	
	@Test
	public void testAverageAttendance() {
		String list[][] = AttendanceManager.getFullCourseAttendance(course);
		for (int i = 0; i < list.length; i++) {
			assertEquals(averageAttendance, AttendanceManager.getAverageAttendance(list));
		}
	}

}
