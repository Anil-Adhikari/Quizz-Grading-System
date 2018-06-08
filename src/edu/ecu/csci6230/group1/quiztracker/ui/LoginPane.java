package edu.ecu.csci6230.group1.quiztracker.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRootPane;
import javax.swing.border.Border;

import edu.ecu.csci6230.group1.quiztracker.controller.Controller;
import edu.ecu.csci6230.group1.quiztracker.users.UserType;

/**
 * Builds the panel for login. Requires validated username and password to
 * continue.
 * 
 * @author Matthew Johnson
 *
 */
public class LoginPane extends JPanel implements ActionListener {
	/** Serial id for object */
	private static final long serialVersionUID = 1L;
	private LoginInfo loginInfo;
	private BottomButtons bottomButtons;
	JRootPane rootPane;

	private Controller quizSystem;
	private QuizTrackerGUI tracker;

	/**
	 * Constructs the panel displaying the login information.
	 */
	public LoginPane(Controller quizSystem, QuizTrackerGUI tracker) {
		super(new BorderLayout());
		loginInfo = new LoginInfo();
		this.tracker = tracker;
		this.quizSystem = quizSystem;
		bottomButtons = new BottomButtons();

		loginInfo.loginButton.addActionListener(this);
		
		add(loginInfo, BorderLayout.CENTER);

		bottomButtons.logoutButton.setEnabled(false);
		bottomButtons.passwordButton.setEnabled(false);
		bottomButtons.quitButton.setEnabled(true);
		bottomButtons.quitButton.addActionListener(this);
		add(bottomButtons, BorderLayout.SOUTH);
		setDefaultLoginButton();
	}
	
	public void setDefaultLoginButton(){
		rootPane = tracker.getRootPane();
		rootPane.setDefaultButton(loginInfo.loginButton);
	}

	/**
	 * Login panel actions.
	 * 
	 * @param e
	 *            the action selected
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == loginInfo.loginButton) {
			tracker.startInactivityListener();
			rootPane.setDefaultButton(null);
			try {
				String passwordAttempt = "";
				for (char letter : loginInfo.password.getPassword()) {
					passwordAttempt += letter;
				}
				quizSystem.loginUser(loginInfo.username.getText(), passwordAttempt);
				UserType type = quizSystem.getUserType();
				switch (type) {
				case ADMIN:
					tracker.getAdminPane().updateUserTable();
					tracker.showAdminPane();
					break;
				case INSTRUCTOR:
					tracker.getFacultyPane().setNeedsUpdate();
					tracker.getFacultyPane().addCourseList();
					tracker.getFacultyPane().updateView();
					tracker.showFacultyPane();
					break;
				case TA:
					tracker.getFacultyPane().setNeedsUpdate();
					tracker.getFacultyPane().addCourseList();
					tracker.getFacultyPane().updateView();
					tracker.showFacultyPane();
					break;
				case DEPT_HEAD:
					tracker.getDeptHeadPane().setNeedsUpdate();
					tracker.getDeptHeadPane().addCourseList();
					tracker.getDeptHeadPane().updateView();
					tracker.showDeptHeadPane();
					break;
				case STUDENT:
					tracker.getStudentPane().addCourseList();
					tracker.getStudentPane().updateView();
					tracker.showStudentPane();
					break;
				}
				loginInfo.username.setText("");
				loginInfo.password.setText("");
			} catch (IllegalArgumentException error) {
				loginInfo.username.setText("");
				loginInfo.password.setText("");
				if (error.getMessage().equals("Please Reset Password Now"))
					new ChangePassword(quizSystem, tracker);
				else
					JOptionPane.showMessageDialog(this, error.getMessage());
			}
		}

		if (e.getSource() == bottomButtons.quitButton){
			tracker.windowClosing(null);
		}
		LoginPane.this.revalidate();
		LoginPane.this.repaint();
	}

	/**
	 * Generates the text fields for the generic login, admin and user logged in
	 * panels.
	 * 
	 * @author Matthew Johnson
	 */
	private class LoginInfo extends JPanel {
		/** serial ID */
		private static final long serialVersionUID = 1L;
		/** Username text field */
		private TextField username = new TextField(36);
		/** Password field */
		private JPasswordField password = new JPasswordField(25);
		/** Panel for login info */
		private JPanel loginMain = new JPanel();
		/** Login button */
		private final JButton loginButton = new JButton("Login");

		/**
		 * Constructs the panel which contains the login information for the
		 * GUI.
		 */
		public LoginInfo() {
			super(new GridLayout(3, 1));

			loginMain.setLayout(new GridLayout(4, 1));
			loginMain.add(new JLabel(""));
			JPanel loginUser = new JPanel(new FlowLayout());
			loginUser.add(new JLabel("Username: "));
			loginUser.add(username);
			loginMain.add(loginUser);

			JPanel loginPassword = new JPanel(new FlowLayout());
			loginPassword.add(new JLabel("Password: "));
			loginPassword.add(password);
			loginMain.add(loginPassword);

			JPanel loginButtonPanel = new JPanel();
			loginButtonPanel.add(loginButton);
			loginMain.add(loginButtonPanel);

			Border redColor = BorderFactory.createLineBorder(Color.red);
			Border outerBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
			Border combined = BorderFactory.createCompoundBorder(outerBorder, redColor);

			loginMain.setBorder(combined);

			add(loginMain);
			setVisible(true);
		}
	}
}
