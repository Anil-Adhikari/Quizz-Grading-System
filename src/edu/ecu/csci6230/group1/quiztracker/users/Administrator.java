package edu.ecu.csci6230.group1.quiztracker.users;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JCheckBox;

public class Administrator extends User {

	public Administrator(String first, String last, String userName, boolean needsPassword, UserType role) {
		super(first, last, userName, needsPassword, role);
	}

	public void resetUserPassword(String userId) {
		PreparedStatement stmt = null;
		try {
			stmt = database.GetConnection()
					.prepareStatement("UPDATE USER SET password = ?, reset_password=1  WHERE user = ?");
			stmt.setString(1, userId + "1234");
			stmt.setString(2, userId);
			stmt.executeUpdate();
			stmt.close();
		} catch (SQLException e) {
			System.out.println("Administrator.resetUserPassword " + e.getMessage());
		}
	}

	public void removeUser(String userId) {
		if (this.getUserId().equals(userId)) {
			throw new IllegalArgumentException("Can not remove administrator");
		} else {
			PreparedStatement stmt = null;
			try {
				String tables[] = { "USER", "QUIZ", "ATTENDANCE" };
				for (String table : tables) {
					String query = "UPDATE " + table + " SET active = 0 WHERE user = ?";
					stmt = database.GetConnection().prepareStatement(query);
					stmt.setString(1, userId);
					stmt.executeUpdate();
				}
				stmt = database.GetConnection().prepareStatement("DELETE FROM ENROLLMENT WHERE user=?");
				stmt.setString(1, userId);
				stmt.executeUpdate();
				stmt.close();
			} catch (SQLException e) {
				System.out.println("Administrator.removeUser " + e.getMessage());
			}
		}
	}

	// Returns the userId of our new user, mostly for testing purposes.
	public String addUser(UserType type, String first, String last, String username,
			ArrayList<JCheckBox> courseSelections) {
		validateUserInfo(username.trim(), first.trim(), last.trim());
		username = validateUsername(username);
		PreparedStatement stmt = null;
		try {
			stmt = database.GetConnection().prepareStatement(
					"INSERT INTO USER (user, first_name, last_name, password, role, reset_password, active) VALUES (?, ?, ?, ?, ?, 1, 1)");
			stmt.setString(1, username);
			stmt.setString(2, first);
			stmt.setString(3, last);
			stmt.setString(4, username + "1234");
			stmt.setString(5, type.toString());
			stmt.executeUpdate();
			stmt.close();

			for (JCheckBox course : courseSelections) {
				if (course.isSelected()) {
					String courseInfo[] = course.getText().split("\\t *");
					stmt = database.GetConnection().prepareStatement(
							"INSERT INTO ENROLLMENT (user, dept, course, section, term, year) VALUES (?, ?, ?, ?, ?, ?)");
					stmt.setString(1, username);
					for (int i = 0; i < courseInfo.length; i++) {
						stmt.setString(i + 2, courseInfo[i]);
					}
					stmt.executeUpdate();
					stmt.close();
				}
			}
		} catch (SQLException e) {
			System.out.println("Administrator.addUser " + e.getMessage());
		}
		return username;
	}

	public String addDeptHead(UserType type, String first, String last, String username,
			ArrayList<JCheckBox> courseSelections) {
		validateUserInfo(username.trim(), first.trim(), last.trim());
		username = validateUsername(username);
		PreparedStatement stmt = null;
		try {
			stmt = database.GetConnection().prepareStatement(
					"INSERT INTO USER (user, first_name, last_name, password, role, reset_password, active) VALUES (?, ?, ?, ?, ?, 1, 1)");
			stmt.setString(1, username);
			stmt.setString(2, first);
			stmt.setString(3, last);
			stmt.setString(4, username + "1234");
			stmt.setString(5, type.toString());
			stmt.executeUpdate();
			stmt.close();
			for (JCheckBox course : courseSelections) {
				if (course.isSelected()) {
					String courseInfo[] = course.getText().split("\\t *");
					stmt = database.GetConnection()
							.prepareStatement("INSERT INTO ENROLLMENT (user, dept) VALUES (?, ?)");
					stmt.setString(1, username);
					stmt.setString(2, courseInfo[0]);
					stmt.executeUpdate();
					stmt.close();
				}
			}
		} catch (SQLException e) {
			System.out.println("Administrator.addDeptHead " + e.getMessage());
		}
		return username;
	}

	// Returns the userId of our new user, mostly for testing purposes.
	public String updateUser(UserType type, String first, String last, String username,
			ArrayList<JCheckBox> courseSelections, String oldUsername) {
		if (!username.equals(oldUsername)) {
			validateUpdatedUserName(username);
		}
		validateUserInfo(username.trim(), first.trim(), last.trim());
		PreparedStatement stmt = null;
		try {
			stmt = database.GetConnection()
					.prepareStatement("UPDATE USER SET first_name=?, last_name=?, role=?, user=?  WHERE user=?");
			stmt.setString(1, first);
			stmt.setString(2, last);
			stmt.setString(3, type.toString());
			stmt.setString(4, username);
			stmt.setString(5, oldUsername);
			stmt.executeUpdate();

			// change username everywhere
			if (!username.equals(oldUsername)) {
				stmt = database.GetConnection().prepareStatement("UPDATE ENROLLMENT SET user=? WHERE user=?");
				stmt.setString(1, username);
				stmt.setString(2, oldUsername);
				stmt.executeUpdate();

				stmt = database.GetConnection().prepareStatement("UPDATE ATTENDANCE SET user=? WHERE user=?");
				stmt.setString(1, username);
				stmt.setString(2, oldUsername);
				stmt.executeUpdate();

				stmt = database.GetConnection().prepareStatement("UPDATE QUIZ SET user=? WHERE user=?");
				stmt.setString(1, username);
				stmt.setString(2, oldUsername);
				stmt.executeUpdate();
			}
			for (JCheckBox course : courseSelections) {
				boolean alreadyEnrolled = getStudentEnrollmentId(username, course.getText());
				String courseInfo[] = course.getText().split("\\t *");
				if (!alreadyEnrolled && course.isSelected()) {
					stmt = database.GetConnection().prepareStatement(
							"INSERT INTO ENROLLMENT (user, dept, course, section, term, year) VALUES (?, ?, ?, ?, ?, ?)");
					stmt.setString(1, username);
					for (int i = 0; i < courseInfo.length; i++) {
						stmt.setString(i + 2, courseInfo[i]);
					}
					stmt.executeUpdate();
				} else if (alreadyEnrolled && !course.isSelected()) {
					stmt = database.GetConnection().prepareStatement(
							"DELETE FROM ENROLLMENT WHERE user=? AND dept=? AND course=? AND section=? AND term=? AND year=?");
					stmt.setString(1, username);
					for (int i = 0; i < courseInfo.length; i++) {
						stmt.setString(i + 2, courseInfo[i]);
					}
					stmt.executeUpdate();
				}
				if (type == UserType.STUDENT && alreadyEnrolled) {
					stmt = database.GetConnection().prepareStatement(
							"UPDATE ATTENDANCE SET active=? WHERE user=? AND dept=? AND course=? AND section=? AND term=? AND year=?");
					stmt.setBoolean(1, course.isSelected());
					stmt.setString(2, username);
					for (int i = 0; i < courseInfo.length; i++) {
						stmt.setString(i + 3, courseInfo[i]);
					}
					stmt.executeUpdate();
					stmt = database.GetConnection().prepareStatement(
							"UPDATE QUIZ SET active=? WHERE user=? AND dept=? AND course=? AND section=? AND term=? AND year=?");
					stmt.setBoolean(1, course.isSelected());
					stmt.setString(2, username);
					for (int i = 0; i < courseInfo.length; i++) {
						stmt.setString(i + 3, courseInfo[i]);
					}
					stmt.executeUpdate();
				}
			}
			stmt.close();
		} catch (SQLException e) {
			System.out.println("Administrator.updateUser " + e.getMessage());
		}
		return username;
	}

	private boolean getStudentEnrollmentId(String username, String currentCourse) {
		PreparedStatement stmt;
		ResultSet results;
		String courseInfo[] = currentCourse.split("\\t *");
		try {
			stmt = database.GetConnection().prepareStatement(
					"SELECT id FROM ENROLLMENT WHERE user=? AND dept=? AND course=? AND section=? AND term=? AND year=?");
			stmt.setString(1, username);
			for (int i = 0; i < courseInfo.length; i++) {
				stmt.setString(i + 2, courseInfo[i]);
			}
			results = stmt.executeQuery();
			if (results.next()) {
				stmt.close();
				return true;
			}
		} catch (SQLException e) {
			System.out.println("Administrator.getStudentEnrollmentId " + e.getMessage());
		}
		return false;
	}

	public String updateDeptHead(UserType type, String first, String last, String username,
			ArrayList<JCheckBox> courseSelections, String oldUsername) {
		if (!username.equals(oldUsername)) {
			validateUpdatedUserName(username);
		}
		validateUserInfo(username.trim(), first.trim(), last.trim());
		PreparedStatement stmt = null;
		try {
			stmt = database.GetConnection()
					.prepareStatement("UPDATE USER SET first_name=?, last_name=?, role=?, user=?  WHERE user=?");
			stmt.setString(1, first);
			stmt.setString(2, last);
			stmt.setString(3, type.toString());
			stmt.setString(4, username);
			stmt.setString(5, oldUsername);
			stmt.executeUpdate();

			// change name everywhere
			if (!username.equals(oldUsername)) {
				stmt = database.GetConnection().prepareStatement("UPDATE ENROLLMENT SET user=? WHERE user=?");
				stmt.setString(1, username);
				stmt.setString(2, oldUsername);
				stmt.executeUpdate();

				stmt = database.GetConnection().prepareStatement("UPDATE ATTENDANCE SET user=? WHERE user=?");
				stmt.setString(1, username);
				stmt.setString(2, oldUsername);
				stmt.executeUpdate();

				stmt = database.GetConnection().prepareStatement("UPDATE QUIZ SET user=? WHERE user=?");
				stmt.setString(1, username);
				stmt.setString(2, oldUsername);
				stmt.executeUpdate();
			}

			for (JCheckBox course : courseSelections) {
				boolean alreadyEnrolled = getDeptHeadEnrollmentId(username, course.getText());
				// this is a new course
				if (!alreadyEnrolled && course.isSelected()) {
					stmt = database.GetConnection()
							.prepareStatement("INSERT INTO ENROLLMENT (user, dept) VALUES (?, ?)");
					stmt.setString(1, username);
					stmt.setString(2, course.getText());
					stmt.executeUpdate();
					// drop an existing course
				} else if (alreadyEnrolled && !course.isSelected()) {
					stmt = database.GetConnection().prepareStatement("DELETE FROM ENROLLMENT WHERE user=? AND dept=?");
					stmt.setString(1, username);
					stmt.setString(2, course.getText());
					stmt.executeUpdate();
				}
			}
			stmt.close();
		} catch (SQLException e) {
			System.out.println("Administrator.updateDeptHead " + e.getMessage());
		}
		return username;
	}

	private boolean getDeptHeadEnrollmentId(String username, String currentCourse) {
		PreparedStatement stmt;
		ResultSet results;
		try {
			stmt = database.GetConnection()
					.prepareStatement("SELECT id FROM ENROLLMENT WHERE user=? AND dept=?");
			stmt.setString(1, username);
			stmt.setString(2, currentCourse);
			results = stmt.executeQuery();
			if (results.next()) {
				return true;
			}
		} catch (SQLException e) {
			System.out.println("Administrator.getDeptHeadEnrollmentId " + e.getMessage());
		}
		return false;
	}

	private String validateUsername(String username) {
		if (username == null || username.trim().length() == 0) {
			throw new IllegalArgumentException("Username can not be blank");
		}
		String newUsername = username;
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		int count = 0;
		try {
			stmt = database.GetConnection()
					.prepareStatement("SELECT COUNT(user) AS usercount FROM USER WHERE user = ?");
			stmt.setString(1, username);
			resultSet = stmt.executeQuery();
			if (resultSet.next()) {
				count = resultSet.getInt("usercount");
			}
			stmt.close();
		} catch (SQLException e) {
			System.out.println("Administrator.validateUsername " + e.getMessage());
		}
		if (count > 0) {
			newUsername += (count + 1);
		}
		return newUsername;
	}

	private void validateUserInfo(String userId, String firstName, String lastName) {
		if (userId.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
			throw new IllegalArgumentException("Fields can not be empty. Please try again");
		}
	}

	private void validateUpdatedUserName(String username) {
		if (username == null || username.trim().length() == 0) {
			throw new IllegalArgumentException("Username can not be blank");
		}
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		int count = 0;
		try {
			stmt = database.GetConnection()
					.prepareStatement("SELECT COUNT(user) AS usercount FROM USER WHERE user = ?");
			stmt.setString(1, username);
			resultSet = stmt.executeQuery();
			if (resultSet.next()) {
				count = resultSet.getInt("usercount");
			}
			stmt.close();
		} catch (SQLException e) {
			System.out.println("Administrator.validateUsername " + e.getMessage());
		}
		if (count > 0) {
			throw new IllegalArgumentException("Username " + username + " is already taken.");
		}
	}
}
