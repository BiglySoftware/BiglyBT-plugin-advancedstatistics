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
package org.darkman.plugins.advancedstatistics.util;

import java.text.DecimalFormat;
import com.biglybt.core.util.DisplayFormatters;
import com.biglybt.core.util.Constants;

/**
 * @author Darko Matesic
 *
 * 
 */
public class TransferFormatter {
    private final static DecimalFormat percentDecimalFormat = new DecimalFormat("##.#");
    private final static DecimalFormat ratioDecimalFormat = new DecimalFormat("#.####");

    public static String formatPercent(float percent) {
        return percentDecimalFormat.format(percent) + "%";
    }
    public static String formatPercent(long partial, long total) {
        if(partial == 0) return "0%";
        if(total == 0) return Constants.INFINITY_STRING;
        return percentDecimalFormat.format((float)partial * 100.0 / (float)total) + "%";
    }
    public static String formatTransfered(long transfered) {
        return DisplayFormatters.formatByteCountToKiBEtc(transfered);
    }
    public static String formatTransfered(long transfered, long total) {
        if(transfered == 0 || total == 0) return DisplayFormatters.formatByteCountToKiBEtc(transfered);
        return DisplayFormatters.formatByteCountToKiBEtc(transfered) + " (" + 
                percentDecimalFormat.format((float)transfered * 100.0 / (float)total) + "%)";
    }
    public static String formatRatio(long sent, long received) {
        if(sent == 0) return "0";
        if(received == 0) return Constants.INFINITY_STRING;        
        return ratioDecimalFormat.format((float)sent / (float)received);
    }
}
