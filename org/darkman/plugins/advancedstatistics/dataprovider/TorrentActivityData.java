/*
 * Azureus Advanced Statistics Plugin
 * 
 * Created on Monday, October 24th 2005
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
package org.darkman.plugins.advancedstatistics.dataprovider;

import java.util.Enumeration;

import org.darkman.plugins.advancedstatistics.AdvancedStatistics;
import org.darkman.plugins.advancedstatistics.util.Log;
import org.eclipse.swt.graphics.GC;
import com.biglybt.core.util.AEMonitor;
import com.biglybt.pif.PluginInterface;
//
/**
 * @author Darko Matesic
 *
 * 
 */
public class TorrentActivityData {
    public static final int DOWNLOAD_ACTIVITY_DATA = 1;
    public static final int UPLOAD_ACTIVITY_DATA   = 2;
    
    protected AEMonitor this_mon = new AEMonitor("TorrentActivityData");
    protected TorrentDataProvider torrentDataProvider;
    protected PluginInterface pluginInterface;
    
    public ActivityData activityData[];
    public String torrentName[];
    private int torrentNameWidth[];
    public int maxTorrentNameWidth;
    public int activityDataType; 
    public boolean changed;
    private AdvancedStatistics advstats;

    public TorrentActivityData(TorrentDataProvider torrentDataProvider, PluginInterface pluginInterface, int activityDataType, AdvancedStatistics as) {
        this.torrentDataProvider = torrentDataProvider;
        this.pluginInterface = pluginInterface;
        this.activityDataType = activityDataType;
        activityData = new ActivityData[0];
        torrentName = new String[0];
        torrentNameWidth = new int[0];
        maxTorrentNameWidth = 0;
        changed = false;
        this.advstats = as;
    }
    public void enter() { this_mon.enter(); }
    public void exit()  { this_mon.exit();  }

    public void update() {
        int activityDataCount = activityDataCount = activityData.length;
        if(torrentDataProvider.torrents.size() == activityDataCount) return;
        try {
            this_mon.enter();
            activityDataCount = torrentDataProvider.torrents.size();
            activityData = new ActivityData[activityDataCount];
            torrentName = new String[activityDataCount];
            torrentNameWidth = new int[activityDataCount];
            maxTorrentNameWidth = 0;
            changed = true;

            int index = 0;
            for(Enumeration enumeration = torrentDataProvider.torrents.elements(); enumeration.hasMoreElements();) {
                TorrentData data = (TorrentData)enumeration.nextElement();
                if(index < activityDataCount) {
                    switch(activityDataType) {
                        case DOWNLOAD_ACTIVITY_DATA:
                            activityData[index] = data.downloadActivityData;
                            break;
                        case UPLOAD_ACTIVITY_DATA:
                            activityData[index] = data.uploadActivityData;
                            break;
                    }
                    torrentName[index] = data.download.getName();
                    torrentNameWidth[index] = getTextWidth(torrentName[index]);
                    if(maxTorrentNameWidth < torrentNameWidth[index]) maxTorrentNameWidth = torrentNameWidth[index];
                    index++;
                }
            }
        } catch(Exception ex) {
            Log.out("Error updating torrent activity data: " + ex.getMessage());
            Log.outStackTrace(ex);            

            activityData = new ActivityData[0];
            torrentName = new String[0];
            torrentNameWidth = new int[0];
            maxTorrentNameWidth = 0;
            changed = true;

        } finally {
            this_mon.exit();
        }
    }
    private int getTextWidth(String text) {   
        int width = 0;
        try {
            GC gc = new GC(advstats.swt_ui.getDisplay());
            for(int i = 0; i < text.length(); i++)
                width += gc.getAdvanceWidth(text.charAt(i));
            gc.dispose();
        } catch(Exception ex) {
            width = text.length() * 5;
        }
        return width;
    }
    public void setScale(int scale) {
        try{
            this_mon.enter();
            if(activityData != null) {
                for(int i = 0; i < activityData.length; i++)
                    activityData[i].setScale(scale);
            }
        } finally {
            this_mon.exit();
        }
    }
    public void closedown() {
        try {
            this_mon.enter();
            activityData = null;
            torrentName = null;
            torrentNameWidth = null;
        } finally {
            this_mon.exit();
        }        
    }
}
