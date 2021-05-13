package com.logger;

class ConsoleColor {

	public static String getColor(int r, int g, int b) {
		return "\033[38;2;" + r + ";" + g + ";" + b + "m";
	}
	
	public static String getColor(int rbg, int gbg, int bbg, int rfg, int gfg, int bfg) {
		return "\033[38;2;" + rfg + ";" + gfg + ";" + bfg + ";48;2;" + rbg + ";" + gbg + ";" + bbg + "m";
	}
	
	public static String getColorBG(int r, int g, int b) {
		return "\033[48;2;" + r + ";" + g + ";" + b + "m";
	}
	
}
