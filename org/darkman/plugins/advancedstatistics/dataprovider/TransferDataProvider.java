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
package org.darkman.plugins.advancedstatistics.dataprovider;

import org.darkman.plugins.advancedstatistics.util.Log;
import org.darkman.plugins.advancedstatistics.util.DateTime;
import com.biglybt.core.global.GlobalManagerStats;
import com.biglybt.core.stats.transfer.OverallStats;
import com.biglybt.core.stats.transfer.StatsFactory;

import java.sql.*;
import java.util.*;

/**
 * @author Darko Matesic
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TransferDataProvider {
    protected DataProvider dataProvider;
    protected GlobalManagerStats stats;
    protected OverallStats totalStats;

    private PreparedStatement pstmt;
    private TransferData lastStats; // holds last read stats
    private TransferData currStats;
    private TransferData currData; // points to currently used data
    
    public Vector transfers;
    public TransferData transferNow;
    public TransferData transferToday;
    public TransferData transferSession;
    private TransferData transferHistorySummary;    
    public TransferData transferHistory;    
    public TransferData transferSystem;
    
    public long bytesDataReceived;  // data bytes received in last update
    public long bytesDataSent;      // data bytes sent in last update
    public long bytesProtReceived;  // protocol bytes received in last update
    public long bytesProtSent;      // protocol bytes sent in last update
    
    public TransferDataProvider(DataProvider dataProvider) {
        Log.out("TransferDataProvider.construct");
        this.dataProvider = dataProvider;
        this.stats = dataProvider.globalManager.getStats();
        this.totalStats = StatsFactory.getStats();
        
        lastStats = null;
        currStats = new TransferData();
        currData = null;
        transfers = new Vector();

        transferNow = new TransferData();
        transferToday = null;
        transferSession = new TransferData();
        transferHistorySummary = new TransferData();    
        transferHistory = new TransferData();    
        transferSystem = new TransferData();
        
        try {
            Statement stmt = dataProvider.connection.createStatement();
            ResultSet rset = stmt.executeQuery("select TRANSFER_DATE, RECEIVED, DISCARDED, SENT, PROT_RECEIVED, PROT_SENT, UP_TIME from TRANSFER_DATA order by TRANSFER_DATE");
            while(rset.next()) {
                TransferData data = new TransferData();
                data.TRANSFER_DATE = rset.getLong(1);
                data.RECEIVED      = rset.getLong(2);
                data.DISCARDED     = rset.getLong(3);
                data.SENT          = rset.getLong(4);
                data.PROT_RECEIVED = rset.getLong(5);
                data.PROT_SENT     = rset.getLong(6);
                data.UP_TIME       = rset.getLong(7);
                transfers.addElement(data);
            }
            rset.close();
            stmt.close();
        } catch(Exception ex) {
            Log.out("Error loading transfer data: " + ex.getMessage());
        }
        try {
            pstmt = dataProvider.connection.prepareStatement(
                    "update TRANSFER_DATA set " +
                    "RECEIVED = ?, DISCARDED = ?, SENT = ?, PROT_RECEIVED = ?, PROT_SENT = ?, UP_TIME = ? " +
                    "where TRANSFER_DATE = ?");
        } catch(Exception ex) {
            Log.out("Error creating prepared statement: " + ex.getMessage());
        }
	}
    public void closedown() {
        try {
            pstmt.close();
        } catch(Exception ex) {
        }        
        if(lastStats != null) { lastStats.closedown(); lastStats = null; }
        if(currStats != null) { currStats.closedown(); currStats = null; }
        currData = null;

        for(Enumeration enumeration = transfers.elements(); enumeration.hasMoreElements();){
	        TransferData data = (TransferData)enumeration.nextElement();
            data.closedown();
	        data = null;
	    }
	    transfers.removeAllElements();
	    transfers = null;

        transferNow.closedown(); transferNow = null;
        transferToday = null;
        transferSession.closedown(); transferSession = null;

        transferHistorySummary.closedown(); transferHistorySummary = null;    
        transferHistory.closedown();    transferHistory = null;    
        transferSystem.closedown();     transferSystem = null;        
    }
    
    private TransferData getTransferData(long date) {
	    for(Enumeration enumeration = transfers.elements(); enumeration.hasMoreElements();){
	        TransferData data = (TransferData)enumeration.nextElement();
	        if(data.TRANSFER_DATE == date) return data;
	    }
	    return null;
    }
    
    private void getTransferHistorySummary(long date) {
        if(transferHistorySummary.TRANSFER_DATE != date) {
            transferHistorySummary.TRANSFER_DATE = date;
            try {
                Statement stmt = dataProvider.connection.createStatement();
                ResultSet rset = stmt.executeQuery(
                        "select SUM(RECEIVED), SUM(DISCARDED), SUM(SENT), " +
                        "SUM(PROT_RECEIVED), SUM(PROT_SENT), SUM(UP_TIME) " +
                        "from TRANSFER_DATA where TRANSFER_DATE < " + date);
                if(rset.next()) {
                    transferHistorySummary.RECEIVED      = rset.getLong(1);
                    transferHistorySummary.DISCARDED     = rset.getLong(2);
                    transferHistorySummary.SENT          = rset.getLong(3);
                    transferHistorySummary.PROT_RECEIVED = rset.getLong(4);
                    transferHistorySummary.PROT_SENT     = rset.getLong(5);
                    transferHistorySummary.UP_TIME       = rset.getLong(6);
                } else {
                    transferHistorySummary.RECEIVED      = 0;
                    transferHistorySummary.DISCARDED     = 0;
                    transferHistorySummary.SENT          = 0;
                    transferHistorySummary.PROT_RECEIVED = 0;
                    transferHistorySummary.PROT_SENT     = 0;
                    transferHistorySummary.UP_TIME       = 0;
                }
                rset.close();
                stmt.close();
            } catch(Exception ex) {
                Log.out("Error summarizing transfer history: " + ex.getMessage());
            }        
        }
    }
   
	public void updateData() {
	    // get current date
        long currTime = System.currentTimeMillis(); 
        long time = DateTime.getMillisecondsSinceMidnight(currTime);
        long date = currTime - time;

        bytesDataReceived = 0;
        bytesDataSent     = 0;
        bytesProtReceived = 0;
        bytesProtSent     = 0;
        
        if(currData == null || currData.TRANSFER_DATE != date) {
            currData = getTransferData(date);
            if(currData == null) {
                // current data does not exist, create (new date)
                currData = new TransferData();
                currData.TRANSFER_DATE = date;
                transfers.addElement(currData);
                try {
                    Statement stmt = dataProvider.connection.createStatement();
                    stmt.executeUpdate("insert into TRANSFER_DATA(TRANSFER_DATE, RECEIVED, DISCARDED, SENT, PROT_RECEIVED, PROT_SENT, UP_TIME) values(" + currData.TRANSFER_DATE + ", 0, 0, 0, 0, 0, 0)");
                    stmt.close();
                } catch(Exception ex) {
                    Log.out("Error creating transfer data: " + ex.getMessage());
                }
            }
        }
        currStats.TRANSFER_DATE = date;
        currStats.RECEIVED      = stats.getTotalDataBytesReceived();
        currStats.SENT          = stats.getTotalDataBytesSent();
        currStats.PROT_RECEIVED = stats.getTotalProtocolBytesReceived();
        currStats.PROT_SENT     = stats.getTotalProtocolBytesSent();
        currStats.UP_TIME       = currTime;
        if(lastStats == null) {
            lastStats = new TransferData();
        } else {
            long time_diff = currStats.UP_TIME - lastStats.UP_TIME;
            if(currData.TRANSFER_DATE == lastStats.TRANSFER_DATE  && currStats.TRANSFER_DATE == lastStats.TRANSFER_DATE && time_diff > 0 && time_diff <= 5000) {
                currData.RECEIVED      += currStats.RECEIVED      - lastStats.RECEIVED;
                currData.DISCARDED     += dataProvider.torrentDataProvider.totalBytesDiscarded;
                currData.SENT          += currStats.SENT          - lastStats.SENT;
                currData.PROT_RECEIVED += currStats.PROT_RECEIVED - lastStats.PROT_RECEIVED;
                currData.PROT_SENT     += currStats.PROT_SENT     - lastStats.PROT_SENT;
                currData.UP_TIME       += currStats.UP_TIME       - lastStats.UP_TIME;
                try {
                    pstmt.setLong(1, currData.RECEIVED);
                    pstmt.setLong(2, currData.DISCARDED);
                    pstmt.setLong(3, currData.SENT);
                    pstmt.setLong(4, currData.PROT_RECEIVED);
                    pstmt.setLong(5, currData.PROT_SENT);
                    pstmt.setLong(6, currData.UP_TIME);
                    pstmt.setLong(7, currData.TRANSFER_DATE);
                    pstmt.executeUpdate();
                } catch(Exception ex) {
                    Log.out("Error updating transfer data: " + ex.getMessage());
                }
                bytesDataReceived = currStats.RECEIVED      - lastStats.RECEIVED;
                bytesDataSent     = currStats.SENT          - lastStats.SENT;
                bytesProtReceived = currStats.PROT_RECEIVED - lastStats.PROT_RECEIVED;
                bytesProtSent     = currStats.PROT_SENT     - lastStats.PROT_SENT;
            }
        }
        lastStats.set(currStats);

        transferNow.RECEIVED      = stats.getDataReceiveRate();
        transferNow.DISCARDED     = dataProvider.torrentDataProvider.totalBytesDiscarded;
        transferNow.SENT          = stats.getDataSendRate();
        transferNow.PROT_RECEIVED = stats.getProtocolReceiveRate();
        transferNow.PROT_SENT     = stats.getProtocolSendRate();
        
        transferToday = currData;

        transferSession.RECEIVED      = stats.getTotalDataBytesReceived();
        transferSession.DISCARDED    += dataProvider.torrentDataProvider.totalBytesDiscarded;
        transferSession.SENT          = stats.getTotalDataBytesSent();
        transferSession.PROT_RECEIVED = stats.getTotalProtocolBytesReceived();
        transferSession.PROT_SENT     = stats.getTotalProtocolBytesSent();
        transferSession.UP_TIME       = totalStats.getSessionUpTime() * 1000;

        getTransferHistorySummary(date);
        transferHistory.RECEIVED      = transferHistorySummary.RECEIVED      + transferToday.RECEIVED;
        transferHistory.DISCARDED     = transferHistorySummary.DISCARDED     + transferToday.DISCARDED;
        transferHistory.SENT          = transferHistorySummary.SENT          + transferToday.SENT;
        transferHistory.PROT_RECEIVED = transferHistorySummary.PROT_RECEIVED + transferToday.PROT_RECEIVED;
        transferHistory.PROT_SENT     = transferHistorySummary.PROT_SENT     + transferToday.PROT_SENT;
        transferHistory.UP_TIME       = transferHistorySummary.UP_TIME       + transferToday.UP_TIME;

        transferSystem.RECEIVED      = totalStats.getDownloadedBytes();
        transferSystem.SENT          = totalStats.getUploadedBytes();
        transferSystem.UP_TIME       = totalStats.getTotalUpTime() * 1000;        
    }
}
