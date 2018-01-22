package com.suming.plugin.ui;

import com.suming.plugin.bean.BasePropType;
import com.suming.plugin.bean.PropTypeBean;
import com.suming.plugin.utils.PropTypesHelper;

import javax.swing.table.DefaultTableModel;
import java.util.*;
import java.util.stream.Collectors;

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
//            // extra data
            HashMap<String,Object> extraData = new HashMap<>();
            extraData.put("describe", beans.get(i).getDescribe());
            extraData.put("shapeProps", beans.get(i).getShapePropTypeList());
            data[i][4] = extraData;
        }
        this.setDataVector(data,columnNames);
    }

    @SuppressWarnings("unchecked")
    void updateExtraDataByName(int row, String name, Object data){
        HashMap extraData = (HashMap) this.getValueAt(row, 4);
        extraData.put(name, data);
        this.setValueAt(extraData, row,4);
    }

    String getValueFromIndex(int column, int row){
        Object[] objects = getDataVector().toArray();
        if(row <= objects.length -1 ){
            Vector rowVector = ((Vector)objects[row]);
            return  rowVector.elementAt(column).toString();
        }else {
            return "";
        }
    }

    List<PropTypeBean> data2PropList(){
        Vector vector = this.getDataVector();
        List<PropTypeBean> propTypeBeans = new ArrayList<>();
        for(Object a : vector){
            if(a instanceof Vector){
                Object[] o =  ((Vector) a).toArray();
                if(o[0].toString().trim().equals("")) continue;
                String name = o[0].toString();
                String type = o[1].toString();
                boolean isRequired = o[2].toString().equals("true");
                String defaultValue = o[3] == null ? null : o[3].toString();
                HashMap extraData = (HashMap) o[4];
                Object describeObj = extraData.get("describe");
                Object shapePropsObj = extraData.get("shapeProps");
                String describe =  describeObj == null ? null : describeObj.toString();
                PropTypeBean bean = new PropTypeBean(name, type ,isRequired , describe, defaultValue);
                if(type!=null && type.equals("shape") && shapePropsObj !=null ){
                    List<BasePropType>  shapePropList = ((List<?>) shapePropsObj).stream()
                            .map(e->(BasePropType)e).collect(Collectors.toList());
                    bean.setShapePropTypeList(shapePropList);
                }
                propTypeBeans.add(bean);
            }
        }
        // sort by name
        return propTypeBeans.stream()
                .filter(PropTypesHelper.distinctByKey(PropTypeBean::getName))
                .sorted(Comparator.comparing(PropTypeBean::getName))
                .collect(Collectors.toList());
    }
}
