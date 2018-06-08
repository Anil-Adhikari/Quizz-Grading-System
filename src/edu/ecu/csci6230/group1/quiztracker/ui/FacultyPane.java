package edu.ecu.csci6230.group1.quiztracker.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import edu.ecu.csci6230.group1.quiztracker.controller.Controller;
import edu.ecu.csci6230.group1.quiztracker.users.UserType;

public class FacultyPane extends DeptHeadPane implements ActionListener {

	private static final long serialVersionUID = 1L;
	private final JButton uploadQuiz = new JButton("<html><center>Upload New<br>Quiz</center></html>");;
	private JButton gradeQuizzes;
	private final JButton attendance = new JButton("<html><center>Record New<br>Attendance</html></center>");
	private final JPanel buttons;

	public FacultyPane(Controller quizSystem, QuizTrackerGUI tracker) {
		super(quizSystem, tracker);
		gradeQuizzes = new JButton("<html><center>Get Course<br>Student List</center></html>");
		buttons = new JPanel(new GridLayout(3, 1));
		buttons.add(uploadQuiz);
		buttons.add(gradeQuizzes);
		buttons.add(attendance);
		super.statistics.add(buttons, BorderLayout.NORTH);
		uploadQuiz.addActionListener(this);
		gradeQuizzes.addActionListener(this);
		attendance.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == super.bottomButton.quitButton) {
			tracker.windowClosing(null);
		}

		if (e.getSource() == super.bottomButton.passwordButton) {
			new ChangePassword(quizSystem, tracker);
		}

		if (e.getSource() == bottomButton.logoutButton) {
			tracker.logoutRequested();
		}
		if (e.getSource() == uploadQuiz) {
			new QuizUploader();
		}
		if (e.getSource() == attendance) {
			tracker.getAttendancePane().setSelectedCourse(currentCourse);
			tracker.showAttendancePane();
		}
		if (e.getSource() == gradeQuizzes) {
			tracker.getStudentListPane().setCurrentCourse(currentCourse);
			tracker.getStudentListPane().updateUserTable();
			tracker.showStudentListPane();
		}
		if (e.getSource() == courseSelection) {
			currentCourse = (String) courseSelection.getSelectedItem();
			setNeedsUpdate();
		}
		updateView();
		super.revalidate();
		super.repaint();
	}

	public void setUser() {
		if (quizSystem.getCurrentUser().getUserRole() == UserType.TA)
			uploadQuiz.setEnabled(false);
		else if (quizSystem.getCurrentUser().getUserRole() == UserType.INSTRUCTOR)
			uploadQuiz.setEnabled(true);
	}

	private String getUploadFileName() {
		JFileChooser fc = new JFileChooser("./");
		int returnVal = fc.showOpenDialog(this);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return "";
		}
		File quizFile = fc.getSelectedFile();
		return quizFile.getAbsolutePath();
	}

	private class QuizUploader implements ActionListener {

		private JDialog newQuizDialog;
		/** OK button for the new user dialog */
		private final JButton oKButton = new JButton("OK");
		/** cancel button for the new user dialog */
		private final JButton cancelButton = new JButton("Cancel");
		private final JButton selectFile = new JButton("Select File");
		private JTextField quizName;
		private JTextField quizValue;
		private JTextField filePath;
		private JTextField dateField;
		private JButton launchCalander;
		private DatePicker dp;

		public QuizUploader() {
			/** Dialog popup for entering a new user */
			newQuizDialog = new JDialog((JFrame) tracker.getRootPane().getParent(), "Add a Quiz");

			quizName = new JTextField(25);
			quizValue = new JTextField(25);
			filePath = new JTextField(25);
			dateField = new JTextField(25);
			newQuizDialog.setSize(450, 220);
			JPanel dialog = new JPanel();
			dialog.setLayout(new GridLayout(5, 1));

			JPanel file = new JPanel(new FlowLayout());
			file.add(selectFile);
			selectFile.addActionListener(this);
			file.add(filePath);
			dialog.add(file);

			JPanel name = new JPanel(new FlowLayout());
			name.add(new JLabel("Quiz Name"));
			name.add(quizName);
			dialog.add(name);

			JPanel value = new JPanel(new FlowLayout());
			value.add(new JLabel("Quiz Value"));
			value.add(quizValue);
			dialog.add(value);

			JPanel dueDate = new JPanel(new FlowLayout());
			dueDate.add(new JLabel("Due Date"));
			dueDate.add(dateField);

			launchCalander = new JButton();
			try {
				Image img = ImageIO.read(getClass().getResource("datepicker.gif"));
				launchCalander.setIcon(new ImageIcon(img));
			} catch (IOException ex) {
			}
			dueDate.add(launchCalander);
			launchCalander.addActionListener(this);

			dp = new DatePicker();

			dialog.add(dueDate);

			JPanel buttons = new JPanel(new FlowLayout());
			buttons.add(oKButton);
			buttons.add(cancelButton);
			cancelButton.addActionListener(this);
			dialog.add(buttons);
			newQuizDialog.add(dialog);
			JRootPane rootPane = SwingUtilities.getRootPane(oKButton);
			rootPane.setDefaultButton(oKButton);
			newQuizDialog.setLocation(100, 100);
			newQuizDialog.setVisible(true);

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
			if (e.getSource() == launchCalander) {
				dp.setDate(dateField.getText());
				dp.popupShow(launchCalander);
				dp.addPopupListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dateField.setText(dp.getFormattedDate());
						dp.popupHide();
					}
				});
			}
			if (e.getSource() == selectFile) {
				filePath.setText(getUploadFileName());
			}
			if (e.getSource() == oKButton) {
				try {
					String fileName = filePath.getText().trim();
					String name = quizName.getText().trim();
					String date = dateField.getText().trim();
					if (fileName.isEmpty() || name.isEmpty() || date.isEmpty()) {
						throw new IllegalArgumentException("Date, Filename and Quiz name must not be blank");
					}
					int value = Integer.parseInt(quizValue.getText().trim());
					if (value < 1) {
						throw new NumberFormatException();
					}
					try {
						SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
						dateFormat.parse(date);
					} catch (ParseException ex) {
						throw new IllegalArgumentException("Invalid date");
					}
					quizSystem.assignQuiz(currentCourse, name, value, fileName, date);
					newQuizDialog.dispose();
					updateView();
				} catch (NumberFormatException numberError) {
					JOptionPane.showMessageDialog(tracker, "Not a valid quiz value!");
				} catch (IllegalArgumentException stringError) {
					JOptionPane.showMessageDialog(tracker, stringError.getMessage());
				}
			}
			if (e.getSource() == cancelButton) {
				newQuizDialog.dispose();
			}
		}
	}
}
