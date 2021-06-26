package com.logger;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Printer {

	private PrintingType printType;
	private FileAccess fileAccess;
	
	private String formatPatten = "HH:mm:ss";
	
	private OutputStream out = System.out;
	
	private PrintMode mode;
	
	Printer(PrintingType p, PrintMode mode, FileAccess f) {
		this.printType = p;
		fileAccess = f;
		this.mode = mode;
		setFormate();
	}

	public void print(Level level, String s) {
		String toPrint = "[" + getFormatedTime() + " " + level.toString() + "]:" + s;

		switch (printType) {
		case Console:
			consolePrint(toPrint + "\n");
			break;
		case FlatFile:
			flatFile(toPrint);
			break;
		case FileAndConsole:
			consolePrint(toPrint + "\n");
			flatFile(toPrint);
			break;
		default:
			return;
		}

	}
	
	public String getFormatedTime() {
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern(formatPatten));
	}

	private void consolePrint(String s) {
		try {
			out.write(s.getBytes());
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setOutputStream(OutputStream out) {
		this.out = out;
	}
	
	private void flatFile(String s) {
		if(fileAccess != null) {
			fileAccess.write(s);
		}
	}

	private void setFormate() {
		if(mode == PrintMode.DebugDetaild || mode == PrintMode.EventDetaild) {
			formatPatten = "MM.dd.yyyy-HH:mm:ss";
		}
	}

	public void setMode(PrintMode mode) {
		this.mode = mode;
		setFormate();
	}
}
