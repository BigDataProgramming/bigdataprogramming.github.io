package it.unical.dimes.scalab.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;

public class Flickr {

	private final static DateTimeFormatter dateStringFormat = DateTimeFormat.forPattern("MMM dd, yyyy h:mm:ss a").withLocale(Locale.ENGLISH);
	
	private String userId;
	private double longitude;
	private double latitude;
	private LocalDateTime date;
	private String roi;

	public static DateTimeFormatter getDatestringformat() {
		return dateStringFormat;
	}

	public String getUserId() {
		return userId;
	}

	public double getLongitude() {
		return longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public String getRoi() {
		return roi;
	}

	public void setRoi(String roi) {
		this.roi = roi;
	}

	@Override
	public String toString() {
		return "Flickr [userId=" + userId + ", longitude=" + longitude + ", latitude=" + latitude + ", date=" + date
				+ ", roi=" + roi + "]";
	}

	public String export() {
		JSONObject ret = new JSONObject();
		ret.put("userId", userId);
		ret.put("longitude", longitude);
		ret.put("latitude", latitude);
		ret.put("date", dateStringFormat.print(date));
		ret.put("roi", roi);
		return ret.toString();
	}

	public void importFromString(String s) {
		JSONObject json = new JSONObject(s);
		this.userId = json.getString("userId");
		this.latitude = json.getDouble("latitude");
		this.longitude = json.getDouble("longitude");
		this.date = dateStringFormat.parseDateTime(json.getString("date")).toLocalDateTime();
		this.roi = json.getString("roi");
	}

	public void importFromFlickrJSON(String s) {
		try {
			JSONObject jsonObject = new JSONObject(s);
			this.userId = jsonObject.getJSONObject("owner").getString("id");
			if (jsonObject.has("geoData")) {
				JSONObject geo = jsonObject.getJSONObject("geoData");
				this.longitude = geo.getDouble("longitude");
				this.latitude = geo.getDouble("latitude");
			}
			this.date = dateStringFormat.parseDateTime(jsonObject.getString("dateTaken")).toLocalDateTime();
			this.roi = "";
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void removeTime() {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("MMM dd, yyyy").withLocale(Locale.ENGLISH);
		String s = formatter.print(date);
		this.date = formatter.parseLocalDateTime(s);
	}
}
