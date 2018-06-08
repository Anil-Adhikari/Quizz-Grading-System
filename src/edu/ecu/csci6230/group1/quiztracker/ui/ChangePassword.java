package edu.ecu.csci6230.group1.quiztracker.ui;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import edu.ecu.csci6230.group1.quiztracker.controller.Controller;

public class ChangePassword implements ActionListener {

	private JDialog newPasswordDialog;
	/** OK button for the new user dialog */
	private final JButton newPasswordOKButton = new JButton("OK");
	/** cancel button for the new user dialog */
	private final JButton newPasswordCancelInputButton = new JButton("Cancel");
	private JPasswordField oldPassword;
	private JPasswordField newPassword;

	private Controller quizSystem;
	private QuizTrackerGUI tracker;

	public ChangePassword(Controller quizSystem, QuizTrackerGUI tracker) {
		this.quizSystem = quizSystem;
		this.tracker = tracker;
		/** Dialog popup for entering a new user */
		newPasswordDialog = new JDialog((JFrame) tracker.getRootPane().getParent(),
				"Confirm old password and enter new password, then log in again.");

		oldPassword = new JPasswordField(26);
		newPassword = new JPasswordField(26);
		newPasswordDialog.setSize(500, 200);
		JPanel loginDialog = new JPanel();
		loginDialog.add(new JPanel()); // add a filler
		loginDialog.setLayout(new GridLayout(5, 1));
		JPanel loginUser = new JPanel(new FlowLayout());
		loginUser.add(new JLabel("Old Password"));
		loginUser.add(oldPassword);
		JPanel loginPassword = new JPanel(new FlowLayout());
		loginPassword.add(new JLabel("New Password"));
		loginPassword.add(newPassword);

		loginDialog.add(loginUser);
		loginDialog.add(loginPassword);

		JPanel buttons = new JPanel(new FlowLayout());
		buttons.add(newPasswordOKButton);
		buttons.add(newPasswordCancelInputButton);
		newPasswordCancelInputButton.addActionListener(this);
		loginDialog.add(buttons);
		newPasswordDialog.add(loginDialog);
		JRootPane rootPane = SwingUtilities.getRootPane(newPasswordOKButton);
		rootPane.setDefaultButton(newPasswordOKButton);
		newPasswordDialog.setLocation(100, 100);
		newPasswordDialog.setVisible(true);
		// private method starts a new listener every time it's opened, this
		// keeps the extras at bay.
		if (newPasswordOKButton.getActionListeners().length < 1) {
			newPasswordOKButton.addActionListener(this);
		}
		if (newPasswordCancelInputButton.getActionListeners().length < 1) {
			newPasswordCancelInputButton.addActionListener(this);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == newPasswordOKButton) {
			try {
				String newPasswordAttempt = "";
				for (char letter : newPassword.getPassword()) {
					newPasswordAttempt += letter;
				}
				String oldPasswordAttempt = "";
				for (char letter : oldPassword.getPassword()) {
					oldPasswordAttempt += letter;
				}
				quizSystem.resetPassword(oldPasswordAttempt, newPasswordAttempt);
				newPasswordDialog.dispose();
//				if (tracker.getCurrentPane() == tracker.getLoginPane()) {
					quizSystem.logout();
					tracker.showLoginPane();
//				}
			} catch (IllegalArgumentException error) {
				JOptionPane.showMessageDialog(tracker, error.getMessage());
			}
		}
		if (e.getSource() == newPasswordCancelInputButton) {
			if (quizSystem.getCurrentUser().getNeedsPassword()) {
				quizSystem.logout();
				tracker.showLoginPane();
			}
			newPasswordDialog.dispose();
		}
	}
}
