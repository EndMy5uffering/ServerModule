package com.server.packageing;

import java.util.HashMap;

public class PackageManager {

	private static HashMap<Integer, PackageInfo> PACKAGELOOKUP = new HashMap<>();
	
	public static void register(byte[] id, short length, boolean dynamicLength, PackageConstructor construct) {
		register(id, length, dynamicLength, construct, null);
	}
	
	public static void register(byte[] id, short length, boolean dynamicLength, PackageConstructor construct, PackageCallBack packageCallBack) {
		PACKAGELOOKUP.put(DataPackage.getIntFromByte(id), new PackageInfo(id, length, dynamicLength, construct, packageCallBack));
	}
	
	public static PackageInfo getPackageInfo(byte[] id) {
		return getPackageInfo(DataPackage.getIntFromByte(id));
	}
	
	public static PackageInfo getPackageInfo(int id) {
		return PACKAGELOOKUP.get(id);
	}
	
}