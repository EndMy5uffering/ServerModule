package com.server.basepackages;

import com.server.packageing.DataPackage;

public class PostData extends DataPackage{

	public static boolean IS_DYNAMIC_LENGTH = true;
	public static short PACK_LENGTH = (short)4;
	public static byte[] ID = new byte[] {0x0 , 0x5};
	
	public PostData(byte[] byteDataRaw) {
		super(PACK_LENGTH, true, byteDataRaw);
		this.setId(ID);
	}

}
