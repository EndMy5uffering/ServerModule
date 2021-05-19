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
	
	public void register(Class<? extends DataPackage> pack, PackageConstructor construct) {
		register(pack, construct, null);
	}
	
	public void register(Class<? extends DataPackage> pack, PackageConstructor construct, PackageCallBack packageCallBack) {
		
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
	
	public void register(byte[] id, short length, boolean dynamicLength, PackageConstructor construct) {
		register(id, length, dynamicLength, construct, null);
	}
	
	public void register(byte[] id, short length, boolean dynamicLength, PackageConstructor construct, PackageCallBack packageCallBack) {
		register(new PackageInfo(id, length, dynamicLength, construct, packageCallBack));
	}
	
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
			throw new IllegalArgumentException("Cannot register package with id: (" + DataPackage.getIntFromByte(info.getId()) + ")! A package is already registered under that id! ");
		
		PACKAGELOOKUP.put(DataPackage.getIntFromByte(info.getId()), info);
	}
	
	public void setPackageCallBack(byte[] id, PackageCallBack callback) {
		setPackageCallBack(DataPackage.getIntFromByte(id), callback);
	}
	
	public void setPackageCallBack(int id, PackageCallBack callback) {
		if(getPackageInfo(id) == null) {
			throw new IllegalArgumentException("Could not find package with id: " + id);
		}
		PackageInfo info = getPackageInfo(id);
		info.setCallback(callback);
		PACKAGELOOKUP.put(id, info);
	}
	
	public void clearPackages() {
		this.PACKAGELOOKUP.clear();
	}
	
	public PackageInfo getPackageInfo(byte[] id) {
		return getPackageInfo(DataPackage.getIntFromByte(id));
	}
	
	public PackageInfo getPackageInfo(int id) {
		return PACKAGELOOKUP.get(id);
	}
	
	public boolean hasPackage(byte[] id) {
		return PACKAGELOOKUP.get(DataPackage.getIntFromByte(id)) != null;
	}
	
}