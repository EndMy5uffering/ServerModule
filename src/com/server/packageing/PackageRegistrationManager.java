package com.server.packageing;

import java.lang.reflect.Field;
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
import com.server.main.Server;

public class PackageRegistrationManager {

	private HashMap<Class<? extends PackageManager>, Set<PackageInfo>> REGISTERED_PACKAGES = new HashMap<>();
	
	public PackageRegistrationManager() {
		register(DefaultPackageManager.class, CloseConnection.class, (l,d,b) -> {return new CloseConnection();});
		register(DefaultPackageManager.class, KeepAlive.class, (l,d,b) -> {return new KeepAlive();});
		register(DefaultPackageManager.class, MessagePackage.class, (l,d,b) -> {return new MessagePackage(b);});
		register(DefaultPackageManager.class, PostData.class, (l,d,b) -> {return new PostData(b);});
		register(DefaultPackageManager.class, ReconnectPackage.class, (l,d,b) -> {return new ReconnectPackage(b);});
		register(DefaultPackageManager.class, RequestData.class, (l,d,b) -> {return new RequestData(b);});
		register(DefaultPackageManager.class, RemoteClosedConnection.class, (l,d,b) -> {return new RemoteClosedConnection();}, (data, con) -> {
			con.disable(Level.INFO, "Remote closed connection! Stream ended.");
		});
	}
	
	public void register(Class<? extends PackageManager> type, Class<? extends DataPackage> pack, PackageConstructor construct) {
		register(type, pack, construct, null);
	}
	
	public void register(Class<? extends PackageManager> type, Class<? extends DataPackage> pack, PackageConstructor construct, PackageCallBack packageCallBack) {
		
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
		register(type, id, length, dynLength, construct, packageCallBack);
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
	
	public void clearPackageManager(Class<? extends PackageManager> type) {
		this.REGISTERED_PACKAGES.put(type, new HashSet<>());
	}
	
	public void setPackageCallBack(Class<? extends PackageManager> type, byte[] id, PackageCallBack callback) {
		setPackageCallBack(type, DataPackage.getIntFromByte(id), callback);
	}
	
	public void setPackageCallBack(Class<? extends PackageManager> type, int id, PackageCallBack callback) {
		if(getPackageInfo(type, id) == null) {
			throw new IllegalArgumentException("Could not find package with id: " + id);
		}
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
}
