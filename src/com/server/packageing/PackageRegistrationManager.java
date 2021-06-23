package com.server.packageing;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.logger.Level;
import com.server.basepackages.CloseConnection;
import com.server.basepackages.KeepAlive;
import com.server.basepackages.MessagePackage;
import com.server.basepackages.PostData;
import com.server.basepackages.ReconnectPackage;
import com.server.basepackages.RemoteClosedConnection;
import com.server.basepackages.RequestData;
import com.server.packageing.annotations.DataPackageConstructor;
import com.server.packageing.annotations.DataPackageDynamic;
import com.server.packageing.annotations.DataPackageID;
import com.server.packageing.annotations.DataPackageLength;

public class PackageRegistrationManager {

	private HashMap<Class<? extends PackageManager>, Set<PackageInfo>> REGISTERED_PACKAGES = new HashMap<>();
	
	public PackageRegistrationManager() {
		register(DefaultPackageManager.class, CloseConnection.class, (id,l,d,b) -> {return new CloseConnection();});
		register(DefaultPackageManager.class, KeepAlive.class, (id,l,d,b) -> {return new KeepAlive();});
		register(DefaultPackageManager.class, MessagePackage.class, (id,l,d,b) -> {return new MessagePackage(b);});
		register(DefaultPackageManager.class, PostData.class, (id,l,d,b) -> {return new PostData(b);});
		register(DefaultPackageManager.class, ReconnectPackage.class, (id,l,d,b) -> {return new ReconnectPackage(b);});
		register(DefaultPackageManager.class, RequestData.class, (id,l,d,b) -> {return new RequestData(b);});
		register(DefaultPackageManager.class, RemoteClosedConnection.class, (id,l,d,b) -> {return new RemoteClosedConnection();}, (data, con) -> {
			con.disable(Level.INFO, "Remote closed connection! Stream ended.");
		});
	}
	
	/**
	 * Registers a package for a given PackageManager.<br>
	 * <br>
	 * This function uses reflection to access the fields <b>byte[] ID, short PACK_LENGTH, boolean IS_DYNAMIC_LENGTH</b>.
	 * <br><br>
	 * The constructor can be written as a lambda function like:<br>
	 * (length, dynamicLength, rawData) -> {return new DefaultPackageManager(length, dynamicLength, rawData);}<br>
	 * <br>
	 * @throws NullPointerException When <b>type == null</b> or <b>pack == null</b> or <b>construct == null</b>
	 * @throws NullPointerException When fields <b>ID, PACK_LENGTH, IS_DYNAMIC_LENGTH</b> are not declared.
	 * 
	 * @param type The .class type of the package manager for the package to be registered under.
	 * @param pack The .class type of the package that will be register under the give type of package manager.
	 * @param construct a constructor function for the package manager.
	 * */
	@Deprecated
	public void register(Class<? extends PackageManager> type, Class<? extends DataPackage> pack, PackageConstructor construct) {
		register(type, pack, construct, null);
	}
	
	/**
	 * Registers a package for a given PackageManager.<br>
	 * <br>
	 * This function uses reflection to access the fields <b>byte[] ID, short PACK_LENGTH, boolean IS_DYNAMIC_LENGTH</b>.
	 * <br><br>
	 * The constructor can be written as a lambda function like:<br>
	 * (length, dynamicLength, rawData) -> {return new DefaultPackageManager(length, dynamicLength, rawData);}<br>
	 * <br>
	 * @throws NullPointerException When <b>type == null</b> or <b>pack == null</b> or <b>construct == null</b>
	 * @throws NullPointerException When fields <b>ID, PACK_LENGTH, IS_DYNAMIC_LENGTH</b> are not declared.
	 * 
	 * @param type The .class type of the package manager for the package to be registered under.
	 * @param pack The .class type of the package that will be register under the give type of package manager.
	 * @param construct a constructor function for the package manager.
	 * @param packageCallBack Package callback function. Can be null.
	 * */
	@Deprecated
	public void register(Class<? extends PackageManager> type, Class<? extends DataPackage> pack, PackageConstructor construct, PackageCallback packageCallBack) {
		
		if(construct == null)
			throw new NullPointerException("Constructor can not be null!");
		if(type == null)
			throw new NullPointerException("Type can not be null!");
		if(pack == null)
			throw new NullPointerException("Package can not be null!");
		
		Field[] fields = pack.getDeclaredFields();
		byte[] id = null;
		short length = -1;
		boolean dynLength = false;
		try {
			for(Field f : fields) {
				switch(f.getName().toLowerCase()) {
				case "id":
						id = (byte[]) f.get(f);
					break;
				case "pack_length":
					length = f.getShort(f);
					break;
				case "is_dynamic_length":
					dynLength = f.getBoolean(f);
					break;
					default:
						break;
				}
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return;
		}
		if(id == null || length == -1)
			throw new NullPointerException("Variable were not inizialized. If you want to use this function declair the feelds(byte[] ID, short PACK_LENGTH, boolean IS_DYNAMIC_LENGTH)");
		register(type, id, length, dynLength, construct, packageCallBack);
	}
		
	public void register(Class<? extends PackageManager> type, Class<? extends DataPackage> pack, PackageCallback packageCallBack) {
		if(type == null)
			throw new NullPointerException("Type can not be null!");
		if(pack == null)
			throw new NullPointerException("Package can not be null!");
		
		Field[] fields = pack.getDeclaredFields();
		boolean hasID = false;
		boolean hasLenght = false;
		boolean hasDynLength = false;
		byte[] id = null;
		short length = -1;
		boolean dynLength = false;
		try {
			for(Field f : fields) {
				if(f.isAnnotationPresent(DataPackageID.class)) {
					hasID = true;
					id = (byte[]) f.get(f);
				}else if(f.isAnnotationPresent(DataPackageLength.class)) {
					hasLenght = true;
					length = f.getShort(f);
				}else if(f.isAnnotationPresent(DataPackageDynamic.class)) {
					hasDynLength = true;
					dynLength = f.getBoolean(f);
				}
				
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		if(!hasID)
			throw new IllegalArgumentException("No field was found for 'byte[] ID'. The argument pack requires a 'byte[]' with a @DataPackageID annotation.");
		if(!hasLenght)
			throw new IllegalArgumentException("No field was found for 'short LENGTH'. The argument pack requires a 'short' with a @DataPackageLength annotation.");
		if(!hasDynLength)
			throw new IllegalArgumentException("No field was found for 'boolean DYNAMIC_LENGTH'. The argument pack requires a 'boolean' with a @DataPackageDynamic annotation.");
		
		
		boolean hasConstructor = false;
		PackageConstructor constructor = null;
		for(Constructor<?> c : pack.getDeclaredConstructors()) {
			if(c.isAnnotationPresent(DataPackageConstructor.class)) {
				DataPackageConstructor annotation = c.getAnnotation(DataPackageConstructor.class);
				
				hasConstructor = true;
				
				Class<?>[] parameterTypes = c.getParameterTypes();
				Class<?>[] templateParameterTypes = new Class<?>[] {new byte[0].getClass(), short.class, boolean.class, new byte[0].getClass()};
				
				if(!annotation.ID()) templateParameterTypes[0] = null;
				if(!annotation.LENGTH()) templateParameterTypes[1] = null;
				if(!annotation.DYNAMIC()) templateParameterTypes[2] = null;
				if(!annotation.DATA()) templateParameterTypes[3] = null;
				
				if(!hasCorrectParameterOrder(templateParameterTypes, parameterTypes))
					throw new IllegalArgumentException("Declaird constructor does not conform to the template! Arguments can not be out of order!");
				
				constructor = (packageID, packageLength, dynamicLength, byteDataRaw) -> {
					ArrayList<Object> initArray = new ArrayList<>();
					if(annotation.ID()) initArray.add(packageID);
					if(annotation.LENGTH()) initArray.add(packageLength);
					if(annotation.DYNAMIC()) initArray.add(dynamicLength);
					if(annotation.DATA()) initArray.add(byteDataRaw);
					Object[] initArgs = initArray.toArray();
					try {
						return (DataPackage) c.newInstance(initArgs);
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException e) {
						e.printStackTrace();
						return null;
					}
				};
				break;
			}
		}
		
		if(!hasConstructor)
			throw new IllegalArgumentException("No constructor was found with a @DataPackageConstructor annoation!");
		
		register(type, id, length, dynLength, constructor, packageCallBack);
	}
	
	public void register(Class<? extends DataPackage> pack, PackageCallback packageCallBack) {
		register(DefaultPackageManager.class, pack, packageCallBack);
	}
	
	
	/**
	 * Registers a package to the DefaultPackageManager.class.<br>
	 * <br>
	 * This function uses reflection to access the fields <b>byte[] ID, short PACK_LENGTH, boolean IS_DYNAMIC_LENGTH</b>.
	 * <br><br>
	 * The constructor can be written as a lambda function like:<br>
	 * (length, dynamicLength, rawData) -> {return new DefaultPackageManager(length, dynamicLength, rawData);}<br>
	 * <br>
	 * @throws NullPointerException When <b>type == null</b> or <b>pack == null</b> or <b>construct == null</b>
	 * @throws NullPointerException When fields <b>ID, PACK_LENGTH, IS_DYNAMIC_LENGTH</b> are not declared.
	 * 
	 * @param pack The .class type of the package that will be register under the give type of package manager.
	 * @param construct a constructor function for the package manager.
	 * @param packageCallBack Package callback function. Can be null.
	 * */
	@Deprecated
	public void register(Class<? extends DataPackage> pack, PackageConstructor construct, PackageCallback packageCallBack) {
		register(DefaultPackageManager.class, pack, construct, packageCallBack);
	}
	
	/**
	 * Registers a package to the DefaultPackageManager.class.<br>
	 * <br>
	 * This function uses reflection to access the fields <b>byte[] ID, short PACK_LENGTH, boolean IS_DYNAMIC_LENGTH</b>.
	 * <br><br>
	 * The constructor can be written as a lambda function like:<br>
	 * (length, dynamicLength, rawData) -> {return new DefaultPackageManager(length, dynamicLength, rawData);}<br>
	 * <br>
	 * @throws NullPointerException When <b>type == null</b> or <b>pack == null</b> or <b>construct == null</b>
	 * @throws NullPointerException When fields <b>ID, PACK_LENGTH, IS_DYNAMIC_LENGTH</b> are not declared.
	 * 
	 * @param pack The .class type of the package that will be register under the give type of package manager.
	 * @param construct a constructor function for the package manager.
	 * */
	@Deprecated
	public void register(Class<? extends DataPackage> pack, PackageConstructor construct) {
		register(DefaultPackageManager.class, pack, construct, null);
	}
	
	/**
	 * Registers a package for a given PackageManager.<br>
	 * <br>
	 * The constructor can be written as a lambda function like:<br>
	 * (length, dynamicLength, rawData) -> {return new DefaultPackageManager(length, dynamicLength, rawData);}<br>
	 * <br>
	 * @throws IllegalArgumentException When <b>info.getId() == null</b> or <b>info.getId().length < </b>
	 * @throws IllegalArgumentException When <b>info.getId().length > DataPackage.IDLENGTH</b>
	 * @throws IllegalArgumentException When <b>info.getLength() < 0</b>
	 * @throws NullPointerException When <b>info.getConstruct() == null</b>
	 * @throws IllegalArgumentException When a package with the given id has already been registered for the given type.
	 * 
	 * @param type The .class type of the package manager for the package to be registered under.
	 * @param id A byte array containing the id of the package like: {0x0, 0x1} for a package with id 1.
	 * @param length the static length of the package or the number of bytes associated with the length in the package.
	 * @param dynamicLength Modifier for the dynamic length
	 * @param construct a wrapper function for a package constructor.
	 * */
	@Deprecated
	public void register(Class<? extends PackageManager> type, byte[] id, short length, boolean dynamicLength, PackageConstructor construct) {
		register(type, id, length, dynamicLength, construct, null);
	}
	
	/**
	 * Registers a package for a given PackageManager.<br>
	 * <br>
	 * The constructor can be written as a lambda function like:<br>
	 * (length, dynamicLength, rawData) -> {return new DefaultPackageManager(length, dynamicLength, rawData);}<br>
	 * <br>
	 * The callback function can be written as a lambda function like:<br>
	 * (data, connection) -> {"Something to execute..."};
	 * <br>
	 * @throws IllegalArgumentException When <b>info.getId() == null</b> or <b>info.getId().length < </b>
	 * @throws IllegalArgumentException When <b>info.getId().length > DataPackage.IDLENGTH</b>
	 * @throws IllegalArgumentException When <b>info.getLength() < 0</b>
	 * @throws NullPointerException When <b>info.getConstruct() == null</b>
	 * @throws IllegalArgumentException When a package with the given id has already been registered for the given type.
	 * 
	 * @param type The .class type of the package manager for the package to be registered under.
	 * @param id A byte array containing the id of the package like: {0x0, 0x1} for a package with id 1.
	 * @param length the static length of the package or the number of bytes associated with the length in the package.
	 * @param dynamicLength Modifier for the dynamic length
	 * @param construct a wrapper function for a package constructor.
	 * @param packageCallBack a callback function executed by the package when received.
	 * */
	public void register(Class<? extends PackageManager> type, byte[] id, short length, boolean dynamicLength, PackageConstructor construct, PackageCallback packageCallBack) {
		register(type, new PackageInfo(id, length, dynamicLength, construct, packageCallBack));
	}
	
	
	/**
	 * Registers a package for a given PackageManager.<br>
	 * <br>
	 * @throws IllegalArgumentException When <b>info.getId() == null</b> or <b>info.getId().length < </b>
	 * @throws IllegalArgumentException When <b>info.getId().length > DataPackage.IDLENGTH</b>
	 * @throws IllegalArgumentException When <b>info.getLength() < 0</b>
	 * @throws NullPointerException When <b>info.getConstruct() == null</b>
	 * @throws IllegalArgumentException When a package with the given id has already been registered for the given type.
	 * 
	 * @param type The .class type of the package manager for the package to be registered under.
	 * @param info A PackageInfo object that contains the basic information about a specific package.
	 * */
	public void register(Class<? extends PackageManager> type, PackageInfo info) {
		if(info.getId() == null || info.getId().length <= 0) 
			throw new IllegalArgumentException("The package id can not be smaller or equal to 0! The package id has to be " + DataPackage.IDLENGTH + " byte long!");
		if(info.getId().length > DataPackage.IDLENGTH) 
			throw new IllegalArgumentException("The package id has to be " + DataPackage.IDLENGTH + " byte long and can not be larger or smaller!");
		if(info.getLength() < 0) 
			throw new IllegalArgumentException("The package length can not be negative!");
		if(info.getConstruct() == null) 
			throw new NullPointerException("Package constructor can not be null!");
		if(hasPackage(type, info.getId())) 
			throw new IllegalArgumentException("Cannot register package with id: (" + DataPackage.getFromByte(info.getId()) + ")! A package is already registered under that id! ");
		
		if(getAllPackagesForManager(type) == null)
			REGISTERED_PACKAGES.put(type, new HashSet<>());
		
		Set<PackageInfo> set = getAllPackagesForManager(type);
		set.add(info);
		REGISTERED_PACKAGES.put(type, set);
	}
	
	public PackageInfo getPackageInfo(Class<? extends PackageManager> type, byte[] id) {
		return getPackageInfo(type, (int)DataPackage.getFromByte(id));
	}
	
	public PackageInfo getPackageInfo(Class<? extends PackageManager> type, int id) {
		Set<PackageInfo> out = REGISTERED_PACKAGES.get(type);
		if(out == null) return null;
		for(PackageInfo i : out) {
			if(id == (int)DataPackage.getFromByte(i.getId())) {
				return i;
			}
		}
		return null;
	}
	
	/**
	 * Clears all packages registered for the given type.
	 * */
	public void clearPackageManager(Class<? extends PackageManager> type) {
		this.REGISTERED_PACKAGES.put(type, new HashSet<>());
	}
	
	/**
	 * Sets package callback function.
	 * 
	 * The callback function can be written as a lambda function like:<br>
	 * (data, connection) -> {"Something to execute..."};
	 * 
	 * @throws IllegalArgumentException When no package was found for the given id.
	 * 
	 * @param type The .class type of the package manager for the package to be registered under.
	 * @param id A byte array of the id.
	 * @param callback The callback function.
	 * */
	public void setPackageCallBack(Class<? extends PackageManager> type, byte[] id, PackageCallback callback) {
		setPackageCallBack(type, (int)DataPackage.getFromByte(id), callback);
	}
	
	/**
	 * Sets package callback function.
	 * 
	 * The callback function can be written as a lambda function like:<br>
	 * (data, connection) -> {"Something to execute..."};
	 * 
	 * @throws IllegalArgumentException When no package was found for the given id.
	 * 
	 * @param type The .class type of the package manager for the package to be registered under.
	 * @param id The package id.
	 * @param callback The callback function.
	 * */
	public void setPackageCallBack(Class<? extends PackageManager> type, int id, PackageCallback callback) {
		if(getPackageInfo(type, id) == null) 
			throw new IllegalArgumentException("Could not find package with id: " + id);
		if(callback == null)
			throw new IllegalArgumentException("Callback can not be null!");
		PackageInfo info = getPackageInfo(type, id);
		Set<PackageInfo> set = getAllPackagesForManager(type);
		set.remove(info);
		info.setCallback(callback);
		set.add(info);
		REGISTERED_PACKAGES.put(type, set);
	}
	
	public Set<PackageInfo> getAllPackagesForManager(Class<? extends PackageManager> type){
		if(REGISTERED_PACKAGES.get(type) != null) return REGISTERED_PACKAGES.get(type);
		return new HashSet<>(); 
	}
	
	public boolean hasPackage(Class<? extends PackageManager> type, byte[] id) {
		return getPackageInfo(type, id) != null;
	}
	
	private boolean hasCorrectParameterOrder(Class<?>[] template, Class<?>[] toCompair) {
		int j = 0;
		for(int i = 0; i < template.length; i++) {
			if(j >= toCompair.length) return false;
			if(template[i] != null && template[i].getTypeName().equals(toCompair[j].getTypeName())) {
				j++;
			}
		}
		return j == toCompair.length;
	}
}
