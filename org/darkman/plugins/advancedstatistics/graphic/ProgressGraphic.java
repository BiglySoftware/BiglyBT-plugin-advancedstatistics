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
package org.darkman.plugins.advancedstatistics.graphic;

import org.darkman.plugins.advancedstatistics.ProgressView;
import org.darkman.plugins.advancedstatistics.dataprovider.AdvancedStatisticsConfig;
import org.darkman.plugins.advancedstatistics.dataprovider.TorrentData;
import org.darkman.plugins.advancedstatistics.util.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.SWT;
import com.biglybt.core.config.COConfigurationManager;
import com.biglybt.core.config.ParameterListener;

/**
 * @author Darko Matesic
 *
 * 
 */
public class ProgressGraphic extends BackgroundGraphic implements ParameterListener {
    protected ProgressView progressView; 
    protected AdvancedStatisticsConfig config;
    private Image bufferScale;
    private Image bufferImage;
    private boolean scaleDrawn;
    private Point oldSize;    
    private int internalLoop;
    private int graphicsUpdate;    

    private Color colorLight;
    private Color colorDark;

    private int yScale[] = new int[101];
    private int scaleStep;
    
	public ProgressGraphic(Canvas canvas, ProgressView progressView, AdvancedStatisticsConfig config) {
        super(canvas);
        this.progressView = progressView;
        this.config = config;
	    this.bufferScale = null;
        colorLight = null;
        colorDark = null;
	    COConfigurationManager.addParameterListener("Graphics Update",this);
	    parameterChanged("Graphics Update");
    }

    @Override
    public void refresh() {
        if(drawCanvas == null || drawCanvas.isDisposed()) return;
        if(colorLight == null) colorLight = new Color(drawCanvas.getDisplay(), 198, 226, 255); // Colors.blues[Colors.BLUES_MIDLIGHT];
        if(colorDark  == null) colorDark =  new Color(drawCanvas.getDisplay(),   0, 128, 255); //Colors.blues[Colors.BLUES_DARKEST];         

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

	protected void drawScale(boolean sizeChanged, boolean drawScale) {
	    if(drawCanvas == null || drawCanvas.isDisposed()) return;
	    drawBackGround(sizeChanged);
	    if(sizeChanged || scaleDrawn != drawScale || bufferScale == null) {
            scaleDrawn = drawScale;
	        Rectangle bounds = drawCanvas.getClientArea();
	        if(bounds.height < 30 || bounds.width  < 100) return;

            // recalculate scale
            for(int i = 0 ; i <= 100 ; i++)
                yScale[i] = bounds.height - 30 - (bounds.height - 45) * i / 100;
            
            if(bufferScale != null && !bufferScale.isDisposed()) bufferScale.dispose();
	        bufferScale = new Image(drawCanvas.getDisplay(), bounds);
	        
	        GC gcImage = new GC(bufferScale);
            gcImage.drawImage(bufferBackground, 0, 0);
            if(drawScale) {
                scaleStep = 10;
                int height = yScale[0] - yScale[100];
                if(height < 200) scaleStep = 20;
                if(height < 120) scaleStep = 25;
                if(height < 100) scaleStep = 50;
                if(height <  60) scaleStep = 100;
                gcImage.setForeground(colorBlack);
                for(int i = 0 ; i <= 100 ; i += scaleStep) {
                    if(i == 0)
                        gcImage.drawText("" + i + "%", bounds.width - 35, yScale[i] - 12, true);
                    else
                        gcImage.drawText("" + i + "%", bounds.width - 35, yScale[i] - 8, true);                  
                }
                drawScaleGuidesAndAxis(gcImage, bounds);
            }
            gcImage.dispose();
	    }
    }

    private void drawScaleGuidesAndAxis(GC gcImage, Rectangle bounds) {
        gcImage.setLineWidth(1);
        gcImage.setLineStyle(SWT.LINE_DOT);
        gcImage.setForeground(colorGrey[2]);
        for(int i = 0 ; i <= 100 ; i += scaleStep) {
            gcImage.drawLine(1, yScale[i], bounds.width - 40, yScale[i]);
        }
        gcImage.setLineWidth(2);
        gcImage.setLineStyle(SWT.LINE_SOLID);
        gcImage.setForeground(colorBlack);
        gcImage.drawLine(bounds.width - 40, 0, bounds.width - 40, yScale[0]);    
        gcImage.drawLine(0, yScale[0], bounds.width - 40, yScale[0]);                     
    }
    
    
	protected void drawChart(boolean sizeChanged) {
	    try{
	        this_mon.enter();
            TorrentData torrentData = progressView.getSelectedTorrentData();
	        drawScale(sizeChanged, (torrentData != null));
	        Rectangle bounds = drawCanvas.getClientArea();    
            
	        //If bufferedImage is not null, dispose it
	        if(bufferImage != null && !bufferImage.isDisposed()) bufferImage.dispose();
	        bufferImage = new Image(drawCanvas.getDisplay(), bounds);
	        GC gcImage = new GC(bufferImage);          
	        gcImage.drawImage(bufferScale, 0, 0);
            
            int i1 = 0, i2 = 1000;
            if(torrentData != null) {
                long timeCompleted[] = torrentData.timeCompleted;

                // calculate interval
                for(int i = 0; i <= 1000; i++) if(timeCompleted[i] != 0) { i1 = i; break; }
                for(int i = 1000; i >= i1; i--) if(timeCompleted[i] != 0) { i2 = i; break; }
                long time_start = timeCompleted[i1];
                float scale = (float)(bounds.width - 70) / (float)(timeCompleted[i2] - timeCompleted[i1]);

                int index1 = -1, index2 = -1, x1 = -1, y1 = -1, x2 = -1, y2 = -1;
                int x_text = -1; // last position of text
                for(int index = i1; index <= i2; index++) {
                    if(timeCompleted[index] > 0) {
                        index1 = index2; x1 = x2; y1 = y2;
                        index2 = index;
                        x2 = (int)(30 + (float)(timeCompleted[index] - time_start) * scale);
                        y2 = yScale[index / 10];
                        if(x1 >= 0 && x2 >= 0) {
                            drawLines(gcImage, x1, y1, x2, y2, bounds);
                            if(x_text == -1 || (x1 - x_text) > 80 && x1 < (bounds.width - 120)) {
                                x_text = x1;
                                drawTimeDateText(gcImage, x_text, timeCompleted[index1]);
                            }
                        }
                    }
                }
                if(x1 >= 0 && x2 >= 0 && index2 > 0) drawTimeDateText(gcImage, x2, timeCompleted[index2]);
                drawScaleGuidesAndAxis(gcImage, bounds);
            }
	        gcImage.dispose();
        } catch (Exception ex) {
            Log.out("ProgressGraphic: error in drawchart " + ex.getMessage());            
            Log.outStackTrace(ex);
	    } finally {
	        this_mon.exit();
	    }
    }
    private void drawLines(GC gcImage, int x1, int y1, int x2, int y2, Rectangle bounds) {
        switch(config.torrentsDisplayStyle) {
            case 0:
                gcImage.setLineWidth(2);
                gcImage.setLineStyle(SWT.LINE_SOLID);
                gcImage.setForeground(colorRed);
                gcImage.drawLine(x1, y1, x2, y2);
                break;
            case 1:
                gcImage.setLineWidth(1);
                gcImage.setLineStyle(SWT.LINE_SOLID);
                gcImage.setForeground(colorBlue);
                for(int x = x1; x < x2; x++) {
                    int y = y1 + (y2-y1) * (x-x1) / (x2-x1);
                    gcImage.drawLine(x, y, x, bounds.height - 30);
                }
                break;
            case 2:
                gcImage.setForeground(colorBlue);
                gcImage.setBackground(colorLightBlue);
                for(int x = x1; x < x2; x++) {
                    int y = y1 + (y2-y1) * (x-x1) / (x2-x1);
                    gcImage.setClipping(x, y, 1, bounds.height - 30 - y);
                    gcImage.fillGradientRectangle(x, 0, 1, bounds.height - 30, true);
                }
                gcImage.setClipping(0, 0, bounds.width, bounds.height);                    
                break;
        }
    }
    
	private void drawTimeDateText(GC gcImage, int x, long time) {
        gcImage.setLineWidth(1);
        gcImage.setLineStyle(SWT.LINE_DOT);
        gcImage.setForeground(colorGrey[2]);
        gcImage.drawLine(x, 0, x, yScale[0]);
        gcImage.setForeground(colorBlack);
        gcImage.drawText(DateTime.getDateString(time), x - 25, yScale[0] +  2, true);                        
        gcImage.drawText(DateTime.getTimeString(time), x - 20, yScale[0] + 15, true);        
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
