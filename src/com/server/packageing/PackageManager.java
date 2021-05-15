package com.server.packageing;

import java.util.HashMap;

import com.server.basepackages.CloseConnection;
import com.server.basepackages.KeepAlive;
import com.server.basepackages.MessagePackage;
import com.server.basepackages.PostData;
import com.server.basepackages.ReconnectPackage;
import com.server.basepackages.RequestData;


public class PackageManager {

	private HashMap<Integer, PackageInfo> PACKAGELOOKUP = new HashMap<>();
	
	public PackageManager() {
		register(CloseConnection.ID, CloseConnection.PACK_LENGTH, CloseConnection.IS_DYNAMIC_LENGTH, (l,d,b) -> {return new CloseConnection();});
		register(KeepAlive.ID, KeepAlive.PACK_LENGTH, KeepAlive.IS_DYNAMIC_LENGTH, (l,d,b) -> {return new KeepAlive();});
		register(MessagePackage.ID, MessagePackage.PACK_LENGTH, MessagePackage.IS_DYNAMIC_LENGTH, (l,d,b) -> {return new MessagePackage(b);});
		register(PostData.ID, PostData.PACK_LENGTH, PostData.IS_DYNAMIC_LENGTH, (l,d,b) -> {return new PostData(b);});
		register(ReconnectPackage.ID, ReconnectPackage.PACK_LENGTH, ReconnectPackage.IS_DYNAMIC_LENGTH, (l,d,b) -> {return new ReconnectPackage(b);});
		register(RequestData.ID, RequestData.PACK_LENGTH, RequestData.IS_DYNAMIC_LENGTH, (l,d,b) -> {return new RequestData(b);});
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