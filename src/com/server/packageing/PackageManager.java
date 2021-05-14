package com.server.packageing;

import java.util.HashMap;

public class PackageManager {

	private static HashMap<Integer, PackageInfo> PACKAGELOOKUP = new HashMap<>();
	
	public static void register(byte[] id, short length, boolean dynamicLength, PackageConstructor construct) {
		register(id, length, dynamicLength, construct, null);
	}
	
	public static void register(byte[] id, short length, boolean dynamicLength, PackageConstructor construct, PackageCallBack packageCallBack) {
		register(new PackageInfo(id, length, dynamicLength, construct, packageCallBack));
	}
	
	public static void register(PackageInfo info) {
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
	
	public static void setPackageCallBack(byte[] id, PackageCallBack callback) {
		setPackageCallBack(DataPackage.getIntFromByte(id), callback);
	}
	
	public static void setPackageCallBack(int id, PackageCallBack callback) {
		if(getPackageInfo(id) == null) {
			throw new IllegalArgumentException("Could not find package with id: " + id);
		}
		PackageInfo info = getPackageInfo(id);
		info.setCallback(callback);
		PACKAGELOOKUP.put(id, info);
	}
	
	public static PackageInfo getPackageInfo(byte[] id) {
		return getPackageInfo(DataPackage.getIntFromByte(id));
	}
	
	public static PackageInfo getPackageInfo(int id) {
		return PACKAGELOOKUP.get(id);
	}
	
}