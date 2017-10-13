/*
 * Azureus Advanced Statistics Plugin
 * 
 * Created on Wednesday, August 24th 2005
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

import java.io.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import com.biglybt.core.util.Debug;

/**
 * @author Darko Matesic
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Log {
    private final static String logFileName = "log.txt";
    private static String logDirectoryName = "";
    public static void setLogDirectoryName(String newLogDirectoryName) {
        logDirectoryName = newLogDirectoryName;
        if(!logDirectoryName.endsWith(File.separator)) logDirectoryName += File.separator;
    }
	public static void out(String text) {
	    try {
	        RandomAccessFile raf = new RandomAccessFile(logDirectoryName + logFileName, "rw");
	        SimpleDateFormat f = new SimpleDateFormat("yyyy.MM.dd  HH:mm:ss  ");
	        raf.seek(raf.length());
	        raf.writeBytes(f.format(new Date()) +  text + "\r\n");
	        raf.close();
	    } catch(Exception ex) {
			Debug.printStackTrace(ex);
	    }
	}
    public static void outStackTrace(Exception ex) {
        out("----------------------------------------");
        StackTraceElement[] st = ex.getStackTrace();
        for(int i = 0; i < st.length; i++)
            out(st[i].toString());
        out("----------------------------------------");
    }
	public static void clear() {
	    try {
	        File file = new File(logDirectoryName + logFileName);
	        if(file.exists()) file.delete();
	    } catch(Exception ex) {
			Debug.printStackTrace(ex);
	    }	    
	}
}
