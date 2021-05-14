package com.server.packageing;

import java.util.HashMap;

public class PackageManager {

	private static HashMap<byte[], PackageInfo> PACKAGELOOKUP = new HashMap<>();
	
	public static void register(byte[] id, short length, boolean dynamicLength, PackageConstructor construct) {
		PACKAGELOOKUP.put(id, new PackageInfo(id, length, dynamicLength, construct));
	}
	
	public static PackageInfo getPackageInfo(byte[] id) {
		return PACKAGELOOKUP.get(id);
	}
	
}