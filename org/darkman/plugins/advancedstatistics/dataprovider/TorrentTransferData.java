/*
 * Azureus Advanced Statistics Plugin
 * 
 * Created on Thursday, September 21st 2005
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

/**
 * @author Darko Matesic
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TorrentTransferData {
    public long TRANSFER_DATE;
    public long COMPLETED_TO_DATE;
    public long RECEIVED_TO_DATE;
    public long DISCARDED_TO_DATE;
    public long SENT_TO_DATE;
    public long RECEIVED;
    public long DISCARDED;
    public long SENT;
    public long UP_TIME;

    public TorrentTransferData() {
    }
    
    public void set(TorrentTransferData data) {
        this.TRANSFER_DATE     = data.TRANSFER_DATE;
        this.COMPLETED_TO_DATE = data.COMPLETED_TO_DATE;
        this.RECEIVED_TO_DATE  = data.RECEIVED_TO_DATE;
        this.DISCARDED_TO_DATE = data.DISCARDED_TO_DATE;
        this.SENT_TO_DATE      = data.SENT_TO_DATE;
        this.RECEIVED          = data.RECEIVED;
        this.DISCARDED         = data.DISCARDED;
        this.SENT              = data.SENT;
        this.UP_TIME           = data.UP_TIME;
    }

    public void closedown() {
    }
}
