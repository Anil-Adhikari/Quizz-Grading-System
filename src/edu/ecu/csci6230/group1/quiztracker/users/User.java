package edu.ecu.csci6230.group1.quiztracker.users;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.ecu.csci6230.group1.quiztracker.dataconnection.DatabaseConnection;

public abstract class User {

	/** ID number specific to user, must be unique for all users */
	private String userId;
	/** User's first name */
	private String firstName;
	/** User's last name */
	private String lastName;
	/**
	 * setting this parameter to true will force the user to reset their
	 * password
	 */
	private boolean needsPassword;
	/** defines the user role */
	private UserType userRole;

	protected DatabaseConnection database;

	public User(String first, String last, String userName, boolean needsPassword, UserType role) {
		this.firstName = first;
		this.lastName = last;
		this.userId = userName;
		this.needsPassword = needsPassword;
		this.userRole = role;
		try {
			database = DatabaseConnection.getInstance();
		} catch (ClassNotFoundException | SQLException e) {
			System.out.println("User.User " + e.getMessage());
		}
	}

	/** Method for testing only - delete later */
	public void setPasswordFlag() {
		this.needsPassword = false;
	}

	public String getUserId() {
		return userId;
	}

	public String getPassword() {
		ResultSet resultSet = null;
		PreparedStatement stmt = null;
		String password = "";
		try {
			stmt = database.GetConnection().prepareStatement("SELECT password FROM USER WHERE user = ?");
			stmt.setString(1, userId);
			resultSet = stmt.executeQuery();
			if (resultSet.next()) {
				password = resultSet.getString("password");
			} else {
				throw new IllegalArgumentException("SQL error");
			}
			resultSet.close();
			stmt.close();
		} catch (SQLException e) {
			System.out.println("User.getPassword " + e.getMessage());
		}
		return password;
	}

	public boolean getNeedsPassword() {
		return this.needsPassword;
	}

	public void changePassword(String oldPassword, String newPassword) {
		ResultSet resultSet = null;
		PreparedStatement stmt = null;
		PreparedStatement stmt2 = null;
		String currentPassword = "";
		try {
			stmt = database.GetConnection().prepareStatement("SELECT password FROM USER WHERE user = ? AND password = ?");
			stmt.setString(1, userId);
			stmt.setString(2, oldPassword);
			resultSet = stmt.executeQuery();
			if (resultSet.next()) {
				currentPassword = resultSet.getString("password");
			} else {
				throw new IllegalArgumentException("Old password is incorrect");
			}
			if (!currentPassword.equals(newPassword) && newPassword.length() > 5) {
				stmt2 = database.GetConnection()
						.prepareStatement("UPDATE USER SET password=?,reset_password=0  WHERE user = ?");
				stmt2.setString(1, newPassword);
				stmt2.setString(2, userId);
				stmt2.executeUpdate();
			} else {
				throw new IllegalArgumentException(
						"Invalid password: Password must be at least 6 characters and not the same as the old password");
			}
			resultSet.close();
			stmt.close();
			stmt2.close();
		} catch (SQLException e) {
			System.out.println("User.changePassword " + e.getMessage());
		}
		this.needsPassword = false;
	}

	public UserType getUserRole() {
		return userRole;
	}

	public String getFullname() {
		return firstName + " " + lastName;
	}
}
