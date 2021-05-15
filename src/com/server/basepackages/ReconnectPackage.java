package com.server.basepackages;

import com.server.packageing.DataPackage;

public class ReconnectPackage extends DataPackage{

	public static boolean IS_DYNAMIC_LENGTH = true;
	public static short PACK_LENGTH = (short)4;
	public static byte[] ID = new byte[] {(byte)0x0, (byte)0x3};
		
	public ReconnectPackage(byte[] byteDataRaw) {
		super(PACK_LENGTH, true, byteDataRaw);
		this.setId(ID);
	}
	
}
