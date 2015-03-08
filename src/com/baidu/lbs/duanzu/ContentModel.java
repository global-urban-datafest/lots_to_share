package com.baidu.lbs.duanzu;

public class ContentModel {
	private String name;
	private String addr;
	private String distance;
	private double latitude;
	private double longitude;
	
	private String owner;
	private int count;
	private int occupCount;
	private double price;
	private String iconStyleID;
	
	public String getIconStyleID() {
		return iconStyleID;
	}

	public void setIconStyleID(String iconStyleID) {
		this.iconStyleID = iconStyleID;
	}
	
	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	public int getOccupCount() {
		return occupCount;
	}

	public void setOccupCount(int occupCount) {
		this.occupCount = occupCount;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

}