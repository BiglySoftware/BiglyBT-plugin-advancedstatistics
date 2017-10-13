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

import org.darkman.plugins.advancedstatistics.util.Log;
import org.darkman.plugins.advancedstatistics.util.DateTime;
import java.sql.*;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;
import java.math.BigInteger;

import com.biglybt.core.internat.MessageText;
import com.biglybt.core.util.Constants;
import com.biglybt.core.util.TimeFormatter;
import com.biglybt.pif.download.*;

/**
 * @author Darko Matesic
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TorrentData {
    protected DataProvider dataProvider; 
    public Download download;

    private PreparedStatement pstmt;
    private TorrentTransferData lastStats; // holds last read stats
    private TorrentTransferData currStats;
    private TorrentTransferData currData; // points to currently used data
    private TorrentTransferData etaDataSummary;
    private TorrentTransferData etaData;

    public Vector transfers;
    public ActivityData downloadActivityData;
    public ActivityData uploadActivityData;
    
    private int torrentID;
    public long timeCompleted[];
    public long bytesReceived;  // bytes received in last update
    public long bytesDiscarded; // bytes discarded in last update
    public long bytesSent;      // bytes sent in last update
    public long torrentSize;
    public String ETA;
    
    private void setTimeCompleted(int completed, long time) {
        if(completed >= 0 && completed <= 1000) {
            timeCompleted[completed] = time;
            try {
                Statement stmt = dataProvider.connection.createStatement();
                ResultSet rset = stmt.executeQuery("select TIME_COMPLETED from TORRENT_COMPLETED_DATA where ID = " + torrentID + " and COMPLETED = " + completed);
                if(rset.next())
                    stmt.executeUpdate("update TORRENT_COMPLETED_DATA set TIME_COMPLETED = " + time + " where ID = " + torrentID + " and COMPLETED = " + completed);                    
                else
                    stmt.executeUpdate("insert into TORRENT_COMPLETED_DATA(ID, COMPLETED, TIME_COMPLETED) values(" + torrentID + ", " + completed + ", " + time +")");                    
                rset.close();
                stmt.close();
            } catch(Exception ex) {
                Log.out("Error setting completed time (" + download.getName() + "): " + ex.getMessage());
            }
        }
    }
    private int getMaxTorrentID()throws SQLException {
        int maxTorrentID = 0;
        Statement stmt = dataProvider.connection.createStatement();
        ResultSet rset = stmt.executeQuery("select max(ID) from TORRENT");
        if(rset.next()) maxTorrentID = rset.getInt(1);
        rset.close();
        stmt.close();
        return maxTorrentID;
    }
    private int createNewTorrentID(String torrentName) throws SQLException {
        int newTorrentID = getMaxTorrentID() + 1;
        Statement stmt = dataProvider.connection.createStatement();
        int rowsAffected = stmt.executeUpdate("insert into TORRENT values(" + newTorrentID + ", '" + torrentName + "')");
        stmt.close();
        if(rowsAffected == 0) newTorrentID = 0;
        return newTorrentID;
    }
    private int getTorrentID(String torrentName) throws SQLException {
        int torrentID = 0;
        Statement stmt = dataProvider.connection.createStatement();        
        ResultSet rset = stmt.executeQuery("select ID from TORRENT where NAME = '" + torrentName + "'");
        if(rset.next()) torrentID = rset.getInt(1);
        rset.close();
        stmt.close();
        if(torrentID == 0) torrentID = createNewTorrentID(torrentName);
        return torrentID;
    }
    
    public TorrentData(DataProvider dataProvider, Download download) {
        this.dataProvider = dataProvider;
        this.download = download;

        lastStats = null;
        currStats = new TorrentTransferData();
        currData = null;
        etaDataSummary = new TorrentTransferData();
        etaData = new TorrentTransferData();
        
        transfers = new Vector();
        downloadActivityData = new ActivityData(false, dataProvider.config.activityDataSize);
        uploadActivityData = new ActivityData(false, dataProvider.config.activityDataSize);
        
        timeCompleted = new long[1001];
        // convert torrent name to hexadecimal array (name stored in database)
        BigInteger bi = new BigInteger(download.getName().getBytes());
        String torrentName = bi.toString(16);
        
        try {
            torrentID = getTorrentID(torrentName);
            // Load completed data
            Statement stmt = dataProvider.connection.createStatement();
            ResultSet rset = stmt.executeQuery("select COMPLETED, TIME_COMPLETED from TORRENT_COMPLETED_DATA where ID = " + torrentID);
            while(rset.next()) {
                int completed = rset.getInt(1);
                if(completed >= 0 && completed <= 1000) timeCompleted[completed] = rset.getLong(2);
            }
            rset.close();
            // Load transfer data
            rset = stmt.executeQuery("select TRANSFER_DATE, COMPLETED_TO_DATE, RECEIVED_TO_DATE, DISCARDED_TO_DATE, SENT_TO_DATE, RECEIVED, DISCARDED, SENT, UP_TIME from TORRENT_TRANSFER_DATA where ID = " + torrentID + " order by TRANSFER_DATE");
            while(rset.next()) {
                TorrentTransferData data = new TorrentTransferData();
                data.TRANSFER_DATE     = rset.getLong(1);
                data.COMPLETED_TO_DATE = rset.getLong(2);
                data.RECEIVED_TO_DATE  = rset.getLong(3);
                data.DISCARDED_TO_DATE = rset.getLong(4);
                data.SENT_TO_DATE      = rset.getLong(5);
                data.RECEIVED          = rset.getLong(6);
                data.DISCARDED         = rset.getLong(7);
                data.SENT              = rset.getLong(8);
                data.UP_TIME           = rset.getLong(9);
                transfers.addElement(data);
            }
            rset.close();
            stmt.close();
        } catch(Exception ex) {
            Log.out("Error loading torrent data (" + download.getName() + "):" + ex.getMessage());
        }
        try {
            pstmt = dataProvider.connection.prepareStatement(
                    "update TORRENT_TRANSFER_DATA set " +
                    "RECEIVED = ?, DISCARDED = ?, SENT = ?, UP_TIME = ? " +
                    "where ID = ? AND TRANSFER_DATE = ?");
        } catch(Exception ex) {
            Log.out("Error creating prepared statement: " + ex.getMessage());
        }
        torrentSize = download.getTorrent().getSize();
        ETA = Constants.INFINITY_STRING;
    }
    public void closedown() {
        try {
            pstmt.close();
        } catch(Exception ex) {
        }
        if(lastStats != null) { lastStats.closedown(); lastStats = null; }
        if(currStats != null) { currStats.closedown(); currStats = null; }
        currData = null;
        etaDataSummary.closedown(); etaDataSummary = null;
        etaData.closedown(); etaData = null;

        for(Enumeration enumeration = transfers.elements(); enumeration.hasMoreElements();){
            TorrentTransferData data = (TorrentTransferData)enumeration.nextElement();
            data.closedown();
            data = null;
        }
        downloadActivityData.closedown(); downloadActivityData = null;        
        uploadActivityData.closedown();   uploadActivityData = null;
        
        timeCompleted = null;
    }
    public void deleteData() {
        try {
            Statement stmt = dataProvider.connection.createStatement();
            stmt.executeUpdate("delete from TORRENT where ID = " + torrentID);
            stmt.executeUpdate("delete from TORRENT_COMPLETED_DATA where ID = " + torrentID);
            stmt.executeUpdate("delete from TORRENT_TRANSFER_DATA where ID = " + torrentID);
            stmt.close();
        } catch(Exception ex) {
            Log.out("Error deleting torrent data (" + download.getName() + "): " + ex.getMessage());
        }
    }

    private TorrentTransferData getTransferData(long date) {
        for(Enumeration enumeration = transfers.elements(); enumeration.hasMoreElements();){
            TorrentTransferData data = (TorrentTransferData)enumeration.nextElement();
            if(data.TRANSFER_DATE == date) return data;
        }
        return null;
    }

    private void getSummaryData(long date) {
        if(etaDataSummary.TRANSFER_DATE != date) {
            etaDataSummary.TRANSFER_DATE = date;
            try {
                String SQL = "select SUM(RECEIVED), SUM(UP_TIME) " +
                             "from TORRENT_TRANSFER_DATA where ID = " + torrentID;
                if(date > 0)  SQL += " AND TRANSFER_DATE < " + date;                
                Statement stmt = dataProvider.connection.createStatement();
                ResultSet rset = stmt.executeQuery(SQL);
                if(rset.next()) {
                    etaDataSummary.RECEIVED = rset.getLong(1);
                    etaDataSummary.UP_TIME  = rset.getLong(2);
                } else {
                    etaDataSummary.RECEIVED = 0;
                    etaDataSummary.UP_TIME  = 0;
                }
                rset.close();
                stmt.close();
            } catch(Exception ex) {
                Log.out("Error summarizing torrent transfer history: " + ex.getMessage());
            }        
        }
    }    

    public void updateData() {
        // get current date
        long currTime = System.currentTimeMillis(); 
        long time = DateTime.getMillisecondsSinceMidnight(currTime);
        long date = currTime - time;

        DownloadStats stats = download.getStats();
        int state = download.getState();

        bytesReceived  = 0;
        bytesDiscarded = 0;
        bytesSent      = 0;
        
        if(state == Download.ST_DOWNLOADING || state == Download.ST_SEEDING) {
            // Update completed data
            int completed = stats.getCompleted();
            if(completed >= 0 && completed <= 1000 && timeCompleted[completed] == 0) {
                Date dateCompleted = new Date();
                setTimeCompleted(completed, dateCompleted.getTime());
                dateCompleted = null;
            }
            // Update transfer data        
            if(currData == null || currData.TRANSFER_DATE != date) {
                currData = getTransferData(date);
                if(currData == null) {
                    //  current data does not exist, create (new date)
                    currData = new TorrentTransferData();
                    currData.TRANSFER_DATE     = date;
                    currData.COMPLETED_TO_DATE = stats.getCompleted();
                    currData.RECEIVED_TO_DATE  = stats.getDownloaded();
                    currData.DISCARDED_TO_DATE = stats.getDiscarded();
                    currData.SENT_TO_DATE      = stats.getUploaded();
                    transfers.addElement(currData);
                    try {
                        Statement stmt = dataProvider.connection.createStatement();
                        stmt.executeUpdate("insert into " +
                                "TORRENT_TRANSFER_DATA(ID, TRANSFER_DATE, COMPLETED_TO_DATE, RECEIVED_TO_DATE, DISCARDED_TO_DATE, SENT_TO_DATE, RECEIVED, DISCARDED, SENT, UP_TIME) " +
                                "values(" + torrentID + ", " + currData.TRANSFER_DATE + ", " + currData.COMPLETED_TO_DATE + ", " + currData.RECEIVED_TO_DATE + ", " + currData.DISCARDED_TO_DATE + ", " + currData.SENT_TO_DATE + ", 0, 0, 0, 0)");
                        stmt.close();
                    } catch(Exception ex) {
                        Log.out("Error creating torrent transfer data: " + ex.getMessage());
                    }
                }
            }
            currStats.TRANSFER_DATE = date;
            currStats.RECEIVED      = stats.getDownloaded();
            currStats.DISCARDED     = stats.getDiscarded();
            currStats.SENT          = stats.getUploaded();
            currStats.UP_TIME       = currTime;
            if(lastStats == null) {
                lastStats = new TorrentTransferData();
            } else {
                long time_diff = currStats.UP_TIME - lastStats.UP_TIME;
                if(currData.TRANSFER_DATE == lastStats.TRANSFER_DATE  && currStats.TRANSFER_DATE == lastStats.TRANSFER_DATE && time_diff > 0 && time_diff <= 5000) {
                    currData.RECEIVED      += currStats.RECEIVED  - lastStats.RECEIVED;
                    currData.DISCARDED     += currStats.DISCARDED - lastStats.DISCARDED;
                    currData.SENT          += currStats.SENT      - lastStats.SENT;
                    currData.UP_TIME       += currStats.UP_TIME   - lastStats.UP_TIME;
                    try {
                        pstmt.setLong(1, currData.RECEIVED);
                        pstmt.setLong(2, currData.DISCARDED);
                        pstmt.setLong(3, currData.SENT);
                        pstmt.setLong(4, currData.UP_TIME);
                        pstmt.setLong(5, torrentID);
                        pstmt.setLong(6, currData.TRANSFER_DATE);
                        pstmt.executeUpdate();
                    } catch(Exception ex) {
                        Log.out("Error updating torrent transfer data: " + ex.getMessage());
                    }
                    bytesReceived  = currStats.RECEIVED  - lastStats.RECEIVED;
                    bytesDiscarded = currStats.DISCARDED - lastStats.DISCARDED;
                    bytesSent      = currStats.SENT      - lastStats.SENT;
                }
            }
            lastStats.set(currStats);
            if(state == Download.ST_SEEDING) {
                etaData.RECEIVED = 1;
                etaData.UP_TIME  = 0;                
            } else {
                getSummaryData(date);
                etaData.RECEIVED = etaDataSummary.RECEIVED + currData.RECEIVED;
                etaData.UP_TIME  = etaDataSummary.UP_TIME  + currData.UP_TIME;                
            }
        } else {
            getSummaryData(0);
            etaData.RECEIVED = etaDataSummary.RECEIVED;
            etaData.UP_TIME  = etaDataSummary.UP_TIME;
        }
        // calculate ETA
        if(etaData.RECEIVED == 0) {
            ETA = Constants.INFINITY_STRING;
        } else {
            long eta = (long)((float)etaData.UP_TIME * (float)(torrentSize - etaData.RECEIVED) / (float)etaData.RECEIVED);
            if(eta == 0) 
                ETA = MessageText.getString("PeerManager.status.finished");
            else
                ETA = TimeFormatter.format(eta / 1000L);
        }

        downloadActivityData.add((int)stats.getDownloadAverage(), 0, 0, (int)time); 
        uploadActivityData.add((int)stats.getUploadAverage(), 0, 0, (int)time);
    }
}