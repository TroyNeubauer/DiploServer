package com.troy.diplo.server.database;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

import com.troy.diplo.server.RefrenceTypeHandler;

public class UserList extends RefrenceTypeHandler<DatabaseAccount>{

	private HashMap<String, DatabaseAccount> users;

	public UserList(File refrenceFile) {
		super(refrenceFile);
		this.users = new HashMap<String, DatabaseAccount>();
		readIDs();
	}

	public void add(DatabaseAccount account) {
		super.addAndSetID(account);
		users.put(account.getAccount().getUsername(), account);
	}

	public boolean containsUsername(String username) {
		return users.containsKey(username);
	}

	public DatabaseAccount getAccount(String username) {
		return users.get(username);
	}
	
	@Override
	public void onReadIDs(DatabaseAccount account) {
		users.put(account.getAccount().getUsername(), account);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		UserList other = (UserList) obj;
		if (users == null) {
			if (other.users != null)
				return false;
		} else if (!users.equals(other.users))
			return false;
		return true;
	}

	public List<DatabaseAccount> getList() {
		ArrayList<DatabaseAccount> list = new ArrayList<DatabaseAccount>();
		for (Entry<String, DatabaseAccount> entry : users.entrySet()) {
			list.add(entry.getValue());
		}
		return list;
	}
	
	public HashMap<String, DatabaseAccount> getUsers() {
		return users;
	}
	
	public void cleanUp() {
		super.cleanUp();
	}

}
