package com.server.packageing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class PackageRegistrationManager {

	private HashMap<Class<? extends PackageManager>, Set<PackageInfo>> REGISTERED_PACKAGES = new HashMap<>();
	
	public PackageRegistrationManager() {
		
	}
	
	public void register(Class<? extends PackageManager> type, byte[] id, short length, boolean dynamicLength, PackageConstructor construct) {
		register(type, id, length, dynamicLength, construct, null);
	}
	
	public void register(Class<? extends PackageManager> type, byte[] id, short length, boolean dynamicLength, PackageConstructor construct, PackageCallBack packageCallBack) {
		register(type, new PackageInfo(id, length, dynamicLength, construct, packageCallBack));
	}
	
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
			throw new IllegalArgumentException("Cannot register package with id: (" + DataPackage.getIntFromByte(info.getId()) + ")! A package is already registered under that id! ");
		
		if(getAllPackagesForManager(type) == null)
			REGISTERED_PACKAGES.put(type, new HashSet<>());
		
		Set<PackageInfo> set = getAllPackagesForManager(type);
		set.add(info);
		REGISTERED_PACKAGES.put(type, set);
	}
	
	public PackageInfo getPackageInfo(Class<? extends PackageManager> type, byte[] id) {
		return getPackageInfo(type, DataPackage.getIntFromByte(id));
	}
	
	public PackageInfo getPackageInfo(Class<? extends PackageManager> type, int id) {
		Set<PackageInfo> out = REGISTERED_PACKAGES.get(type);
		if(out == null) return null;
		for(PackageInfo i : out) {
			if(id == DataPackage.getIntFromByte(i.getId())) {
				return i;
			}
		}
		return null;
	}
	
	public Set<PackageInfo> getAllPackagesForManager(Class<? extends PackageManager> type){
		return REGISTERED_PACKAGES.get(type);
	}
	
	public boolean hasPackage(Class<? extends PackageManager> type, byte[] id) {
		return getPackageInfo(type, id) != null;
	}
}
