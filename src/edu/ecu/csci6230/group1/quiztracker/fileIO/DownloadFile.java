package edu.ecu.csci6230.group1.quiztracker.fileIO;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.ecu.csci6230.group1.quiztracker.dataconnection.DatabaseConnection;
import edu.ecu.csci6230.group1.quiztracker.managers.QuizManager;

public class DownloadFile {
	
	public static void getFile(String quizName, String destination, String detailStudent, String courseInfo) {
		Blob fileBlob = null;
		ResultSet resultSet = null;
		PreparedStatement stmt = null;
		int quizId = QuizManager.getQuizId(courseInfo, quizName, detailStudent);
		try {
			stmt = DatabaseConnection.getInstance().GetConnection().
					prepareStatement("SELECT quiz_file FROM QUIZ where id = ?");
			stmt.setInt(1, quizId);
			resultSet = stmt.executeQuery();
			if (resultSet.next()) {
				fileBlob = resultSet.getBlob(1);
			}
			stmt.close();
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}
		Path destinationPath = Paths.get(destination);
		if (Files.exists(destinationPath, LinkOption.NOFOLLOW_LINKS)) {
			throw new IllegalArgumentException("File exists in destination");
		} else {
			try {
				long wrote = 0;
				OutputStream targetFile = new FileOutputStream(destinationPath.toString());
				try {
					wrote = readFromBlob(fileBlob, targetFile);
				} catch (SQLException e) {
					System.out.println(e.getMessage());
				}
				targetFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static long readFromBlob(Blob blob, OutputStream out) throws SQLException, IOException {
		InputStream in = blob.getBinaryStream();
		int length = -1;
		long read = 0;
		int bBufLen = 4 * 8192;
		byte[] buf = new byte[bBufLen];
		while ((length = in.read(buf)) != -1) {
			out.write(buf, 0, length);
			read += length;
		}
		in.close();
		return read;
	}

	public static String getFilename(String quizName, String userId, String courseInfo) {
		PreparedStatement stmt;
		ResultSet results;
		int quizId = QuizManager.getQuizId(courseInfo, quizName, userId);
		try {
			stmt = DatabaseConnection.getInstance().GetConnection().
					prepareStatement("SELECT file_name FROM QUIZ WHERE id = ?");
			stmt.setInt(1, quizId);
			results = stmt.executeQuery();
			if (results.next()) {
				return results.getString(1);
			}
		} catch (SQLException | ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}
		return "";
	}

}
