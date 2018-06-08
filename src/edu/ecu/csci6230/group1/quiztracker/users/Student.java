package edu.ecu.csci6230.group1.quiztracker.users;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Student extends User {

	public Student(String first, String last, String userName, boolean needsPassword, UserType role) {
		super(first, last, userName, needsPassword, role);
	}

	public String[] getStudentCourseQuizListing(String currentCourse) {
		ResultSet resultSet = null;
		PreparedStatement stmt = null;
		ArrayList<String> quizList = new ArrayList<String>();
		String courseInfo[] = currentCourse.split("\\t *");
		try {
			stmt = database.GetConnection().prepareStatement(
					"SELECT * FROM QUIZ WHERE user=? AND dept=? AND course=? AND section=? AND term=? AND year=?"
					+ "ORDER BY name");
			stmt.setString(1, super.getUserId());
			for (int i = 0; i < courseInfo.length; i++) {
				stmt.setString(i + 2, courseInfo[i]);
			}
			resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				String newEntry = String.format("%-10s%9s   ", resultSet.getString("name") + "\t",
						resultSet.getString("score") + "/" + resultSet.getString("value"));
				if (resultSet.getString("status").equals("NEW")) {
					newEntry += "Not Submitted   Due: " + resultSet.getString("due_date");
				} else if (resultSet.getString("status").equals("SUBMITTED")) {
					newEntry += "Not Graded      Due: " + resultSet.getString("due_date");
				}
				quizList.add(newEntry);
			}
			resultSet.close();
			stmt.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return quizList.toArray(new String[quizList.size()]);
	}

	public String getStudentQuizAverage(String currentCourse) {
		ResultSet resultSet = null;
		PreparedStatement stmt = null;
		int correct = 0;
		int possible = 1; // don't divide by 0
		String courseInfo[] = currentCourse.split("\\t *");
		try {
			stmt = database.GetConnection().prepareStatement(
					"SELECT SUM(score), SUM(value) FROM QUIZ WHERE user=? AND dept=? AND course=? AND section=? AND term=? AND year=? AND score IS NOT NULL");
			stmt.setString(1, super.getUserId());
			for (int i = 0; i < courseInfo.length; i++) {
				stmt.setString(i + 2, courseInfo[i]);
			}
			resultSet = stmt.executeQuery();
			if (resultSet.next()) {
				correct = resultSet.getInt(1);
				possible = resultSet.getInt(2);
			}
			resultSet.close();
			stmt.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		double average = (double) correct / (double) possible;
		return String.format("%.0f%%", average * 100);
	}

	public String getQuizResultComments(String quizInfo, String currentCourse) {
		ResultSet resultSet = null;
		PreparedStatement stmt = null;
		String output = "";
		String courseInfo[] = currentCourse.split("\\t *");
		try {
			stmt = database.GetConnection().prepareStatement("SELECT comments FROM QUIZ WHERE user = ? AND name = ?"
					+ "AND dept=? AND course=? AND section=? and term=? and year=?");
			stmt.setString(1, super.getUserId());
			stmt.setString(2, quizInfo.split("\\t *")[0]);
			for (int i = 0; i < courseInfo.length; i++) {
				stmt.setString(i + 3, courseInfo[i]);
			}
			resultSet = stmt.executeQuery();
			if (resultSet.next()) {
				output = resultSet.getString(1);
			}
			resultSet.close();
			stmt.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		if(output == null){
			return "";
		}
		return output;
	}

	public String[][] getStudentCourseAttendance(String currentCourse) {
		ResultSet resultSet = null;
		PreparedStatement stmt = null;
		String courseInfo[] = currentCourse.split("\\t *");
		ArrayList<String[]> attendance = new ArrayList<String[]>();
		try {
			stmt = database.GetConnection()
					.prepareStatement("SELECT A1.date, (SELECT present FROM ATTENDANCE AS A2 WHERE "
							+ "A2.user=? AND A2.dept=? AND A2.course=? AND A2.section=? AND A2.term=? AND A2.year=? AND A2.date=A1.date) AS present "
							+ "FROM CLASS_DATE AS A1 "
							+ "WHERE A1.dept=? AND A1.course=? AND A1.section=? AND A1.term=? AND A1.year=? "
							+ "order by A1.date");
			stmt.setString(1, super.getUserId());
			for (int i = 0; i < courseInfo.length; i++) {
				stmt.setString(i + 2, courseInfo[i]);
			}
			for (int i = 0; i < courseInfo.length; i++) {
				stmt.setString(i + 7, courseInfo[i]);
			}
			resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				String classDate[] = { resultSet.getString(1), String.valueOf(resultSet.getBoolean(2)) };
				attendance.add(classDate);
			}
			resultSet.close();
			stmt.close();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return attendance.toArray(new String[attendance.size()][2]);
	}
}
