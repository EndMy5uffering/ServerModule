package com.server.basepackages;

import com.server.packageing.DataPackage;

public class KeepAlive extends DataPackage{

	public static boolean IS_DYNAMIC_LENGTH = false;
	public static short PACK_LENGTH = (short)0;
	public static byte[] ID = new byte[] {0x0,0x1};
	
	public KeepAlive() {
		super(PACK_LENGTH, false, new byte[] {});
		this.setId(ID);
	}
}
