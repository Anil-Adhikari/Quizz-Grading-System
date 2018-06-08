package edu.ecu.csci6230.group1.quiztracker.managers;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import javax.swing.JCheckBox;

import edu.ecu.csci6230.group1.quiztracker.dataconnection.DatabaseConnection;

public class AttendanceManager {

	public static void markAttendance(String date, JCheckBox[] marks, String selectedCourse) {
		checkAttendanceDate(date, selectedCourse);
		String courseInfo[] = selectedCourse.split("\\t *");
		String dateArray[] = date.split("/");
		PreparedStatement stmt = null;
		try {
			stmt = DatabaseConnection.getInstance().GetConnection().prepareStatement(
					"INSERT INTO ATTENDANCE(user, dept, course, section, term, year, date, present) VALUES(?,?,?,?,?,?,?,?)");
			for (JCheckBox entry : marks) {
				stmt.setString(1, entry.getText().split("\\t *")[0]);
				for (int i = 0; i < courseInfo.length; i++) {
					stmt.setString(i + 2, courseInfo[i]);
				}
				stmt.setDate(7, Date.valueOf(dateArray[2] + "-" + dateArray[0] + "-" + dateArray[1]));
				stmt.setBoolean(8, entry.isSelected());
				stmt.executeUpdate();
			}
			stmt = DatabaseConnection.getInstance().GetConnection().prepareStatement(
					"INSERT INTO CLASS_DATE(dept, course, section, term, year, date) VALUES(?,?,?,?,?,?)");
			for (int i = 0; i < courseInfo.length; i++) {
				stmt.setString(i + 1, courseInfo[i]);
			}
			stmt.setDate(6, Date.valueOf(dateArray[2] + "-" + dateArray[0] + "-" + dateArray[1]));
			stmt.executeUpdate();
			stmt.close();
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println("AttendanceManager.markAttendance " + e.getMessage());
		}
	}

	private static void checkAttendanceDate(String date, String selectedCourse) {
		String courseInfo[] = selectedCourse.split("\\t *");
		String dateArray[] = date.split("/");

		int month = Integer.parseInt(dateArray[0]);
		int day = Integer.parseInt(dateArray[1]);
		int year = Integer.parseInt(dateArray[2]);

		LocalDate courseDate = LocalDate.of(year, month, day);
		LocalDate currentDate = LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(),
				LocalDate.now().getDayOfMonth());

		if (courseDate.isAfter(currentDate)) {
			throw new IllegalArgumentException("Can not record attendance for future date.");
		}

		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = DatabaseConnection.getInstance().GetConnection().prepareStatement(
					"SELECT * FROM CLASS_DATE WHERE date=? AND dept=? AND course=? AND section=? AND term=? AND year=?");
			stmt.setDate(1, Date.valueOf(dateArray[2] + "-" + dateArray[0] + "-" + dateArray[1]));
			for (int i = 0; i < courseInfo.length; i++) {
				stmt.setString(i + 2, courseInfo[i]);
			}
			resultSet = stmt.executeQuery();
			if (resultSet.next()) {
				stmt.close();
				throw new IllegalArgumentException("Attendance has already been taken for this class session.");
			}
			stmt.close();
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println("AttendanceManager.checkAttendance " + e.getMessage());
		}
	}

	public static String[][] getFullCourseAttendance(String currentCourse) {
		ResultSet resultSet = null;
		PreparedStatement stmt = null;
		String courseInfo[] = currentCourse.split("\\t *");

		// students in the class
		String studentList[] = UserManager.getStudentListforCourse(currentCourse);

		// Dates the course met
		String courseDates[] = CourseManager.getCourseSchedule(currentCourse);

		String attendance[][] = new String[studentList.length + 1][courseDates.length + 1];

		attendance[0][0] = "&nbsp&nbsp&nbsp&nbsp";

		for (int i = 0; i < studentList.length; i++) {
			attendance[i + 1][0] = studentList[i];
		}
		for (int i = 0; i < courseDates.length; i++) {
			attendance[0][i + 1] = courseDates[i];
		}

		try {
			stmt = DatabaseConnection.getInstance().GetConnection().prepareStatement(
					"SELECT a1.user, a2.date, a3.present FROM ENROLLMENT AS a1 " + "LEFT JOIN CLASS_DATE AS a2 "
							+ "ON a2.dept=a1.dept AND a2.course=a1.course AND a2.section=a1.section AND a2.term=a1.term AND a2.year=a1.year "
							+ "LEFT JOIN ATTENDANCE AS a3 " + "ON (a3.date=a2.date OR a3.date IS null) AND "
							+ "(a3.user=a1.user OR a3.user is null) AND "
							+ "a3.dept=a1.dept AND a3.course=a1.course AND "
							+ "a3.section=a1.section AND a3.term=a1.term AND a3.year=a1.year " + "LEFT JOIN USER AS a4 "
							+ "ON a4.user=a1.user "
							+ "WHERE a1.dept=? AND a1.course=? AND a1.section=? AND a1.term=? AND a1.year=? AND a4.role='STUDENT' "
							+ "ORDER BY date, a4.last_name, a4.user, a2.date");
			for (int k = 0; k < courseInfo.length; k++) {
				stmt.setString(k + 1, courseInfo[k]);
			}

			resultSet = stmt.executeQuery();

			int dateColumn = 1;
			while (resultSet.next()) {
				for (int i = 0; i < studentList.length; i++) {
					String date = resultSet.getString("date");
					if (date.equals(attendance[0][dateColumn])) {
						attendance[i + 1][dateColumn] = String.valueOf(resultSet.getBoolean("present"));
					} else {
						attendance[i + 1][dateColumn] = "false";
					}
					if (i < studentList.length - 1) {
						resultSet.next();
					}
				}
				dateColumn++;
			}
			resultSet.close();
			stmt.close();
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println("AttendanceManager.getFullCourseAttendance " + e.getMessage());
		}
		return attendance;
	}

	public static String getAverageAttendance(String[][] attendanceList) {
		int dates = (attendanceList[0].length - 1) * (attendanceList.length - 1);
		int count = 0;

		for (int i = 1; i < attendanceList.length; i++) {
			for (int k = 1; k < attendanceList[0].length; k++) {
				if (attendanceList[i][k].equals("true")) {
					count++;
				}
			}
		}
		double percent = (double) count / (double) dates * 100;
		return String.format("%.0f%%", percent);
	}
}
