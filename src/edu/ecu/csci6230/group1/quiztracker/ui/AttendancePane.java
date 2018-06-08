package edu.ecu.csci6230.group1.quiztracker.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import edu.ecu.csci6230.group1.quiztracker.controller.Controller;

public class AttendancePane extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private Controller quizSystem;
	private QuizTrackerGUI tracker;

	private JButton submitButton;
	private JButton cancelButton;
	private JButton doneButton;
	private JButton launchCalander;
	private BottomButtons bottomButton;
	private JTextField date;
	private JScrollPane studentList;
	private JPanel studentDataPanel;
	private ArrayList<JCheckBox> marks;
	private DatePicker dp;
	private String selectedCourse = "";

	public AttendancePane(Controller quizSystem, QuizTrackerGUI tracker) {
		super(new BorderLayout());
		this.quizSystem = quizSystem;
		this.tracker = tracker;

		marks = new ArrayList<JCheckBox>();

		bottomButton = new BottomButtons();
		submitButton = new JButton("Submit");
		cancelButton = new JButton("Cancel");
		doneButton = new JButton("Done");
		JPanel topButtons = new JPanel(new FlowLayout());
		topButtons.add(submitButton);
		topButtons.add(cancelButton);
		topButtons.add(doneButton);

		add(topButtons, BorderLayout.NORTH);
		add(bottomButton, BorderLayout.SOUTH);

		JPanel middleSection = new JPanel(new BorderLayout());
		date = new JTextField(10);
		JPanel middleTop = new JPanel(new FlowLayout());
		middleTop.add(new JLabel("Date"));
		middleTop.add(date);
		middleSection.add(middleTop, BorderLayout.NORTH);
		studentDataPanel = new JPanel(new GridLayout(0, 1));
		studentList = new JScrollPane(studentDataPanel);
		middleSection.add(studentList, BorderLayout.CENTER);
		add(middleSection, BorderLayout.CENTER);

		launchCalander = new JButton();
		try {
			Image img = ImageIO.read(getClass().getResource("datepicker.gif"));
			launchCalander.setIcon(new ImageIcon(img));
		} catch (IOException ex) {
		}
		middleTop.add(launchCalander);
		launchCalander.addActionListener(this);

		dp = new DatePicker();

		bottomButton.logoutButton.addActionListener(this);
		bottomButton.quitButton.addActionListener(this);
		bottomButton.passwordButton.addActionListener(this);
		cancelButton.addActionListener(this);
		doneButton.addActionListener(this);
		submitButton.addActionListener(this);

		updateStudentList();
	}

	private void updateStudentList() {
		String studentList[] = quizSystem.getStudentListForCourse(selectedCourse);
		studentDataPanel.removeAll();
		marks.clear();
		for (String student : studentList) {
			JCheckBox check = new JCheckBox(student);
			check.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
			studentDataPanel.add(check);
			marks.add(check);
		}
	}

	public void updateView() {
		updateStudentList();
		AttendancePane.this.revalidate();
		AttendancePane.this.repaint();
	}

	public void setSelectedCourse(String selectedCourse) {
		this.selectedCourse = selectedCourse;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == launchCalander) {
			dp.setDate(date.getText());
			dp.popupShow(launchCalander);
			dp.addPopupListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					date.setText(dp.getFormattedDate());
					dp.popupHide();
				}
			});
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

		if (e.getSource() == submitButton) {
			String dateString = date.getText().trim();
			try {
				try {
					SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
					dateFormat.parse(dateString);
				} catch (ParseException ex) {
					throw new IllegalArgumentException("Invalid date");
				}
				JCheckBox currentMarks[] = marks.toArray(new JCheckBox[marks.size()]);

				quizSystem.markAttendance(date.getText(), currentMarks, selectedCourse);
			} catch (IllegalArgumentException error) {
				JOptionPane.showMessageDialog(this, error.getMessage());
			}
			date.setText("");
			for (JCheckBox jCheckBox : marks) {
				jCheckBox.setSelected(false);
			}
			tracker.getFacultyPane().setNeedsUpdate();
			tracker.getDeptHeadPane().setNeedsUpdate();
		}

		if (e.getSource() == cancelButton) {
			date.setText("");
			for (JCheckBox jCheckBox : marks) {
				jCheckBox.setSelected(false);
			}
		}
		if (e.getSource() == doneButton) {
			tracker.showFacultyPane();
		}
	}
}
