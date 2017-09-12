package com.troy.diplo.server.database;

import java.io.*;
import java.nio.ByteOrder;
import java.security.SecureRandom;
import java.util.Arrays;

import org.apache.logging.log4j.*;

import com.troy.diplo.game.Account;
import com.troy.diplo.server.DiploServer;
import com.troy.diplo.server.secutiry.Security;
import com.troyberry.util.MiscUtil;
import com.troyberry.util.serialization.TroyBuffer;

/**
 * Represents a database of users
 * 
 * @author Troy Neubauer
 *
 */
public class UserDatabase {

	private static final int DEFAULT_ITERATIONS = 50000, DEFAULT_HASH_BYTES = 64, DEFAULT_SALT_BYTES = 32, DEFAULT_PEPPER_BYTES = 16;

	private static final Logger logger = LogManager.getLogger(UserDatabase.class);

	private File file;
	private TroyBuffer buffer;

	private UserList users;
	private GameList games;
	private TeamList teams;

	private File pepperFile, accountsFile, teamsFile, gamesFile;
	private byte[] pepper;

	private int currentIterations;

	private int hashBytes, saltBytes;

	private int totalUsers;

	private SecureRandom random = new SecureRandom();

	public static UserDatabase loadOrCreateDatabase(DiploServer server, File file) throws IOException {
		if (file.exists()) {
			logger.info("Loading extisting database at " + file);
			return new UserDatabase(server, file);
		} else {
			logger.info("Creating new databaseat at " + file);
			return createDatabase(server, file);
		}
	}

	public static UserDatabase createDatabase(DiploServer server, File file) throws IOException {
		return createDatabase(server, file, DEFAULT_ITERATIONS, DEFAULT_HASH_BYTES, DEFAULT_SALT_BYTES, DEFAULT_PEPPER_BYTES);
	}

	public static UserDatabase createDatabase(DiploServer server, File file, int currentIterations, int hashBytes, int saltBytes, int pepperBytes)
			throws IOException {
		file.delete();
		file.createNewFile();
//pepperFile, accountsFile, teamsFile, gamesFile;
		String parent = file.getParent();
		File pepperFile = new File(parent, "pepper.dat"), accountsFile = new File(parent, "accounts.dat"),
				teamsFile = new File(parent, "teams.dat"), gamesFile = new File(parent, "games.dat");
		pepperFile.delete();
		pepperFile.createNewFile();

		byte[] pepper = new byte[pepperBytes];
		new SecureRandom().nextBytes(pepper);

		FileOutputStream stream = new FileOutputStream(pepperFile);
		stream.write(pepper);
		stream.close();

		TroyBuffer buffer = TroyBuffer.create();
		buffer.setWriteOrder(ByteOrder.BIG_ENDIAN);
		buffer.setReadOrder(ByteOrder.BIG_ENDIAN);
		buffer.writeString(pepperFile.getAbsolutePath());
		buffer.writeString(accountsFile.getAbsolutePath());
		buffer.writeString(teamsFile.getAbsolutePath());
		buffer.writeString(gamesFile.getAbsolutePath());
		
		buffer.writeInt(currentIterations);
		buffer.writeInt(hashBytes);
		buffer.writeInt(saltBytes);
		buffer.writeInt(0);// Total users
		buffer.writeToFile(file);

		return new UserDatabase(server, file);
	}

	public UserDatabase(DiploServer server, File file) throws IOException {
		server.setDatabase(this);
		this.file = file;
		loadExistingSettings();
	}

	private void loadExistingSettings() throws IOException {
		openFile();
		this.pepperFile = new File(buffer.readString());
		this.accountsFile = new File(buffer.readString());
		this.teamsFile = new File(buffer.readString());
		this.gamesFile = new File(buffer.readString());
		this.currentIterations = buffer.readInt();
		this.hashBytes = buffer.readInt();
		this.saltBytes = buffer.readInt();
		this.totalUsers = buffer.readInt();
		this.pepper = MiscUtil.readToByteArray(pepperFile);
		
		
		this.games = new GameList(gamesFile);
		this.users = new UserList(accountsFile);
		this.teams = new TeamList(teamsFile);
		
		games.readIDs();

	}

	private void openFile() throws IOException {
		buffer = TroyBuffer.create(file);
		buffer.setReadOrder(ByteOrder.BIG_ENDIAN);
		buffer.setReadOrder(ByteOrder.BIG_ENDIAN);
	}

	public void cleanUp() {
		buffer.clear();
		buffer.writeString(pepperFile.getAbsolutePath());
		buffer.writeString(accountsFile.getAbsolutePath());
		buffer.writeString(teamsFile.getAbsolutePath());
		buffer.writeString(gamesFile.getAbsolutePath());
		buffer.writeInt(currentIterations);
		buffer.writeInt(hashBytes);
		buffer.writeInt(saltBytes);
		buffer.writeInt(totalUsers);

		try {
			buffer.writeToFile(file);
			logger.info("Saved database to file " + file);
		} catch (IOException e) {
			logger.fatal("Unable to save database!\n" + MiscUtil.getStackTrace(e));
		}
		users.cleanUp();
		games.cleanUp();
		teams.cleanUp();

	}

	public DatabaseAccount registerUser(String username, char[] password, String email) {
		if (users.containsUsername(username))
			throw new IllegalStateException("An account with username \"" + username + "\" already exists!");
		byte[] salt = new byte[saltBytes];
		random.nextBytes(salt);

		byte[] hash = Security.getHashedPassword(password, salt, pepper, currentIterations, hashBytes);
		DatabaseAccount account = new DatabaseAccount(new Account(totalUsers++, username, email), currentIterations, salt, hash, this);
		users.add(account);
		return account;
	}

	public final boolean areCredentialsValid(String username, char[] password) {
		if (username == null)
			return false;
		if (password == null)
			return false;
		if (username.length() == 0 || password.length == 0)
			return false;

		if (users.containsUsername(username)) {
			DatabaseAccount account = users.getAccount(username);
			byte[] storedHash = account.getHash();
			byte[] computedHash = Security.getHashedPassword(password, account.getSalt(), pepper, account.getIterations(), hashBytes);
			if (storedHash.length != computedHash.length)
				return false;
			for (int i = 0; i < storedHash.length; i++) {
				if (storedHash[i] != computedHash[i]) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public boolean containsUser(String username) {
		return users.containsUsername(username);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((buffer == null) ? 0 : buffer.hashCode());
		result = prime * result + currentIterations;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + hashBytes;
		result = prime * result + Arrays.hashCode(pepper);
		result = prime * result + ((pepperFile == null) ? 0 : pepperFile.hashCode());
		result = prime * result + ((random == null) ? 0 : random.hashCode());
		result = prime * result + saltBytes;
		result = prime * result + ((users == null) ? 0 : users.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserDatabase other = (UserDatabase) obj;
		return this.users.equals(other.users);
	}

	public File getFile() {
		return file;
	}

	public UserList getUsers() {
		return users;
	}

	public File getPepperFile() {
		return pepperFile;
	}

	public byte[] getPepper() {
		return pepper;
	}

	public int getCurrentIterations() {
		return currentIterations;
	}

	public int getHashBytes() {
		return hashBytes;
	}

	public int getSaltBytes() {
		return saltBytes;
	}

	public DatabaseAccount getUser(String username) {
		return users.getAccount(username);
	}

}
