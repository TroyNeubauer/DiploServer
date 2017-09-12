package com.troy.diplo.server;

import java.io.*;

import org.apache.logging.log4j.*;

import com.troy.diplo.server.database.*;
import com.troy.diplo.server.net.DiploServerNet;

public class DiploServer {

	private static final Logger logger = LogManager.getLogger(DiploServer.class);

	private UserDatabase database;
	private DiploServerNet net;

	public DiploServer(File databaseFile) {
		Main.serverAccess = this;
		try {
			this.database = UserDatabase.loadOrCreateDatabase(this, databaseFile);
		} catch (IOException e) {
			logger.fatal("Unable to load database");
			e.printStackTrace();
			this.forceShutdown(1);
		}
		this.net = new DiploServerNet(this);
	}

	public final boolean areCredentialsValid(String username, char[] password) {
		return database.areCredentialsValid(username, password);
	}

	public DatabaseAccount registerUser(String username, char[] password, String email) {
		return database.registerUser(username, password, email);
	}

	public void shutdown() {
		logger.info("Preparing to shutdown server");
		cleanUp();
		joinThreads();
		logger.info("server shutdown");
	}

	private void joinThreads() {
		net.join();
	}

	public void forceShutdown(int code) {
		logger.info("Preparing to force shutdown the server");
		cleanUp();
		logger.info("server shutdown");
		System.exit(code);
	}

	public void cleanUp() {
		if (net != null)
			net.cleanUp();
		if (database != null)
			database.cleanUp();
	}

	public DatabaseAccount getAccount(String username) {
		return database.getUser(username);
	}

	public UserDatabase getDatabase() {
		return database;
	}
	
	public void setDatabase(UserDatabase database) {
		this.database = database;
	}
}
