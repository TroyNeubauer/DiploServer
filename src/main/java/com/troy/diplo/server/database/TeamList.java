package com.troy.diplo.server.database;

import java.io.File;

import com.troy.diplo.game.Team;
import com.troy.diplo.server.RefrenceTypeHandler;

public class TeamList extends RefrenceTypeHandler<Team> {

	public TeamList(File refrenceFile) {
		super(refrenceFile);
	}

}
