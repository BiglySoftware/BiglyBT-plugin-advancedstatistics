/*
 * Azureus Advanced Statistics Plugin
 * 
 * Created on Tuesday, August 23th 2005
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
package org.darkman.plugins.advancedstatistics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import org.darkman.plugins.advancedstatistics.dataprovider.DataProvider;
import org.darkman.plugins.advancedstatistics.dataprovider.TransferData;
import org.darkman.plugins.advancedstatistics.util.*;
import org.darkman.plugins.advancedstatistics.util.DateTime;
import com.biglybt.core.internat.MessageText;
import com.biglybt.ui.swt.Messages;
import com.biglybt.ui.swt.Utils;

/**
 * @author Darko Matesic
 *
 * 
 */
public class TransferView {
    protected DataProvider dataProvider;
  
    private Composite panel;
  
    private static final String TABLE_SUMMARY_WIDTHS = "table.summary.widths"; 
    private Table summaryTable;
    private TableItemData summaryTableItems[] = {
        new TableItemData(SWT.RIGHT,  60, "AdvancedStatistics.TransferView.summary.table.title"),
        new TableItemData(SWT.RIGHT,  80, "AdvancedStatistics.TransferView.summary.table.total_received"),
        new TableItemData(SWT.RIGHT,  80, "AdvancedStatistics.TransferView.summary.table.total_sent"),
        new TableItemData(SWT.RIGHT,  60, "AdvancedStatistics.TransferView.summary.table.ratio"),
        new TableItemData(SWT.RIGHT, 110, "AdvancedStatistics.TransferView.summary.table.received"),
        new TableItemData(SWT.RIGHT, 110, "AdvancedStatistics.TransferView.summary.table.discarded"),
        new TableItemData(SWT.RIGHT, 110, "AdvancedStatistics.TransferView.summary.table.useful"),        
        new TableItemData(SWT.RIGHT, 110, "AdvancedStatistics.TransferView.summary.table.sent"),
        new TableItemData(SWT.RIGHT, 110, "AdvancedStatistics.TransferView.summary.table.prot_received"),
        new TableItemData(SWT.RIGHT, 110, "AdvancedStatistics.TransferView.summary.table.prot_sent"),
        new TableItemData(SWT.RIGHT,  90, "AdvancedStatistics.TransferView.summary.table.up_time"),
    };
    private String summaryTableTitle[] = {
        MessageText.getString("AdvancedStatistics.TransferView.summary.table.title.now"),
        MessageText.getString("AdvancedStatistics.TransferView.summary.table.title.today"),
        MessageText.getString("AdvancedStatistics.TransferView.summary.table.title.session"),
        MessageText.getString("AdvancedStatistics.TransferView.summary.table.title.history"),
        MessageText.getString("AdvancedStatistics.TransferView.summary.table.title.system")
    };            
    
    private static final String TABLE_HISTORY_WIDTHS = "table.history.widths"; 
    private Table historyTable;
    private TableItemData historyTableItems[] = {
        new TableItemData(SWT.RIGHT,  70, "AdvancedStatistics.TransferView.history.table.date"),
        new TableItemData(SWT.RIGHT,  80, "AdvancedStatistics.TransferView.history.table.total_received"),
        new TableItemData(SWT.RIGHT,  80, "AdvancedStatistics.TransferView.history.table.total_sent"),
        new TableItemData(SWT.RIGHT,  60, "AdvancedStatistics.TransferView.history.table.ratio"),        
        new TableItemData(SWT.RIGHT, 110, "AdvancedStatistics.TransferView.history.table.received"),
        new TableItemData(SWT.RIGHT, 110, "AdvancedStatistics.TransferView.history.table.discarded"),
        new TableItemData(SWT.RIGHT, 110, "AdvancedStatistics.TransferView.history.table.useful"),
        new TableItemData(SWT.RIGHT, 110, "AdvancedStatistics.TransferView.history.table.sent"),
        new TableItemData(SWT.RIGHT, 110, "AdvancedStatistics.TransferView.history.table.prot_received"),
        new TableItemData(SWT.RIGHT, 110, "AdvancedStatistics.TransferView.history.table.prot_sent"),
        new TableItemData(SWT.RIGHT,  60, "AdvancedStatistics.TransferView.history.table.up_time"),
    };

    public String getFullTitle() { return MessageText.getString("AdvancedStatistics.TransferView.title.full"); }
    public String getData() { return MessageText.getString("AdvancedStatistics.TransferView.title.full"); }
    public Composite getComposite() { return panel; }
    
    public TransferView(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }
  
    public void initialize(Composite composite) {
        panel = new Composite(composite, SWT.NULL);
        panel.setLayout(new GridLayout());

        //////////////////////////////
        // create group "summary"
        //////////////////////////////        
        Group groupSummary = new Group(panel, SWT.NULL);
        Messages.setLanguageText(groupSummary, "AdvancedStatistics.TransferView.summary.title");
        
        GridData layoutDataSummary = new GridData(SWT.FILL, SWT.NULL, true, false);
        layoutDataSummary.heightHint = 120;
        groupSummary.setLayoutData(layoutDataSummary);
        groupSummary.setLayout(new GridLayout());

        summaryTable = TableConstructor.constructTable(groupSummary, SWT.VIRTUAL | SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE, summaryTableItems, dataProvider.config.getTableWidths(summaryTableItems, TABLE_SUMMARY_WIDTHS));
        summaryTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        summaryTable.setItemCount(5);
        summaryTable.addListener(SWT.SetData, new Listener() {
            @Override
            public void handleEvent(Event event) {
                TableItem item = (TableItem)event.item;
                String transferTitle = "";
                boolean showTotalReceived = true;
                boolean showTotalSent     = true;
                boolean showRatio         = true;
                boolean showReceived      = true;
                boolean showDiscarded     = true;
                boolean showSent          = true;
                boolean showProtReceived  = true;
                boolean showProtSent      = true;
                boolean showUpTime        = true; 
                TransferData data = null;
                switch(summaryTable.indexOf(item)) {
                    case 0:
                        transferTitle = summaryTableTitle[0];
                        showRatio     = false;
                        showUpTime    = false; 
                        data = dataProvider.transferDataProvider.transferNow;
                        break;
                    case 1: 
                        transferTitle = summaryTableTitle[1];
                        data = dataProvider.transferDataProvider.transferToday;
                        break;
                    case 2:
                        transferTitle = summaryTableTitle[2];
                        data = dataProvider.transferDataProvider.transferSession;
                        break;
                    case 3:
                        transferTitle = summaryTableTitle[3];
                        data = dataProvider.transferDataProvider.transferHistory;
                        break;
                    case 4:
                        transferTitle = summaryTableTitle[4];
                        showReceived      = false;
                        showDiscarded     = false;
                        showSent          = false;
                        showProtReceived  = false;
                        showProtSent      = false;
                        data = dataProvider.transferDataProvider.transferSystem;
                        break;
                }
                // showUseful == showDiscarded
                if(data != null) {
                    long TOTAL_RECEIVED = data.RECEIVED + data.PROT_RECEIVED;
                    long TOTAL_SENT = data.SENT + data.PROT_SENT;
                    /*  1 - title          */ item.setText(0, transferTitle);
                    /*  2 - total_received */ if(!showTotalReceived) item.setText( 1, ""); else item.setText( 1, TransferFormatter.formatTransfered(TOTAL_RECEIVED));
                    /*  3 - total_sent     */ if(!showTotalSent    ) item.setText( 2, ""); else item.setText( 2, TransferFormatter.formatTransfered(TOTAL_SENT));
                    /*  4 - ratio          */ if(!showRatio        ) item.setText( 3, ""); else item.setText( 3, TransferFormatter.formatRatio(TOTAL_SENT, TOTAL_RECEIVED));
                    /*  5 - received       */ if(!showReceived     ) item.setText( 4, ""); else item.setText( 4, TransferFormatter.formatTransfered(data.RECEIVED, TOTAL_RECEIVED));
                    /*  6 - discarded      */ if(!showDiscarded    ) item.setText( 5, ""); else item.setText( 5, TransferFormatter.formatTransfered(data.DISCARDED, data.RECEIVED));
                    /*  7 - useful         */ if(!showDiscarded    ) item.setText( 6, ""); else item.setText( 6, TransferFormatter.formatTransfered(data.RECEIVED - data.DISCARDED, data.RECEIVED));
                    /*  8 - sent           */ if(!showSent         ) item.setText( 7, ""); else item.setText( 7, TransferFormatter.formatTransfered(data.SENT, TOTAL_SENT));
                    /*  9 - prot_received  */ if(!showProtReceived ) item.setText( 8, ""); else item.setText( 8, TransferFormatter.formatTransfered(data.PROT_RECEIVED, TOTAL_RECEIVED));
                    /* 10 - prot_sent      */ if(!showProtSent     ) item.setText( 9, ""); else item.setText( 9, TransferFormatter.formatTransfered(data.PROT_SENT, TOTAL_SENT));
                    /* 11 - up_time        */ if(!showUpTime       ) item.setText(10, ""); else item.setText(10, DateTime.getElapsedTimeString(data.UP_TIME, true));
                }
            }
          });
        summaryTable.addDisposeListener(new DisposeListener() {
					@Override
					public void widgetDisposed(DisposeEvent e) {
						if (dataProvider != null && dataProvider.config != null) {
			        dataProvider.config.saveTableWidths(summaryTable, TABLE_SUMMARY_WIDTHS);
						}
					}
				});

        
        //////////////////////////////
        // create group "History"
        //////////////////////////////        
        Group groupHistory = new Group(panel, SWT.NONE);
        Messages.setLanguageText(groupHistory, "AdvancedStatistics.TransferView.history.title");
        groupHistory.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        groupHistory.setLayout(new GridLayout());

        historyTable = TableConstructor.constructTable(groupHistory, SWT.VIRTUAL | SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE, historyTableItems, dataProvider.config.getTableWidths(historyTableItems, TABLE_HISTORY_WIDTHS));
        historyTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        historyTable.addListener(SWT.SetData, new Listener() {
            @Override
            public void handleEvent(Event event) {
                TableItem item = (TableItem)event.item;
                TransferData data = (TransferData)dataProvider.transferDataProvider.transfers.get(historyTable.indexOf(item));
                if(data != null) {
                    long TOTAL_RECEIVED = data.RECEIVED + data.PROT_RECEIVED;
                    long TOTAL_SENT = data.SENT + data.PROT_SENT;
                    /*  1 - date           */ item.setText( 0, DateTime.getDateString(data.TRANSFER_DATE));
                    /*  2 - total_received */ item.setText( 1, TransferFormatter.formatTransfered(TOTAL_RECEIVED));
                    /*  3 - total_sent     */ item.setText( 2, TransferFormatter.formatTransfered(TOTAL_SENT));
                    /*  4 - ratio          */ item.setText( 3, TransferFormatter.formatRatio(TOTAL_SENT, TOTAL_RECEIVED));
                    /*  5 - received       */ item.setText( 4, TransferFormatter.formatTransfered(data.RECEIVED, TOTAL_RECEIVED));
                    /*  6 - discarded      */ item.setText( 5, TransferFormatter.formatTransfered(data.DISCARDED, data.RECEIVED));
                    /*  7 - useful         */ item.setText( 6, TransferFormatter.formatTransfered(data.RECEIVED - data.DISCARDED, data.RECEIVED));
                    /*  8 - sent           */ item.setText( 7, TransferFormatter.formatTransfered(data.SENT, TOTAL_SENT));
                    /*  9 - prot_received  */ item.setText( 8, TransferFormatter.formatTransfered(data.PROT_RECEIVED, TOTAL_RECEIVED));
                    /* 10 - prot_sent      */ item.setText( 9, TransferFormatter.formatTransfered(data.PROT_SENT, TOTAL_SENT));
                    /* 11 - up_time        */ item.setText(10, DateTime.getElapsedTimeString(data.UP_TIME, true));
                }
            }
          });
        historyTable.addDisposeListener(new DisposeListener() {
					@Override
					public void widgetDisposed(DisposeEvent e) {
						if (dataProvider != null && dataProvider.config != null) {
			        dataProvider.config.saveTableWidths(historyTable, TABLE_HISTORY_WIDTHS);
						}
					}
				});
        
    }
  
    public void delete() {
        Utils.disposeComposite(panel);
    }

    public void refresh( boolean force ) {
        summaryTable.clearAll();
        summaryTable.redraw(); 
        
        historyTable.setItemCount(dataProvider.transferDataProvider.transfers.size());
        historyTable.clearAll();
        historyTable.redraw(); 
    }  
}
