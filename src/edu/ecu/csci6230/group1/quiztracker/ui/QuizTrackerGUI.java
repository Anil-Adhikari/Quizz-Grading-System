package edu.ecu.csci6230.group1.quiztracker.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.SQLException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import edu.ecu.csci6230.group1.quiztracker.controller.Controller;

/**
 * GUI for the Movie Rental System. Will launch with a command line argument
 * filename, or prompt for a movie file.
 * 
 * @author Matthew Johnson
 *
 */
public class QuizTrackerGUI extends JFrame implements WindowListener {
	/** Quiz controller instance */
	private Controller quizSystem;
	/** Serial ID for object */
	private static final long serialVersionUID = 1L;
	/** Text for main window title */
	private static final String MAIN_WINDOW_TEXT = "Quiz Tracker System";

	/** panel which holds all application views */
	private JPanel pane;
	/** CardlLayout to store the view panels */
	private CardLayout cards;

	/** Creates the login view */
	private final LoginPane paneLogin;
	/** Constant to identify the login pane */
	private static final String LOGIN_PANE = "LoginPane";
	/** Creates the admin view */
	private AdminPane paneAdmin;
	/** Constant to identify the admin pane */
	private static final String ADMIN_PANE = "AdminPane";

	/** Creates the Facultyview */
	private FacultyPane paneFaculty;
	/** Constant to identify the faculty pane */
	private static final String FACULTY_PANE = "FacultyPane";

	/** Creates the Student view */
	private StudentPane paneStudent;
	/** Constant to identify the student pane */
	private static final String STUDENT_PANE = "StudentPane";

	/** Creates the Dept Head view */
	private DeptHeadPane paneDeptHead;
	/** Constant to identify the student pane */
	private static final String DEPT_HEAD_PANE = "DeptHeadPane";

	/** Creates the attendance view */
	private AttendancePane paneAttendance;
	/** Constant to identify the attendance pane */
	private static final String ATTENDANCE_PANE = "AttendancePane";

	/** Creates the student list view */
	private StudentListPane paneStudentList;
	/** Constant to identify the student list pane */
	private static final String STUDENT_LIST_PANE = "StudentListPane";

	/** Shows student details */
	private StudentDetailsPane paneStudentDetails;
	/** Constant to identify the TA pane */
	private static final String STUDENT_DETAILS_PANE = "StudentDetailsPane";

	private JPanel currentPane;
	private InactivityListener warningListener;
	private InactivityListener logoutListener;
	private Thread t1;

	/**
	 * Constructor for the GUI. Will load the movie file and build the
	 * cardlayout with all of the views.
	 * 
	 * Inactivity Listener:
	 * https://tips4java.wordpress.com/2008/10/24/application-inactivity/
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public QuizTrackerGUI() throws ClassNotFoundException, SQLException {
		quizSystem = new Controller();
		paneLogin = new LoginPane(quizSystem, this);
		paneAdmin = new AdminPane(quizSystem, this);
		paneStudent = new StudentPane(quizSystem, this);
		paneDeptHead = new DeptHeadPane(quizSystem, this);
		paneFaculty = new FacultyPane(quizSystem, this);
		paneAttendance = new AttendancePane(quizSystem, this);
		paneStudentList = new StudentListPane(quizSystem, this);
		paneStudentDetails = new StudentDetailsPane(quizSystem, this);

		setTitle(MAIN_WINDOW_TEXT);
		setSize(500, 500);
		setLocation(50, 50);
		addWindowListener(this);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		pane = new JPanel();
		cards = new CardLayout();
		pane.setLayout(cards);
		pane.add(paneLogin, LOGIN_PANE);
		pane.add(paneAdmin, ADMIN_PANE);
		pane.add(paneFaculty, FACULTY_PANE);
		pane.add(paneStudent, STUDENT_PANE);
		pane.add(paneDeptHead, DEPT_HEAD_PANE);
		pane.add(paneStudentList, STUDENT_LIST_PANE);
		pane.add(paneAttendance, ATTENDANCE_PANE);
		pane.add(paneStudentDetails, STUDENT_DETAILS_PANE);
		cards.show(pane, LOGIN_PANE);
		// cards.show(pane, ADMIN_PANE);
		// cards.show(pane, STUDENT_PANE);
		// cards.show(pane, DEPT_HEAD_PANE);
		// cards.show(pane, FACULTY_PANE);
		// cards.show(pane, STUDENT_DETAILS_PANE);
		// cards.show(pane, ATTENDANCE_PANE);
		// cards.show(pane, STUDENT_LIST_PANE);
		//
		Action logoutWarning = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				logoutWarning();
			}
		};

		warningListener = new InactivityListener((JFrame) SwingUtilities.getRoot(this), logoutWarning, 10);

		Container c = getContentPane();
		c.add(pane, BorderLayout.CENTER);
		pack();
		setVisible(true);
	}

	public void logoutWarning() {
		Component rootPanel = SwingUtilities.getRoot(this);
		Action logout = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				t1.interrupt();
				showLoginPane();
				quizSystem.logout();
			}
		};
		logoutListener = new InactivityListener((JFrame) rootPanel, logout, 1);
		logoutListener.start();

		t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				Object options[] = { "OK", "Logoff" };
				int hereIAm = JOptionPane.showOptionDialog(rootPanel,
						"Warning, you will automatically be logged out in one minute.\nClick OK to remain logged in.",
						null, JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, null);
				if (hereIAm == JOptionPane.OK_OPTION) {
					logoutListener.stop();
					warningListener.start();
				} else {
					showLoginPane();
					quizSystem.logout();
				}
			}
		});
		t1.start();
	}

	public void startInactivityListener() {
		warningListener.start();
	}

	public void stopInactivityListener() {
		warningListener.stop();
	}

	public JPanel getCurrentPane() {
		return currentPane;
	}

	public LoginPane getLoginPane() {
		return paneLogin;
	}

	public void showLoginPane() {
		stopInactivityListener();
		currentPane = paneLogin;
		paneLogin.setDefaultLoginButton();
		cards.show(pane, LOGIN_PANE);
	}

	public void showAdminPane() {
		currentPane = paneAdmin;
		cards.show(pane, ADMIN_PANE);
	}

	public AdminPane getAdminPane() {
		return paneAdmin;
	}

	public void showStudentPane() {
		currentPane = paneStudent;
		cards.show(pane, STUDENT_PANE);
	}

	public StudentPane getStudentPane() {
		return paneStudent;
	}

	public FacultyPane getFacultyPane() {
		return paneFaculty;
	}

	public void showFacultyPane() {
		currentPane = paneFaculty;
		paneFaculty.setUser();
		paneFaculty.updateView();
		cards.show(pane, FACULTY_PANE);
	}

	public DeptHeadPane getDeptHeadPane() {
		return paneDeptHead;
	}

	public void showDeptHeadPane() {
		currentPane = paneDeptHead;
		cards.show(pane, DEPT_HEAD_PANE);
	}

	public AttendancePane getAttendancePane() {
		return paneAttendance;
	}

	public void showAttendancePane() {
		currentPane = paneAttendance;
		paneAttendance.updateView();
		cards.show(pane, ATTENDANCE_PANE);
	}

	public StudentListPane getStudentListPane() {
		return paneStudentList;
	}

	public void showStudentListPane() {
		currentPane = paneStudentList;
		cards.show(pane, STUDENT_LIST_PANE);
	}

	public void showStudentDetailsPane() {
		paneStudentDetails.setUser();
		cards.show(pane, STUDENT_DETAILS_PANE);
	}

	public StudentDetailsPane getStudentDetailsPane() {
		currentPane = paneStudentDetails;
		return paneStudentDetails;
	}

	/**
	 * Launches the program and supplies filename if provided.
	 * 
	 * @param args
	 *            commandline arguments
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		new QuizTrackerGUI();
	}

	public void logoutRequested() {
		Object[] options = { "Yes", "No" };
		int quit = JOptionPane.showOptionDialog(this, "Are you sure you want log out?", "Warning",
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
		setLocationRelativeTo(this);
		if (quit == JOptionPane.YES_OPTION) {
			quizSystem.logout();
			showLoginPane();
		}
	}

	@Override
	public void windowClosing(WindowEvent e) {
		Object[] options = { "Yes", "No" };
		int quit = JOptionPane.showOptionDialog(this, "Are you sure you want Quit?", "Warning",
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
		setLocationRelativeTo(this);
		if (quit == JOptionPane.YES_OPTION) {
			quizSystem.quit();
			dispose();
		}
		setVisible(true);
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}
}