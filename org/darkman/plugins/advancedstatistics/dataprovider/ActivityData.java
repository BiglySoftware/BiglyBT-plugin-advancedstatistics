/*
 * Azureus Advanced Statistics Plugin
 * 
 * Created on Saturday, October 15th 2005
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

import com.biglybt.core.util.AEMonitor;
import org.darkman.plugins.advancedstatistics.util.Log;

/**
 * @author Darko Matesic
 *
 * 
 */
public class ActivityData {
    public static final int[] ALLOWED_SCALES =  { 1, 2, 4, 8, 16 };
    public int MAX_SAMPLES;

    public static boolean scaleAllowed(int scale) {
        boolean scaleAllowed = false;
        for(int i = 0; i < ALLOWED_SCALES.length; i++) 
            if(scale == ALLOWED_SCALES[i]) { scaleAllowed = true; break; }
        return scaleAllowed;
    }
    public static int scaleToIndex(int scale) {
        for(int i = 0; i < ALLOWED_SCALES.length; i++) 
            if(scale == ALLOWED_SCALES[i]) return i;
        return 0;
    }
    public static int indexToScale(int index) {
        if(index >= 0 && index < ALLOWED_SCALES.length) return ALLOWED_SCALES[index]; 
        return ALLOWED_SCALES[0];
    }
    
    protected AEMonitor this_mon = new AEMonitor("ActivityData");

    private boolean fullActivityData;
    
    private int[] x1_data;
    private int[] x1_prot;  // full activity data
    private int[] x1_limit; // full activity data
    private int[] x1_max;   // full activity data
    private int[] x1_time;

    public int[] data;
    public int[] prot;  // full activity data
    public int[] limit; // full activity data
    public int[] max;   // full activity data
    public int[] time;
    
    private int x1_position;
    public int position;
    public int samples;
    public int scale;
    
    public ActivityData(boolean fullActivityData, int activityDataSize) {
        this.fullActivityData = fullActivityData;
        MAX_SAMPLES = activityDataSize;
        
        this.x1_data = new int[MAX_SAMPLES];
        if(fullActivityData) {
            this.x1_prot = new int[MAX_SAMPLES];
            this.x1_limit = new int[MAX_SAMPLES];
            this.x1_max = new int[MAX_SAMPLES];
        }
        this.x1_time = new int[MAX_SAMPLES];            

        this.data = new int[MAX_SAMPLES];
        if(fullActivityData) {
            this.prot = new int[MAX_SAMPLES];
            this.limit = new int[MAX_SAMPLES];
            this.max = new int[MAX_SAMPLES];
        }
        this.time = new int[MAX_SAMPLES];

        for(int i = 0; i < MAX_SAMPLES; i++) {
            this.x1_time[i] = -1;
            this.time[i] = -1;
        }

        x1_position = -1;
        position = -1;
        samples = MAX_SAMPLES;
        scale = 1;
    }

    public void enter() { this_mon.enter(); }
    public void exit()  { this_mon.exit();  }
    
    public void add(int data, int prot, int limit, int time) {
        try{
            this_mon.enter();
            //fill scale1 data
            x1_position++;
            if(x1_position >= MAX_SAMPLES) x1_position = 0;
            this.x1_data[x1_position] = data;
            if(fullActivityData) {
                this.x1_prot[x1_position] = prot;
                this.x1_limit[x1_position] = limit;
                if(limit < (data + prot))
                    this.x1_max[x1_position] = data + prot;
                else
                    this.x1_max[x1_position] = limit;
            }
            this.x1_time[x1_position] = time;
            //calculate scaled data
            position = x1_position / scale;
            calculateAverage(position);
        } finally {
            this_mon.exit();
        }
    }

    public void setScale(int scale) {
        try{
            this_mon.enter();
            //accept only allowed scales
            if(this.scale != scale) {
                if(scaleAllowed(scale)) {
                    this.scale = scale;
                    samples = MAX_SAMPLES / scale;
                    position = x1_position / scale;
                    //calculate average values
                    for(int i = 0; i < samples; i++) calculateAverage(i);                    
                }
            }
        } finally {
            this_mon.exit();
        }
    }
    
    private void calculateAverage(int sample) {
        int x1_sample = sample * scale;
        int sum_data = 0;
        int sum_prot = 0;
        if(fullActivityData) {
            for(int i = 0; i < scale; i++) {
                sum_data += x1_data[x1_sample + i];
                sum_prot += x1_prot[x1_sample + i];
            }
        } else {
            for(int i = 0; i < scale; i++)
                sum_data += x1_data[x1_sample + i];
        }
        data[sample] = sum_data / scale;
        if(fullActivityData) {
            prot[sample] = sum_prot / scale;
            limit[sample] = x1_limit[x1_sample];
            if(limit[sample] < (data[sample] + prot[sample]))
                max[sample] = data[sample] + prot[sample];
            else
                max[sample] = limit[sample];        
        }
        time[sample] = x1_time[x1_sample];
    }
    
    //returns max value starting with last position
    public int getMax(int sampleOffset, int samples, boolean includeLimit) {
        int max_value = 0;
        try {
            this_mon.enter();
            if(samples > this.samples) samples = this.samples;
            int index = position % this.samples - sampleOffset;
            while(index < 0) index += this.samples;
            if(fullActivityData) {
                if(includeLimit) {
                    for(int i = 0; i < samples; i++) {
                        if(max[index] > max_value) max_value = max[index];
                        index--;
                        if(index < 0) index += this.samples;
                    }                    
                } else {
                    int value = 0;
                    for(int i = 0; i < samples; i++) {
                        value = data[index] + prot[index];
                        if(value > max_value) max_value = value;
                        index--;
                        if(index < 0) index += this.samples;
                    }
                }
            } else {
                for(int i = 0; i < samples; i++) {
                    if(data[index] > max_value) max_value = data[index];
                    index--;
                    if(index < 0) index += this.samples;
                }
            }
        } catch(Exception ex) {
            Log.out("Error in getMax: " + ex.getMessage());
            Log.outStackTrace(ex);
        } finally {
            this_mon.exit();
        }
        return max_value;
    }
    
    public void closedown() {
        this.x1_data = null;
        this.x1_prot = null;
        this.x1_limit = null;
        this.x1_max = null;
        this.x1_time = null;

        this.data = null;
        this.prot = null;
        this.limit = null;
        this.max = null;
        this.time = null;
    }    
}