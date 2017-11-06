package com.suming.plugin.ui;

import javax.swing.table.DefaultTableModel;

public class PropTypesModel extends DefaultTableModel{
    @Override
    public boolean isCellEditable(int row, int column) {
        return column != 0 && super.isCellEditable(row, column);
    }
}
