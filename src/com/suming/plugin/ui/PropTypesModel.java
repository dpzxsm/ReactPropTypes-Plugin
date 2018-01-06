package com.suming.plugin.ui;

import com.suming.plugin.bean.PropTypeBean;

import javax.swing.table.DefaultTableModel;

class PropTypesModel extends DefaultTableModel{

    void addRow(PropTypeBean bean) {
        super.addRow(new Object[]{bean.name,bean.type,bean.isRequired,bean.describe,true});
    }
}
