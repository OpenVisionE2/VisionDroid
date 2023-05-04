/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.helpers;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.openvision.visiondroid.VisionDroid;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Provides static methods for Date and Time parsing
 * 
 * @author original creator
 * 
 */
public class DateTime {
	public static int getRemaining(@Nullable String duration, @Nullable String eventstart) {
		return getRemaining(duration, eventstart, null);
	}

	/**
	 * @param duration
	 * @param eventstart
	 * @return
	 */
	public static int getRemaining(@Nullable String duration, @Nullable String eventstart, @Nullable String nowTime) {
		if(duration == null || Python.NONE.equals(duration)){
			return 0;
		}
		
		long d = Double.valueOf(duration).longValue();
		
		if (eventstart != null && ! Python.NONE.equals(eventstart)) {
			try {
				long s = Double.valueOf(eventstart).longValue() * 1000;
				Date now = null;
				if (nowTime != null)
					now = DateTime.getDate(nowTime);
				if (now == null)
					 now = new Date();
				if (now.getTime() >= s) {
					d = d - ((now.getTime() - s) / 1000);
					if (d <= 60) {
						d = 60;
					}
				}
			} catch (NumberFormatException nfe) {
				Log.e(VisionDroid.LOG_TAG, nfe.getMessage());
				return 0;
			}
		}

		d = (d / 60);
		return (int) d;
	}

	
	/**
	 * @param eventstart
	 * @param duration
	 * @return
	 */
	@Nullable
	public static String getDurationString(@Nullable String duration, @Nullable String eventstart) {
		if(duration == null || Python.NONE.equals(duration)){
			return "0";
		}
		
		long d = Double.valueOf(duration).longValue();
		String durationPrefix = "";

		if (eventstart != null && !Python.NONE.equals(eventstart)) {
			try {
				long s = Double.valueOf(eventstart).longValue() * 1000;
				Date now = new Date();

				if (now.getTime() >= s) {
					d = d - ((now.getTime() - s) / 1000);
					if (d <= 60) {
						d = 60;
					}
					durationPrefix = "+";
				}
			} catch (NumberFormatException nfe) {
				Log.e(VisionDroid.LOG_TAG, nfe.getMessage());
				return duration;
			}
		}

		d = (d / 60);
		return durationPrefix + (d);
	}

	/**
	 * @param timestamp
	 * @return
	 */
	@NonNull
	public static String getDateTimeString(@NonNull String timestamp) {
		SimpleDateFormat sdfDateTime;

		// Some devices are missing some Translations, wee need to work around
		// that!
		if (VisionDroid.DATE_LOCALE_WO) {
			sdfDateTime = new SimpleDateFormat("E, dd.MM. - HH:mm", Locale.US);
		} else {
			sdfDateTime = new SimpleDateFormat("E, dd.MM. - HH:mm");
		}

		return DateTime.getFormattedDateString(sdfDateTime, timestamp);
	}

	/**
	 * @param timestamp
	 * @return
	 */
	@NonNull
	public static String getYearDateTimeString(long timestamp) {
		return getYearDateTimeString(String.valueOf(timestamp));
	}

	/**
	 * @param timestamp
	 * @return
	 */
	@NonNull
	public static String getYearDateTimeString(@NonNull String timestamp) {
		SimpleDateFormat sdfDateTime;

		// Some devices are missing some Translations, wee need to work around
		// that!
		if (VisionDroid.DATE_LOCALE_WO) {
			sdfDateTime = new SimpleDateFormat("E, dd.MM.yyyy - HH:mm", Locale.US);
		} else {
			sdfDateTime = new SimpleDateFormat("E, dd.MM.yyyy - HH:mm");
		}

		return DateTime.getFormattedDateString(sdfDateTime, timestamp);
	}

	/**
	 * @param timestamp
	 * @return
	 */
	@NonNull
	public static String getTimeString(@NonNull String timestamp) {
		SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
		return DateTime.getFormattedDateString(sdfTime, timestamp);
	}

	/**
	 * @param timestamp
	 * @return
	 */
	@Nullable
	public static Date getDate(@NonNull String timestamp) {
		try {
			long s = Double.valueOf(timestamp).longValue();
			s = s * 1000;
			return new Date(s);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * @param sdf
	 * @param timestamp
	 * @return
	 */
	@NonNull
	public static String getFormattedDateString(@NonNull SimpleDateFormat sdf, @NonNull String timestamp) {
		Date date = DateTime.getDate(timestamp);

		if (date != null) {
			return sdf.format(date);
		}

		return "-";
	}

	public static Integer parseTimestamp(String timestamp){
		return (new BigDecimal(timestamp)).intValue();
	}

	@NonNull
	public static String minutesAndSeconds(int seconds) {
		int min = seconds / 60;
		int sec = seconds % 60;
		return String.format("%02d:%02d", min, sec);
	}

	public static int getPrimeTimestamp(){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 20);
		cal.set(Calendar.MINUTE, 15);
		cal.set(Calendar.SECOND, 0);
		return (int) ( cal.getTimeInMillis() / 1000 );
	}
}
