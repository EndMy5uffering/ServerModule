package com.server.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import com.logger.Level;
import com.server.main.Server;

enum DatabaseType{
	MYSQL,
	SQLITE
}

public class DatabaseManager {

	private Thread SqlStatementThread;

	private boolean StatementThread = true;
	private Semaphore lock = new Semaphore(1);
	
	private ArrayList<String> SQLStatements = new ArrayList<String>();
	
	private DatabaseInfo dbInfo;
	
	private Connection connection;
	
	public DatabaseManager(DatabaseInfo dbInfo) {
		this.dbInfo = dbInfo;
	}
	
	/**
	 * This function sets up the database access.
	 *  
	 * */
	public boolean createDatabaseConnection() {
		switch (dbInfo.getType()) {
		case SQLITE:
			
			File sqlitefile = new File(dbInfo.getDirectory(), File.separator + dbInfo.getFileName() + ".sqlite");
			if (!sqlitefile.exists()) {
				try {
					sqlitefile.createNewFile();
				} catch (IOException e) {
					Server.logger.log(Level.WARNING, "Could not create new save file!");
					return false;
				}
			}
			try {
				this.connection = openNewConnection();
				Server.logger.log(Level.INFO, "Database status: " + (testConnection() ? "Connected" : "Offline"));
				Server.logger.log(Level.INFO, "Using SQLite as save format.");
			} catch (SQLException e) {
				Server.logger.log(Level.WARNING, "Connection to SQLite database failed!");
				Server.logger.log(Level.WARNING, "Check if your connection information is correct ->");
				Server.logger.log(Level.WARNING, "Path: " + dbInfo.getUrl());
				Server.logger.log(Level.WARNING, e, e.getClass());
				return false;
			}
			break;
		case MYSQL:
			try {
				this.connection = openNewConnection();
				Server.logger.log(Level.INFO, "Database status: " + (testConnection() ? "Connected" : "Offline"));
				Server.logger.log(Level.INFO, "Using MySQL as save format.");
			} catch (SQLException e) {
				Server.logger.log(Level.WARNING, "Connection to MySQL database failed!");
				Server.logger.log(Level.WARNING, "Check if your connection information is correct ->");
				Server.logger.log(Level.WARNING, "Path: " + this.dbInfo.getUrl());
				Server.logger.log(Level.WARNING, "UserName: " + this.dbInfo.getUser());
				Server.logger.log(Level.WARNING, "UserPassword: ***");
				Server.logger.log(Level.WARNING, e, e.getClass());
				return false;
			}
			break;
		default:
			Server.logger.log(Level.WARNING, "No correct information given for the save format!");
			return false;
		}
		return true;
	}
	
	public void closeConnection() {
		try {
			this.connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void startAsyncWorker() {
		SqlStatementThread = new Thread(() -> {

			Connection conn;
			try {
				conn = openNewConnection();
			} catch (SQLException e2) {
				Server.getLogger().log(Level.ERROR, "Could not open async database connection!");
				return;
			}
			
			while(StatementThread) {
				try {
					lock.acquire();
				} catch (InterruptedException e) {
					return;
				}
				if(!StatementThread) return;
				ArrayList<String> toRemove = new ArrayList<>();
				
				while(!SQLStatements.isEmpty()) {
					for(String s : SQLStatements) {
						try {
							PreparedStatement statement = conn.prepareStatement(s);
							statement.execute();
							toRemove.add(s);
						} catch (SQLException e) {
							Server.getLogger().log(Level.ERROR, "Error while executing async sql statment");
							Server.getLogger().log(Level.ERROR, "Query: " + s);
							Server.getLogger().log(Level.ERROR, e);
							toRemove.add(s);
						}
					}
					for(String s : toRemove) {
						SQLStatements.remove(s);
					}
				}
				lock.release();
			}
		});
		SqlStatementThread.start();
	}
	
	public void stopAsyncWorker() {
		this.StatementThread = false;
		lock.release();
		try {
			SqlStatementThread.join();
		}catch(InterruptedException e) {
			System.out.println(e);
		}
	}
	
	public void asyncSqlStatements(List<String> list) {
		try {
			lock.acquire();
			SQLStatements.addAll(list);
			lock.release();
		} catch (InterruptedException e) {
			Server.getLogger().log(Level.ERROR, "Could not add async SQL statements!");
			Server.getLogger().log(Level.ERROR, e);
		}
		
	}
	
	public void asyncSqlStatement(String statement) {
		try {
			lock.acquire();
			SQLStatements.add(statement);
			lock.release();
		} catch (InterruptedException e) {
			Server.getLogger().log(Level.ERROR, "Could not add async SQL statement!");
			Server.getLogger().log(Level.ERROR, e);
		}
		
	}
	
	public void asyncSqlStatement(QueryObject query) {
		asyncSqlStatement(query.getQuery());
	}
	
	public boolean executeQuery(String query) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(query);
		boolean out = statement.execute();
		return out;
	}
	
	public boolean executeQuery(QueryObject query) throws SQLException {
		return executeQuery(query.getQuery());
	}
	
	public int executeUpdate(String query) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(query);
		int out = statement.executeUpdate();
		return out;
	}
	
	public boolean testConnection() {
		if(this.connection == null) return false;
		try {
			if(executeQuery("SELECT 1;")) return true;
		} catch (SQLException e) {
			return false;
		}
		return false;
	}
	
	public boolean testConnection(Connection con) {
		if(con == null) return false;
		try {
			if(con.prepareStatement("SELECT 1;").execute()) return true;
		} catch (SQLException e) {
			return false;
		}
		return false;
	}
	
	public boolean hasTable(String TableName) {
		String query = "SELECT * FROM " + TableName;
		try {
			return executeQuery(query);
		} catch (SQLException e) {
			return false;
		}
	}
	
	public synchronized ResultSet getData(String query) throws SQLException {
		Statement stmt = connection.createStatement();
		ResultSet out = stmt.executeQuery(query);
		return out;
	}
	
	public synchronized ResultSet getData(QueryObject query) throws SQLException {
		return getData(query.getQuery());
	}
	
	public static DatabaseInfo getDatabaseInfo(String url, String name, String pass) {
		return new DatabaseInfo(url, name, pass);
	}
	
	public static DatabaseInfo getDatabaseInfo(String directory, String fileName) {
		return new DatabaseInfo(directory, fileName);
	}
	
	public Connection openNewConnection() throws SQLException {
		return (this.dbInfo.getUser() != null && this.dbInfo.getPass() != null ? DriverManager.getConnection(this.dbInfo.getUrl(), this.dbInfo.getUser(), this.dbInfo.getPass()) : DriverManager.getConnection(this.dbInfo.getUrl()));
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	
	public static QueryObject getINSERT(String table, String... args) {
		if(args.length%2 != 0)
			throw new IllegalArgumentException("Arguments are read in pairs of two (column name, column value)");

		QueryObject InsertQueryObject = new QueryObject("INSERT", table);
		
		for(int i = 0; i < args.length; i += 2) {
			InsertQueryObject.addValue(args[i], args[i+1]);
		}
		
		return InsertQueryObject;
	}
	
	public static QueryObject getSELECT(String table, String... args) {
		if(args.length%2 != 0)
			throw new IllegalArgumentException("Arguments are read in pairs of two (column name, column value)");

		QueryObject InsertQueryObject = new QueryObject("INSERT", table);
		
		for(int i = 0; i < args.length; i += 2) {
			InsertQueryObject.addArgument(args[i], args[i+1]);
		}
		
		return InsertQueryObject;
	}
	
	public static QueryObject getDELETE(String table, String... args) {
		if(args.length%2 != 0)
			throw new IllegalArgumentException("Arguments are read in pairs of two (column name, column value)");

		QueryObject InsertQueryObject = new QueryObject("DELETE", table);
		
		for(int i = 0; i < args.length; i += 2) {
			InsertQueryObject.addValue(args[i], args[i+1]);
		}
		
		return InsertQueryObject;
	}
	
	public static QueryObject getQuery(String query) { 
		return QueryObject.getQueryObject(query);
	}
	
}

class DatabaseInfo{
	
	private final String url;
	private final String user;
	private final String pass;
	private final String fileName;
	private final String directory;
	private final DatabaseType type;
	
	public DatabaseInfo(String url, String name, String pass){
		this.url = "jdbc:mysql:" + url;
		this.user = name;
		this.pass = pass;
		this.fileName = "";
		this.directory = "";
		this.type = DatabaseType.MYSQL;
	}
	
	
	public DatabaseInfo(String directory, String fileName){
		this.url = "jdbc:sqlite:" + directory + File.separator + fileName + ".sqlite";
		this.user = null;
		this.pass = null;
		this.fileName = fileName;
		this.directory = directory;
		this.type = DatabaseType.SQLITE;
	}
	
	public String getUrl() {
		return url;
	}

	public String getUser() {
		return user;
	}

	public String getPass() {
		return pass;
	}

	public String getFileName() {
		return fileName;
	}
	
	public String getDirectory() {
		return directory;
	}
	
	public DatabaseType getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return "URL:\t" + (url != null ? url : "null") + "\nName:\t" + (user != null ? user : "null");
	}
}
