package edu.ecu.csci6230.group1.quiztracker.managers;

import edu.ecu.csci6230.group1.quiztracker.dataconnection.DatabaseConnection;
import edu.ecu.csci6230.group1.quiztracker.users.Administrator;
import edu.ecu.csci6230.group1.quiztracker.users.Assistant;
import edu.ecu.csci6230.group1.quiztracker.users.DeptHead;
import edu.ecu.csci6230.group1.quiztracker.users.Instructor;
import edu.ecu.csci6230.group1.quiztracker.users.Student;
import edu.ecu.csci6230.group1.quiztracker.users.User;
import edu.ecu.csci6230.group1.quiztracker.users.UserType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class UserManager {
	public static User verifyUser(String userName, String password) {
		if(userName.trim().isEmpty() || password.trim().isEmpty()){
			throw new IllegalArgumentException("Please enter username and password");
		}
		ResultSet resultSet = null;
		PreparedStatement stmt = null;
		User newUser = null;
		try {
			stmt = DatabaseConnection.getInstance().GetConnection()
					.prepareStatement("SELECT * FROM USER WHERE user = ? AND password = ? AND active=1");
			stmt.setString(1, userName);
			stmt.setString(2, password);
			resultSet = stmt.executeQuery();
			if (resultSet.next()) {
				UserType currentUser = UserType.valueOf(resultSet.getString("role"));

				switch (currentUser) {
				case ADMIN:
					newUser = new Administrator(resultSet.getString("first_name"), resultSet.getString("last_name"),
							resultSet.getString("user"), resultSet.getBoolean("reset_password"), currentUser);
					break;
				case INSTRUCTOR:
					newUser = new Instructor(resultSet.getString("first_name"), resultSet.getString("last_name"),
							resultSet.getString("user"), resultSet.getBoolean("reset_password"), currentUser);
					break;
				case TA:
					newUser = new Assistant(resultSet.getString("first_name"), resultSet.getString("last_name"),
							resultSet.getString("user"), resultSet.getBoolean("reset_password"), currentUser);
					break;
				case DEPT_HEAD:
					newUser = new DeptHead(resultSet.getString("first_name"), resultSet.getString("last_name"),
							resultSet.getString("user"), resultSet.getBoolean("reset_password"), currentUser);
					break;
				case STUDENT:
					newUser = new Student(resultSet.getString("first_name"), resultSet.getString("last_name"),
							resultSet.getString("user"), resultSet.getBoolean("reset_password"), currentUser);
					break;
				}
			} else {
				throw new IllegalArgumentException("Username or password is incorrect.");
			}
			resultSet.close();
			stmt.close();
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println("UserManager.verifyUser " + e.getMessage());
		}
		return newUser;
	}
/**
 * Used to add quizzes to each user.
 * @param courseDetails
 * @return
 */
	public static ArrayList<String> getStudentIDListForCourse(String courseDetails) {
		String courseInfo[] = courseDetails.split("\t *");
		ResultSet resultSet = null;
		PreparedStatement stmt = null;
		ArrayList<String> studentList = new ArrayList<String>();
		try {
			stmt = DatabaseConnection.getInstance().GetConnection()
					.prepareStatement("SELECT E1.user FROM ENROLLMENT E1 " +
										"JOIN USER U2 " +
										"ON E1.user=U2.user " +
										"WHERE role='STUDENT' AND dept=? AND course=? AND section=? AND term=? AND year=? AND active=1");
			for (int i = 0; i < courseInfo.length; i++) {
				stmt.setString(i + 1, courseInfo[i]);
			}
			resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				studentList.add(resultSet.getString(1));
			}
			stmt.close();
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println("UserManager.getStudentIDListForCourse " + e.getMessage());
		}
		return studentList;
	}

	public static String[] getFullUserListasString() {
		ResultSet resultSet = null;
		PreparedStatement stmt = null;
		ArrayList<String> userList = new ArrayList<String>();
		try {
			stmt = DatabaseConnection.getInstance().GetConnection()
					.prepareStatement("SELECT user, first_name, last_name, role FROM USER WHERE active=1 "
							+ "ORDER BY last_name, first_name");
			resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				String newEntry = String.format("%-20s%-15s%-20s%s", resultSet.getString(1) + "\t",
						resultSet.getString(2) + "\t", resultSet.getString(3) + "\t", resultSet.getString(4) + "\t");
				userList.add(newEntry);
			}
			stmt.close();
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println("UserManager.getFullUserListasString " + e.getMessage());
		}
		return userList.toArray(new String[userList.size()]);
	}

	public static String[] getStudentListasString() {
		ResultSet resultSet = null;
		PreparedStatement stmt = null;
		ArrayList<String> userList = new ArrayList<String>();
		try {
			stmt = DatabaseConnection.getInstance().GetConnection()
					.prepareStatement("SELECT user, first_name, last_name FROM USER WHERE ROLE = 'STUDENT' AND active=1 ORDER BY last_name, first_name");
			resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				String newEntry = String.format("%-20s%-15s%s", resultSet.getString(1) + "\t",
						resultSet.getString(2) + "\t", resultSet.getString(3));
				userList.add(newEntry);
			}
			stmt.close();
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println("UserManager.getStudentListasString " + e.getMessage());
		}
		return userList.toArray(new String[userList.size()]);
	}

	public static Student getStudent(String studentDetails) {
		ResultSet resultSet = null;
		PreparedStatement stmt = null;
		Student targetStudent = null;
		String studentId = studentDetails.split("\\t *")[0];
		try {
			stmt = DatabaseConnection.getInstance().GetConnection()
					.prepareStatement("SELECT user, first_name, last_name FROM USER WHERE user=? AND ROLE='STUDENT'");
			stmt.setString(1, studentId);
			resultSet = stmt.executeQuery();
			if (resultSet.next()) {
				targetStudent = new Student(resultSet.getString(2), resultSet.getString(3), resultSet.getString(1),
						false, UserType.STUDENT);
			}
			stmt.close();
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println("UserManager.getStudent " + e.getMessage());
		}
		return targetStudent;
	}

	public static String[] getStudentListforCourse(String currentCourse) {
		String courseInfo[] = currentCourse.split("\\t *");
		ResultSet resultSet = null;
		PreparedStatement stmt = null;
		ArrayList<String> enrolledStudents = new ArrayList<String>();
		try {
			stmt = DatabaseConnection.getInstance().GetConnection()
					.prepareStatement("SELECT U1.user, first_name, last_name FROM USER AS U1 "
							+ "RIGHT JOIN ENROLLMENT as E1 " + "ON E1.user = U1.user "
							+ "WHERE dept=? AND course=? AND section=? AND term=? AND year=? AND role='STUDENT' AND U1.active=1 "
							+ "ORDER BY U1.last_name, U1.user");
			for (int i = 0; i < courseInfo.length; i++) {
				stmt.setString(i + 1, courseInfo[i]);
			}
			resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				String newEntry = String.format("%-20s%-15s%s", resultSet.getString(1) + "\t",
						resultSet.getString(2) + "\t", resultSet.getString(3));
				enrolledStudents.add(newEntry);
			}
			stmt.close();
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println("UserManager.getStudentListForCourse " + e.getMessage());
		}
		return enrolledStudents.toArray(new String[enrolledStudents.size()]);
	}
}
