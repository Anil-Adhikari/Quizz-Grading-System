package edu.ecu.csci6230.group1.quiztracker.managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import edu.ecu.csci6230.group1.quiztracker.dataconnection.DatabaseConnection;

public class CourseManager {

	public static String[] getCurrentCourseList() {
		ResultSet resultSet = null;
		PreparedStatement stmt = null;
		ArrayList<String> userList = new ArrayList<String>();
		try {
			int year = Calendar.getInstance().get(Calendar.YEAR);
			stmt = DatabaseConnection.getInstance().GetConnection()
					.prepareStatement("SELECT dept, course, section, term, year FROM COURSE WHERE year >= ? ORDER BY dept, course, year");
			stmt.setInt(1, year);
			resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				String newEntry = String.format("%-6s%-6s%-5s%-8s%s", resultSet.getString(1) + "\t",
						resultSet.getString(2) + "\t", resultSet.getString(3) + "\t", resultSet.getString(4) + "\t",
						resultSet.getString(5));
				userList.add(newEntry);
			}
			stmt.close();
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}
		return userList.toArray(new String[userList.size()]);
	}

	public static String[] getUsersEnrolledCourses(String student) {
		ResultSet resultSet = null;
		PreparedStatement stmt = null;
		ArrayList<String> userList = new ArrayList<String>();
		try {
			int year = Calendar.getInstance().get(Calendar.YEAR);
			stmt = DatabaseConnection.getInstance().GetConnection().prepareStatement(
					"SELECT dept, course, section, term, year FROM ENROLLMENT WHERE year >= ? AND user = ? "
					+ "order by dept, course, section, term, year");
			stmt.setInt(1, year);
			stmt.setString(2, student);
			resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				String newEntry = String.format("%-6s%-6s%-5s%-8s%s", resultSet.getString(1) + "\t",
						resultSet.getString(2) + "\t", resultSet.getString(3) + "\t", resultSet.getString(4) + "\t",
						resultSet.getString(5));
				userList.add(newEntry);
			}
			stmt.close();
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}
		return userList.toArray(new String[userList.size()]);
	}

	public static String[][] getUsersCourseListForAdmin(String student) {
		String allCourses[] = getCurrentCourseList();
		String studentsCourses[] = getUsersEnrolledCourses(student);
		String output[][] = new String[allCourses.length][2];
		Arrays.sort(studentsCourses);

		for (int i = 0; i < allCourses.length; i++) {
			output[i][0] = allCourses[i];
			int match = Arrays.binarySearch(studentsCourses, allCourses[i]);
			output[i][1] = (match < 0 ? "false" : "true");
		}
		return output;
	}

	public static String[] getCompleteDeptList() {
		ResultSet resultSet = null;
		PreparedStatement stmt = null;
		ArrayList<String> deptList = new ArrayList<String>();
		try {
			int year = Calendar.getInstance().get(Calendar.YEAR);
			stmt = DatabaseConnection.getInstance().GetConnection()
					.prepareStatement("SELECT dept FROM COURSE WHERE year >= ? GROUP BY dept ORDER BY dept");
			stmt.setInt(1, year);
			resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				deptList.add(resultSet.getString(1));
			}
			stmt.close();
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}
		return deptList.toArray(new String[deptList.size()]);
	}

	public static String[] getDeptListForDeptHead(String userId) {
		ResultSet resultSet = null;
		PreparedStatement stmt = null;
		ArrayList<String> deptList = new ArrayList<String>();
		try {
			stmt = DatabaseConnection.getInstance().GetConnection()
					.prepareStatement("SELECT dept FROM ENROLLMENT WHERE user=? ORDER BY dept");
			stmt.setString(1, userId);
			resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				deptList.add(resultSet.getString(1));
			}
			stmt.close();
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}
		return deptList.toArray(new String[deptList.size()]);
	}

	public static void addNewCourse(String department, String courseNum, String courseSect, String courseDesc,
			String courseTerm, String courseYear) {
		validateNewCourse(department, courseNum, courseSect, courseTerm, courseYear, courseDesc);
		PreparedStatement stmt = null;
		try {
			stmt = DatabaseConnection.getInstance().GetConnection().prepareStatement(
					"INSERT INTO COURSE(dept, course, section, term, year, description) VALUES(?, ?, ?, ?, ?, ?)");
			stmt.setString(1, department);
			stmt.setString(2, courseNum);
			stmt.setString(3, courseSect);
			stmt.setString(4, courseTerm);
			stmt.setString(5, courseYear);
			stmt.setString(6, courseDesc);
			stmt.executeUpdate();
			stmt.close();
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}
	}

	private static void validateNewCourse(String department, String courseNum, String courseSect, String courseTerm,
			String courseYear, String courseDescription) {
		int year = Calendar.getInstance().get(Calendar.YEAR);
		int newYear = 0;
		if(		department.trim().isEmpty() || 
				courseNum.trim().isEmpty()  || 
				courseSect.trim().isEmpty() || 
				courseTerm.trim().isEmpty() || 
				courseYear.trim().isEmpty() ||
				courseDescription.trim().isEmpty()){
			throw new IllegalArgumentException("Fields can not be empty. Please try again");
		}
		try {
			newYear = Integer.parseInt(courseYear.trim());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid Year");
		}
		if (newYear < year) {
			throw new IllegalArgumentException("Addition of historical courses not supported");
		}
		if (department.trim().length() > 4) {
			throw new IllegalArgumentException("Department Code must not exceed 4 characters");
		}
		int newCourseNum = 0;
		try {
			newCourseNum = Integer.parseInt(courseNum.trim());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Course Number must be a number");
		}
		if (newCourseNum > 9999) {
			throw new IllegalArgumentException("Course Number must not exceed 9999");
		}
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		try {
			stmt = DatabaseConnection.getInstance().GetConnection().prepareStatement(
					"SELECT * FROM COURSE WHERE dept=? AND course=? AND section=? AND term=? AND year=?");
			stmt.setString(1, department);
			stmt.setString(2, courseNum);
			stmt.setString(3, courseSect);
			stmt.setString(4, courseTerm);
			stmt.setString(5, courseYear);
			resultSet = stmt.executeQuery();
			if (resultSet.next()) {
				throw new IllegalArgumentException("Course is already in the system.");
			}
			stmt.close();
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}
	}

	public static String[] getCourseSchedule(String currentCourse) {
		ResultSet resultSet = null;
		PreparedStatement stmt = null;
		String courseInfo[] = currentCourse.split("\\t *");
		ArrayList<String> courseDates = new ArrayList<String>();
		try {
			stmt = DatabaseConnection.getInstance().GetConnection().prepareStatement(
					"SELECT date FROM CLASS_DATE WHERE dept=? AND course=? AND section=? AND term=? AND year=? ORDER BY date");
			for (int i = 0; i < courseInfo.length; i++) {
				stmt.setString(i + 1, courseInfo[i]);
			}
			resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				courseDates.add(resultSet.getString(1));
			}
			stmt.close();
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}
		return courseDates.toArray(new String[courseDates.size()]);
	}

	public static String[][] getUsersDeptListForAdmin(String userId) {
		String allDeps[] = getCompleteDeptList();
		String deptHeadCourses[] = getDeptListForDeptHead(userId);
		String output[][] = new String[allDeps.length][2];
		Arrays.sort(deptHeadCourses);

		for (int i = 0; i < allDeps.length; i++) {
			output[i][0] = allDeps[i];
			int match = Arrays.binarySearch(deptHeadCourses, allDeps[i]);
			output[i][1] = (match < 0 ? "false" : "true");
		}
		return output;
	}

	public static String[] getDeptHeadCourseList(String userId) {
		String departments[] = getDeptListForDeptHead(userId);
		ResultSet resultSet = null;
		PreparedStatement stmt = null;
		ArrayList<String> courseList = new ArrayList<String>();
		try {
			int year = Calendar.getInstance().get(Calendar.YEAR);
			stmt = DatabaseConnection.getInstance().GetConnection().prepareStatement(
					"SELECT dept, course, section, term, year FROM COURSE " +
					"WHERE year >= ? AND dept = ? " +
					"ORDER BY dept, course, section, term, year");
			for (String dept : departments) {
				stmt.setInt(1, year);
				stmt.setString(2, dept);
				resultSet = stmt.executeQuery();
				while (resultSet.next()) {
					String newEntry = String.format("%-6s%-6s%-5s%-8s%s", resultSet.getString(1) + "\t",
							resultSet.getString(2) + "\t", resultSet.getString(3) + "\t", resultSet.getString(4) + "\t",
							resultSet.getString(5));
					courseList.add(newEntry);
				}
			}
			stmt.close();
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}
		return courseList.toArray(new String[courseList.size()]);
	}
}
