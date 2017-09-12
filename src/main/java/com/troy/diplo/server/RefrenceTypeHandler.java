package com.troy.diplo.server;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.apache.logging.log4j.*;

import com.troy.diplo.game.*;
import com.troyberry.util.MiscUtil;
import com.troyberry.util.serialization.TroyBuffer;

public class RefrenceTypeHandler<T extends Refrence> implements TypeReader<T> {
	private static final Logger logger = LogManager.getLogger();

	private File refrenceFile;
	private HashMap<Integer, T> refrences = new HashMap<Integer, T>();
	private TroyBuffer buffer;
	private int refrenceCount;
	private Class<?> genericType = MiscUtil.getGenericType(this);

	public RefrenceTypeHandler(File refrenceFile) {
		this.refrenceFile = refrenceFile;
		if (refrenceFile.exists()) {
			this.buffer = TroyBuffer.create(refrenceFile);
			readFile();
		} else {
			this.buffer = TroyBuffer.create();
			this.refrenceCount = 0;
		}
	}

	@Override
	public void addAndSetID(T obj) {
		int id = refrenceCount++;
		obj.setID(id);
		refrences.put(id, obj);
	}

	@Override
	public T lookup(int id) {
		return refrences.get(id);
	}

	private List<T> tempList;

	private void readFile() {
		this.refrenceCount = buffer.readInt();
		this.tempList = new ArrayList<T>((int) refrenceCount);
		for (int i = 0; i < refrenceCount; i++) {
			T obj = (T) TroyBuffer.createInstance(genericType);
			obj.read(buffer);
			tempList.add(obj);
		}
	}

	@Override
	public void readIDs() {
		if (tempList != null) {
			for (T obj : tempList) {
				obj.readIDs();
				refrences.put(obj.getID(), obj);
				onReadIDs(obj);
			}
		}
	}

	public void onReadIDs(T obj) {

	}

	public void cleanUp() {
		buffer.clear();
		assert refrences.size() == refrenceCount;
		buffer.writeInt(refrences.size());
		for (Entry<Integer, T> entry : refrences.entrySet()) {
			entry.getValue().write(buffer);
		}
		try {
			buffer.writeToFile(refrenceFile);
		} catch (IOException e) {
			logger.fatal("Unable to write games file! " + refrenceFile);
			logger.catching(Level.FATAL, e);
		}
	}
}
