package com.server.basepackages;

import com.server.packageing.DataPackage;

public class CloseConnection extends DataPackage{

	public static boolean IS_DYNAMIC_LENGTH = false;
	public static short PACK_LENGTH = (short)0;
	public static byte[] ID = new byte[] {(byte)0x0, (byte)0x2};
	
	public CloseConnection() {
		super(PACK_LENGTH, IS_DYNAMIC_LENGTH, new byte[0]);
		this.setId(ID);
	}
	
	
}
