package com.server.database;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

@DatabaseObject
public class DatabaseTestObject {

	@DatabaseValue(columnName = "id")
	@DatabaseArgument(columnName = "id")
	int index = 0;
	@DatabaseValue(columnName = "ObjectName")
	String ObjectName;
	@DatabaseValue(columnName = "testData")
	String testData;
	@DatabaseValue(columnName = "testint")
	int testint;

	@DatabaseObjectConstructor
	public DatabaseTestObject(){
		
	}
	
	public DatabaseTestObject(int index, String ObjectName, String testData, int testint){
		this.index = index;
		this.ObjectName = ObjectName;
		this.testData = testData;
		this.testint = testint;
	}
	
	@Override
	public String toString() {
		return index + " | " + ObjectName + " | " + testData + " | " + testint;
	}
	
	public static void main(String... args) {
		DatabaseManager manager = new DatabaseManager(DatabaseManager.getDatabaseInfo("//localhost:3306/testdb?useSSL=false&serverTimezone=UTC", "VirusEvo", "VirusEvoGameDev"));
		manager.createDatabaseConnection();
		manager.startAsyncWorker();
		QueryObject.addQueryConstructor("INSERT", (q) -> {
			return "INSERT INTO " + q.getTableName() + " " + QueryObject.constructValueList(q.getValueList()) + ";"; 
		});
		QueryObject.addQueryConstructor("SELECT", (q) -> {
			return "SELECT * FROM " + q.getTableName() + " WHERE " + QueryObject.constructKWargList(q.getArgumentList(), " AND ");
		});
		
		QueryObject.addQueryConstructor("SELECT*", (q) -> {
			return "SELECT * FROM " + q.getTableName() + ";";
		});
		
//		QueryObject q = new QueryObject("INSERT", "testtable");
//		DatabaseTestObject o = new DatabaseTestObject(2, "test2", "this is test data 2", 42247365);
//		q.addValues(o);
//		q.addArguments(o);
		
//		manager.asyncSqlStatement(q);
		DatabaseTable table = new DatabaseTable(manager, "testtable");
		
		try {
			List<DatabaseTestObject> output = table.getAllDatabaseObject(DatabaseTestObject.class, "SELECT*");
			for(DatabaseTestObject t : output) {
				System.out.println(t);
			}
		} catch (IllegalArgumentException | InstantiationException | IllegalAccessException | InvocationTargetException
				| SQLException e) {
			e.printStackTrace();
		}
		
	}
	
}
