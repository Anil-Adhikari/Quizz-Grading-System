package edu.ecu.csci6230.group1.quiztracker.fileIO;

import static org.junit.Assert.*;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;

import org.h2.tools.RunScript;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.ecu.csci6230.group1.quiztracker.dataconnection.DatabaseConnection;

public class DownloadFileTest {
	private static String testDatabase = "jdbc:h2:./H2DBforTesting/QUIZTRACKER;MODE=MySQL;DATABASE_TO_UPPER=false;TRACE_LEVEL_SYSTEM_OUT=0;";
	private static String defaultUser = "quizuser";
	private static String defaultPassword = "quizuser";
	private static String testDatabaseClass = "org.h2.Driver";
	private static DatabaseConnection SQLConnection;
	
	public static final String userId = "jdoe";
	public static final String quizName = "Quiz 4";
	public static final String fileName = "SENG_6230_Quiz4.txt";
	public static final String destination = "Quizzes/quiz5.txt";

	public static final String course = "SENG\t  6230\t  601\t  fall\t  2016";
	
	@Before
	public void setUp() throws Exception {
		SQLConnection = DatabaseConnection.getInstance(testDatabase, defaultUser, defaultPassword, testDatabaseClass);
		RunScript.execute(SQLConnection.GetConnection(), new FileReader("TestScripts/InstructorTestSetup.txt"));
	}

	@After
	public void tearDown() throws Exception {
		RunScript.execute(SQLConnection.GetConnection(), new FileReader("TestScripts/TearDownScript.txt"));
		Files.deleteIfExists(Paths.get(destination));
	}

	@Test
	public void testGetFileName() {
		String name = DownloadFile.getFilename(quizName, userId, course);
		assertEquals(fileName, name);
		name = DownloadFile.getFilename("", userId, course);
		assertEquals("", name);
	}
	
	@Test
	public void testGetFile(){
		DownloadFile.getFile(quizName, destination, userId, course);
		assertTrue(Files.exists(Paths.get(destination), LinkOption.NOFOLLOW_LINKS));
		try {
			DownloadFile.getFile(quizName, destination, userId, course);
			fail("file exists");
		} catch (IllegalArgumentException e){
			assertEquals("File exists in destination", e.getMessage());
		}
	}

}
