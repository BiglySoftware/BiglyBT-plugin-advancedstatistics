/*
 * Azureus Advanced Statistics Plugin
 * 
 * Created on Wednesday, October 19th 2005
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

import java.util.Vector;
import org.darkman.plugins.advancedstatistics.util.*;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import com.biglybt.core.config.ParameterListener;
import com.biglybt.core.internat.MessageText;
import com.biglybt.pif.PluginConfig;
import com.biglybt.pif.PluginInterface;
import com.biglybt.pif.config.ConfigParameter;
import com.biglybt.pif.config.ConfigParameterListener;
import com.biglybt.pif.ui.config.*;
import com.biglybt.pifimpl.local.ui.config.ParameterImpl;


/**
 * @author Darko Matesic
 *
 * 
 */
public class AdvancedStatisticsConfig implements ConfigParameterListener {
    private PluginConfig config;
    
    public static final String KEY_PREFIX = "Plugin.advancedstatistics.";
    public static final String LABEL_PREFIX = "AdvancedStatistics.config.";
    
    public static final String TORRENTS_DISPLAY_STYLE = "torrents.display.style";
    public static final String ACTIVITY_DATA_SIZE     = "activity.data.size";
    public static final String ACTIVITY_DISPLAY_STYLE = "activity.display.style";
    public static final String ACTIVITY_DISPLAY_TYPE  = "activity.display.type";
    public static final String ACTIVITY_SCALE         = "activity.scale";
    public static final String ACTIVITY_SHOW_LIMIT    = "activity.show.limit";
    public static final String ACTIVITY_SHOW_LEGEND   = "activity.show.legend";
    
    public static final int[]    TORRENTS_DISPLAY_STYLE_VALUES =  { 0, 1, 2 };
    public static final String[] TORRENTS_DISPLAY_STYLE_LABELS = {
        MessageText.getString("AdvancedStatistics.config.torrents.display.style0"),
        MessageText.getString("AdvancedStatistics.config.torrents.display.style1"),
        MessageText.getString("AdvancedStatistics.config.torrents.display.style2")
    };
    public static final int[]    ACTIVITY_DATA_SIZE_VALUES = { 5 * 3600, 10 * 3600, 20 * 3600, 30 * 3600 };
    public static final String[] ACTIVITY_DATA_SIZE_LABLES = { "5h", "10h", "20h", "30h" };
    public static final int[]    ACTIVITY_DATA_SOURCE_VALUES =  { 0, 1 };
    public static final String[] ACTIVITY_DATA_SOURCE_LABLES = {
        MessageText.getString("AdvancedStatistics.config.activity.datasource0"),
        MessageText.getString("AdvancedStatistics.config.activity.datasource1")
    };
    public static final int[]    ACTIVITY_DISPLAY_STYLE_VALUES =  { 0, 1, 2 };
    public static final String[] ACTIVITY_DISPLAY_STYLE_LABLES = {
        MessageText.getString("AdvancedStatistics.config.activity.display.style0"),
        MessageText.getString("AdvancedStatistics.config.activity.display.style1"),
        MessageText.getString("AdvancedStatistics.config.activity.display.style2")
    };
    public static final int[]    ACTIVITY_DISPLAY_TYPE_VALUES =  { 0, 1, 2 };
    public static final String[] ACTIVITY_DISPLAY_TYPE_LABELS = {
        MessageText.getString("AdvancedStatistics.config.activity.display.type0"),
        MessageText.getString("AdvancedStatistics.config.activity.display.type1"),
        MessageText.getString("AdvancedStatistics.config.activity.display.type2")
    };
    public static final int[]    ACTIVITY_SCALE_VALUES = {   1,    2,    4,    8,    16  };
    public static final String[] ACTIVITY_SCALE_LABELS = { "x1", "x2", "x4", "x8", "x16" };
    
    public int torrentsDisplayStyle;
    public int activityDataSize;
    public int activityDisplayStyle;
    public int activityDisplayType;
    public int activityScale;
    public boolean activityShowLimit;
    public boolean activityShowLegend;
    
    Vector parameterListeners;

    public AdvancedStatisticsConfig(PluginInterface pluginInterface) {
        Log.out("AdvancedStatisticsConfig.construct");
        config = pluginInterface.getPluginconfig();

        //load config
        torrentsDisplayStyle = allowedParamValue(config.getPluginIntParameter(TORRENTS_DISPLAY_STYLE , 0), TORRENTS_DISPLAY_STYLE_VALUES , TORRENTS_DISPLAY_STYLE_VALUES[0]);
        activityDataSize     = allowedParamValue(config.getPluginIntParameter(ACTIVITY_DATA_SIZE     , 0), ACTIVITY_DATA_SIZE_VALUES     , ACTIVITY_DATA_SIZE_VALUES[0]    );
        activityDisplayStyle = allowedParamValue(config.getPluginIntParameter(ACTIVITY_DISPLAY_STYLE , 0), ACTIVITY_DISPLAY_STYLE_VALUES , ACTIVITY_DISPLAY_STYLE_VALUES[0]);
        activityDisplayType  = allowedParamValue(config.getPluginIntParameter(ACTIVITY_DISPLAY_TYPE  , 0), ACTIVITY_DISPLAY_TYPE_VALUES  , ACTIVITY_DISPLAY_TYPE_VALUES[0] );
        activityScale        = allowedParamValue(config.getPluginIntParameter(ACTIVITY_SCALE         , 1), ACTIVITY_SCALE_VALUES         , ACTIVITY_SCALE_VALUES[0]        );
        activityShowLimit    = config.getPluginBooleanParameter(ACTIVITY_SHOW_LIMIT, true);
        activityShowLegend   = config.getPluginBooleanParameter(ACTIVITY_SHOW_LEGEND, true);
         
        //initialize plugin configuration       
        PluginConfigUIFactory factory = pluginInterface.getPluginConfigUIFactory();
        Parameter parameters[] = new Parameter[7];        
        parameters[0] = factory.createIntParameter    (TORRENTS_DISPLAY_STYLE , LABEL_PREFIX + TORRENTS_DISPLAY_STYLE , 0, TORRENTS_DISPLAY_STYLE_VALUES , TORRENTS_DISPLAY_STYLE_LABELS);
        parameters[1] = factory.createIntParameter    (ACTIVITY_DATA_SIZE     , LABEL_PREFIX + ACTIVITY_DATA_SIZE     , 0, ACTIVITY_DATA_SIZE_VALUES     , ACTIVITY_DATA_SIZE_LABLES    );
        parameters[2] = factory.createIntParameter    (ACTIVITY_DISPLAY_STYLE , LABEL_PREFIX + ACTIVITY_DISPLAY_STYLE , 0, ACTIVITY_DISPLAY_STYLE_VALUES , ACTIVITY_DISPLAY_STYLE_LABLES);
        parameters[3] = factory.createIntParameter    (ACTIVITY_DISPLAY_TYPE  , LABEL_PREFIX + ACTIVITY_DISPLAY_TYPE  , 0, ACTIVITY_DISPLAY_TYPE_VALUES  , ACTIVITY_DISPLAY_TYPE_LABELS );
        parameters[4] = factory.createIntParameter    (ACTIVITY_SCALE         , LABEL_PREFIX + ACTIVITY_SCALE         , 1, ACTIVITY_SCALE_VALUES         , ACTIVITY_SCALE_LABELS        );
        parameters[5] = factory.createBooleanParameter(ACTIVITY_SHOW_LIMIT    , LABEL_PREFIX + ACTIVITY_SHOW_LIMIT    , true);
        parameters[6] = factory.createBooleanParameter(ACTIVITY_SHOW_LEGEND   , LABEL_PREFIX + ACTIVITY_SHOW_LEGEND   , true);
        
        for(int i = 0; i < parameters.length; i++) parameters[i].addConfigParameterListener(this);
        pluginInterface.addConfigUIParameters(parameters, "AdvancedStatistics.title.full");

        //BasicPluginConfigModel configModel = pluginInterface.getUIManager().createBasicPluginConfigModel("Plugins", "AdvancedStatistics.title.full");      
        //configModel.createGroup("AdvancedStatistics.title.full", parameters);

        parameterListeners = new Vector();
    }
    public void addParameterListener(ParameterListener listener) {
        parameterListeners.add(listener);
    }
    public boolean removeParameterListener(ParameterListener listener) {
        return parameterListeners.remove(listener);
    }
    
    private void informListenersParameterChanged(String parameter) {
        for(int i = 0; i < parameterListeners.size(); i++) {
            ((ParameterListener)parameterListeners.get(i)).parameterChanged(parameter);
        }
    }
    
    private int allowedParamValue(int value, int[] allowedValues, int defaultValue) {
        for(int i = 0; i < allowedValues.length; i++) 
            if(value == allowedValues[i]) return value;
        return defaultValue;
    }

    public void closedown() {
    }

    @Override
    public void configParameterChanged(ConfigParameter param) {
        String key = ((ParameterImpl)param).getKey();
        if(key.startsWith(KEY_PREFIX)) {
            //remove prefix from key name
            key = key.substring(KEY_PREFIX.length(), key.length());
            if(key.compareTo(TORRENTS_DISPLAY_STYLE) == 0) {
                torrentsDisplayStyle = allowedParamValue(config.getPluginIntParameter(TORRENTS_DISPLAY_STYLE, 0), TORRENTS_DISPLAY_STYLE_VALUES , TORRENTS_DISPLAY_STYLE_VALUES[0]);

            } else if(key.compareTo(ACTIVITY_DATA_SIZE) == 0) {
                activityDataSize = allowedParamValue(config.getPluginIntParameter(ACTIVITY_DATA_SIZE, 0), ACTIVITY_DATA_SOURCE_VALUES   , ACTIVITY_DATA_SOURCE_VALUES[0]  );
                
            } else if(key.compareTo(ACTIVITY_DISPLAY_STYLE) == 0) {
                activityDisplayStyle = allowedParamValue(config.getPluginIntParameter(ACTIVITY_DISPLAY_STYLE, 0), ACTIVITY_DISPLAY_STYLE_VALUES , ACTIVITY_DISPLAY_STYLE_VALUES[0]);

            } else if(key.compareTo(ACTIVITY_DISPLAY_TYPE) == 0) {
                activityDisplayType = allowedParamValue(config.getPluginIntParameter(ACTIVITY_DISPLAY_TYPE, 0), ACTIVITY_DISPLAY_TYPE_VALUES  , ACTIVITY_DISPLAY_TYPE_VALUES[0] );

            } else if(key.compareTo(ACTIVITY_SCALE) == 0) {
                activityScale = allowedParamValue(config.getPluginIntParameter(ACTIVITY_SCALE, 1), ACTIVITY_SCALE_VALUES         , ACTIVITY_SCALE_VALUES[0]        );

            } else if(key.compareTo(ACTIVITY_SHOW_LIMIT) == 0) {
                activityShowLimit = config.getPluginBooleanParameter(ACTIVITY_SHOW_LIMIT, true);

            } else if(key.compareTo(ACTIVITY_SHOW_LEGEND) == 0) {
                activityShowLegend = config.getPluginBooleanParameter(ACTIVITY_SHOW_LEGEND, true);
            }

            informListenersParameterChanged(key);
        }        
    }


    public static int parseInt(String value, int defaultValue) { try { return Integer.parseInt(value); } catch (Exception ex) { return defaultValue; } }
    
    public int[] getTableWidths(TableItemData[] items, String tableConfigParameter) {
        int widths[] = new int[items.length];
        String values[] = config.getPluginStringParameter(tableConfigParameter, "").split(";");
        for(int i = 0; i < items.length; i++) {
            if(i < values.length) widths[i] = parseInt(values[i], items[i].width);
            if(widths[i] <= 0) widths[i] = items[i].width;
        }
        return widths;
    }
    
    public void saveTableWidths(Table table, String tableConfigParameter) {
        TableColumn columns[] = table.getColumns();
        String values = "";
        for(int i = 0; i < columns.length; i++) values += columns[i].getWidth() + ";";
        config.setPluginParameter(tableConfigParameter, values);
    }
}
