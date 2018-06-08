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
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.ecu.csci6230.group1.quiztracker.controller.Controller;
import edu.ecu.csci6230.group1.quiztracker.users.UserType;

public class StudentDetailsPane extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JPanel bottomElements = new JPanel();
	private JPanel quizPanel;
	private JPanel commentsPanel;
	private JPanel top;
	private JPanel bottom;

	private BottomButtons bottomButton;

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

	private JLabel stats;
	private JButton gradeSelected;
	private JButton downloadButton;
	private JButton backButton;
	private Controller quizSystem;
	private QuizTrackerGUI tracker;
	private String detailStudent;
	private String currentCourse;

	private JScrollPane attendance;
	private JPanel attendanceData;
	private JLabel studentHeader;

	public StudentDetailsPane(Controller quizSystem, QuizTrackerGUI tracker) {
		super(new BorderLayout());
		this.quizSystem = quizSystem;
		this.tracker = tracker;

		Border outerBorder = BorderFactory.createEmptyBorder(0, 10, 0, 10);
		this.setBorder(outerBorder);

		top = new JPanel(new BorderLayout());

		quizListModel = new DefaultListModel<String>();
		quizList = new JList<String>(quizListModel);
		quizListScrollPane = new JScrollPane(quizList);
		quizListScrollPane.setBorder(listBorder);
		quizPanel = new JPanel();
		quizPanel.setBorder(new TitledBorder("Quiz List"));
		quizPanel.add(quizListScrollPane);
		quizList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));

		JPanel fileButtons = new JPanel(new GridLayout(3, 1));
		gradeSelected = new JButton("<html><center>Grade<br>Selected</center></html>");
		downloadButton = new JButton("<html><center>Download<br>Selected</center></html>");
		JPanel uploadLayout = new JPanel(new FlowLayout());
		JPanel downloadLayout = new JPanel(new FlowLayout());
		downloadLayout.add(downloadButton);
		uploadLayout.add(gradeSelected);
		
		/*This is a bug.
		if (quizSystem.getCurrentUser() != null && quizSystem.getCurrentUser().getUserRole() == UserType.TA) {
			gradeSelected.setEnabled(false);
			downloadButton.setEnabled(false);
		}
		
		End this is a bug.*/
		
		
		fileButtons.add(uploadLayout, BorderLayout.NORTH);
		fileButtons.add(downloadLayout, BorderLayout.CENTER);
		gradeSelected.addActionListener(this);
		downloadButton.addActionListener(this);

		JPanel statistics = new JPanel(new FlowLayout());
		stats = new JLabel("<html><center>Quiz Average<br>Average</center></html>");
		statistics.add(stats, BorderLayout.CENTER);
		fileButtons.add(statistics);

		top.add(quizPanel, BorderLayout.CENTER);
		top.add(fileButtons, BorderLayout.EAST);

		top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
		studentHeader = new JLabel();
		top.add(studentHeader, BorderLayout.NORTH);
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
						comments.setText(quizSystem.getQuizComments(quizList.getSelectedValue(), detailStudent, currentCourse));
				}
			}
		});

		attendanceData = new JPanel(new GridLayout(0, 1));
		attendance = new JScrollPane(attendanceData);
		bottom.add(attendance, BorderLayout.EAST);
		attendance.setBorder(new TitledBorder("Attendance"));

		bottomButton = new BottomButtons();
		backButton = new JButton("Back");
		bottomButton.add(backButton);
		backButton.addActionListener(this);
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

	public void setUser() {
		if (quizSystem.getCurrentUser().getUserRole() == UserType.TA) {
			gradeSelected.setEnabled(false);
			downloadButton.setEnabled(false);
		} else if (quizSystem.getCurrentUser().getUserRole() == UserType.INSTRUCTOR) {
			gradeSelected.setEnabled(true);
			downloadButton.setEnabled(true);
		}
	}

	public void setStudent(String student) {
		this.detailStudent = student;
	}

	public void setCurrentCourse(String currentCourse) {
		this.currentCourse = currentCourse;
	}

	/**
	 * Updates the students quiz table any time it changes.
	 */
	private void updateQuizTable() {
		if (quizSystem != null && quizSystem.getCurrentUser() != null) {
			String[] quizzes = quizSystem.getStudenQuizList(currentCourse,
					quizSystem.getSelectedStudent(detailStudent));
			quizListModel.removeAllElements();
			for (String string : quizzes) {
				quizListModel.addElement(string);
			}
		}
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
		if (e.getSource() == backButton) {
			tracker.showStudentListPane();
			tracker.getStudentListPane().updateUserTable();
		}
		if (e.getSource() == gradeSelected) {
			if (quizList.getSelectedIndex() == -1)
				JOptionPane.showMessageDialog(this, "Please select an assignment to grade");
			else {
				new GradeQuiz(quizList.getSelectedValue());
			}
		}
		if (e.getSource() == downloadButton) {
			if (quizList.getSelectedIndex() == -1)
				JOptionPane.showMessageDialog(this, "Please select an assignment to download");
			else {
				String fileName = quizSystem.getDownloadQuizFileName(quizList.getSelectedValue(), detailStudent,
						currentCourse);
				String filePath = getDownloadFileName(fileName);
				if (!filePath.equals(""))
					try {
						quizSystem.downloadQuiz(quizList.getSelectedValue(), filePath, detailStudent, currentCourse);
					} catch (IllegalArgumentException error) {
						JOptionPane.showMessageDialog(this, error.getMessage());
					}
			}
		}
		StudentDetailsPane.this.revalidate();
		StudentDetailsPane.this.repaint();
	}

	public void updateView() {
		studentHeader.setText("<html>" + currentCourse + "<br>" + detailStudent + "</html>");
		updateQuizTable();
		if (quizSystem.getCurrentUser() != null) {
			stats.setText("<html><center>Quiz Average<br>"
					+ quizSystem.getQuizAverage(currentCourse, quizSystem.getSelectedStudent(detailStudent))
					+ "</center></html>");
			String attendanceList[][] = quizSystem.getStudentCourseAttendanceList(detailStudent, currentCourse);
			attendanceData.removeAll();
			if (attendanceList != null && attendanceList.length > 0) {
				for (int i = 0; i < attendanceList.length; i++) {
					JCheckBox check = new JCheckBox(attendanceList[i][0]);
					check.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
					check.setSelected(attendanceList[i][1].equals("true"));
					check.setDisabledIcon(check.getIcon());
					check.setDisabledSelectedIcon(check.getSelectedIcon());
					check.setEnabled(false);
					attendanceData.add(check);
				}
			} else {
				JCheckBox check = new JCheckBox("No data   ");
				check.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
				check.setDisabledIcon(check.getIcon());
				check.setDisabledSelectedIcon(check.getSelectedIcon());
				check.setSelected(false);
				check.setEnabled(false);
				attendanceData.add(check);
			}
		}
	}

	private class GradeQuiz implements ActionListener {

		private JDialog newQuizGradeDialog;
		/** OK button for the new user dialog */
		private final JButton oKButton = new JButton("OK");
		/** cancel button for the new user dialog */
		private final JButton cancelButton = new JButton("Cancel");

		private JTextField quizScore;
		private JTextArea quizComments;
		private String selectedQuiz;

		public GradeQuiz(String selectedQuiz) {
			this.selectedQuiz = selectedQuiz;
			int quizValue = quizSystem.getQuizValue(selectedQuiz, detailStudent, currentCourse);

			newQuizGradeDialog = new JDialog((JFrame) tracker.getRootPane().getParent(), "Grade Quiz");

			quizScore = new JTextField(5);
			quizComments = new JTextArea();
			newQuizGradeDialog.setSize(450, 200);
			JPanel dialog = new JPanel();
			dialog.setLayout(new GridLayout(3, 1));

			JPanel value = new JPanel(new FlowLayout());
			value.add(new JLabel("Quiz Score"));
			value.add(quizScore);
			value.add(new JLabel("/" + quizValue));
			dialog.add(value);

			JScrollPane commentsPane = new JScrollPane(quizComments);
			JPanel commentsPanel = new JPanel(new BorderLayout());
			commentsPanel.add(new JLabel("Quiz Coments"), BorderLayout.NORTH);
			commentsPanel.add(commentsPane, BorderLayout.CENTER);
			commentsPanel.add(new JPanel(new FlowLayout()), BorderLayout.EAST);
			commentsPanel.add(new JPanel(new FlowLayout()), BorderLayout.WEST);

			dialog.add(commentsPanel);

			JPanel buttons = new JPanel(new FlowLayout());
			buttons.add(oKButton);
			buttons.add(cancelButton);
			cancelButton.addActionListener(this);
			dialog.add(buttons);
			newQuizGradeDialog.add(dialog);
			JRootPane rootPane = SwingUtilities.getRootPane(oKButton);
			rootPane.setDefaultButton(oKButton);
			newQuizGradeDialog.setLocation(100, 100);
			newQuizGradeDialog.setVisible(true);
			// private method starts a new listener every time it's opened, this
			// keeps the extras at bay.
			if (oKButton.getActionListeners().length < 1) {
				oKButton.addActionListener(this);
			}
			if (cancelButton.getActionListeners().length < 1) {
				cancelButton.addActionListener(this);
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == oKButton) {
				try {
					String comments = quizComments.getText();
					int value = Integer.parseInt(quizScore.getText());
					if (value < 0) {
						throw new NumberFormatException();
					}
					quizSystem.gradeQuiz(selectedQuiz, detailStudent, currentCourse, value, comments);
					newQuizGradeDialog.dispose();
					updateView();
				} catch (NumberFormatException numberError) {
					JOptionPane.showMessageDialog(tracker, "Not a valid quiz value!");
				} catch (IllegalArgumentException stringError) {
					JOptionPane.showMessageDialog(tracker, stringError.getMessage());
				}
			}
			if (e.getSource() == cancelButton) {
				newQuizGradeDialog.dispose();
			}
		}
	}
}
