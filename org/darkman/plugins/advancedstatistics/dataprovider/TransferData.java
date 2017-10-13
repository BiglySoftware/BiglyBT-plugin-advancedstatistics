/*
 * Azureus Advanced Statistics Plugin
 * 
 * Created on Thursday, September 11th 2005
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
public class TransferData {
    public long TRANSFER_DATE;
    public long RECEIVED;
    public long DISCARDED;
    public long SENT;
    public long PROT_RECEIVED;
    public long PROT_SENT;
    public long UP_TIME;

    public TransferData() {
    }
    
    public void set(TransferData data) {
        this.TRANSFER_DATE = data.TRANSFER_DATE;
        this.RECEIVED      = data.RECEIVED;
        this.DISCARDED     = data.DISCARDED;
        this.SENT          = data.SENT;
        this.PROT_RECEIVED = data.PROT_RECEIVED;
        this.PROT_SENT     = data.PROT_SENT;
        this.UP_TIME       = data.UP_TIME;
    }

    public void closedown() {
    }
}
