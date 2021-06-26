package com.server.basepackages;

import com.server.packageing.DataPackage;
import com.server.packageing.annotations.DataPackageConstructor;
import com.server.packageing.annotations.DataPackageDynamic;
import com.server.packageing.annotations.DataPackageID;
import com.server.packageing.annotations.DataPackageLength;

public class MessagePackage extends DataPackage{

	@DataPackageDynamic
	public static boolean IS_DYNAMIC_LENGTH = true;
	
	@DataPackageLength
	public static short PACK_LENGTH = (short)4;
	
	@DataPackageID
	public static byte[] ID = new byte[] {(byte)0x0, (byte)0x6};
	
	@DataPackageConstructor(DATA = true)
	public MessagePackage(byte[] byteDataRaw) {
		super(PACK_LENGTH, IS_DYNAMIC_LENGTH, byteDataRaw);
		this.setId(ID);
	}
	
	public MessagePackage(String s) {
		super(PACK_LENGTH, true, null);
		this.setByteDataRaw(s.getBytes());
		this.setId(ID);
	}
	
	public String getMessage() {
		return new String(this.getByteDataRaw());
	}
	
}
