package com.server.basepackages;

import com.server.packageing.DataPackage;
import com.server.packageing.annotations.DataPackageConstructor;
import com.server.packageing.annotations.DataPackageDynamic;
import com.server.packageing.annotations.DataPackageID;
import com.server.packageing.annotations.DataPackageLength;

public class CloseConnection extends DataPackage{

	@DataPackageDynamic
	public static boolean IS_DYNAMIC_LENGTH = false;
	
	@DataPackageLength
	public static short PACK_LENGTH = (short)0;

	@DataPackageID
	public static byte[] ID = new byte[] {(byte)0x0, (byte)0x2};
	
	@DataPackageConstructor
	public CloseConnection() {
		super(PACK_LENGTH, IS_DYNAMIC_LENGTH, new byte[0]);
	    this.setId(ID);
	}
	
}
