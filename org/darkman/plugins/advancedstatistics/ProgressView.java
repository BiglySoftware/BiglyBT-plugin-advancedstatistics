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

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import org.darkman.plugins.advancedstatistics.dataprovider.DataProvider;
import org.darkman.plugins.advancedstatistics.dataprovider.TorrentData;
import org.darkman.plugins.advancedstatistics.dataprovider.TorrentTransferData;
import org.darkman.plugins.advancedstatistics.graphic.ProgressGraphic;
import org.darkman.plugins.advancedstatistics.util.*;
import org.darkman.plugins.advancedstatistics.util.DateTime;
import com.biglybt.core.internat.MessageText;
import com.biglybt.pif.download.Download;
import com.biglybt.pif.download.DownloadStats;
import com.biglybt.ui.swt.Messages;
import com.biglybt.ui.swt.Utils;
import com.biglybt.ui.swt.mainwindow.Colors;

/**
 * @author Darko Matesic
 *
 * 
 */
public class ProgressView {
    protected DataProvider dataProvider;
	private Composite panel;
   
    private static final String TABLE_TORRENTS_WIDTHS = "table.torrents.widths"; 
    private Table torrentsTable;
    private TableItemData torrentsTableItems[] = {
		new TableItemData(SWT.RIGHT,  20, "AdvancedStatistics.ProgressView.torrents.table.no"),
	    new TableItemData( SWT.LEFT, 160, "AdvancedStatistics.ProgressView.torrents.table.name"),
	    new TableItemData(SWT.RIGHT,  60, "AdvancedStatistics.ProgressView.torrents.table.completed"),
	    new TableItemData(SWT.RIGHT,  75, "AdvancedStatistics.ProgressView.torrents.table.downloaded"),
	    new TableItemData(SWT.RIGHT,  75, "AdvancedStatistics.ProgressView.torrents.table.uploaded"),
        new TableItemData(SWT.RIGHT,  75, "AdvancedStatistics.ProgressView.torrents.table.discarded"),
        new TableItemData(SWT.RIGHT,  75, "AdvancedStatistics.ProgressView.torrents.table.remaining"),
	    new TableItemData(SWT.RIGHT, 130, "AdvancedStatistics.ProgressView.torrents.table.started"),
	    new TableItemData(SWT.RIGHT,  80, "AdvancedStatistics.ProgressView.torrents.table.elapsed"),
	    new TableItemData(SWT.RIGHT,  80, "AdvancedStatistics.ProgressView.torrents.table.downloading"),
	    new TableItemData(SWT.RIGHT,  80, "AdvancedStatistics.ProgressView.torrents.table.seeding"),
        new TableItemData(SWT.RIGHT,  80, "AdvancedStatistics.ProgressView.torrents.table.eta.speed"),
        new TableItemData(SWT.RIGHT,  80, "AdvancedStatistics.ProgressView.torrents.table.eta.progress")
	};

    private TabFolder folderProgress;   
    private TabItem itemProgressGraphic;
    private TabItem itemProgressTable;

    private ProgressGraphic progressGraphic;

    private static final String TABLE_PROGRESS_WIDTHS = "table.progress.widths"; 
    private Table progressTable;
    private TableItemData progressTableItems[] = {
        new TableItemData(SWT.RIGHT,  70, "AdvancedStatistics.ProgressView.progress.table.date"),
        new TableItemData(SWT.RIGHT,  60, "AdvancedStatistics.ProgressView.progress.table.completed_to_date"),
        new TableItemData(SWT.RIGHT, 120, "AdvancedStatistics.ProgressView.progress.table.received_to_date"),
        new TableItemData(SWT.RIGHT, 120, "AdvancedStatistics.ProgressView.progress.table.discarded_to_date"),
        new TableItemData(SWT.RIGHT, 120, "AdvancedStatistics.ProgressView.progress.table.sent_to_date"),
        new TableItemData(SWT.RIGHT,  55, "AdvancedStatistics.ProgressView.progress.table.ratio_to_date"),
        new TableItemData(SWT.RIGHT, 110, "AdvancedStatistics.ProgressView.progress.table.received"),
        new TableItemData(SWT.RIGHT, 110, "AdvancedStatistics.ProgressView.progress.table.discarded"),
        new TableItemData(SWT.RIGHT, 110, "AdvancedStatistics.ProgressView.progress.table.sent"),
        new TableItemData(SWT.RIGHT,  55, "AdvancedStatistics.ProgressView.progress.table.ratio"),
        new TableItemData(SWT.RIGHT,  60, "AdvancedStatistics.ProgressView.progress.table.up_time"),
    };

	public String getFullTitle() { return MessageText.getString("AdvancedStatistics.ProgressView.title.full"); }
	public String getData() { return MessageText.getString("AdvancedStatistics.ProgressView.title.full"); }
	public Composite getComposite() { return panel; }

	public ProgressView(DataProvider dataProvider) {
	    this.dataProvider = dataProvider;
	}
  
	public void initialize(Composite composite) {
	    panel = new Composite(composite, SWT.NULL);
	    panel.setLayout(new GridLayout());
        
        SashForm form = new SashForm(panel, SWT.VERTICAL);
        form.setLayoutData(new GridData(GridData.FILL_BOTH));

        //////////////////////////////
        // create group "Torrents"
        //////////////////////////////
	    Group groupTorrents = new Group(form, SWT.NULL);
	    Messages.setLanguageText(groupTorrents, "AdvancedStatistics.ProgressView.torrents.title");
	    groupTorrents.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));    
	    groupTorrents.setLayout(new GridLayout());
	            
	    torrentsTable = TableConstructor.constructTable(groupTorrents, SWT.VIRTUAL | SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE, torrentsTableItems, dataProvider.config.getTableWidths(torrentsTableItems, TABLE_TORRENTS_WIDTHS));
        torrentsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        torrentsTable.addListener(SWT.SetData, new Listener() {
	        @Override
	        public void handleEvent(Event event) {
	            TableItem item = (TableItem)event.item;
	            TorrentData data = (TorrentData)dataProvider.torrentDataProvider.torrents.get(torrentsTable.indexOf(item));
                if(data != null) {
                    Download download = data.download;                    
                    DownloadStats stats = download.getStats();                    
                    Date date = new Date();
                    /*  1 - no           */ item.setText( 0, "" + (download.getPosition() + 1));
                    /*  2 - name         */ item.setText( 1, download.getName());
                    /*  3 - completed    */ item.setText( 2, TransferFormatter.formatPercent((float)stats.getCompleted() / 10));
                    /*  4 - downloaded   */ item.setText( 3, TransferFormatter.formatTransfered(stats.getDownloaded()));
                    /*  5 - uploaded     */ item.setText( 4, TransferFormatter.formatTransfered(stats.getUploaded()));
                    /*  6 - discarded    */ item.setText( 5, TransferFormatter.formatTransfered(stats.getDiscarded()));
                    /*  7 - remaining    */ item.setText( 6, TransferFormatter.formatTransfered(data.torrentSize - ((long)stats.getCompleted() * data.torrentSize / 1000L)));
                    /*  8 - started      */ item.setText( 7, DateTime.getDateTimeString(download.getCreationTime()));
                    /*  9 - elapsed      */ item.setText( 8, DateTime.getElapsedTimeString(date.getTime() - download.getCreationTime(), true));
                    /* 10 - downloading  */ item.setText( 9, DateTime.getElapsedTimeString(stats.getSecondsDownloading() * 1000, true));
                    /* 11 - seeding      */ item.setText(10, DateTime.getElapsedTimeString(stats.getSecondsOnlySeeding() * 1000, true));
                    /* 12 - speed eta    */ item.setText(11, stats.getETA());
                    /* 12 - progress eta */ item.setText(12, data.ETA);
                } else {
                    for(int i = 0; i < 11; i++) item.setText(i, "");
                }
	        }
	      });
        torrentsTable.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                refresh();
            }
        });
        
        torrentsTable.addDisposeListener(new DisposeListener() {
					@Override
					public void widgetDisposed(DisposeEvent e) {
						if (dataProvider != null && dataProvider.config != null) {
							dataProvider.config.saveTableWidths(torrentsTable, TABLE_TORRENTS_WIDTHS);
						}
					}
				});

        //////////////////////////////
        // create group "Progress"
        //////////////////////////////
        Group groupProgress = new Group(form, SWT.NONE);
        Messages.setLanguageText(groupProgress, "AdvancedStatistics.ProgressView.progress.title");
        groupProgress.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        groupProgress.setLayout(new GridLayout());
        
        folderProgress = new TabFolder(groupProgress, SWT.NONE);
        folderProgress.setBackground(Colors.background);
        folderProgress.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        folderProgress.setLayout(new GridLayout());

        Composite compositeProgressGraphic = new Composite(folderProgress, SWT.NULL);
        compositeProgressGraphic.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        compositeProgressGraphic.setLayout(new GridLayout());
        Composite compositeProgressTable = new Composite(folderProgress, SWT.NULL);
        compositeProgressTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        compositeProgressTable.setLayout(new GridLayout());

        itemProgressGraphic = new TabItem(folderProgress, SWT.NULL);
        itemProgressGraphic.setControl(compositeProgressGraphic);
        itemProgressTable = new TabItem(folderProgress, SWT.NULL);        
        itemProgressTable.setControl(compositeProgressTable);
        Messages.setLanguageText(itemProgressGraphic, "AdvancedStatistics.ProgressView.progress.graphic.title");
        Messages.setLanguageText(itemProgressTable, "AdvancedStatistics.ProgressView.progress.table.title");

        Canvas canvasProgressGraphic = new Canvas(compositeProgressGraphic, SWT.NULL);
        canvasProgressGraphic.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        progressGraphic = new ProgressGraphic(canvasProgressGraphic, this, dataProvider.config);

        progressTable = TableConstructor.constructTable(compositeProgressTable, SWT.VIRTUAL | SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE, progressTableItems, dataProvider.config.getTableWidths(progressTableItems, TABLE_PROGRESS_WIDTHS));
        progressTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        progressTable.addListener(SWT.SetData, new Listener() {
            @Override
            public void handleEvent(Event event) {
                TableItem item = (TableItem)event.item;
                TorrentData torrentData = getSelectedTorrentData();
                if(torrentData != null) {
                    TorrentTransferData data = (TorrentTransferData)torrentData.transfers.get(progressTable.indexOf(item));
                    if(data != null) {
                        /*  1 - date              */ item.setText( 0, DateTime.getDateString(data.TRANSFER_DATE));
                        /*  2 - completed_to_date */ item.setText( 1, TransferFormatter.formatPercent((float)data.COMPLETED_TO_DATE / 10));
                        /*  3 - received_to_date  */ item.setText( 2, TransferFormatter.formatTransfered(data.RECEIVED_TO_DATE, torrentData.torrentSize));
                        /*  4 - discarded_to_date */ item.setText( 3, TransferFormatter.formatTransfered(data.DISCARDED_TO_DATE, data.RECEIVED_TO_DATE));
                        /*  5 - sent_to_date      */ item.setText( 4, TransferFormatter.formatTransfered(data.SENT_TO_DATE, torrentData.torrentSize));
                        /*  6 - ratio_to_date     */ item.setText( 5, TransferFormatter.formatRatio(data.SENT_TO_DATE, torrentData.torrentSize));
                        /*  7 - received          */ item.setText( 6, TransferFormatter.formatTransfered(data.RECEIVED, torrentData.torrentSize));
                        /*  8 - discarded         */ item.setText( 7, TransferFormatter.formatTransfered(data.DISCARDED, data.RECEIVED));
                        /*  9 - sent              */ item.setText( 8, TransferFormatter.formatTransfered(data.SENT, torrentData.torrentSize));
                        /* 10 - ratio             */ item.setText( 9, TransferFormatter.formatRatio(data.SENT, data.RECEIVED));
                        /* 11 - up_time           */ item.setText(10, DateTime.getElapsedTimeString(data.UP_TIME, true));
                    }
                } else {
                    for(int i = 0; i < 10; i++) item.setText(i, "");
                }
            }
          });
        torrentsTable.addDisposeListener(new DisposeListener() {
					@Override
					public void widgetDisposed(DisposeEvent e) {
						if (dataProvider != null && dataProvider.config != null) {
			        dataProvider.config.saveTableWidths(progressTable, TABLE_PROGRESS_WIDTHS);        
						}
					}
				});

    }

	public void delete() {
	    Utils.disposeComposite(panel);
        progressGraphic.dispose();
        if(!folderProgress.isDisposed()) Utils.disposeComposite(folderProgress);
	}

	public void refresh() {
        torrentsTable.setItemCount(dataProvider.torrentDataProvider.torrents.size());
        torrentsTable.clearAll();
	    torrentsTable.redraw();

        switch(folderProgress.getSelectionIndex()) {
            case 0:
                progressGraphic.refresh();
                break;
            case 1:
                TorrentData torrentData = getSelectedTorrentData();
                if(torrentData != null) {
                    progressTable.setItemCount(torrentData.transfers.size());
                    progressTable.clearAll();
                    progressTable.redraw();
                }
                break;
        }
	}
    public TorrentData getSelectedTorrentData() {
        int index = torrentsTable.getSelectionIndex();
        if(index >= 0) return (TorrentData)dataProvider.torrentDataProvider.torrents.get(index);
        return null;        
    }
}
