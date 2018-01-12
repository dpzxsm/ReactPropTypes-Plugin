package com.suming.plugin.ui;

import com.suming.plugin.bean.PropTypeBean;

import javax.swing.table.DefaultTableModel;
import java.util.List;

class PropTypesModel extends DefaultTableModel{

    void addRow(PropTypeBean bean) {
        super.addRow(new Object[]{bean.name,bean.type,bean.isRequired,
                bean.getDefaultValue() ,bean.getDescribe()});
    }

    void initData(List<PropTypeBean> beans){
        String[] columnNames = {
                "name",
                "type",
                "isRequired",
                "defaultValue",
                "ops"};
        Object[][] data = new Object[beans.size()][5];
        for (int i = 0; i < beans.size(); i++) {
            data[i][0] = beans.get(i).name;
            data[i][1] = beans.get(i).type;
            data[i][2] = beans.get(i).isRequired;
            data[i][3] = beans.get(i).getDefaultValue();
            data[i][4] = beans.get(i).getDescribe();
        }
        this.setDataVector(data,columnNames);
    }
}
