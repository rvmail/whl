package com.dopool.icntvoverseas.entity;

import java.io.Serializable;
import java.util.ArrayList;

public class ChannelItem implements Serializable{

	private String name;
	
	private ArrayList<String> properties;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<String> getProperties() {
		return properties;
	}

	public void setProperties(ArrayList<String> properties) {
		this.properties = properties;
	}
	
}
