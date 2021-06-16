package com.server.asyncworker;

import java.util.concurrent.SynchronousQueue;

import com.logger.Level;
import com.server.main.Server;

public class AsyncExecuter {

	private Thread[] worker;
		
	private SynchronousQueue<AsyncTask> tasks = new SynchronousQueue<>();
	
	public boolean running = true;
	
	public AsyncExecuter() {
		this(1);
	}
	
	public AsyncExecuter(int worker) {
		this.worker = new Thread[worker];
		for(int i = 0; i < this.worker.length; ++i) {
			this.worker[i] = new Thread(() -> {
				
				while(running) {
					try {
						AsyncTask task = tasks.poll();
						if(task != null)
							task.call();
					} catch (Exception e) {
						Server.logger.log(Level.ERROR, "The async execution of a task encoutnered a problem!");
						Server.logger.log(Level.ERROR, e);
					}
				}
				
			});
			this.worker[i].start();
		}
		Server.logger.log(Level.INFO, "Starting backgound worker: " + this.worker.length);
	}
	
	public synchronized void addTask(AsyncTask t) {
		this.tasks.add(t);
	}
	
	public void stopWorker() {
		this.running = false;
		for(Thread t : this.worker) {
			try {
				t.join();
			} catch (InterruptedException e) {
				Server.logger.log(Level.ERROR, "Error while joining worker threads");
			}
		}
		Server.logger.log(Level.INFO, "Worker disabled");
	}
	
}
