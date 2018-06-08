package edu.ecu.csci6230.group1.quiztracker.fileIO;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.ecu.csci6230.group1.quiztracker.dataconnection.DatabaseConnection;
import edu.ecu.csci6230.group1.quiztracker.managers.QuizManager;

public class UploadFile {

	public static void putStudentSubmission(String currentCourse, String filePath, String quizName,
			String detailStudent) {
		int len;
		int quizId = QuizManager.getQuizId(currentCourse, quizName, detailStudent);
		PreparedStatement pstmt;
		try {
			File file = new File(filePath);
			FileInputStream fis = new FileInputStream(file);
			len = (int) file.length();
			String query = "INSERT INTO QUIZ(id, name, status, user, file_name, quiz_file) VALUES(?,?,?,?,?,?) ON DUPLICATE KEY UPDATE status=VALUES(status), file_name=VALUES(file_name), quiz_file=VALUES(quiz_file)";
			String filename = Paths.get(filePath).getFileName().toString();
			pstmt = DatabaseConnection.getInstance().GetConnection().prepareStatement(query);
			pstmt.setInt(1, quizId);
			pstmt.setString(2, quizName);
			pstmt.setString(3, "SUBMITTED");
			pstmt.setString(4, detailStudent);
			pstmt.setString(5, filename);
			pstmt.setBinaryStream(6, fis, len);
			pstmt.executeUpdate();
			pstmt.close();

		} catch (Exception e) {
			System.out.println("UploadFile.putStudentSubmission " + e.getMessage());
		}
	}

	public static void putInstructorFileSubmission(String currentCourse, String filePath, String quizName,
			String detailStudent, int value, String dueDate) {
		int len;
		PreparedStatement pstmt;
		String dateFields[] = dueDate.split("/");
		try {
			File file = new File(filePath);
			FileInputStream fis = new FileInputStream(file);
			len = (int) file.length();
			String query = "INSERT INTO QUIZ(dept, course, section, term, year, name, status, user, value, file_name, quiz_file, due_date)"
					+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
			String courseInfo[] = currentCourse.split("\\t *");
			String filename = Paths.get(filePath).getFileName().toString();
			pstmt = DatabaseConnection.getInstance().GetConnection().prepareStatement(query);
			for (int i = 0; i < courseInfo.length; i++) {
				pstmt.setString(i + 1, courseInfo[i]);
			}
			pstmt.setString(6, quizName);
			pstmt.setString(7, "NEW");
			pstmt.setString(8, detailStudent);
			pstmt.setInt(9, value);
			pstmt.setString(10, filename);
			pstmt.setBinaryStream(11, fis, len);
			pstmt.setString(12, dateFields[2] + "-" + dateFields[0] + "-" + dateFields[1]);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (Exception e) {
			System.out.println("UploadFile.putInstructorFileSubmission " + e.getMessage());
		}
	}

	public static String validateQuizName(String quizName, String courseDetails) {
		String courseInfo[] = courseDetails.split("\\t *");
		if (quizName == null || quizName.trim().length() == 0) {
			throw new IllegalArgumentException("Quiz name can not be blank");
		}
		String newQuizName = quizName;
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		int count = 1;
		int attempt = 2;
		while (count != 0) {
			count = 0;
			try {
				stmt = DatabaseConnection.getInstance().GetConnection()
						.prepareStatement("SELECT name, COUNT(name) AS quizCount FROM QUIZ WHERE "
								+ " dept=? AND course=? AND section=? AND term=? AND year=? AND name=? GROUP BY name");
				for (int i = 0; i < courseInfo.length; i++) {
					stmt.setString(i + 1, courseInfo[i]);
				}
				stmt.setString(6, newQuizName);
				resultSet = stmt.executeQuery();
				if (resultSet.next()) {
					count = resultSet.getInt("quizCount");
				}
				stmt.close();
			} catch (SQLException | ClassNotFoundException e) {
				System.out.println("UploadFile.validateQuizName " + e.getMessage());
			}
			if (count > 0) {
				newQuizName = quizName + "(" + (attempt++) + ")";
			}
		}
		return newQuizName;
	}
}
