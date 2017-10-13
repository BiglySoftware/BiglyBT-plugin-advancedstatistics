/*
 * Azureus Advanced Statistics Plugin
 * 
 * Created on Sunday, October 16th 2005
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
import org.darkman.plugins.advancedstatistics.graphic.*;
//import org.darkman.plugins.advancedstatistics.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import com.biglybt.core.config.ParameterListener;
import com.biglybt.core.internat.MessageText;
import com.biglybt.ui.swt.Messages;
import com.biglybt.ui.swt.Utils;

/**
 * @author Darko Matesic
 *
 * 
 */
public class ActivityView implements ParameterListener  {
    protected DataProvider dataProvider;
    
    private Composite panel;
    
    private ActivityGraphic activityGraphicDownload;
    private ActivityGraphic activityGraphicUpload;
    
    public String getFullTitle() { return MessageText.getString("AdvancedStatistics.ActivityView.title.full"); }
    public String getData() { return MessageText.getString("AdvancedStatistics.ActivityView.title.full"); }
    public Composite getComposite() { return panel; }

    private Combo comboDisplayType;
    private Combo comboScale;
    private Button checkShowLimit;
    private Button checkShowLegend;
    private Slider slider;
    private boolean selectZeroOffset; 
    
    public ActivityView(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }
  
    public void initialize(Composite composite) {
        panel = new Composite(composite, SWT.NULL);
        panel.setLayout(new GridLayout());

        //////////////////////////////
        // create group "Settings"
        //////////////////////////////
        Group groupSettings = new Group(panel, SWT.NULL);
        Messages.setLanguageText(groupSettings, "AdvancedStatistics.ActivityView.settings.title");
        GridData groupSettingsGridData = new GridData(SWT.FILL, SWT.NULL, true, false);
        groupSettingsGridData.heightHint = 35;
        groupSettings.setLayoutData(groupSettingsGridData);
        groupSettings.setLayout(new GridLayout(8, false));

        Label labelDisplayType = new Label(groupSettings, SWT.RIGHT);
        Messages.setLanguageText(labelDisplayType, "AdvancedStatistics.ActivityView.settings.display.type");
        comboDisplayType = new Combo(groupSettings, SWT.DROP_DOWN | SWT.READ_ONLY);
        for(int i = 0; i < AdvancedStatisticsConfig.ACTIVITY_DISPLAY_TYPE_LABELS.length; i++)
            comboDisplayType.add(AdvancedStatisticsConfig.ACTIVITY_DISPLAY_TYPE_LABELS[i]);
        comboDisplayType.select(dataProvider.config.activityDisplayType);
        comboDisplayType.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Combo combo = (Combo)e.widget;
                dataProvider.config.activityDisplayType = combo.getSelectionIndex();
                refresh();
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        Label labelScale = new Label(groupSettings, SWT.RIGHT);
        Messages.setLanguageText(labelScale, "AdvancedStatistics.ActivityView.settings.scale");
        comboScale = new Combo(groupSettings, SWT.DROP_DOWN | SWT.READ_ONLY);
        for(int i = 0; i < AdvancedStatisticsConfig.ACTIVITY_SCALE_LABELS.length; i++)
            comboScale.add(AdvancedStatisticsConfig.ACTIVITY_SCALE_LABELS[i]);
        comboScale.select(ActivityData.scaleToIndex(dataProvider.config.activityScale));
        comboScale.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Combo combo = (Combo)e.widget;
                dataProvider.config.activityScale = ActivityData.indexToScale(combo.getSelectionIndex());
                selectZeroOffset = true;
                refresh();
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        Label labelShowLimit = new Label(groupSettings, SWT.RIGHT);
        Messages.setLanguageText(labelShowLimit, "AdvancedStatistics.ActivityView.settings.show.limit");
        checkShowLimit = new Button(groupSettings, SWT.CHECK);
        checkShowLimit.setSelection(dataProvider.config.activityShowLimit);
        checkShowLimit.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button button = (Button)e.widget;
                dataProvider.config.activityShowLimit = button.getSelection();
                refresh();
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        Label labelShowLegend = new Label(groupSettings, SWT.RIGHT);
        Messages.setLanguageText(labelShowLegend, "AdvancedStatistics.ActivityView.settings.show.legend");
        checkShowLegend = new Button(groupSettings, SWT.CHECK);
        checkShowLegend.setSelection(dataProvider.config.activityShowLegend);
        checkShowLegend.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button button = (Button)e.widget;
                dataProvider.config.activityShowLegend = button.getSelection();
                refresh();
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        
        dataProvider.config.addParameterListener(this);
        
        //////////////////////////////
        // create group "Activity"
        //////////////////////////////
        Group groupActivity = new Group(panel, SWT.NULL);
        Messages.setLanguageText(groupActivity, "AdvancedStatistics.ActivityView.activity.title");
        groupActivity.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));    
        groupActivity.setLayout(new GridLayout());
        
        Canvas canvasGraphicDownload = new Canvas(groupActivity, SWT.NULL);
        canvasGraphicDownload.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        activityGraphicDownload = new ActivityGraphic(canvasGraphicDownload, dataProvider.config, dataProvider.downloadActivityData, dataProvider.torrentDataProvider.downloadActivityData);
        
        slider = new Slider(groupActivity, SWT.HORIZONTAL);
        GridData sliderGridData = new GridData(SWT.FILL, SWT.NULL, true, false);
        sliderGridData.heightHint = 20;
        slider.setLayoutData(sliderGridData);
        slider.setMinimum(0);
        slider.setMaximum(100);
        slider.setThumb(100);
        slider.setIncrement(60);
        slider.setPageIncrement(60);
        slider.setMaximum(activityGraphicDownload.getSliderMax());
        selectZeroOffset = true;        
        
        Canvas canvasGraphicUpload = new Canvas(groupActivity, SWT.NULL);
        canvasGraphicUpload.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        activityGraphicUpload = new ActivityGraphic(canvasGraphicUpload, dataProvider.config, dataProvider.uploadActivityData, dataProvider.torrentDataProvider.uploadActivityData);
    }

    @Override
    public void parameterChanged(String parameter) {
        if(parameter.compareTo(AdvancedStatisticsConfig.ACTIVITY_DISPLAY_TYPE) == 0) {
            comboDisplayType.select(dataProvider.config.activityDisplayType);

        } else if(parameter.compareTo(AdvancedStatisticsConfig.ACTIVITY_SCALE) == 0) {
            comboScale.select(ActivityData.scaleToIndex(dataProvider.config.activityScale));

        } else if(parameter.compareTo(AdvancedStatisticsConfig.ACTIVITY_SHOW_LIMIT) == 0) {            
            checkShowLimit.setSelection(dataProvider.config.activityShowLimit);

        } else if(parameter.compareTo(AdvancedStatisticsConfig.ACTIVITY_SHOW_LEGEND) == 0) {            
            checkShowLegend.setSelection(dataProvider.config.activityShowLegend);
        }
    }

    public void delete() {
        dataProvider.config.removeParameterListener(this);
        Utils.disposeComposite(panel);
        if(activityGraphicDownload != null) activityGraphicDownload.dispose();
        if(activityGraphicUpload != null) activityGraphicUpload.dispose();
    }

    public void refresh() {
        activityGraphicDownload.updateScale();
        int sliderMax = activityGraphicDownload.getSliderMax();
        int sliderThumb = activityGraphicDownload.getSliderThumb();
        slider.setMaximum(sliderMax);
        slider.setThumb(sliderThumb);
        slider.setPageIncrement(sliderThumb);
       int sampleOffset = 0;
        if(selectZeroOffset)
            slider.setSelection(sliderMax);
        else
            sampleOffset = sliderMax - sliderThumb - slider.getSelection();
        selectZeroOffset = false;
        
        if(activityGraphicDownload != null) activityGraphicDownload.refresh(sampleOffset);
        if(activityGraphicUpload != null) activityGraphicUpload.refresh(sampleOffset);
    }
}