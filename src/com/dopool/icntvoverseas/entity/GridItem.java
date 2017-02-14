package com.dopool.icntvoverseas.entity;

import dopool.cntv.base.LocalizedInfo;

public class GridItem {
	
	public static final int HEADER_SECTION = 2;
	public static final int HEADER_FILLTER_SECTION = 1;
	public static final int FILLTER_SECTION = 0;
	
	private LocalizedInfo info;
	private long section;
	
	public GridItem(LocalizedInfo info, long section){
		this.info = info;
		this.section = section;
	}
	
	public long getSection() {
		return section;
	}
	
	public void setSection(long section){
		this.section = section;
	}
	
	public void setSection(int section) {
		this.section = section;
	}
	
	public LocalizedInfo getLocalizedInfo() {
		return info;
	}
	
	public void setLocalizedInfo(LocalizedInfo info){
		this.info = info;
	}
	
}
