package com.troy.diplo.server;

import java.io.*;
import java.util.Scanner;

import org.apache.logging.log4j.*;

import com.esotericsoftware.kryo.io.OutputChunked;
import com.troy.diplo.commands.CommandsParser;
import com.troy.diplo.game.*;
import com.troy.diplo.server.database.DatabaseAccount;
import com.troyberry.internal.LibraryUtils;
import com.troyberry.util.*;
import com.troyberry.util.serialization.*;

public class Main {

	public static DiploServer serverAccess;

	private static final Logger logger = LogManager.getLogger(Main.class);

	public static void main(String[] args) throws IOException {
		DiploServer server = null;
		try {
			logger.info("Starting Server");
			System.setSecurityManager(new DiploServerSecutiryManager());
			server = new DiploServer(new File("./database.dat"));
			for (DatabaseAccount account : server.getDatabase().getUsers().getList())
				System.out.println("\t" + account);
			/*
			 * server.registerUser("Troy_Neubauer", "Furminator1".toCharArray(), "troyneubauer@gmail.com"); server.registerUser("Albert_Ding",
			 * "IAMA'DING_DONG".toCharArray(), "ding@dong.com"); server.registerUser("Justin_Kim", "ILikeCompsci".toCharArray(), "jus@kim.com");
			 * server.registerUser("Chas_Huang", "PenitrateYannie!".toCharArray(), "chashuang_1@gmail.com"); server.registerUser("Drew_Gautier",
			 * "Diplo4lyfe!".toCharArray(), "drew@gmail.com");
			 */

			// server.registerUser("NEW_USER!", "YEAH_YEAH".toCharArray(), "newuser@gmail.com");
		} catch (Throwable r) {
			logger.fatal("Unhandled exception in main");
			logger.catching(Level.FATAL, r);
			System.exit(1);
		}
		Scanner scanner = new Scanner(System.in);
		CommandsParser parser = new CommandsParser(server);
		while (true) {
			String line = scanner.nextLine();
			if (parser.parse(line))
				break;

		}

		server.shutdown();
		scanner.close();
	}
}
