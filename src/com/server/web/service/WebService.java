package com.server.web.service;

//import java.io.File;
//import java.io.IOException;
//import java.net.InetSocketAddress;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//
//import com.logger.Level;
//import com.server.main.Server;
//import com.sun.net.httpserver.HttpServer;

public class WebService {

//	private int port;
	
//	private HttpServer webServer;
//	String fs = System.getProperty("file.separator");
//	String path = System.getProperty("user.dir") + fs + "res" + fs + "WebTool" + fs + "index.html";
//	File indexPage;
	public WebService(int port) {
//		indexPage = new File(path);
//		
//		if(indexPage != null && indexPage.exists())
//			Server.logger.log(Level.INFO, "Got File");
//		else
//			return;
//		
////		try {
////			System.out.println(Files.readString(Paths.get(path)));
////		} catch (IOException e1) {
////			e1.printStackTrace();
////		}
//		
//		this.port = port;
//		try {
//			webServer = HttpServer.create(new InetSocketAddress(port), 0);
//		} catch (IOException e) {
//			Server.logger.log(Level.ERROR, e.getMessage());
//		}
//		if(webServer != null) {
//			init();
//			webServer.start();
//		}
		
	}
	
	public void init() {
//		webServer.createContext("/", (he)->{
////			Server.logger.log(Level.INFO, he.);
//			if(indexPage != null) {
//				byte[] output = Files.readAllBytes(indexPage.toPath());
//				he.sendResponseHeaders(200, output.length);
//				he.getResponseBody().write(output);
//				he.getResponseBody().flush();
//				he.getResponseBody().close();
//			}else {
//				String responce = "<h1>Page not found!</h1>";
//				he.sendResponseHeaders(404, responce.length());
//				he.getResponseBody().write(responce.getBytes());
//				he.getResponseBody().flush();
//				he.getResponseBody().close();
//			}
//			
//
//		});
	}
	
}
