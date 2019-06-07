/*
 * Azureus Advanced Statistics Plugin
 * 
 * Created on Wednesday, September 22nd 2005
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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import com.biglybt.core.util.AEMonitor;

/**
 * @author Darko Matesic
 *
 * 
 */
public class BackgroundGraphic {
    protected Canvas drawCanvas;    
    protected Image bufferBackground;
    protected AEMonitor this_mon = new AEMonitor("BackgroundGraphic");
    protected Color colorGrey[];
    protected Color colorBlack;
    protected Color colorWhite;
    protected Color colorRed, colorLightRed;
    protected Color colorBlue, colorLightBlue;

    public BackgroundGraphic(Canvas canvas) {
        this.drawCanvas = canvas;
        colorGrey = new Color[] {
                new Color(canvas.getDisplay(), 250, 250, 250),
                new Color(canvas.getDisplay(), 233, 233, 233),
                new Color(canvas.getDisplay(), 170, 170, 170)
        };
        colorBlack      = new Color(canvas.getDisplay(),   0,   0,   0);
        colorWhite      = new Color(canvas.getDisplay(), 255, 255, 255);
        colorRed        = new Color(canvas.getDisplay(), 255,   0,   0);
        colorLightRed   = new Color(canvas.getDisplay(), 255, 200, 200);
        colorBlue       = new Color(canvas.getDisplay(),   0, 128, 255);
        colorLightBlue  = new Color(canvas.getDisplay(), 200, 225, 250);
    }
  
    public void refresh( boolean force ) {    
    }
  
    protected void drawBackGround(boolean sizeChanged) {    
        if(drawCanvas == null || drawCanvas.isDisposed()) return;
    
        if(sizeChanged || bufferBackground == null) {             
            Rectangle bounds = drawCanvas.getClientArea();
            if(bounds.height < 30 || bounds.width < 100) return; 
            
            if(bufferBackground != null && !bufferBackground.isDisposed()) bufferBackground.dispose();
      
            if(bounds.width > 2000 || bounds.height > 2000) return;
      
            bufferBackground = new Image(drawCanvas.getDisplay(), bounds);
      
            Color colors[] = new Color[4];
            colors[0] = colorWhite;
            colors[1] = colorGrey[0];
            colors[2] = colorGrey[1];
            colors[3] = colorGrey[0];
            GC gcBuffer = new GC(bufferBackground);
            for(int i = 0 ; i < bounds.height - 2 ; i++) {
                gcBuffer.setForeground(colors[i % 4]);
                gcBuffer.drawLine(1, i + 1, bounds.width - 1, i + 1);
            }             
            gcBuffer.setForeground(colorBlack);
            gcBuffer.drawRectangle(0, 0, bounds.width - 1, bounds.height - 1);
            gcBuffer.dispose();
        }
    }
  
    public void dispose() {
        if(bufferBackground != null && !bufferBackground.isDisposed()) bufferBackground.dispose();
        if(colorGrey[0]   != null && !colorGrey[0].isDisposed()  ) colorGrey[0].dispose();
        if(colorGrey[1]   != null && !colorGrey[1].isDisposed()  ) colorGrey[1].dispose();
        if(colorGrey[2]   != null && !colorGrey[2].isDisposed()  ) colorGrey[2].dispose();
        if(colorBlack     != null && !colorBlack.isDisposed()    ) colorBlack.dispose();
        if(colorWhite     != null && !colorWhite.isDisposed()    ) colorWhite.dispose();
        if(colorRed       != null && !colorRed.isDisposed()      ) colorRed.dispose();
        if(colorLightRed  != null && !colorLightRed.isDisposed() ) colorLightRed.dispose();
        if(colorBlue      != null && !colorBlue.isDisposed()     ) colorBlue.dispose();
        if(colorLightBlue != null && !colorLightBlue.isDisposed()) colorLightBlue.dispose();
    }
}
