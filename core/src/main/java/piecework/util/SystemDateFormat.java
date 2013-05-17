/*
 * Copyright 2011 University of Washington
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package piecework.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

/**
 * @author James Renfro
 */
public class SystemDateFormat {

	public static final String STANDARD_PATTERN = "MMM dd, yyyy hh:mm aa";
	
	private static final Logger LOG = Logger.getLogger(SystemDateFormat.class);
	
	private static final long MINUTE_IN_MILLISECONDS = 60 * 1000;
	private static final long HOUR_IN_MILLISECONDS = 60 * MINUTE_IN_MILLISECONDS;
	private static final long DAY_IN_MILLISECONDS = 24 * HOUR_IN_MILLISECONDS;
	private static final long WEEK_IN_MILLISECONDS = 7 * DAY_IN_MILLISECONDS;
	private static final long MONTH_IN_MILLISECONDS = 30 * DAY_IN_MILLISECONDS;
	private static final long YEAR_IN_MILLISECONDS = 365 * DAY_IN_MILLISECONDS;
	
	public static FastDateFormat getDefault() {
		return FastDateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
	}
	
	public static FastDateFormat getDateInstance() {
		return FastDateFormat.getDateInstance(DateFormat.MEDIUM);
	}
	
	public static FastDateFormat getISO8601() {
		return FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ssZ");
	}
	
	public static Date parse(String source) {
		if (source == null)
			return null;
		
		try {
			return SimpleDateFormat.getDateTimeInstance().parse(source);
		} catch (ParseException e) {
			try {
				return SimpleDateFormat.getDateInstance(DateFormat.MEDIUM).parse(source);
			} catch (ParseException e2) {
				try {
					return SimpleDateFormat.getDateInstance(DateFormat.SHORT).parse(source);
				} catch (ParseException e3) {
					LOG.warn(e3);
					return null;
				}
			}
		}
	}
	
	public static String format(Date date) {
		if (date == null)
			return null;
		
		return getDefault().format(date);
	}
	
	public static String formatISO(Date date) {
		if (date == null)
			return null;
		
		return getISO8601().format(date);
	}
	
	public static String calculateTimeElapsed(DateTime startDate, DateTime endDate) {
		long start = startDate != null ? startDate.getMillis() : -1;
		long end = endDate != null ? endDate.getMillis() : -1;
		
		if (start == -1 || end == -1)
			return null;
		
		long millisecondsElapsed = end - start;
		
		return approximateTime(millisecondsElapsed);
	}
	
	public static String calculateTimeElapsed(Date startDate, Date endDate) {
		if (startDate == null || endDate == null)
			return "";
		
		long millisecondsElapsed = endDate.getTime() - startDate.getTime();
		
		return approximateTime(millisecondsElapsed);
	}
	
	public static String approximateTime(long millisecondsElapsed) {
		StringBuilder builder = new StringBuilder();
		
		if (millisecondsElapsed > YEAR_IN_MILLISECONDS) {
			long numberOfYears = millisecondsElapsed / YEAR_IN_MILLISECONDS;
			millisecondsElapsed = millisecondsElapsed % YEAR_IN_MILLISECONDS;
			
			builder.append(numberOfYears);
			
			if (numberOfYears > 1)
				builder.append(" years ");
			else
				builder.append(" year ");

			if (numberOfYears > 0)
				return builder.toString();
		} 
		
		if (millisecondsElapsed > MONTH_IN_MILLISECONDS) {
			long numberOfMonths = millisecondsElapsed / MONTH_IN_MILLISECONDS;
			millisecondsElapsed = millisecondsElapsed % MONTH_IN_MILLISECONDS;
			
			builder.append(numberOfMonths);
			
			if (numberOfMonths > 1)
				builder.append(" months ");
			else
				builder.append(" month ");
			
			if (numberOfMonths > 0)
				return builder.toString();
		}
		
		if (millisecondsElapsed > WEEK_IN_MILLISECONDS) {
			long numberOfWeeks = millisecondsElapsed / WEEK_IN_MILLISECONDS;
			millisecondsElapsed = millisecondsElapsed % WEEK_IN_MILLISECONDS;
			
			builder.append(numberOfWeeks);
			
			if (numberOfWeeks > 1)
				builder.append(" weeks ");
			else
				builder.append(" week ");
			
			if (numberOfWeeks > 0)
				return builder.toString();
		}
		
		if (millisecondsElapsed > DAY_IN_MILLISECONDS) {
			long numberOfDays = millisecondsElapsed / DAY_IN_MILLISECONDS;
			millisecondsElapsed = millisecondsElapsed % DAY_IN_MILLISECONDS;
			
			builder.append(numberOfDays);
			
			if (numberOfDays > 1)
				builder.append(" days ");
			else
				builder.append(" day ");

			if (numberOfDays > 0)
				return builder.toString();
		}
		
		if (millisecondsElapsed > HOUR_IN_MILLISECONDS) {
			long numberOfHours = millisecondsElapsed / HOUR_IN_MILLISECONDS;
			millisecondsElapsed = millisecondsElapsed % HOUR_IN_MILLISECONDS;
			
			builder.append(numberOfHours);
			
			if (numberOfHours > 1)
				builder.append(" hours ");
			else
				builder.append(" hour ");

			if (numberOfHours > 0)
				return builder.toString();
		}
		
		if (millisecondsElapsed > MINUTE_IN_MILLISECONDS) {
			long numberOfMinutes = millisecondsElapsed / MINUTE_IN_MILLISECONDS;
			millisecondsElapsed = millisecondsElapsed % MINUTE_IN_MILLISECONDS;
			
			builder.append(numberOfMinutes);
			
			if (numberOfMinutes > 1)
				builder.append(" minutes ");
			else
				builder.append(" minute ");
			
			if (numberOfMinutes > 0)
				return builder.toString();
		}
		
		if (millisecondsElapsed > 1000) {
			long numberOfSeconds = millisecondsElapsed / 1000;
			millisecondsElapsed = millisecondsElapsed % 1000;
			
			builder.append(numberOfSeconds);
			
			if (numberOfSeconds > 1)
				builder.append(" seconds ");
			else
				builder.append(" second ");
		}
		
		return builder.toString();
	}
	
}
