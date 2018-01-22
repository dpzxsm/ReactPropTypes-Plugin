package com.suming.plugin.ui;

import com.suming.plugin.bean.BasePropType;
import com.suming.plugin.bean.PropTypeBean;
import com.suming.plugin.utils.PropTypesHelper;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class ShapePropTypesModel extends DefaultTableModel  {
  void addRow(BasePropType bean) {
    super.addRow(new Object[]{bean.name,bean.type,bean.isRequired});
  }

  void initData(List<BasePropType> beans){
    String[] columnNames = {
            "name",
            "type",
            "isRequired",
            "ops"};
    Object[][] data = new Object[beans.size()][4];
    for (int i = 0; i < beans.size(); i++) {
      data[i][0] = beans.get(i).name;
      data[i][1] = beans.get(i).type;
      data[i][2] = beans.get(i).isRequired;
      data[i][3] = false;
    }
    this.setDataVector(data,columnNames);
  }

  void reInitData(List<BasePropType> beans){
    for (int i = 0; i < this.getRowCount(); i++) {
      this.removeRow(i);
    }
    for (BasePropType bean : beans) {
      this.addRow(bean);
    }
  }

  List<BasePropType> data2PropList(){
    Vector vector = this.getDataVector();
    List<BasePropType> propTypeBeans = new ArrayList<>();
    for(Object a : vector){
      if(a instanceof Vector){
        Object[] o =  ((Vector) a).toArray();
        if(o[0].toString().trim().equals("")) continue;
        String name = o[0].toString();
        String type = o[1].toString();
        boolean isRequired = o[2].toString().equals("true");
        BasePropType bean = new BasePropType(name, type ,isRequired);
        propTypeBeans.add(bean);
      }
    }
    // sort by name
    return propTypeBeans.stream()
            .filter(PropTypesHelper.distinctByKey(BasePropType::getName))
            .sorted(Comparator.comparing(BasePropType::getName))
            .collect(Collectors.toList());
  }
}
