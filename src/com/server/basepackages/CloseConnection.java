package com.server.basepackages;

import com.server.packageing.DataPackage;

public class CloseConnection extends DataPackage{

	public static short PACK_LENGTH = (short)0;
	public static byte[] ID = new byte[] {(byte)0x0, (byte)0x2};
	
	public CloseConnection() {
		super(PACK_LENGTH, false, new byte[0]);
		this.setId(ID);
	}
	
	
}
