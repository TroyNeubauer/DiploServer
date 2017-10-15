package com.troy.diplo.server.commands;

import com.troy.diplo.game.*;
import com.troyberry.util.serialization.*;

public class AccountSerializer extends TroySerializer<Account> {

	@Override
	public void read(Account obj, TroySerializationFile file, TroyBuffer buffer) {
		obj.id = buffer.readInt();
		obj.username = buffer.readString();
		obj.email = buffer.readString();
		obj.profile = (Profile) file.read();
	}

	@Override
	public void write(Account obj, TroySerializationFile file, TroyBuffer buffer) {
		buffer.writeInt(obj.id);
		buffer.writeString(obj.username);
		buffer.writeString(obj.email);
		file.write(obj.profile);
	}

}
