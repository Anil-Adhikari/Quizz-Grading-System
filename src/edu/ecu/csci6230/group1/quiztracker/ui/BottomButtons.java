package edu.ecu.csci6230.group1.quiztracker.ui;

import javax.swing.JButton;
import javax.swing.JPanel;

public class BottomButtons extends JPanel{

	private static final long serialVersionUID = 1L;
	/** Quit Button */
	public final JButton quitButton = new JButton("Quit");
	/** logout button */
	public final JButton logoutButton = new JButton("Logout");
	/** Change Password button */
	public final JButton passwordButton = new JButton("Change Password");

	/**
	 * Constructs the panel containing top buttons in the GUI.
	 */
	public BottomButtons() {
		super();

		add(quitButton);
		add(logoutButton);
		add(passwordButton);
		setVisible(true);
	}
}
