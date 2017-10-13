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
package org.darkman.plugins.advancedstatistics.graphic;

import org.darkman.plugins.advancedstatistics.dataprovider.*;
import org.darkman.plugins.advancedstatistics.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import com.biglybt.core.config.COConfigurationManager;
import com.biglybt.core.config.ParameterListener;
import com.biglybt.core.util.DisplayFormatters;
import com.biglybt.ui.swt.components.graphics.Scale;

/**
 * @author Darko Matesic
 *
 * 
 */
public class ActivityGraphic extends BackgroundGraphic implements ParameterListener {
    protected AdvancedStatisticsConfig config;
    protected ActivityData activityData; 
    protected TorrentActivityData torrentActivityData;
    private Image bufferScale;
    private Image bufferImage;
    private Image bufferLegend;
    private Point oldSize;
    private int internalLoop;
    private int graphicsUpdate;    
    private Scale speedScale;
    private int[] scaleLevels;
    
    private int sampleOffset;

    private Color[] colorLight;
    private Color[] colorDark;
    private int px, py;
    private int legendWidth, legendHeight, legendType;
    
    private int chartSamples;

    public ActivityGraphic(Canvas canvas, AdvancedStatisticsConfig config, ActivityData activityData, TorrentActivityData torrentActivityData) {
        super(canvas);
        this.config = config;
        this.activityData = activityData;
        this.torrentActivityData = torrentActivityData;
        this.bufferScale = null;
        speedScale = new Scale();
        scaleLevels = null;
        colorLight = null;
        colorDark = null;        
        COConfigurationManager.addAndFireParameterListener("Graphics Update", this);
        chartSamples = 0;

        colorLight = new Color[] {
            new Color(drawCanvas.getDisplay(), 100, 100, 255), // light blue
            new Color(drawCanvas.getDisplay(), 100, 255, 100), // light green
            new Color(drawCanvas.getDisplay(), 255, 255, 100), // light yellow
            new Color(drawCanvas.getDisplay(), 100, 255, 255), // light cyan
            new Color(drawCanvas.getDisplay(), 255, 100, 255), // light purple
            new Color(drawCanvas.getDisplay(), 234, 157, 100), // light orange
            new Color(drawCanvas.getDisplay(), 180, 180, 180), // light gray
            new Color(drawCanvas.getDisplay(), 255, 100, 100)  // light red
        };            
        colorDark =  new Color[] {
            new Color(drawCanvas.getDisplay(),   0,   0, 255), // dark blue
            new Color(drawCanvas.getDisplay(),   0, 255,   0), // dark green
            new Color(drawCanvas.getDisplay(), 255, 255,   0), // dark yellow
            new Color(drawCanvas.getDisplay(),   0, 255, 255), // dark cyan
            new Color(drawCanvas.getDisplay(), 255,   0, 255), // dark purple
            new Color(drawCanvas.getDisplay(), 228, 122,  44), // dark orange
            new Color(drawCanvas.getDisplay(), 150, 150, 150), // dark gray
            new Color(drawCanvas.getDisplay(), 255,   0,   0)  // dark red
        };
    }
    
    public int getSliderMax() { return activityData.samples; }
    public int getSliderThumb() { 
        if(chartSamples <= 0 || chartSamples >= activityData.samples) return activityData.samples;  
        return chartSamples;
    }
    
    public void updateScale() {
        Rectangle bounds = drawCanvas.getClientArea();
        activityData.setScale(config.activityScale);
        chartSamples = (bounds.width - 70);
        if(chartSamples > activityData.samples) chartSamples = activityData.samples; 
    }

    public void refresh(int sampleOffset) {
        this.sampleOffset = sampleOffset;

        Rectangle bounds = drawCanvas.getClientArea();
        if(bounds.height < 30 || bounds.width  < 100 || bounds.width > 2000 || bounds.height > 2000) return;
        
        boolean sizeChanged = (oldSize == null || oldSize.x != bounds.width || oldSize.y != bounds.height);
        oldSize = new Point(bounds.width, bounds.height);
        
        internalLoop++;
        if(internalLoop > graphicsUpdate) internalLoop = 0;
        if(internalLoop == 0 || sizeChanged) drawChart(sizeChanged);
        
        GC gc = new GC(drawCanvas);
        gc.drawImage(bufferImage, bounds.x, bounds.y);
        gc.dispose();    
    }

    protected void drawScale(boolean sizeChanged, int max) {
        if(drawCanvas == null || drawCanvas.isDisposed()) return;
        drawBackGround(sizeChanged);
        if(sizeChanged || speedScale.getMax() != max || bufferScale == null) {
            Rectangle bounds = drawCanvas.getClientArea();
            if(bounds.height < 30 || bounds.width  < 100) return;
            speedScale.setMax(max);
            speedScale.setNbPixels(bounds.height - 30);

            if(bufferScale != null && !bufferScale.isDisposed()) bufferScale.dispose();
            bufferScale = new Image(drawCanvas.getDisplay(), bounds);
            
            GC gcImage = new GC(bufferScale);
            gcImage.drawImage(bufferBackground, 0, 0);

            gcImage.setForeground(colorBlack);
            scaleLevels = speedScale.getScaleValues();
            for(int i = 0 ; i < scaleLevels.length ; i++) {
                int height = bounds.height - speedScale.getScaledValue(scaleLevels[i]) - 15 ;
                if(scaleLevels[i] == 0)
                    gcImage.drawString(DisplayFormatters.formatByteCountToBase10KBEtcPerSec(scaleLevels[i]), bounds.width - 65, height - 12, true);
                else
                    gcImage.drawString(DisplayFormatters.formatByteCountToBase10KBEtcPerSec(scaleLevels[i]), bounds.width - 65, height - 8, true);
            }
            drawScaleGuidesAndAxis(gcImage, bounds);
            gcImage.dispose();
        }
    }
    
    private void drawScaleGuidesAndAxis(GC gcImage, Rectangle bounds) {
        if(scaleLevels != null) {
            gcImage.setLineWidth(1);
            gcImage.setLineStyle(SWT.LINE_DOT);
            gcImage.setForeground(colorGrey[2]);
            for(int i = 0 ; i < scaleLevels.length ; i++) {
                int height = bounds.height - speedScale.getScaledValue(scaleLevels[i]) - 15 ;
                gcImage.drawLine(1, height, bounds.width - 70 , height);
            }
        }
        gcImage.setLineWidth(2);
        gcImage.setLineStyle(SWT.LINE_SOLID);
        gcImage.setForeground(colorBlack);
        gcImage.drawLine(bounds.width - 70, 0, bounds.width - 70, bounds.height - 15);
        gcImage.drawLine(0, bounds.height - 15, bounds.width - 70, bounds.height - 15);
    }

    static final int legendOffset = 5;
    static final int legendItemHeight = 15;

    private void drawLegend() {
        if(drawCanvas == null || drawCanvas.isDisposed()) return;
        int width = 0, height = 0;
        switch(config.activityDisplayType) {
            case 0:
                width = 120;
                height = legendOffset * 2 + legendItemHeight;
                break;
            case 1:
                width = 80;
                height = legendOffset * 2 + legendItemHeight * 2;
                break;
            case 2:
                if(torrentActivityData.torrentName.length > 0) {
                    width = legendOffset * 4 + legendItemHeight + torrentActivityData.maxTorrentNameWidth ;
                    height = legendOffset * 2 + (torrentActivityData.torrentName.length + 1) * legendItemHeight; 
                } else {
                    width = 80;
                    height = legendOffset * 2 + legendItemHeight;                    
                }
                break;
        }
        if(torrentActivityData.changed || width != legendWidth || height != legendHeight || config.activityDisplayType != legendType || bufferLegend == null) {
            Rectangle bounds = drawCanvas.getClientArea();
            if(bounds.height < 30 || bounds.width  < 100) return;
            legendWidth = width;
            legendHeight = height;
            legendType = config.activityDisplayType;
            torrentActivityData.changed = false;

            if(bufferLegend != null && !bufferLegend.isDisposed()) bufferLegend.dispose();
            bufferLegend = new Image(drawCanvas.getDisplay(), legendWidth, legendHeight);
            
            GC gcImage = new GC(bufferLegend);
            gcImage.setBackground(colorWhite);
            gcImage.fillRectangle(0, 0, legendWidth, legendHeight);
            gcImage.setLineWidth(1);
            gcImage.setLineStyle(SWT.LINE_SOLID);
            gcImage.setForeground(colorBlack);
            gcImage.drawRectangle(0, 0, legendWidth - 1, legendHeight - 1);

            switch(config.activityDisplayType) {
                case 0:
                    drawLegentItem(gcImage, legendOffset, colorBlue, "data + protocol");
                    break;
                case 1:
                    drawLegentItem(gcImage, legendOffset, colorBlue, "data");
                    drawLegentItem(gcImage, legendOffset + legendItemHeight, colorRed, "protocol");
                    break;
                case 2:
                    if(torrentActivityData.torrentName.length > 0) {
                        for(int i = 0; i < torrentActivityData.torrentName.length; i++) {
                            int color = i % (colorDark.length - 1);
                            drawLegentItem(gcImage, legendOffset + i * legendItemHeight, colorDark[color], torrentActivityData.torrentName[i]);
                        }
                        drawLegentItem(gcImage, legendOffset + torrentActivityData.torrentName.length * legendItemHeight, colorDark[colorDark.length - 1], "protocol");
                    } else {
                        gcImage.setForeground(colorBlack);
                        gcImage.drawString("no torrents", legendOffset, legendOffset);
                    }
                    break;
            }
            gcImage.dispose();
        }
    }
    
    private void drawLegentItem(GC gcImage, int y, Color color, String text) {
        int rx = legendOffset;
        int ry = y + 1;
        int rw = legendItemHeight + legendOffset;
        int rh = legendItemHeight - 1;
        gcImage.setBackground(color);
        gcImage.fillRectangle(rx, ry, rw, rh);
        gcImage.setLineWidth(1);
        gcImage.setLineStyle(SWT.LINE_SOLID);
        gcImage.setForeground(colorBlack);
        gcImage.drawRectangle(rx, ry, rw - 1, rh - 1);
        gcImage.drawString(text, rx + rw + legendOffset, y, true);
    }
    
    private void drawChart(boolean sizeChanged) {
        try{
            this_mon.enter();
            activityData.enter();
            torrentActivityData.enter();
            
            Rectangle bounds = drawCanvas.getClientArea();
            activityData.setScale(config.activityScale);
            if(config.activityDisplayType == 2) torrentActivityData.setScale(config.activityScale);
            chartSamples = (bounds.width - 70);
            if(chartSamples > activityData.samples) chartSamples = activityData.samples; 
            drawScale(sizeChanged, activityData.getMax(sampleOffset, chartSamples, config.activityShowLimit));
            int height = bounds.height - 15;

            //If bufferedImage is not null, dispose it
            if(bufferImage != null && !bufferImage.isDisposed()) bufferImage.dispose();
            bufferImage = new Image(drawCanvas.getDisplay(), bounds);
            GC gcImage = new GC(bufferImage);
            gcImage.drawImage(bufferScale, 0, 0);
                
            int x_text = -1;
            int prevX = -1, prevLimit = -1;
            int pos, x, value, h, h1, h2, color, tpos;
            px = -1; py = -1;
            for(int i = 0; i < chartSamples; i++) {
                pos =  activityData.position - sampleOffset - i;
                while(pos < 0) pos += activityData.samples;
                if(activityData.time[pos] == 0) break;
                x = bounds.width - 70 - i;
                gcImage.setLineWidth(1);
                gcImage.setLineStyle(SWT.LINE_SOLID);
                switch(config.activityDisplayType) {
                    case 0:
                        value = activityData.data[pos] + activityData.prot[pos];
                        h = speedScale.getScaledValue(value);
                        drawLine(gcImage, x, height, height - h, bounds, colorLightBlue, colorBlue);
                        break;
                    case 1:
                        h1 = speedScale.getScaledValue(activityData.data[pos]);
                        h2 = speedScale.getScaledValue(activityData.prot[pos]);
                        drawLine(gcImage, x, height, height - h1, bounds, colorLightBlue, colorBlue);
                        drawLine(gcImage, x, height - h1, height - h1 - h2, bounds, colorLightRed, colorRed);                        
                        break;
                    case 2:
                        if(torrentActivityData.activityData.length > 0) {
                            h1 = 0;
                            //draw torrents
                            for(int j = 0; j < torrentActivityData.activityData.length; j++) {
                                color = j % (colorLight.length - 1);
                                tpos = torrentActivityData.activityData[j].position - sampleOffset - i;
                                while(tpos < 0) tpos += torrentActivityData.activityData[j].samples;
                                h2 = h1 + torrentActivityData.activityData[j].data[tpos];
                                if(h2 > h1) drawLine(gcImage, x, height - speedScale.getScaledValue(h1), height - speedScale.getScaledValue(h2), bounds, colorLight[color], colorDark[color]);                        
                                h1 = h2;
                            }
                            // draw protocol
                            color = colorLight.length - 1;
                            tpos = activityData.position - sampleOffset - i;
                            while(tpos < 0) tpos += activityData.samples;
                            h2 = h1 + activityData.prot[tpos];
                            if(h2 > h1) drawLine(gcImage, x, height - speedScale.getScaledValue(h1), height - speedScale.getScaledValue(h2), bounds, colorLight[color], colorDark[color]);
                        }
                        break;
                }
                if(x_text == -1 || (x_text - x) > 80 && x > 60) {
                    x_text = x;
                    drawTimeText(gcImage, x_text, height, activityData.time[pos]);
                }
                if(config.activityShowLimit) {
                    int limit = activityData.limit[pos];                
                    if(prevLimit > 0 && limit > 0) { 
                        gcImage.setLineWidth(2);
                        gcImage.setLineStyle(SWT.LINE_SOLID);
                        gcImage.setForeground(colorBlack);
                        gcImage.drawLine(prevX, height - speedScale.getScaledValue(prevLimit), x, height - speedScale.getScaledValue(limit));
                    }
                    prevX = x;
                    prevLimit = limit;
                }
            }
            drawScaleGuidesAndAxis(gcImage, bounds);
            if(config.activityShowLegend) {
                drawLegend();
                gcImage.drawImage(bufferLegend, 10, 10);                                
            }

/*            
            gcImage.setForeground(colorBlack);
            gcImage.setBackground(colorWhite);
            gcImage.drawString("total samples: " + activityData.samples, 400, 10);
            gcImage.drawString("chart samples: " + chartSamples, 400, 25);
            gcImage.drawString("curr position: " + activityData.position, 400, 40);
            gcImage.drawString("sample offset: " + sampleOffset, 400, 55);

            gcImage.drawString("slider max: " + getSliderMax(), 400, 100);
            gcImage.drawString("slider thumb: " + getSliderThumb(), 400, 115);
*/

            gcImage.dispose();
        } catch (Exception ex) {
            Log.out("ActivityGraphic: error in drawchart " + ex.getMessage());
            Log.outStackTrace(ex);
        } finally {
            this_mon.exit();
            activityData.exit();
            torrentActivityData.exit();
        }
    }

    private void drawLine(GC gcImage, int x, int y1, int y2, Rectangle bounds, Color colorLight, Color colorDark) {
        switch(config.activityDisplayStyle) {
            case 0:
                if(px >= 0 && py >= 0) {
                    gcImage.setLineWidth(2);
                    gcImage.setLineStyle(SWT.LINE_SOLID);
                    gcImage.setForeground(colorDark);
                    gcImage.drawLine(px, py, x, y2);
                }
                px = x; py = y2;
                break;
            case 1:
                if((y1 - y2) > 0) {
                    gcImage.setLineWidth(1);
                    gcImage.setLineStyle(SWT.LINE_SOLID);
                    gcImage.setForeground(colorDark);
                    gcImage.drawLine(x, y2, x, y1);
                }
                break;
            case 2:
                if((y1 - y2) > 0) {
                    gcImage.setForeground(colorDark);
                    gcImage.setBackground(colorLight);
                    gcImage.setClipping(x, y2, 1, y1 - y2);
                    gcImage.fillGradientRectangle(x, 0, 1, bounds.height - 15, true);
                    gcImage.setClipping(0, 0, bounds.width, bounds.height);
                }
                break;
        }
    }
    
    private void drawTimeText(GC gcImage, int x, int height, long time) {
        if(time >= 0) {
            gcImage.setLineWidth(1);
            gcImage.setLineStyle(SWT.LINE_DOT);
            gcImage.setForeground(colorGrey[2]);
            gcImage.drawLine(x, 0, x, height);                                                    
            gcImage.setForeground(colorBlack);
            gcImage.drawString(DateTime.getElapsedTimeString(time, false), x - 20, height, true);
        }
    }

    @Override
    public void parameterChanged(String parameter) {
        graphicsUpdate = COConfigurationManager.getIntParameter("Graphics Update");
    }
      
    @Override
    public void dispose() {
        super.dispose();
        if(bufferImage != null && !bufferImage.isDisposed()) bufferImage.dispose();
        COConfigurationManager.removeParameterListener("Graphics Update",this);
    }
}
