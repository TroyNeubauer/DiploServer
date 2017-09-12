package com.troy.diplo.server.database;

import java.io.File;

import com.troy.diplo.map.Game;
import com.troy.diplo.server.RefrenceTypeHandler;

public class GameList extends RefrenceTypeHandler<Game> {

	public GameList(File refrenceFile) {
		super(refrenceFile);
	}

}
