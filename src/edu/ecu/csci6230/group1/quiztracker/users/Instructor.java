package edu.ecu.csci6230.group1.quiztracker.users;

import java.util.ArrayList;

import edu.ecu.csci6230.group1.quiztracker.fileIO.UploadFile;
import edu.ecu.csci6230.group1.quiztracker.managers.UserManager;

public class Instructor extends User{

	public Instructor(String first, String last, String userName, boolean needsPassword, UserType role) {
		super(first, last, userName, needsPassword, role);
	}
	
	public void assignQuiz(String currentCourse, String quizName, int value, String fullFilePath, String dueDate) {
		quizName = UploadFile.validateQuizName(quizName, currentCourse);
		ArrayList<String> students = UserManager.getStudentIDListForCourse(currentCourse);
		for (String student : students) {
			UploadFile.putInstructorFileSubmission(currentCourse, fullFilePath, quizName, student, value, dueDate);
		}
	}
}
