package edu.ecu.csci6230.group1.quiztracker.controller;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JCheckBox;

import edu.ecu.csci6230.group1.quiztracker.dataconnection.DatabaseConnection;
import edu.ecu.csci6230.group1.quiztracker.fileIO.DownloadFile;
import edu.ecu.csci6230.group1.quiztracker.fileIO.UploadFile;
import edu.ecu.csci6230.group1.quiztracker.managers.AttendanceManager;
import edu.ecu.csci6230.group1.quiztracker.managers.CourseManager;
import edu.ecu.csci6230.group1.quiztracker.managers.QuizManager;
import edu.ecu.csci6230.group1.quiztracker.managers.UserManager;
import edu.ecu.csci6230.group1.quiztracker.users.Student;
import edu.ecu.csci6230.group1.quiztracker.users.User;
import edu.ecu.csci6230.group1.quiztracker.users.UserType;
import edu.ecu.csci6230.group1.quiztracker.users.Administrator;
import edu.ecu.csci6230.group1.quiztracker.users.Instructor;

public class Controller {

	private User currentUser;

	public Controller() {
		currentUser = null;
	}

	public void loginUser(String userName, String password) throws IllegalArgumentException {
		currentUser = UserManager.verifyUser(userName, password);
		if (checkPassword()) {
			throw new IllegalArgumentException("Please Reset Password Now");
		}
	}

	public User getCurrentUser() {
		return currentUser;
	}

	public void logout() {
		currentUser = null;
	}

	private boolean checkPassword() {
		return currentUser.getNeedsPassword();
	}

	public void resetPassword(String oldPassword, String newPassword) throws IllegalArgumentException {
		currentUser.changePassword(oldPassword, newPassword);
	}

	public UserType getUserType() {
		return currentUser.getUserRole();
	}

	public String[] getUserList() {
		return UserManager.getFullUserListasString();
	}

	public String[] getStudentList() {
		return UserManager.getStudentListasString();
	}

	public void addUser(UserType type, String first, String last, String username,
			ArrayList<JCheckBox> courseSelections) {
		if (currentUser.getUserRole() == UserType.ADMIN) {
			if (type != UserType.DEPT_HEAD) {
				((Administrator) currentUser).addUser(type, first, last, username, courseSelections);
			} else {
				((Administrator) currentUser).addDeptHead(type, first, last, username, courseSelections);
			}
		}
	}

	public void updateUser(UserType type, String first, String last, String username,
			ArrayList<JCheckBox> courseSelections, String oldUsername) {
		if (currentUser.getUserRole() == UserType.ADMIN) {
			if (type != UserType.DEPT_HEAD) {
				((Administrator) currentUser).updateUser(type, first, last, username, courseSelections, oldUsername);
			} else {
				((Administrator) currentUser).updateDeptHead(type, first, last, username, courseSelections, oldUsername);
			}
		}
	}

	public void setNeedsNewPassword(String userInfo) {
		if (userInfo == null || userInfo.length() == 0) {
			throw new IllegalArgumentException("invalid index");
		} else if (currentUser.getUserRole() == UserType.ADMIN) {
			((Administrator) currentUser).resetUserPassword(userInfo.split("\\t *")[0]);
		}
	}

	public void deleteUser(String userInfo) {
		if (userInfo == null || userInfo.length() == 0) {
			throw new IllegalArgumentException("invalid index");
		} else {
			((Administrator) currentUser).removeUser(userInfo.split("\\t *")[0]);
		}
	}

	public String[] getStudenQuizList(String currentCourse, Student student) {
		String[] output = { "" };
		if (student != null) {
			return student.getStudentCourseQuizListing(currentCourse);
		} else {
			return output;
		}
	}

	public void gradeQuiz(String selectedQuiz, String detailStudent, String currentCourse, int value, String comments) {
		QuizManager.addQuizGrade(selectedQuiz, detailStudent, currentCourse, value, comments);
	}

	public String getQuizComments(String quizInfo, String currentCourse) {
		return ((Student) currentUser).getQuizResultComments(quizInfo, currentCourse);
	}

	public String getQuizComments(String quizInfo, String detailStudent, String currentCourse) {
		return UserManager.getStudent(detailStudent).getQuizResultComments(quizInfo, currentCourse);
	}

	public void uploadFinishedQuiz(String currentCourse, String quizInfo, String path) {
		if (quizInfo == null || quizInfo.length() == 0) {
			throw new IllegalArgumentException("Please select the quiz you are submitting");
		}
		String quizName = quizInfo.split("\\t *")[0];
		UploadFile.putStudentSubmission(currentCourse, path, quizName, currentUser.getUserId());
	}

	public Student getSelectedStudent(String studentInfo) {
		return UserManager.getStudent(studentInfo);
	}

	public void assignQuiz(String currentCourse, String quizName, int value, String fullFilePath, String dueDate) {
		((Instructor) currentUser).assignQuiz(currentCourse, quizName, value, fullFilePath, dueDate);
	}

	public String getQuizAverage(String currentCourse, Student student) {
		if (currentUser == null || student == null) {
			return "";
		} else if (currentUser.getUserRole() == UserType.STUDENT) {
			return ((Student) getCurrentUser()).getStudentQuizAverage(currentCourse);
		} else {
			return student.getStudentQuizAverage(currentCourse);
		}
	}

	public String getDownloadQuizFileName(String quizInfo, String userInfo, String courseDetail) {
		if (currentUser.getUserRole() == UserType.STUDENT) {
			return DownloadFile.getFilename(quizInfo.split("\\t *")[0], currentUser.getUserId(), courseDetail);
		} else {
			return DownloadFile.getFilename(quizInfo.split("\\t *")[0], userInfo.split("\\t *")[0], courseDetail);
		}
	}

	public void downloadQuiz(String quizInfo, String destination, String detailStudent, String courseDetail) {
		if (currentUser.getUserRole() == UserType.STUDENT) {
			DownloadFile.getFile(quizInfo.split("\\t *")[0], destination, currentUser.getUserId(), courseDetail);
		} else {
			DownloadFile.getFile(quizInfo.split("\\t *")[0], destination, detailStudent.split("\\t *")[0],
					courseDetail);
		}
	}

	public String getAggregateQuizMean(String course) {
		return String.format("%.0f%%", QuizManager.getAggregateQuizMean(course));
	}

	public String[] getAggregateGradedQuizList(String course) {
		ArrayList<String> gradedQuizzes = QuizManager.getGradedQuizzesList(course);
		String arrayOut[] = gradedQuizzes.toArray(new String[gradedQuizzes.size()]);
		return arrayOut;
	}

	public void markAttendance(String date, JCheckBox[] marks, String selectedCourse) {
		AttendanceManager.markAttendance(date, marks, selectedCourse);

	}

	public String[][] getStudentCourseAttendanceList(String studentDetails, String courseDetails) {
		if (currentUser == null && studentDetails == null) {
			return null;
		} else if (currentUser.getUserRole() == UserType.STUDENT) {
			return ((Student) getCurrentUser()).getStudentCourseAttendance(courseDetails);
		} else {
			return UserManager.getStudent(studentDetails).getStudentCourseAttendance(courseDetails);
		}
	}

	public String[][] getAttendanceSummary(String courseDetails) {
		return AttendanceManager.getFullCourseAttendance(courseDetails);
	}

	public void quit() {
		try {
			DatabaseConnection.getInstance().CloseConnection();
		} catch (ClassNotFoundException | SQLException e) {
			System.out.println(e.getMessage());
		}
		System.exit(0);
	}

	public String[][] getFullCourseListForUser(String userId) {
		return CourseManager.getUsersCourseListForAdmin(userId);
	}

	public String[][] getFullDeptSelectionListForUser(String userId) {
		return CourseManager.getUsersDeptListForAdmin(userId);
	}

	public String[] getDeptList() {
		return CourseManager.getCompleteDeptList();
	}

	public void addNewCourse(String department, String courseNum, String courseSect, String courseDesc,
			String courseTerm, String courseYear) {
		CourseManager.addNewCourse(department, courseNum, courseSect, courseDesc, courseTerm, courseYear);
	}

	public String[] getEnrolledCoursesForUser(String userId) {
		return CourseManager.getUsersEnrolledCourses(userId);
	}

	public String[] getDeptHeadCourseList(String userId) {
		return CourseManager.getDeptHeadCourseList(userId);
	}

	public String[] getStudentListForCourse(String currentCourse) {
		return UserManager.getStudentListforCourse(currentCourse);
	}

	public int getQuizValue(String selectedQuiz, String studentDetails, String courseDetails) {
		return QuizManager.getQuizValue(selectedQuiz, studentDetails, courseDetails);
	}

	public boolean checkDueDate(String currentCourse, String quizInfo) {
		if (quizInfo == null || quizInfo.length() == 0) {
			throw new IllegalArgumentException("Please select the quiz you are submitting");
		}
		String quizName = quizInfo.split("\\t *")[0];
		return QuizManager.checkDueDate(currentCourse, quizName, currentUser.getUserId());
	}

	public String getCurrentUserFullName() {
		if (currentUser != null) {
			return currentUser.getFullname();
		} else {
			return "";
		}
	}

	public String getAverageAttendance(String[][] attendanceList) {
		return AttendanceManager.getAverageAttendance(attendanceList);
	}
}
