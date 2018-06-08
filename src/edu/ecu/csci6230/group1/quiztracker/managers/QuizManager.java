package edu.ecu.csci6230.group1.quiztracker.managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import edu.ecu.csci6230.group1.quiztracker.dataconnection.DatabaseConnection;

public class QuizManager {

	public static double getAggregateQuizMean(String course) {
		String courseInfo[] = extractCourseInfo(course);
		ResultSet resultSet = null;
		PreparedStatement stmt = null;
		double mean = 0;
		try {
			stmt = DatabaseConnection.getInstance().GetConnection()
					.prepareStatement("SELECT avg(t1.average) FROM "
							+ "(SELECT CAST(SUM(score) AS DECIMAL) / CAST(SUM(value) AS DECIMAL)*100 AS average FROM QUIZ WHERE SCORE IS NOT NULL "
							+ "AND dept=? AND course=? AND section=? AND TERM=? AND year=? AND active=1 "
							+ "GROUP BY name) as t1");
			for (int i = 0; i < courseInfo.length; i++) {
				stmt.setString(i + 1, courseInfo[i]);
			}
			resultSet = stmt.executeQuery();
			if (resultSet.next()) {
				mean = resultSet.getDouble(1);
			}
			stmt.close();
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println("QuizManager.getAggregateQuizMean " + e.getMessage());
		}
		return mean;
	}

	public static ArrayList<String> getGradedQuizzesList(String course) {
		String courseInfo[] = extractCourseInfo(course);
		ResultSet resultSet = null;
		PreparedStatement stmt = null;
		ArrayList<String> gradedQuizzes = new ArrayList<String>();
		gradedQuizzes.add(String.format("%-15s%3s%12s%12s", "Quiz Name", "Average", "Min", "Max"));
		try {
			stmt = DatabaseConnection.getInstance().GetConnection().prepareStatement(
					"SELECT name, CAST(SUM(score) AS DECIMAL) / CAST(SUM(CASE WHEN score IS NOT NULL THEN value ELSE NULL END) AS DECIMAL)*100 AS average, MAX(score) as max, MIN(score) as min FROM QUIZ "
							+ "WHERE dept=? AND course=? AND section=? AND TERM=? AND year=? AND active=1 GROUP BY name ORDER by name");
			for (int i = 0; i < courseInfo.length; i++) {
				stmt.setString(i + 1, courseInfo[i]);
			}
			resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				String newEntry = String.format("%-15s%6.0f%%%12s%12s", resultSet.getString(1),
						resultSet.getDouble("average"), resultSet.getString("min"), resultSet.getString("max"));
				gradedQuizzes.add(newEntry);
			}
			stmt.close();
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}
		return gradedQuizzes;
	}

	private static String[] extractCourseInfo(String course) {
		return course.split("\\t *");
	}

	public static int getQuizValue(String selectedQuiz, String studentDetails, String courseDetails) {
		String quizName = selectedQuiz.split("\\t *")[0];
		String studentId = studentDetails.split("\\t *")[0];
		String courseInfo[] = extractCourseInfo(courseDetails);
		int value = 0;
		ResultSet resultSet = null;
		PreparedStatement stmt = null;
		try {
			stmt = DatabaseConnection.getInstance().GetConnection().prepareStatement(
					"SELECT value FROM QUIZ where dept=? AND course=? AND section=? AND term=? AND year=? AND user=? AND name=?");
			for (int i = 0; i < courseInfo.length; i++) {
				stmt.setString(i + 1, courseInfo[i]);
			}
			stmt.setString(6, studentId);
			stmt.setString(7, quizName);
			resultSet = stmt.executeQuery();
			if (resultSet.next()) {
				value = resultSet.getInt(1);
			}
			stmt.close();
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}
		return value;
	}

	public static void addQuizGrade(String selectedQuiz, String detailStudent, String courseDetails, int value,
			String comments) {

		validateQuiz(selectedQuiz, value);

		String quizName = selectedQuiz.split("\\t *")[0];
		String studentId = detailStudent.split("\\t *")[0];
		String courseInfo[] = extractCourseInfo(courseDetails);
		PreparedStatement stmt = null;
		try {
			stmt = DatabaseConnection.getInstance().GetConnection()
					.prepareStatement("UPDATE QUIZ SET status='GRADED', score=?, comments=? "
							+ "WHERE dept=? AND course=? AND section=? "
							+ "AND term=? AND year=? AND user=? AND name=?");
			stmt.setInt(1, value);
			stmt.setString(2, comments);
			for (int i = 0; i < courseInfo.length; i++) {
				stmt.setString(i + 3, courseInfo[i]);
			}
			stmt.setString(8, studentId);
			stmt.setString(9, quizName);
			stmt.executeUpdate();
			stmt.close();
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}
	}

	private static void validateQuiz(String selectedQuiz, int value) {
		int quizMaximum = Integer.parseInt(selectedQuiz.split("\\t *")[1].split("/")[1].split(" ")[0]);
		if (value > quizMaximum) {
			throw new IllegalArgumentException("Quiz score must not be greater than maximum points allowed.");
		}
	}

	public static int getQuizId(String currentCourse, String quizName, String detailStudent) {
		PreparedStatement stmt;
		ResultSet results;
		int id = 0;
		String courseInfo[] = currentCourse.split("\\t *");
		try {
			stmt = DatabaseConnection.getInstance().GetConnection().prepareStatement(
					"SELECT id FROM QUIZ WHERE user=? AND name=? AND dept=? AND course=? AND section=? AND term=? AND year=?");
			stmt.setString(1, detailStudent);
			stmt.setString(2, quizName);
			for (int i = 0; i < courseInfo.length; i++) {
				stmt.setString(i + 3, courseInfo[i]);
			}
			results = stmt.executeQuery();
			if (results.next()) {
				id = results.getInt(1);
			}
			stmt.close();
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}
		return id;
	}

	public static boolean checkDueDate(String currentCourse, String quizName, String detailStudent) {
		PreparedStatement stmt;
		ResultSet results;
		String courseInfo[] = currentCourse.split("\\t *");
		Date dueDate = null;
		try {
			stmt = DatabaseConnection.getInstance().GetConnection().prepareStatement(
					"SELECT due_date FROM QUIZ WHERE user=? AND name=? AND dept=? AND course=? AND section=? AND term=? AND year=?");
			stmt.setString(1, detailStudent);
			stmt.setString(2, quizName);
			for (int i = 0; i < courseInfo.length; i++) {
				stmt.setString(i + 3, courseInfo[i]);
			}
			results = stmt.executeQuery();
			if (results.next()) {
				dueDate = results.getDate(1);
			}
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calobj = Calendar.getInstance();
		String currentDateString = df.format(calobj.getTime());
		try {
			if (df.parse(currentDateString).compareTo(dueDate) > 0) {
				return false;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return true;
	}
}
