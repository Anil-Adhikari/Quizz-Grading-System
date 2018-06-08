package edu.ecu.csci6230.group1.quiztracker.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.ecu.csci6230.group1.quiztracker.controller.Controller;
import edu.ecu.csci6230.group1.quiztracker.users.UserType;

/**
 * Builds the administrator panel for the application.
 * 
 * @author Matthew Johnson
 *
 */
public class AdminPane extends JPanel implements ActionListener {
	/** Serial id for object */
	private static final long serialVersionUID = 1L;
	/** OK button for the new user dialog */
	private final JButton newUserOKButton = new JButton("OK");
	/** cancel button for the new user dialog */
	private final JButton newUserCancelInputButton = new JButton("Cancel");
	/** Dialog popup for entering a new user */
	private JFrame changeUser;
	/** Username text field to input new user ID */
	private TextField newUsername;
	private TextField newFirstName;
	private TextField newLastName;

	private JDialog newCourse;
	private JComboBox<String> newCourseDept;
	private TextField newCourseNum;
	private TextField newCourseSection;
	private TextField newCourseDescription;
	public static final String TERMS[] = { "fall", "spring", "summer" };
	private JComboBox<String> newCourseTerm;
	public static final String YEARS[] = { "2016", "2017", "2018", "2019", "2020" };
	private JComboBox<String> newCourseYear;
	private final JButton newCourseOKButton = new JButton("OK");
	private final JButton newCourseCancelInputButton = new JButton("Cancel");

	/** List model for displaying users */
	private DefaultListModel<String> listModel;
	/** Text for Delete User */
	// private static final String DELETE_USER = "Delete Selected User";
	/** Text for User List */
	private static final String USER_LIST_TEXT = "User List";
	/** Table for showing user list */
	private JList<String> userList;
	/** Scroll pane for the user list */
	private JScrollPane listScrollPane;

	/** Dialog popup for canceling a user account */
	// private JDialog cancelUser;
	/** Accept user account delete */
	// private final JButton acceptDeleteAccountButton = new JButton("OK");
	/** Do not cancel the user account */
	// private final JButton doNotDeleteAccountButton = new
	// JButton("Cancel");
	/** Panel to contain everything except navigation buttons */
	private JPanel list;
	/** Panel for buttons at top */
	private TopButtons topButtons;
	/** Panel for buttons at bottom */
	private BottomButtons bottomButton;

	/** Combobox to display the selection for user role */
	private JComboBox<UserType> selectRole;

	private Controller quizSystem;
	private QuizTrackerGUI tracker;

	/** List to store enrolled courses for a user */
	private ArrayList<JCheckBox> courseSelection;
	private JScrollPane courses;
	private JPanel editUser;
	private JPanel courseData;
	private JLabel welcomeLabel;
	
	private String oldUsername;

	/**
	 * Draws the administrator panel. Will have popup windows for adding and
	 * deleting user accounts.
	 */
	public AdminPane(Controller quizSystem, QuizTrackerGUI tracker) {
		super(new BorderLayout());
		this.tracker = tracker;
		this.quizSystem = quizSystem;
		bottomButton = new BottomButtons();
		topButtons = new TopButtons();
		list = new JPanel();
		listModel = new DefaultListModel<String>();
		userList = new JList<String>(listModel);
		userList.setFont(new Font("monospaced", Font.PLAIN, 12));
		listScrollPane = new JScrollPane(userList);

		list.add(listScrollPane);

		userList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				if (!arg0.getValueIsAdjusting()) {
					if (userList.getSelectedIndex() > -1) {
						topButtons.editButton.setEnabled(true);
						topButtons.deleteButton.setEnabled(true);
						topButtons.resetButton.setEnabled(true);
					}
				}
			}
		});

		Border title = new TitledBorder(USER_LIST_TEXT);
		Border outerBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		Border combined = BorderFactory.createCompoundBorder(outerBorder, title);
		list.setBorder(combined);
		listScrollPane.setBorder(outerBorder);
		add(list, BorderLayout.CENTER);
		
		JPanel welcome = new JPanel(new BorderLayout());
		welcomeLabel = new JLabel();
		welcome.add(welcomeLabel, BorderLayout.NORTH);
		welcome.add(topButtons, BorderLayout.CENTER);
		
		add(welcome, BorderLayout.NORTH);
		topButtons.addButton.addActionListener(this);
		topButtons.editButton.addActionListener(this);
		topButtons.deleteButton.addActionListener(this);
		topButtons.resetButton.addActionListener(this);
		topButtons.newCourseButton.addActionListener(this);

		disableTopButtons();

		bottomButton.logoutButton.addActionListener(this);
		bottomButton.quitButton.addActionListener(this);
		bottomButton.passwordButton.addActionListener(this);
		add(bottomButton, BorderLayout.SOUTH);
		updateUserTable();
		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			listScrollPane.setPreferredSize(new Dimension(510, 470));
		} else {
			listScrollPane.setPreferredSize(new Dimension(510, 500));
		}
	}

	public void disableTopButtons() {
		topButtons.editButton.setEnabled(false);
		topButtons.deleteButton.setEnabled(false);
		topButtons.resetButton.setEnabled(false);
	}

	/**
	 * Updates the user table any time it changes.
	 */
	public void updateUserTable() {
		welcomeLabel.setText("Welcome: " + quizSystem.getCurrentUserFullName());
		welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		if (quizSystem != null) {
			String[] users = quizSystem.getUserList();
			listModel.removeAllElements();
			for (String string : users) {
				listModel.addElement(string);
			}
		}
	}

	/**
	 * Administrator panel actions.
	 * 
	 * @param e
	 *            the action selected
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == topButtons.addButton) {
			addOrEditUser(null);
		}

		if (e.getSource() == bottomButton.quitButton) {
			tracker.windowClosing(null);
		}

		if (e.getSource() == bottomButton.passwordButton) {
			new ChangePassword(quizSystem, tracker);
		}

		if (e.getSource() == bottomButton.logoutButton) {
			tracker.logoutRequested();
		}
		if (e.getSource() == newUserOKButton) {
			try {
				if (userList.getSelectedIndex() == -1) {
					quizSystem.addUser((UserType) selectRole.getSelectedItem(), newFirstName.getText(),
							newLastName.getText(), newUsername.getText(), courseSelection);
				} else {
					quizSystem.updateUser((UserType) selectRole.getSelectedItem(), newFirstName.getText(),
							newLastName.getText(), newUsername.getText(), courseSelection, oldUsername);
				}
				updateUserTable();
				tracker.getDeptHeadPane().setNeedsUpdate();
				tracker.getFacultyPane().setNeedsUpdate();
				changeUser.dispose();
			} catch (IllegalArgumentException error) {
				JOptionPane.showMessageDialog(this, error.getMessage());
			}
		}
		if (e.getSource() == newUserCancelInputButton) {
			changeUser.dispose();
			userList.clearSelection();
		}
		if (e.getSource() == topButtons.editButton) {
			addOrEditUser(userList.getSelectedValue());
			disableTopButtons();
		}
		if (e.getSource() == topButtons.resetButton) {
			try {
				quizSystem.setNeedsNewPassword(userList.getSelectedValue());
			} catch (IllegalArgumentException error) {
				// user didn't select anything
			}
			disableTopButtons();
			userList.clearSelection();
			updateUserTable();
		}
		if (e.getSource() == topButtons.deleteButton) {
			Object[] options = { "OK", "Cancel" };
			int delete = JOptionPane.showOptionDialog(this, "Are you sure you want to delete this user?", "Warning",
					JOptionPane.OK_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
			if (delete == JOptionPane.YES_OPTION) {
				try {
					quizSystem.deleteUser(userList.getSelectedValue());
				} catch (IllegalArgumentException error) {
					if (error.getMessage().equals("Can not remove administrator")) {
						JOptionPane.showMessageDialog(this, error.getMessage());
					}
				}
			}
			disableTopButtons();
			userList.clearSelection();
			updateUserTable();
		}
		if (e.getSource() == topButtons.newCourseButton) {
			addCourse();
			disableTopButtons();
			userList.clearSelection();
		}
		if (e.getSource() == newCourseOKButton) {
			try {
				quizSystem.addNewCourse((String) newCourseDept.getSelectedItem(), newCourseNum.getText(),
						newCourseSection.getText(), newCourseDescription.getText(),
						(String) newCourseTerm.getSelectedItem(), (String) newCourseYear.getSelectedItem());
				JOptionPane.showMessageDialog(this, "New Course Successfully Added");
				newCourse.dispose();
			} catch (IllegalArgumentException error) {
				JOptionPane.showMessageDialog(this, error.getMessage());
			}
		}
		if (e.getSource() == newCourseCancelInputButton) {
			newCourse.dispose();
		}
	}

	/**
	 * Draws the popup dialog for entering a new user.
	 */
	private void addOrEditUser(String userInfo) {
		changeUser = new JFrame("Enter user's information");
		changeUser.setSize(450, 350);

		editUser = new JPanel(new BorderLayout());

		JPanel userDetailsDialog = new JPanel(new GridLayout(4, 1));

		newUsername = new TextField(38);
		newFirstName = new TextField(38);
		newLastName = new TextField(38);

		JPanel firstName = new JPanel(new FlowLayout());
		firstName.add(new JLabel("         First: "));
		firstName.add(newFirstName);

		JPanel lastName = new JPanel(new FlowLayout());
		lastName.add(new JLabel("         Last: "));
		lastName.add(newLastName);

		JPanel userName = new JPanel(new FlowLayout());
		userName.add(new JLabel("Username: "));
		userName.add(newUsername);

		JPanel userType = new JPanel(new FlowLayout());
		JLabel limitTitle = new JLabel("User Role: ");
		selectRole = new JComboBox<UserType>(UserType.values());

		userType.add(limitTitle);
		userType.add(selectRole);

		userDetailsDialog.add(firstName);
		userDetailsDialog.add(lastName);
		userDetailsDialog.add(userName);
		userDetailsDialog.add(userType);

		JPanel buttons = new JPanel(new FlowLayout());
		buttons.add(newUserOKButton);
		buttons.add(newUserCancelInputButton);

		editUser.add(buttons, BorderLayout.SOUTH);
		editUser.add(userDetailsDialog, BorderLayout.NORTH);

		changeUser.add(editUser);

		JRootPane rootPane = SwingUtilities.getRootPane(newUserOKButton);
		rootPane.setDefaultButton(newUserOKButton);

		changeUser.setLocation(100, 100);
		String userParameters[] = null;
		if (userInfo != null) {
			userParameters = userInfo.split("\\t *");
			oldUsername = userParameters[0];
			newUsername.setText(userParameters[0]);
			//newUsername.setEnabled(false);
			newFirstName.setText(userParameters[1]);
			newLastName.setText(userParameters[2]);
			selectRole.setSelectedItem(UserType.valueOf(userParameters[3]));
			selectRole.setEnabled(false);
		}
		String userId = (userParameters == null ? "" : userParameters[0]);
		courseData = new JPanel(new GridLayout(0, 1));
		courses = new JScrollPane(courseData);
		editUser.add(courses, BorderLayout.CENTER);
		courses.setBorder(new TitledBorder("Course Selection"));
		courseData.setBackground(Color.WHITE);

		updateCourseDeptList(userId);

		selectRole.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateCourseDeptList(newUsername.getText().trim());
			}
		});
		changeUser.setVisible(true);

		// private method starts a new listener every time it's opened,
		// this keeps the extras at bay.
		if (newUserOKButton.getActionListeners().length < 1) {
			newUserOKButton.addActionListener(this);
		}
		if (newUserCancelInputButton.getActionListeners().length < 1) {
			newUserCancelInputButton.addActionListener(this);
		}
	}

	private void updateCourseDeptList(String userId) {
		if (courseData != null) {
			courseData.removeAll();
		}
		if ((UserType) selectRole.getSelectedItem() != UserType.DEPT_HEAD) {
			String courseList[][] = quizSystem.getFullCourseListForUser(userId);
			courseSelection = new ArrayList<JCheckBox>();
			if (courseList != null) {
				for (int i = 0; i < courseList.length; i++) {
					JCheckBox check = new JCheckBox(courseList[i][0]);
					check.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
					if (courseList[i][1].equals("true")) {
						check.setSelected(true);
						// check.setEnabled(false);
					}
					courseData.add(check);
					courseSelection.add(check);
				}
			}
		} else {
			String deptList[][] = quizSystem.getFullDeptSelectionListForUser(userId);
			courseSelection = new ArrayList<JCheckBox>();
			if (deptList != null) {
				for (int i = 0; i < deptList.length; i++) {
					JCheckBox check = new JCheckBox(deptList[i][0]);
					check.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
					if (deptList[i][1].equals("true")) {
						check.setSelected(true);
						// check.setEnabled(false);
					}
					courseData.add(check);
					courseSelection.add(check);
				}
			}
		}
		changeUser.revalidate();
		changeUser.repaint();
	}

	/**
	 * Draws the popup dialog for entering a new user.
	 */
	private void addCourse() {
		newCourse = new JDialog((JFrame) this.getRootPane().getParent(), "Enter course information");
		newCourse.setSize(600, 400);

		JPanel courseDetailsDialog = new JPanel(new GridLayout(4, 1));

		newCourseDept = new JComboBox<String>(quizSystem.getDeptList());
		newCourseDept.setEditable(true);
		newCourseNum = new TextField(8);
		newCourseSection = new TextField(8);
		newCourseDescription = new TextField(45);
		newCourseTerm = new JComboBox<String>(TERMS);
		newCourseYear = new JComboBox<String>(YEARS);
		newCourseYear.setEditable(true);

		JPanel topRow = new JPanel(new FlowLayout());
		topRow.add(new JLabel("Department: "));
		topRow.add(newCourseDept);

		topRow.add(new JLabel("Number: "));
		topRow.add(newCourseNum);

		topRow.add(new JLabel("Section: "));
		topRow.add(newCourseSection);

		JPanel courseDesc = new JPanel(new FlowLayout());
		courseDesc.add(new JLabel("Description:"));
		courseDesc.add(newCourseDescription);

		JPanel bottomRow = new JPanel(new FlowLayout());
		bottomRow.add(new JLabel("Term:"));
		bottomRow.add(newCourseTerm);

		bottomRow.add(new JLabel("Year:"));
		bottomRow.add(newCourseYear);

		courseDetailsDialog.add(topRow);
		courseDetailsDialog.add(courseDesc);
		courseDetailsDialog.add(bottomRow);

		JPanel buttons = new JPanel(new FlowLayout());
		buttons.add(newCourseOKButton);
		buttons.add(newCourseCancelInputButton);

		courseDetailsDialog.add(buttons);

		newCourse.add(courseDetailsDialog);
		newCourse.setLocation(100, 100);
		newCourse.setVisible(true);

		JRootPane rootPane = SwingUtilities.getRootPane(newCourseOKButton);
		rootPane.setDefaultButton(newCourseOKButton);
		// private method starts a new listener every time it's opened,
		// this keeps the extras at bay.
		if (newCourseOKButton.getActionListeners().length < 1) {
			newCourseOKButton.addActionListener(this);
		}
		if (newCourseCancelInputButton.getActionListeners().length < 1) {
			newCourseCancelInputButton.addActionListener(this);
		}
	}

	/**
	 * Generates the buttons for the top of view
	 * 
	 * @author Matthew Johnson
	 */
	private class TopButtons extends JPanel {
		/** serial ID */
		private static final long serialVersionUID = 1L;
		private JButton addButton = new JButton("Add User");
		private JButton editButton = new JButton("Edit User");
		private JButton deleteButton = new JButton("Delete Selected");
		private JButton resetButton = new JButton("Reset Password");
		private JButton newCourseButton = new JButton("Add Course");

		/**
		 * Constructs the panel containing top buttons in the GUI.
		 */
		public TopButtons() {
			super();
			JPanel buttonPanel = new JPanel(new GridLayout(2, 3));
			buttonPanel.add(addButton);
			buttonPanel.add(deleteButton);
			buttonPanel.add(resetButton);
			buttonPanel.add(editButton);
			buttonPanel.add(new JPanel());
			buttonPanel.add(newCourseButton);
			add(buttonPanel);
			setVisible(true);
		}
	}
}
