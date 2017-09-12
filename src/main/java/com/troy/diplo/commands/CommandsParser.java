package com.troy.diplo.commands;

import com.troy.diplo.server.DiploServer;
import com.troyberry.util.MiscUtil;

public class CommandsParser {

	private DiploServer server;

	public CommandsParser(DiploServer server) {
		this.server = server;
	}

	public boolean parse(String line) {
		if (line.equalsIgnoreCase("stop") || line.equalsIgnoreCase("end") || line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
			return true;
		}
		try {
			String[] commands = line.split(" ");
			if (commands[0].equalsIgnoreCase("register")) {
				try {
					server.registerUser(commands[1], commands[2].toCharArray(), commands[3]);
				} catch (IllegalStateException e) {
					System.out.println("Unable to register user. User with name \"" + commands[1] + "\" already exists!");
					return false;
				}
				char[] password = new char[commands[2].length()];
				password[0] = commands[2].charAt(0);
				password[password.length - 1] = commands[2].charAt(password.length - 1);
				for (int i = 1; i < password.length - 1; i++)
					password[i] = '*';
				System.out.println("registering user {username \"" + commands[1] + "\", password \"" + new String(password) + "\" email \""
						+ commands[3] + "\"");
			}
			if (commands[0].equalsIgnoreCase("auth")) {
				System.out.println(server.areCredentialsValid(commands[1], commands[2].toCharArray()) ? "Authentication succscful!" : "Invalid username or password");
			}
		} catch (Exception e) {
			System.out.println("Unknown command \"" + line + "\"\n" + MiscUtil.getStackTrace(e));
		}
		return false;
	}

}
