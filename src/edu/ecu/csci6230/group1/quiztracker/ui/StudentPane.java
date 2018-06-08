package edu.ecu.csci6230.group1.quiztracker.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.ecu.csci6230.group1.quiztracker.controller.Controller;
import edu.ecu.csci6230.group1.quiztracker.users.Student;

public class StudentPane extends JPanel implements ActionListener {
	/** Serial id for object */
	private static final long serialVersionUID = 1L;
	/** Panel for bottom elements */
	private JPanel bottomElements = new JPanel();
	/** Panel to contain at-home movie list */
	private JPanel quizPanel;
	/** Panel to movie queue */
	private JPanel commentsPanel;
	/** Contains the top portion of the panel */
	private JPanel top;

	private JPanel bottom;

	/** Contains the bottom panel features */
	private BottomButtons bottomButton;
	/** Return selected movie button */

	/** Container for the quiz list */
	private DefaultListModel<String> quizListModel;
	/** The quiz list */
	private JList<String> quizList;
	/** Scroll panel for the quiz list */
	private JScrollPane quizListScrollPane;
	/** Container for the comments */
	private JTextArea comments;
	/** Scroll panel for the comments */
	private JScrollPane commentsWindow;
	/** Border to center lists better (addresses cross platform issues) */
	private Border listBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);

	private JScrollPane attendance;
	private JPanel attendanceData;

	private JLabel stats;
	private JButton uploadButton;
	private JButton downloadButton;
	private Controller quizSystem;
	private QuizTrackerGUI tracker;

	private JComboBox courseSelection;
	private DefaultComboBoxModel<String> courseList;
	private String currentCourse = "";
	private JLabel welcomeLabel;

	public StudentPane(Controller quizSystem, QuizTrackerGUI tracker) {
		super(new BorderLayout());
		this.quizSystem = quizSystem;
		this.tracker = tracker;
		Border outerBorder = BorderFactory.createEmptyBorder(0, 10, 0, 10);
		this.setBorder(outerBorder);

		top = new JPanel(new BorderLayout());

		courseList = new DefaultComboBoxModel<String>();
		top = new JPanel(new BorderLayout());
		courseSelection = new JComboBox(courseList);
		courseSelection.addActionListener(this);

		JPanel welcome = new JPanel(new GridLayout(2, 1));
		welcomeLabel = new JLabel();
		welcome.add(welcomeLabel);
		welcome.add(courseSelection);
		top.add(welcome, BorderLayout.NORTH);

		quizListModel = new DefaultListModel<String>();
		quizList = new JList<String>(quizListModel);
		quizListScrollPane = new JScrollPane(quizList);

		quizListScrollPane.setBorder(listBorder);
		quizPanel = new JPanel();
		quizPanel.setBorder(new TitledBorder("Quiz List"));
		quizPanel.add(quizListScrollPane);
		quizList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));

		JPanel fileButtons = new JPanel(new GridLayout(3, 1));
		uploadButton = new JButton("<html><center>Upload<br>Quiz</center></html>");
		downloadButton = new JButton("<html><center>Download<br>Selected</center></html>");
		JPanel uploadLayout = new JPanel(new FlowLayout());
		JPanel downloadLayout = new JPanel(new FlowLayout());
		downloadLayout.add(downloadButton);
		uploadLayout.add(uploadButton);
		fileButtons.add(uploadLayout);
		fileButtons.add(downloadLayout);

		JPanel statistics = new JPanel(new FlowLayout());
		stats = new JLabel("<html><center>Quiz Average<br>Average</center></html>");
		statistics.add(stats);
		fileButtons.add(statistics);

		uploadButton.addActionListener(this);
		downloadButton.addActionListener(this);

		top.add(quizPanel, BorderLayout.CENTER);
		top.add(fileButtons, BorderLayout.EAST);

		top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
		add(top, BorderLayout.NORTH);

		bottom = new JPanel(new BorderLayout());

		comments = new JTextArea();
		comments.setEditable(false);
		commentsWindow = new JScrollPane(comments);
		commentsWindow.setBorder(listBorder);
		commentsPanel = new JPanel();
		Border queueTop = BorderFactory.createEmptyBorder(10, 0, 0, 0);
		Border queueTitle = new TitledBorder("Quiz Comments");
		commentsPanel.setBorder(BorderFactory.createCompoundBorder(queueTop, queueTitle));
		commentsPanel.add(commentsWindow);

		bottom.add(commentsPanel, BorderLayout.CENTER);
		add(bottom, BorderLayout.CENTER);

		quizList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				if (!arg0.getValueIsAdjusting()) {
					if (quizList.getSelectedIndex() > -1)
						comments.setText(quizSystem.getQuizComments(quizList.getSelectedValue(), currentCourse));
				}
			}
		});

		attendanceData = new JPanel(new GridLayout(0, 1));
		attendanceData.setBackground(Color.WHITE);
		attendance = new JScrollPane(attendanceData);
		bottom.add(attendance, BorderLayout.EAST);
		attendance.setBorder(new TitledBorder("Attendance"));

		bottomButton = new BottomButtons();
		bottomElements.add(bottomButton);
		bottomButton.logoutButton.addActionListener(this);
		bottomButton.quitButton.addActionListener(this);
		bottomButton.passwordButton.addActionListener(this);
		add(bottomElements, BorderLayout.SOUTH);

		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			quizListScrollPane.setPreferredSize(new Dimension(385, 245));
			quizPanel.setPreferredSize(new Dimension(400, 280));
			commentsWindow.setPreferredSize(new Dimension(385, 210));
		} else {
			quizListScrollPane.setPreferredSize(new Dimension(385, 245));
			quizPanel.setPreferredSize(new Dimension(400, 280));
			commentsWindow.setPreferredSize(new Dimension(385, 265));
		}
		updateView();
	}

	public void addCourseList() {
		if (quizSystem.getCurrentUser() != null) {
			courseList.removeAllElements();
			String courses[] = quizSystem.getEnrolledCoursesForUser(quizSystem.getCurrentUser().getUserId());
			for (String course : courses) {
				courseList.addElement(course);
			}
			currentCourse = (String) courseSelection.getSelectedItem();
		}
		updateView();
		StudentPane.this.revalidate();
		StudentPane.this.repaint();
	}

	/**
	 * Updates the students quiz table any time it changes.
	 */
	private void updateQuizTable() {
		if (quizSystem != null && currentCourse != null) {
			String[] quizzes = quizSystem.getStudenQuizList(currentCourse, (Student) quizSystem.getCurrentUser());
			quizListModel.removeAllElements();
			for (String string : quizzes) {
				quizListModel.addElement(string);
			}
		}
	}

	/**
	 * Gets the filename of a movie file for the system.
	 * 
	 * Code being reused from BugManagerGUI
	 * 
	 * @author Matthew Johnson
	 */
	private String getUploadFileName() {
		JFileChooser fc = new JFileChooser("./");
		int returnVal = fc.showOpenDialog(this);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return "";
		}
		File quizFile = fc.getSelectedFile();
		return quizFile.getAbsolutePath();
	}

	private String getDownloadFileName(String fileName) {
		JFileChooser fc = new JFileChooser("./");
		fc.setSelectedFile(new File(fileName));
		int returnVal = fc.showSaveDialog(this);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return "";
		}
		File quizFile = fc.getSelectedFile();
		return quizFile.getAbsolutePath();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == bottomButton.quitButton) {
			tracker.windowClosing(null);
		}
		if (e.getSource() == bottomButton.passwordButton) {
			new ChangePassword(quizSystem, tracker);
		}
		if (e.getSource() == bottomButton.logoutButton) {
			tracker.logoutRequested();
		}
		if (e.getSource() == uploadButton) {
			try {
				if (quizList.getSelectedIndex() == -1) {
					JOptionPane.showMessageDialog(this, "Please select an assignment to submit");
				} else if (!quizSystem.checkDueDate(currentCourse, quizList.getSelectedValue())) {
					throw new IllegalArgumentException("Quiz is past due, submission closed.");
				} else {
					String filePath = getUploadFileName();
					if (!filePath.equals("")) {
						quizSystem.uploadFinishedQuiz(currentCourse, quizList.getSelectedValue(), filePath);
					}
				}
			} catch (IllegalArgumentException ex) {
				JOptionPane.showMessageDialog(this, ex.getMessage());
			}
		}
		if (e.getSource() == downloadButton) {
			if (quizList.getSelectedIndex() == -1)
				JOptionPane.showMessageDialog(this, "Please select an assignment to download");
			else {
				String fileName = quizSystem.getDownloadQuizFileName(quizList.getSelectedValue(), null, currentCourse);
				String filePath = getDownloadFileName(fileName);
				if (!filePath.equals(""))
					try {
						quizSystem.downloadQuiz(quizList.getSelectedValue(), filePath, null, currentCourse);
					} catch (IllegalArgumentException error) {
						JOptionPane.showMessageDialog(this, error.getMessage());
					}
			}
		}
		if (e.getSource() == courseSelection) {
			currentCourse = (String) courseSelection.getSelectedItem();
			updateQuizTable();
		}
		updateView();
		StudentPane.this.revalidate();
		StudentPane.this.repaint();
	}

	public void updateView() {
		if (currentCourse != null && quizSystem.getCurrentUser() != null) {
			welcomeLabel.setText("Welcome: " + quizSystem.getCurrentUserFullName());
			welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
			updateQuizTable();
			stats.setText("<html><center>Quiz Average<br>"
					+ quizSystem.getQuizAverage(currentCourse, (Student) quizSystem.getCurrentUser())
					+ "</center></html>");
			String attendanceList[][] = quizSystem.getStudentCourseAttendanceList(null, currentCourse);
			attendanceData.removeAll();
			if (attendanceList != null && attendanceList.length > 0) {
				for (int i = 0; i < attendanceList.length; i++) {
					JCheckBox check = new JCheckBox(attendanceList[i][0]);
					check.setEnabled(false);
					check.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
					check.setSelected(attendanceList[i][1].equals("true"));
					attendanceData.add(check);
				}
			} else {
				JCheckBox check = new JCheckBox("No data   ");
				check.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
				check.setSelected(false);
				check.setEnabled(false);
				attendanceData.add(check);
			}
		}
	}
}
