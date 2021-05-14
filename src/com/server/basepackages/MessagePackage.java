package com.server.basepackages;

import com.server.packageing.DataPackage;

public class MessagePackage extends DataPackage{

	public static short PACK_LENGTH = (short)4;
	public static byte[] ID = new byte[] {(byte)0x0, (byte)0x6};
	
	public MessagePackage( boolean dynamicLength, byte[] byteDataRaw) {
		super(PACK_LENGTH, dynamicLength, byteDataRaw);
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
