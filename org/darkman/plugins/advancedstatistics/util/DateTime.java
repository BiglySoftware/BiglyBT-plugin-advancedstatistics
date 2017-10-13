/*
 * Azureus Advanced Statistics Plugin
 * 
 * Created on Thursday, September 11th 2005
 * Created by Darko Matesic
 * Copyright (C) 2005 Darko Matesic, All Rights Reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details ( see the LICENSE file ).
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.darkman.plugins.advancedstatistics.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Darko Matesic
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DateTime {
    static SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm:ss");
    static SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy");
    static SimpleDateFormat formatDateTime = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss");
    static Calendar calendar = Calendar.getInstance();

    private static String twoDigits(int i) {
        return (i < 10) ? "0" + i : String.valueOf(i);
    }
    
    public static String getElapsedTimeString(long timeMilliseconds, boolean returnDays) {
        long time = timeMilliseconds / 1000;
        int seconds  = (int)(time %   60);
        int minutes  = (int)(time /   60) % 60;
        int hours    = (int)(time / 3600) % 24;
        int days     = (int)(time / 86400);
        if(returnDays && days > 0) return days + "d " + twoDigits(hours) + ":" + twoDigits(minutes) + ":" + twoDigits(seconds);            
        return twoDigits(hours) + ":" + twoDigits(minutes) + ":" + twoDigits(seconds);
    }
    public static String getTimeString(long timeMilliseconds) {
        return formatTime.format(new Date(timeMilliseconds));
    }
    public static String getDateString(long timeMilliseconds) {
        return formatDate.format(new Date(timeMilliseconds));
    }
    public static String getDateTimeString(long timeMilliseconds) {
        return formatDateTime.format(new Date(timeMilliseconds));
    }

    //returns number of milliseconds since midnight
    public static int getMillisecondsSinceMidnight(long currTime) {
        calendar.setTimeInMillis(currTime);
        return calendar.get(Calendar.HOUR_OF_DAY) * 3600000 + calendar.get(Calendar.MINUTE) * 60000 + calendar.get(Calendar.SECOND) * 1000 + calendar.get(Calendar.MILLISECOND);
    }

}
