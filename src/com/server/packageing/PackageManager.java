package com.server.packageing;

import java.lang.reflect.Field;
import java.util.HashMap;

import com.server.main.Server;

public abstract class PackageManager {

	private HashMap<Integer, PackageInfo> PACKAGELOOKUP = new HashMap<>();
	
	public PackageManager(Server server, Class<? extends PackageManager> type) {
		if(server == null || type == null)
			throw new NullPointerException("Server and type can not be null!");
		server.getPackageRegistrationManager().getAllPackagesForManager(type).forEach(x -> register(x));
	}
	
	/**
	 * Registers a package to this PackageManager.<br>
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
	public void register(Class<? extends DataPackage> pack, PackageConstructor construct) {
		register(pack, construct, null);
	}
	
	/**
	 * Registers a package to this PackageManager.<br>
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
	 * @param packageCallBack The callback function for the package
	 * */
	public void register(Class<? extends DataPackage> pack, PackageConstructor construct, PackageCallback packageCallBack) {
		
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
		}
		if(id == null || length == -1)
			throw new NullPointerException("Variable were not inizialized. If you want to use this function declair the feelds(byte[] ID, short PACK_LENGTH, boolean IS_DYNAMIC_LENGTH)");
		register(id, length, dynLength, construct, packageCallBack);
	}
	
	/**
	 * Registers a package to this PackageManager.<br>
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
	 * @param id A byte array containing the id of the package like: {0x0, 0x1} for a package with id 1.
	 * @param length the static length of the package or the number of bytes associated with the length in the package.
	 * @param dynamicLength Modifier for the dynamic length
	 * @param construct a wrapper function for a package constructor.
	 * */
	public void register(byte[] id, short length, boolean dynamicLength, PackageConstructor construct) {
		register(id, length, dynamicLength, construct, null);
	}
	
	/**
	 * Registers a package to this PackageManager.<br>
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
	 * @param id A byte array containing the id of the package like: {0x0, 0x1} for a package with id 1.
	 * @param length the static length of the package or the number of bytes associated with the length in the package.
	 * @param dynamicLength Modifier for the dynamic length
	 * @param construct a wrapper function for a package constructor.
	 * @param packageCallBack a callback function executed by the package when received.
	 * */
	public void register(byte[] id, short length, boolean dynamicLength, PackageConstructor construct, PackageCallback packageCallBack) {
		register(new PackageInfo(id, length, dynamicLength, construct, packageCallBack));
	}
	
	/**
	 * Registers a package to this PackageManager.<br>
	 * <br>
	 * @throws IllegalArgumentException When <b>info.getId() == null</b> or <b>info.getId().length < </b>
	 * @throws IllegalArgumentException When <b>info.getId().length > DataPackage.IDLENGTH</b>
	 * @throws IllegalArgumentException When <b>info.getLength() < 0</b>
	 * @throws NullPointerException When <b>info.getConstruct() == null</b>
	 * @throws IllegalArgumentException When a package with the given id has already been registered for the given type.
	 * 
	 * @param info A PackageInfo object that contains the basic information about a specific package.
	 * */
	public void register(PackageInfo info) {
		if(info.getId() == null || info.getId().length <= 0) 
			throw new IllegalArgumentException("The package id can not be smaller or equal to 0! The package id has to be " + DataPackage.IDLENGTH + " byte long!");
		if(info.getId().length > DataPackage.IDLENGTH) 
			throw new IllegalArgumentException("The package id has to be " + DataPackage.IDLENGTH + " byte long and can not be larger or smaller!");
		if(info.getLength() < 0) 
			throw new IllegalArgumentException("The package length can not be negative!");
		if(info.getConstruct() == null) 
			throw new NullPointerException("Package constructor can not be null!");
		if(getPackageInfo(info.getId()) != null) 
			throw new IllegalArgumentException("Cannot register package with id: (" + DataPackage.getFromByte(info.getId()) + ")! A package is already registered under that id! ");
		
		PACKAGELOOKUP.put((int)DataPackage.getFromByte(info.getId()), info);
	}
	
	/**
	 * Sets package callback function.
	 * 
	 * The callback function can be written as a lambda function like:<br>
	 * (data, connection) -> {"Something to execute..."};
	 * 
	 * @throws IllegalArgumentException When no package was found for the given id or the give callback was null.
	 * 
	 * @param id A byte array of the id.
	 * @param callback The callback function.
	 * */
	public void setPackageCallBack(byte[] id, PackageCallback callback) {
		setPackageCallBack((int)DataPackage.getFromByte(id), callback);
	}
	
	/**
	 * Sets package callback function.
	 * 
	 * The callback function can be written as a lambda function like:<br>
	 * (data, connection) -> {"Something to execute..."};
	 * 
	 * @throws IllegalArgumentException When no package was found for the given id or the give callback was null.
	 * 
	 * @param id The package id.
	 * @param callback The callback function.
	 * */
	public void setPackageCallBack(int id, PackageCallback callback) {
		if(getPackageInfo(id) == null)
			throw new IllegalArgumentException("Could not find package with id: " + id);
		if(callback == null)
			throw new IllegalArgumentException("Callback can not be null!");
		PackageInfo info = getPackageInfo(id);
		info.setCallback(callback);
		PACKAGELOOKUP.put(id, info);
	}
	
	public void clearPackages() {
		this.PACKAGELOOKUP.clear();
	}
	
	public PackageInfo getPackageInfo(byte[] id) {
		return getPackageInfo((int)DataPackage.getFromByte(id));
	}
	
	public PackageInfo getPackageInfo(int id) {
		return PACKAGELOOKUP.get(id);
	}
	
	public boolean hasPackage(byte[] id) {
		return PACKAGELOOKUP.get((int)DataPackage.getFromByte(id)) != null;
	}
	
}