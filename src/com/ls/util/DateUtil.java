package com.ls.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {

	public static Date getDateByTimeZone(String dateStr) throws ParseException{
		DateFormat df = new SimpleDateFormat("EEE MMM dd yyyy hh:mm:ss z",Locale.US);
		df.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		dateStr = dateStr.replace("0800", "08:00");
		return df.parse(dateStr);
	}
	
	public static String formatDateLong(Date date){
		return formatDate(date,"yyyy-MM-dd HH:mm:ss");
	}
	
	public static String formatDate(Date date,String pattern){
		DateFormat df = new SimpleDateFormat(pattern);
		return df.format(date);
	}
}
