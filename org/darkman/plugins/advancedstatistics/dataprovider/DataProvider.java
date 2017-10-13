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
package org.darkman.plugins.advancedstatistics.dataprovider;

import com.biglybt.core.CoreFactory;
import com.biglybt.pif.PluginConfig;
import org.darkman.plugins.advancedstatistics.AdvancedStatistics;
import org.darkman.plugins.advancedstatistics.util.Log;
import org.darkman.plugins.advancedstatistics.util.DateTime;
import java.io.File;
import java.sql.*;
import java.lang.Exception;
import com.biglybt.core.config.COConfigurationManager;
import com.biglybt.core.global.GlobalManager;
import com.biglybt.core.global.GlobalManagerStats;
import com.biglybt.core.util.AEMonitor;
import com.biglybt.pif.PluginInterface;
import com.biglybt.pif.utils.UTTimer;
import com.biglybt.pif.utils.UTTimerEvent;
import com.biglybt.pif.utils.UTTimerEventPerformer;

/**
 * @author Darko Matesic
 *
 * 
 */
public class DataProvider {
    protected AEMonitor this_mon = new AEMonitor("DataProvider");
    private final static String TIMER_NAME = "DATA_PROVIDER_TIMER";
    public PluginInterface pluginInterface;
    public GlobalManager globalManager;
    public String pluginDirectoryName;
    public Connection connection;
    public AdvancedStatisticsConfig config;
    public TorrentDataProvider torrentDataProvider;
    public TransferDataProvider transferDataProvider;
    public ActivityData downloadActivityData;
    public ActivityData uploadActivityData;
    private UTTimer timer;
    private boolean closed;

    public DataProvider(PluginInterface pluginInterface, AdvancedStatistics as) throws Exception {
        Log.out("DataProvider.construct");
        // get plugin directory name
        pluginDirectoryName = pluginInterface.getPluginDirectoryName();
        if(!pluginDirectoryName.endsWith(File.separator)) pluginDirectoryName += File.separator;

        // get variables
        this.pluginInterface = pluginInterface;
        this.globalManager = CoreFactory.getSingleton().getGlobalManager();

        //load database driver and initialize stats database
        Log.out("DataProvider.construct: initializing jdbc");
        Class.forName("org.hsqldb.jdbcDriver");
        Log.out("DataProvider.construct: opening database");
        String url = "jdbc:hsqldb:file:" + pluginDirectoryName.replace('\\', '/') + "stats;ifexists=true";
        connection = DriverManager.getConnection(url, "sa", "");

        // initialize configuration
        Log.out("DataProvider.construct: initializing plugin configuration");
        config = new AdvancedStatisticsConfig(pluginInterface); 

        // initialize torrent data provider
        Log.out("DataProvider.construct: initializing torrent data provider");
        this.torrentDataProvider = new TorrentDataProvider(this, as);

        // initialize transfer data provider
        Log.out("DataProvider.construct: initializing transfer data provider");
        this.transferDataProvider = new TransferDataProvider(this);

        //initialize activity data
        Log.out("DataProvider.construct: initializing activity data (size: " + config.activityDataSize + " seconds)");
        downloadActivityData = new ActivityData(true, config.activityDataSize);
        uploadActivityData = new ActivityData(true, config.activityDataSize);

        //fill test data
/*        
        int time = (int)DateTime.getMillisecondsSinceMidnight(System.currentTimeMillis());
        for(int i = 0 ; i < 9000; i++) {
            downloadActivityData.add( i%15 + 1, i%10 + 1, 0, time - (config.activityDataSize - i) * 1000);
            uploadActivityData.add( i%15 + 1, i%10 + 1, 0, time - (config.activityDataSize - i) * 1000);
        }
*/        
        //start timer (for data update)
        Log.out("DataProvider.construct: starting timer");
        closed = false;
        this.startTimer();
    }

    public void closedown() {
        try {
            closed = true;
            this_mon.enter();
            Log.out("DataProvider.closedown");
            Log.out("DataProvider.closedown: destroying timer");
            timer.destroy();

            Log.out("DataProvider.closedown: closing torrent data provider");
            torrentDataProvider.closedown();
            torrentDataProvider = null;

            Log.out("DataProvider.closedown: closing transfer data provider");
            transferDataProvider.closedown();
            transferDataProvider = null;

            Log.out("DataProvider.closedown: closing activity data");            
            downloadActivityData.closedown();
            downloadActivityData = null;        
            uploadActivityData.closedown();
            uploadActivityData = null;
            
            //initiate HSQL database shutdown
            try {
                Log.out("DataProvider.closedown: shutting down database");
                Statement stmt = connection.createStatement();
                stmt.executeUpdate("SHUTDOWN");
                stmt.close();
            } catch(Exception ex) {
                Log.out("Error shutting down database: " + ex.getMessage());
            }
            try {
                Log.out("DataProvider.closedown: closing database connection");
                connection.close();
            } catch(Exception ex) {
                Log.out("Error closing database connection: " + ex.getMessage());
            }
        } finally {
            this_mon.exit();
        }
    }
    private void startTimer() { 
        Log.out("DataProvider.startTimer");
        timer = pluginInterface.getUtilities().createTimer(TIMER_NAME);         
        timer.addPeriodicEvent(1000, new UTTimerEventPerformer()  {             
            @Override
            public void perform(UTTimerEvent ev)  {
                updateData();
            }
        });           
    } 

    private void updateData() {
        try {
            if(closed) return;
            this_mon.enter();
            //update torrent data first for discarded bytes calculation
            try {
                if(torrentDataProvider != null) torrentDataProvider.updateData();
            } catch(Exception ex) {
                Log.out("Error in torrentDataProvider.updateData: " + ex.getMessage());
                Log.outStackTrace(ex);
            }
            try {
                if(transferDataProvider != null) transferDataProvider.updateData();
            } catch(Exception ex) {
                Log.out("Error in transferDataProvider.updateData: " + ex.getMessage());
                Log.outStackTrace(ex);
            }
            //update activity data
            GlobalManagerStats stats = globalManager.getStats();
            int time = (int)DateTime.getMillisecondsSinceMidnight(System.currentTimeMillis());
        
            downloadActivityData.add(
                    stats.getDataReceiveRate(), 
                    stats.getProtocolReceiveRate(),
                    COConfigurationManager.getIntParameter(PluginConfig.CORE_PARAM_INT_MAX_DOWNLOAD_SPEED_KBYTES_PER_SEC) * 1024,
                    time);
            uploadActivityData.add( 
                    stats.getDataSendRate(), 
                    stats.getProtocolSendRate(),                
                    COConfigurationManager.getIntParameter(PluginConfig.CORE_PARAM_INT_MAX_UPLOAD_SPEED_KBYTES_PER_SEC) * 1024,
                    time);                
        } finally {
            this_mon.exit();
        }
    }
}