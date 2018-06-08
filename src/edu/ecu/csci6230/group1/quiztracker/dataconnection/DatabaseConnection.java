package edu.ecu.csci6230.group1.quiztracker.dataconnection;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.h2.tools.RunScript;

//import org.h2.tools.RunScript;

public class DatabaseConnection {
//	private static String defaultDatabase = "jdbc:mysql://70.130.79.105:3306/QUIZTRACKER";
	private static String defaultDatabase = "jdbc:h2:./H2ProductionDB/QUIZTRACKER;"
			+ "MODE=MySQL;"
			+ "DATABASE_TO_UPPER=false;"
			+ "TRACE_LEVEL_SYSTEM_OUT=0;";
	private static String defaultUser = "quizuser";
	private static String defaultPassword = "quizuser";
	private static String defaultDatabaseClass = "com.mysql.jdbc.Driver";
	private static DatabaseConnection SQLConnection;
	private Connection connection;

	/**
	 * Opens a connection with the specified values.
	 * 
	 * @param database
	 * @param user
	 * @param password
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
//	private DatabaseConnection(String database, String user, String password, String databaseClassDriver)
//			throws ClassNotFoundException, SQLException {
//		Class.forName(databaseClassDriver);
//		try {
//			connection = DriverManager.getConnection(database, user, password);
//		} catch (SQLException e) {
//			System.out.println("Connection failed: " + e.getMessage());
//			connection = DriverManager
//					.getConnection("jdbc:mysql://raspberrypi:3306/QUIZTRACKER", defaultUser, defaultPassword);
//		}
//	}
	
	private DatabaseConnection(String database, String user, String password, String databaseClassDriver)
			throws ClassNotFoundException, SQLException {
		Class.forName("org.h2.Driver");
		try {
			connection = DriverManager.getConnection(database, user, password);
			RunScript.execute(connection, new FileReader("./H2ProductionDB/TestingImport.txt"));
		} catch (Exception e) {
			System.out.println("DatabaseConnection.DatabaseConnection " + e.getMessage());
//			connection = DriverManager
//					.getConnection("jdbc:h2:/Users/scott/Dropbox/ECU/SENG6230/importansi.sql;MODE=MySQL", defaultUser, defaultPassword);
		}
	}

	/**
	 * Opens a connection with default values. Does this by calling the
	 * overloaded constructor - code reuse!
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private DatabaseConnection() throws ClassNotFoundException, SQLException {
		this(defaultDatabase, defaultUser, defaultPassword, defaultDatabaseClass);
	}

	/**
	 * We call getInstance to insure only ONE connection is active while the
	 * program runs.
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static DatabaseConnection getInstance() throws ClassNotFoundException, SQLException {
		if (SQLConnection == null) {
			SQLConnection = new DatabaseConnection();
		}
		return SQLConnection;
	}

	/**
	 * We call getInstance to insure only ONE connection is active while the
	 * program runs. This is an overloaded constructor to allow us to specify
	 * initial values (for testing, etc).
	 * 
	 * @param database
	 * @param user
	 * @param password
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static DatabaseConnection getInstance(String database, String user, String password, String databaseDriver)
			throws ClassNotFoundException, SQLException {
		if (SQLConnection == null) {
			SQLConnection = new DatabaseConnection(database, user, password, databaseDriver);
		}
		return SQLConnection;
	}

	public Connection GetConnection() {
		return connection;
	}

	public void CloseConnection() throws SQLException {
		connection.close();
	}
}
