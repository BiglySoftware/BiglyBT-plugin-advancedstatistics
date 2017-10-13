/*
 * Azureus Advanced Statistics Plugin
 * 
 * Created on Thursday, August 25th 2005
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

import org.darkman.plugins.advancedstatistics.AdvancedStatistics;
import org.darkman.plugins.advancedstatistics.util.Log;
import java.util.*;
import com.biglybt.pif.download.*;

/**
 * @author Darko Matesic
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TorrentDataProvider implements DownloadManagerListener {
    protected DataProvider dataProvider;
    
    public Vector torrents;
    public TorrentActivityData downloadActivityData;
    public TorrentActivityData uploadActivityData;
    public long totalBytesDiscarded;

    public TorrentDataProvider(DataProvider dataProvider, AdvancedStatistics as) {
        Log.out("TorrentDataProvider.construct");
        this.dataProvider = dataProvider;
        this.torrents = new Vector();
        downloadActivityData = new TorrentActivityData(this, dataProvider.pluginInterface, TorrentActivityData.DOWNLOAD_ACTIVITY_DATA, as);
        uploadActivityData   = new TorrentActivityData(this, dataProvider.pluginInterface, TorrentActivityData.UPLOAD_ACTIVITY_DATA , as );
        dataProvider.pluginInterface.getDownloadManager().addListener(this);
    }
    public void closedown() {
	    for(Enumeration enumeration = torrents.elements(); enumeration.hasMoreElements();){
	        TorrentData data = (TorrentData)enumeration.nextElement();
            data.closedown();
	        data = null;
	    }
	    torrents.removeAllElements();
	    torrents = null;
        downloadActivityData.closedown();
        downloadActivityData = null;
        uploadActivityData.closedown();
        uploadActivityData = null;
    }
    @Override
    public void downloadAdded(Download download) {
        Log.out("TorrentDataProvider.downloadAdded(" + download.getName() + ")");
        TorrentData data = new TorrentData(dataProvider, download);
        torrents.addElement(data);
        downloadActivityData.update();
        uploadActivityData.update();
    }
	@Override
	public void downloadRemoved(Download download) {
        Log.out("TorrentDataProvider.downloadRemoved(" + download.getName() + ")");
        for(Enumeration enumeration = torrents.elements(); enumeration.hasMoreElements();) {
            TorrentData data = (TorrentData)enumeration.nextElement();
            if(data.download.getName() == download.getName()) {
                torrents.remove(data);
                data.deleteData();
                data.closedown();
                data = null;
                break;
            }
        }
        downloadActivityData.update();
        uploadActivityData.update();
	}
	public void updateData() {
        totalBytesDiscarded = 0;
	    for(Enumeration enumeration = torrents.elements(); enumeration.hasMoreElements();){
	        TorrentData data = (TorrentData)enumeration.nextElement();
            data.updateData();
            totalBytesDiscarded += data.bytesDiscarded;
	    }
	}
}
