package edu.ecu.csci6230.group1.quiztracker.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import edu.ecu.csci6230.group1.quiztracker.controller.Controller;
import edu.ecu.csci6230.group1.quiztracker.users.UserType;

public class DeptHeadPane extends JPanel implements ActionListener {
	/** Serial id for object */
	private static final long serialVersionUID = 1L;
	/** Panel for bottom elements */
	private JPanel bottomElements = new JPanel();
	/** Panel to contain at-home movie list */
	private JPanel quizPanel;
	/** Panel to movie queue */
	/** Contains the top portion of the panel */
	private JPanel top;

	/** Contains the bottom panel features */
	protected BottomButtons bottomButton;
	/** Return selected movie button */

	/** Container for the quiz list */
	private DefaultListModel<String> quizListModel;
	/** The quiz list */
	private JList<String> quizList;
	/** Scroll panel for the quiz list */
	private JScrollPane quizListScrollPane;

	/** Border to center lists better (addresses cross platform issues) */
	private Border listBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);

	private JLabel stats;
	protected JPanel statistics;
	protected Controller quizSystem;
	protected QuizTrackerGUI tracker;

	private JScrollPane attendance;
	private JTextPane attendanceData;
	protected JComboBox courseSelection;
	private DefaultComboBoxModel<String> courseList;

	private boolean needsUpdate = true;
	protected String currentCourse = "";
	private JLabel welcomeLabel;
	
	protected JPanel attendanceStatistics;
	private JLabel attendanceStats;

	public DeptHeadPane(Controller quizSystem, QuizTrackerGUI tracker) {
		super(new BorderLayout());
		this.quizSystem = quizSystem;
		this.tracker = tracker;
		Border outerBorder = BorderFactory.createEmptyBorder(0, 10, 0, 10);
		this.setBorder(outerBorder);

		courseList = new DefaultComboBoxModel<String>();
		top = new JPanel(new BorderLayout());
		courseSelection = new JComboBox(courseList);
		courseSelection.addActionListener(this);

		JPanel welcome = new JPanel(new GridLayout(2,1));
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
		quizList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

		top.add(quizPanel, BorderLayout.CENTER);
		top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
		add(top, BorderLayout.NORTH);

		statistics = new JPanel(new BorderLayout());
		stats = new JLabel("<html><center>Overall<br>Quiz Average<br>Average</center></html>");
		stats.setPreferredSize(new Dimension(120, 20));
		statistics.add(stats, BorderLayout.CENTER);
		statistics.add(new JPanel(), BorderLayout.NORTH);

		top.add(statistics, BorderLayout.EAST);

		bottomButton = new BottomButtons();
		bottomElements.add(bottomButton);
		bottomButton.logoutButton.addActionListener(this);
		bottomButton.quitButton.addActionListener(this);
		bottomButton.passwordButton.addActionListener(this);
		add(bottomElements, BorderLayout.SOUTH);

		
		attendanceStatistics = new JPanel(new BorderLayout());
		attendanceStats = new JLabel();
		
		attendanceData = new JTextPane();
		attendance = new JScrollPane(attendanceData);
		attendance.setBorder(new TitledBorder("Attendance Summary"));
		attendanceStatistics.add(attendance, BorderLayout.CENTER);
		attendanceStatistics.add(attendanceStats, BorderLayout.EAST);
		add(attendanceStatistics, BorderLayout.CENTER);

		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			quizListScrollPane.setPreferredSize(new Dimension(390, 220));
			quizPanel.setPreferredSize(new Dimension(400, 250));
		} else {
			quizListScrollPane.setPreferredSize(new Dimension(400, 220));
			quizPanel.setPreferredSize(new Dimension(400, 250));
		}
		updateView();
	}

	/**
	 * Updates the aggregate quiz table any time it changes.
	 */
	private void updateQuizTable() {
		if (quizSystem != null && currentCourse != null) {
			String[] quizzes = quizSystem.getAggregateGradedQuizList(currentCourse);
			quizListModel.removeAllElements();
			for (String string : quizzes) {
				quizListModel.addElement(string);
			}
		}
	}

	public void addCourseList() {
		if (quizSystem.getCurrentUser() != null) {
			courseList.removeAllElements();
			if (quizSystem.getCurrentUser().getUserRole() != UserType.DEPT_HEAD) {
				String courses[] = quizSystem.getEnrolledCoursesForUser(quizSystem.getCurrentUser().getUserId());
				for (String course : courses) {
					courseList.addElement(course);
				}
			} else {
				String courses[] = quizSystem.getDeptHeadCourseList(quizSystem.getCurrentUser().getUserId());
				for (String course : courses) {
					courseList.addElement(course);
				}
			}
			currentCourse = (String) courseSelection.getSelectedItem();
		}
		updateView();
	}

	public void updateView() {
		welcomeLabel.setText("Welcome: " + quizSystem.getCurrentUserFullName());
		welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
		if (currentCourse != null) {
			updateQuizTable();

			stats.setText("<html><center>Overall<br>Quiz Average<br>" + quizSystem.getAggregateQuizMean(currentCourse)
					+ "</center></html>");
			stats.setHorizontalAlignment(SwingConstants.CENTER);
	
			String attendanceList[][] = quizSystem.getAttendanceSummary(currentCourse);
			
			if (attendanceList != null && attendanceList.length > 1 && needsUpdate) {
				
				attendanceStats.setText("<html><center>Average<br>Attendance<br>" + quizSystem.getAverageAttendance(attendanceList)
				+ "</center></html>");
				attendanceStats.setHorizontalAlignment(SwingConstants.CENTER);
				
				attendanceData.setText("");
				attendanceData.setContentType("text/html");
				StringBuilder data = new StringBuilder();
				data.append("<html><table border=\"1\"><font face=\"courier\" size=\"0\"><tr>");
				for(int i = 0; i < attendanceList[0].length; i++){
					data.append("<th>").append(attendanceList[0][i].substring(5, attendanceList[0][i].length())).append("</th>");
				}
				data.append("</font></tr>");
				for(int i = 1; i < attendanceList.length; i++){
					String name[] = attendanceList[i][0].split("\\t *");
					data.append("<font face=\"courier\" size=\"0\"><tr>");
					data.append("<td nowrap>").append(name[1] + " " + name[2]).append("</td>");
					for(int k = 1; k < attendanceList[0].length; k++){
						data.append("<td><center>").append(attendanceList[i][k].equals("true")?"X":"").append("</center></td>");
					}
					data.append("</font></tr>");
				}
				data.append("</table></html>");
				attendanceData.setText(data.toString());
				attendanceData.setCaretPosition(0);
				needsUpdate = false;
			} else if (needsUpdate) {
				attendanceStats.setText("<html><center>Average<br>Attendance<br>0%</center></html>");
				attendanceStats.setHorizontalAlignment(SwingConstants.CENTER);
				attendanceData.setText("\nNo data");
				needsUpdate = false;
			}
		}
		DeptHeadPane.this.revalidate();
		DeptHeadPane.this.repaint();
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
		if (e.getSource() == courseSelection) {
			currentCourse = (String) courseSelection.getSelectedItem();
			setNeedsUpdate();
		}
		updateView();
	}

	public void setNeedsUpdate() {
		needsUpdate = true;
	}

}
