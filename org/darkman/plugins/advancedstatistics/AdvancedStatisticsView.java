/*
 * Azureus Advanced Statistics Plugin
 * 
 * Created on Monday, August 22nd 2005
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

import org.darkman.plugins.advancedstatistics.dataprovider.*;
import org.darkman.plugins.advancedstatistics.util.Log;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import com.biglybt.core.util.Debug;
import com.biglybt.ui.swt.Messages;
import com.biglybt.ui.swt.Utils;
import com.biglybt.ui.swt.mainwindow.Colors;

import com.biglybt.ui.swt.pif.*;

/**
 * @author Darko Matesic
 *
 * 
 */
public class AdvancedStatisticsView implements UISWTViewEventListener {
    protected DataProvider dataProvider; 
    private boolean isCreated = false;
	  
    TabFolder folder;
    
	TabItem itemProgress;
    TabItem itemActivity;
    TabItem itemTransfer;

    ActivityView viewActivity;
    ProgressView viewProgress;
    TransferView viewTransfer;
    
	public AdvancedStatisticsView(DataProvider dataProvider) {
	    Log.out("AdvancedStatisticsView.construct");
		this.dataProvider = dataProvider;
	}

	private void initialize(Composite composite) {
	    Log.out("AdvancedStatisticsView.initialize");

	    folder = new TabFolder(composite, SWT.LEFT);
	    folder.setBackground(Colors.background);

	    itemProgress = new TabItem(folder, SWT.NULL);
	    itemActivity = new TabItem(folder, SWT.NULL);
	    itemTransfer = new TabItem(folder, SWT.NULL);

        try {
            viewProgress = new ProgressView(dataProvider);
        } catch(Exception ex) {
            Log.out("Error creating progress view: " + ex.getMessage());
        }
        try {
            viewActivity = new ActivityView(dataProvider);
        } catch(Exception ex) {
            Log.out("Error creating activity view: " + ex.getMessage());
        }
        try {
            viewTransfer = new TransferView(dataProvider);
        } catch(Exception ex) {
            Log.out("Error creating transfer view: " + ex.getMessage());            
        }
	    
	    Messages.setLanguageText(itemProgress, "AdvancedStatistics.ProgressView.title.full");
	    Messages.setLanguageText(itemActivity, "AdvancedStatistics.ActivityView.title.full");
	    Messages.setLanguageText(itemTransfer, "AdvancedStatistics.TransferView.title.full");
	    
	    //TabItem items[] = { itemProgress };
	    //folder.setSelection(items);

        if(viewProgress != null) {
            viewProgress.initialize(folder);
            itemProgress.setControl(viewProgress.getComposite());
        }
        if(viewActivity != null) {
            viewActivity.initialize(folder);
            itemActivity.setControl(viewActivity.getComposite());
        }
        if(viewTransfer != null) {
            viewTransfer.initialize(folder);
            itemTransfer.setControl(viewTransfer.getComposite());
        }

	    folder.addSelectionListener(new SelectionListener() {
	        @Override
	        public void widgetSelected(SelectionEvent e) {
	          refresh();
	        }
	        @Override
	        public void widgetDefaultSelected(SelectionEvent e) {
	        }
	    });

	    refresh();
	}
	  
	private void refresh() {
		if(folder == null || folder.isDisposed()) return;
		try {
            switch(folder.getSelectionIndex()) {
                case 0:
                    if(viewProgress != null && !itemProgress.isDisposed()) viewProgress.refresh();
				    break;
                case 1:
                    if(viewActivity != null && !itemActivity.isDisposed()) viewActivity.refresh();
                    break;
                case 2:
                    if(viewTransfer != null && !itemTransfer.isDisposed()) viewTransfer.refresh();
                    break;
            }
		} catch (Exception e) {
			Debug.printStackTrace( e );
		}
	}
    
	private void delete() {
		try {
  	    Log.out("AdvancedStatisticsView.delete");
          if(viewProgress != null) viewProgress.delete();
  		if(viewActivity != null) viewActivity.delete();
  		if(viewTransfer != null) viewTransfer.delete();
  		if(folder != null && !folder.isDisposed()) Utils.disposeComposite(folder);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean eventOccurred(UISWTViewEvent event) {
		switch (event.getType()) {
			case UISWTViewEvent.TYPE_CREATE:
				if (isCreated)
					return false;

				isCreated = true;
				break;
				
		    case UISWTViewEvent.TYPE_DESTROY:
		    	delete();
		    	isCreated = false;
		    	break;
		
		    case UISWTViewEvent.TYPE_INITIALIZE:
		        initialize((Composite)event.getData());
		        break;

		    case UISWTViewEvent.TYPE_REFRESH:
		        refresh();
		        break;
		}

		return true;
	}
}
