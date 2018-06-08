package edu.ecu.csci6230.group1.quiztracker.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import edu.ecu.csci6230.group1.quiztracker.controller.Controller;

public class StudentListPane extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	/** List model for displaying users */
	private DefaultListModel<String> listModel;
	/** Text for Delete User */
	// private static final String DELETE_USER = "Delete Selected User";
	/** Text for User List */
	private static final String STUDENT_LIST_TEXT = "Student List";
	/** Table for showing movie list */
	private JList<String> userList;
	/** Scroll pane for the movie list */
	private JScrollPane listScrollPane;
	/** Panel to contain everything except navigation buttons */
	private JPanel list;

	/** Panel for buttons at top */
	private TopButtons topButtons = new TopButtons();
	private BottomButtons bottomButton = new BottomButtons();

	private Controller quizSystem;
	private QuizTrackerGUI tracker;
	private String currentCourse;
	private TitledBorder title;

	public StudentListPane(Controller quizSystem, QuizTrackerGUI tracker) {
		super(new BorderLayout());
		this.tracker = tracker;
		this.quizSystem = quizSystem;

		list = new JPanel();
		listModel = new DefaultListModel<String>();
		userList = new JList<String>(listModel);
		userList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		updateUserTable();
		listScrollPane = new JScrollPane(userList);
		list.add(listScrollPane);

		title = new TitledBorder(STUDENT_LIST_TEXT + " for " + currentCourse);
		Border outerBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		Border combined = BorderFactory.createCompoundBorder(outerBorder, title);
		list.setBorder(combined);
		listScrollPane.setBorder(outerBorder);
		add(list, BorderLayout.CENTER);
		add(topButtons, BorderLayout.NORTH);
		topButtons.detailsButton.addActionListener(this);
		topButtons.doneButton.addActionListener(this);

		bottomButton.logoutButton.addActionListener(this);
		bottomButton.quitButton.addActionListener(this);
		bottomButton.passwordButton.addActionListener(this);
		add(bottomButton, BorderLayout.SOUTH);
		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			listScrollPane.setPreferredSize(new Dimension(500, 500));
		} else {
			listScrollPane.setPreferredSize(new Dimension(510, 535));
		}
	}

	/**
	 * Updates the user table any time it changes.
	 */
	public void updateUserTable() {
		if (quizSystem != null && quizSystem.getCurrentUser() != null) {
			String[] users = quizSystem.getStudentListForCourse(currentCourse);
			listModel.removeAllElements();
			for (String string : users) {
				listModel.addElement(string);
			}
		}
	}

	public void setCurrentCourse(String currentCourse) {
		title.setTitle(STUDENT_LIST_TEXT + " for " + currentCourse);
		this.currentCourse = currentCourse;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == topButtons.detailsButton) {
			if (userList.getSelectedIndex() == -1)
				JOptionPane.showMessageDialog(this, "Please select a student to view");
			else {
				String detailStudent = userList.getSelectedValue();
				tracker.getStudentDetailsPane().setStudent(detailStudent);
				tracker.getStudentDetailsPane().setCurrentCourse(currentCourse);
				tracker.getStudentDetailsPane().updateView();
				tracker.showStudentDetailsPane();
			}
		}

		if (e.getSource() == topButtons.doneButton) {
			tracker.showFacultyPane();
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
	}

	/**
	 * Generates the buttons for the top of view
	 * 
	 * @author Matthew Johnson
	 */
	private class TopButtons extends JPanel {
		private static final long serialVersionUID = 1L;
		private JButton detailsButton = new JButton("Get Student Details");
		private JButton doneButton = new JButton("Done");

		/**
		 * Constructs the panel containing top buttons in the GUI.
		 */
		public TopButtons() {
			super();

			add(detailsButton);
			add(doneButton);
			setVisible(true);
		}
	}

}
