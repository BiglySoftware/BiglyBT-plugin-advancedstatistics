/*
 * Azureus Advanced Statistics Plugin
 * 
 * Created on Friday, August 26th 2005
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

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Composite;
import com.biglybt.ui.swt.Messages;

public class TableConstructor {
    public static Table constructTable(Composite parent, int style, TableItemData[] items, int[] tableWidths) {
	    Table table = new Table(parent, style);
	    table.setLayoutData(new GridData(GridData.FILL_BOTH));
	    for(int i = 0; i < items.length; i++) {
		    TableColumn column = new TableColumn(table, items[i].style);
		    Messages.setLanguageText(column, items[i].text);
            if(tableWidths.length > i)
                column.setWidth(tableWidths[i]);
            else
                column.setWidth(items[i].width);
	    }
	    table.setHeaderVisible(true);
	    return table;
    }
}
