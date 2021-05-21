package com.server.basepackages;

import com.server.packageing.DataPackage;

public class RemoteClosedConnection extends DataPackage{

	public static boolean IS_DYNAMIC_LENGTH = false;
	public static short PACK_LENGTH = (short)0;
	public static byte[] ID = new byte[] {0x0 , 0x0};
	
	public RemoteClosedConnection() {
		super(PACK_LENGTH, IS_DYNAMIC_LENGTH, new byte[] {});
		this.setId(ID);
	}

}
