package com.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import com.logger.Level;
import com.server.main.Server;
import com.server.main.SessionID;
import com.server.packageing.DataPackage;
import com.server.packageing.PackageInfo;
import com.server.packageing.PackageManager;
import com.server.packageing.UnknownPackageCallback;


enum State{
	Active,
	Dead
}

public class ClientConnection{

	private final SessionID id;
	private SessionID customID;
	private String connectionName = "";
	
	private Socket socket;
	private Server server;
	private PackageManager packageManager;
	
	private Thread clientThread;

	private State state;
	
	private OutputStream out;
	private InputStream reader;
	
	private List<ClientPackageReceiveCallback> callback = new ArrayList<ClientPackageReceiveCallback>();
	private UnknownPackageCallback unknownPackageCallback = null;
	private ClientTimeOutCallback clientTimeOutCallback = null;
	private ClientDisconnectCallback clientDisconnectCallback = null;
	
	public ClientConnection(Socket socket, Server server, PackageManager packageManager, SessionID uuid){
		this(socket, server, packageManager, -1, uuid);
	}
	
	public ClientConnection(Socket socket, Server server, PackageManager packageManager, int timeout, SessionID uuid) {
		if(socket == null) {
			this.state = State.Dead;
		}
		this.packageManager = packageManager;
		this.socket = socket;
		this.server = server;
		
		try {
			out = socket.getOutputStream();
			this.reader = socket.getInputStream();
		} catch (IOException e) {
			out = null;
			this.reader = null;
			Server.logger.log(Level.ERROR, e, e.getClass());
		}
		
		if(timeout > 0) {
			try {
				this.socket.setSoTimeout(timeout);
			} catch (SocketException e) {
				Server.logger.log(Level.ERROR, e, e.getClass());
			}
		}
		this.id = uuid;
		this.state = State.Active;
	}
	
	
	/**
	 * Enables the client connection and starts the client thread.<br>
	 * When enabled the client connection will listen to incoming packages.<br>
	 * All received packages are relayed back via the callback functions.<br>
	 * All packages can also define there on callback function.<br>
	 * <br>
	 * 
	 * <b>The client will be automatically disconnected when a faulty package is received.</b>
	 * 
	 * */
	public void enable() {
		this.clientThread = new Thread(() -> {
			if(this.state != State.Active) return;
			try {
				if(reader == null) return;
				while(this.state == State.Active) {
					DataPackage dataOut = null;
					byte[] data = new byte[DataPackage.IDLENGTH];
					
					reader.read(data);
					if(packageManager == null) {
						Server.logger.log(Level.ERROR, "Package manager can not be null!");
						disable();
						return;
					}
					PackageInfo info = this.packageManager.getPackageInfo(data);
					
					if(info == null) {
						Server.logger.log(Level.ERROR, "Unknown package recived by: " + socket.getInetAddress().toString());
						Server.logger.log(Level.ERROR, "Unknown package id: " + DataPackage.getIntFromByte(data));
						if(this.unknownPackageCallback != null) unknownPackageCallback.handle(data, this);
						disable();
						return;
					}
					if(!info.isDynamicLength()) {
						
						byte[] rawData = new byte[info.getLength()];
						reader.read(rawData);
						dataOut = info.getConstruct().build(info.getLength(), info.isDynamicLength(), rawData);
					
					}else {
						
						byte[] rawData = new byte[info.getLength()];
						reader.read(rawData);
						int length = DataPackage.getIntFromByte(rawData);
						if(length >= 0 && length < Server.getMaxPackageSize()) {
							rawData = new byte[length];
							reader.read(rawData);
						}else if(length > Server.getMaxPackageSize() || length < 0) {
							Server.logger.log(Level.ERROR, "Size missmatch!");
							Server.logger.log(Level.ERROR, "PackageID: \t" + DataPackage.getIntFromByte(info.getId()));
							Server.logger.log(Level.ERROR, "Length: \t" + length);
							break;
						}
					
						dataOut = info.getConstruct().build(info.getLength(), info.isDynamicLength(), rawData);
					
					}
					
					for(ClientPackageReceiveCallback event : callback) event.call(dataOut, this);
					if(dataOut != null && info.getCallback() != null) info.getCallback().call(dataOut, this);
					if(this.state != State.Active) return;
				}
			}catch(SocketTimeoutException timeout) {
				if(this.clientTimeOutCallback != null) {
					try {
						clientTimeOutCallback.call(this);
					} catch (Exception e) {
						Server.logger.log(Level.ERROR, "The timeout callback caused an error while executing!");
						Server.logger.log(Level.ERROR, "The connection will be closed!");
					}
				}
				this.disable();
				Server.logger.log(Level.WARNING, "Connection timed out for: " + this.socket.getInetAddress().getHostAddress());
			}catch (IOException e) {
				if(this.state != State.Dead) {
					Server.logger.log(Level.ERROR, e.getMessage());
				}	
			} 
			disable();
		});
		
		this.clientThread.start();
	}
	
	/**
	 * Will send a data package to the client that is connected.<br>
	 * 
	 * @param data
	 * */
	public void send(DataPackage data) {
		if(out == null) return;
		if(this.state == State.Active) {
			try {
				out.write(data.pack());
				out.flush();
			} catch (IOException e) {
				Server.logger.log(Level.ERROR, e, e.getClass());
				disable();
			}
		}
	}
	
	/**
	 * Disable function that can log an error if the connection had to be disabled in a catch block.
	 * */
	public void disable(Level level, String log) {
		Server.logger.log(level, log);
		disable();
	}
	
	/**
	 * Disables the client connection and stops all the in and output streams and removes the connection form the manager.
	 * */
	public void disable() {
		Server.logger.log(Level.INFO, "Disabling connection for: " + socket.getInetAddress().getHostAddress());
		this.state = State.Dead;
		try {
			if(socket != null) this.socket.close();
			if(reader != null) this.reader.close();
			if(out != null) this.out.close();
		} catch (IOException e) {
			Server.logger.log(Level.ERROR, e, e.getClass());
		}
		if(this.server != null) {
			this.server.getClientManager().removeClient(this);
		}else {
			Server.logger.log(Level.WARNING, "Could not remove client from any clien manager: Server was null");
		}
		
		if(this.clientDisconnectCallback != null) clientDisconnectCallback.call(this);
		
	}

	/**
	 * Returns the list of callback function for received packages.
	 * */
	public List<ClientPackageReceiveCallback> getClientPackageReceiveCallbacks() {
		return callback;
	}

	/**
	 * Adds a callback function for the client to call when a package has been received.
	 * 
	 * @param callback A callback function that takes the <b>package</b> as <b>DataPackage</b> and the <b>connection</b>.
	 * */
	public void addClientPackageReceiveCallback(ClientPackageReceiveCallback callback) {
		this.callback.add(callback);
	}
	
	/**
	 * Sets the list of callback functions for the connection to invoke when a package has been received.
	 * 
	 * @param callback A list of callback functions that takes the <b>package</b> as <b>DataPackage</b> and the <b>connection</b>.
	 * */
	public void setClientPackageReceiveCallback(List<ClientPackageReceiveCallback> callback) {
		this.callback = callback;
	}

	public PackageManager getPackageManager() {
		return packageManager;
	}

	/**
	 * Sets the package manager for the client connection.<br>
	 * 
	 * @param packageManager The package manager that will be used by the connection. <b>Can not be null!</b>
	 * 
	 * @throws NullPointerException When packageManager == null
	 * */
	public void setPackageManager(PackageManager packageManager) {
		if(packageManager == null)
			throw new NullPointerException("Package Manager can not be null!");
		this.packageManager = packageManager;
	}
	
	/**
	 * Sets the timeout callback function that will be called when a connection did not receive any packages for a set time (timeout).<br>
	 * The function will be called before the connection is closed an disposed of.<br>
	 * Right after execution of the callback the connection will be closed and disposed of.
	 * @param timeout The timeout function that will be executed when a connection timed out
	 * */
	public void setClientTimeOutCallback(ClientTimeOutCallback timeout) {
		this.clientTimeOutCallback = timeout;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public Socket getSocket() {
		return socket;
	}

	public Server getServer() {
		return server;
	}

	public SessionID getId() {
		return id;
	}

	public String getConnectionName() {
		return connectionName;
	}

	/**
	 * Sets the name for the connection for better access.<br>
	 * The name can be used to identify the connection in the <b>ClientManager</b>.<br>
	 * <br>
	 * When giving a name to the connection the client automatically registers the name in the ClientManager.
	 * 
	 * @param connectionName The name of the connection. <b>The name has to be unique!</b>
	 * @throws IllegalArgumentException When the connection was already named.
	 * @throws NullPointerException When connectionName == null or connectionName == ""
	 * 
	 * */
	public void setConnectionName(String connectionName) {
		if(connectionName == "" || connectionName == null)
			throw new NullPointerException("A conneciton name can not be null or empty!");
		if(this.connectionName != "" || server.getClientManager().getClientConnection(this.connectionName) != null)
			throw new IllegalArgumentException("The name of a connection can not be changed when it has been set!");
		
		this.connectionName = connectionName;
	}

	/**
	 * Sets a callback for unknown packages.<br>
	 * When a unknown package was read by the connection the given function will be called.<br>
	 * After the function execution the client connection will be closed to prevent errors caused by unknown data in the input stream.
	 * 
	 * @param unknownPackageCallback A handler function that is called for unknown packages.
	 * */
	public void setUnknownPackageCallback(UnknownPackageCallback unknownPackageCallback) {
		this.unknownPackageCallback = unknownPackageCallback;
	}

	/**
	 * The custom session id is a session id that can be added to help manage the connection.
	 * */
	public SessionID getCustomID() {
		return customID;
	}

	/**
	 * The custom session id is a session id that can be added to help manage the connection.
	 * */
	public void setCustomID(SessionID customID) {
		this.customID = customID;
	}

	/**
	 * Sets a callback function that is invoked when the client is disabled and stopped.<b>
	 * This can happen when an error occurs while receiving a package or due to a time out.<b>
	 * In the best case the client should disconnect without any errors.
	 * 
	 * @param clientDisconnectCallback The callback that is invoked on a client disconnect.
	 * */
	public void setClientDisconnectCallback(ClientDisconnectCallback clientDisconnectCallback) {
		this.clientDisconnectCallback = clientDisconnectCallback;
	}
}
