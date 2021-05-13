package com.logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

class FileAccess {

	private File f = null;
	private long fileSize = 1000000;
	private String path = "";
	private String fileName = "";
	
	private int fileCount = 0;
	
	FileAccess(String path) {
		
		if(path == "" || path == null) path = System.getProperty("user.dir");
		
		//seperating full path with file name from just path
		//case: folder/folder/file.type
		//case: folder/folder/ or folder/folder
		//case: file.type
		//if no type is given its a path
		if(path.split(".").length > 1) {
			String[] parts = path.split(System.getProperty("file.separator"));
			if(parts.length > 1) {
				fileName = parts[parts.length-1];
				this.path = path.replace(fileName, "");
			}else {
				this.path = System.getProperty("user.dir");
			}
		}else {
			this.path = path;
		}
		
		String date = LocalDate.now().toString();
		File local = new File(path);
		
		if(local.exists()) {
			for(File f : local.listFiles()) {
				if(f.isFile()) {
					if(this.f == null) this.f = f;
					if(f.getName().compareTo(this.f.getName()) > 0 
							&& f.getName().contains(date.toString())
							&& f.length() < fileSize) {
						this.f = f;
					}
				}
				if(f.getName().contains(date)) {
					fileCount++;
				}
			}
		}else {
			local.mkdirs();
		}
		
		if(f == null) {
			fileCount++;
			f = new File(path + System.getProperty("file.separator") + "Log-" + date + "_" + fileCount + ".log");
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void write(String s) {
		s += "\n";
		if(f.length() < fileSize) {
			FileWriter writer;
			try {
				writer = new FileWriter(f, true);
				writer.write(s);
				writer.flush();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			fileCount++;
			String date = LocalDate.now().toString();
			f = new File(path + System.getProperty("file.separator") + "Log-" + date + "_" + fileCount + ".log");
		
			FileWriter writer;
			try {
				writer = new FileWriter(f, true);
				writer.write(s);
				writer.flush();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//System.out.println("File written: " + f.getPath() + " - " + f.getName());
	}
	
}
