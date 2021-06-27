package com.server.database;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.logger.Level;
import com.server.main.Server;

public class DatabaseTable {
	private static Set<Class<?>> templateTypes = Set.of(int.class, boolean.class, byte.class, short.class, long.class, String.class, Integer.class, Boolean.class, Short.class, Long.class, Byte.class);

	private DatabaseManager databaseManager;
	private String tableName;
	
	public DatabaseTable(DatabaseManager manager, String tableName) {
		this.databaseManager = manager;
		this.tableName = tableName;
	}
	
	public<T> List<T> getAllDatabaseObject(Class<T> t, String queryName) throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException, SQLException{
		return getAllDatabaseObject(t, new QueryObject(queryName, tableName));
	}
	
	public<T> List<T> getAllDatabaseObject(Class<T> t, QueryObject query) throws IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException, SQLException{
		return getAllDatabaseObject(t, query, 0);
	}
	
	public<T> List<T> getAllDatabaseObject(Class<T> t, QueryObject query, int... argGroup) throws SQLException, IllegalArgumentException, IllegalAccessException, InstantiationException, InvocationTargetException{
		Constructor<?> myConstructor = getConstructorForClass(t);
				
		ResultSet resultData = this.databaseManager.getData(query);
		
		List<Integer> groupsOfField = new ArrayList<>(argGroup.length);
		for(int i : argGroup) groupsOfField.add(i);
		
		List<T> buildObjects = new ArrayList<T>();
		while(resultData.next()) {
			Object newInstance = fillObjectWithData(myConstructor, resultData, groupsOfField);
			T castedObject = safeCast(newInstance, t);
			if(castedObject != null) {
				buildObjects.add(castedObject);
			}
		}
		
		return buildObjects;
	}
	
	public<T> T getDatabaseObject(Class<T> clazz, T t, String queryName) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, SQLException {
		return getDatabaseObject(clazz, t, queryName, 0);
	}
	
	public<T> T getDatabaseObject(Class<T> clazz, T t, String queryName, int... argGroup) throws IllegalArgumentException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException{
		
		Constructor<?> myConstructor = getConstructorForClass(clazz);
				
		QueryObject selectFieldData = new QueryObject(queryName, this.tableName);
		selectFieldData.addValues(t, argGroup);
		selectFieldData.addArguments(t, argGroup);
		
		ResultSet resultData = this.databaseManager.getData(selectFieldData);
		resultData.next();
		List<Integer> groupsOfField = new ArrayList<>(argGroup.length);
		for(int i : argGroup) groupsOfField.add(i);
		
		Object newInstance = fillObjectWithData(myConstructor, resultData, groupsOfField);
		
		return safeCast(newInstance, clazz);
	}
	
	private Object fillObjectWithData(Constructor<?> constructor, ResultSet resultData , Collection<Integer> groups) throws IllegalArgumentException, IllegalAccessException, SQLException, InstantiationException, InvocationTargetException {
		Object newInstance = constructor.newInstance();
		
		for(Field f : newInstance.getClass().getDeclaredFields()) {
			f.setAccessible(true);
			if(f.isAnnotationPresent(DatabaseValue.class)) {
				if(templateTypes.contains(f.getType())) {
					if(inSameGroup(groups, f.getAnnotation(DatabaseValue.class).groups())) {
						String columnName = f.getAnnotation(DatabaseValue.class).columnName();
						if(columnName.equals("") || columnName == null) columnName = f.getName();
						f.set(newInstance, resultData.getObject(columnName));
					}
				}else {
					Server.getLogger().log(Level.ERROR, "Can not read field of type: " + f.getType().getSimpleName() + " as value type in: " + newInstance.getClass().getSimpleName());
				}
			}
		}
		return newInstance;
	}
	
	private boolean inSameGroup(Collection<Integer> groups, int[] fieldGroups) {
		for(int i : fieldGroups) {
			if(groups.contains(i)) return true;
		}
		return false;
	}
	
	private Constructor<?> getConstructorForClass(Class<?> clazz){
		Constructor<?>[] constructors = clazz.getConstructors();
		Constructor<?> myConstructor = null;
		for(Constructor<?> c : constructors) {
			if(c.isAnnotationPresent(DatabaseObjectConstructor.class)) {
				myConstructor = c;
				break;
			}
		}
		if(myConstructor == null)
			throw new IllegalArgumentException("Can not find constructor for: " + clazz.getSimpleName() + ". Declair a constructor with @" + DatabaseObjectConstructor.class.getSimpleName());
		
		if(myConstructor.getParameterCount() > 0)
			throw new IllegalArgumentException("Declaird constructor for database object " + clazz.getSimpleName() + " can not have any parameters.");
		
		return myConstructor;
	}
	
	public static <T> T safeCast(Object o, Class<T> clazz) {
		if(clazz == null)
			throw new IllegalArgumentException("Can not cast object to nullpointer!");
	    return clazz.isInstance(o) ? clazz.cast(o) : null;
	}
	
}
