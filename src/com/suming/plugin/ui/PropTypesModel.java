package com.suming.plugin.ui;

import com.intellij.openapi.components.ServiceManager;
import com.suming.plugin.bean.BasePropType;
import com.suming.plugin.bean.PropTypeBean;
import com.suming.plugin.persist.SettingService;
import com.suming.plugin.utils.PropTypesHelper;

import javax.swing.table.DefaultTableModel;
import java.util.*;
import java.util.stream.Collectors;

class PropTypesModel extends DefaultTableModel {
    SettingService settingService = ServiceManager.getService(SettingService.class);

    void addRow(PropTypeBean bean) {
        HashMap<String, Object> extraData = new HashMap<>();
        super.addRow(new Object[]{bean.name, bean.type, bean.isRequired,
                bean.getDefaultValue(), extraData});
    }

    void initData(List<PropTypeBean> beans) {
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
            HashMap<String, Object> extraData = new HashMap<>();
            extraData.put("shapeProps", beans.get(i).getShapePropTypeList());
            extraData.put("jsonData", beans.get(i).getJsonData());
            data[i][4] = extraData;
        }
        this.setDataVector(data, columnNames);
    }

    @SuppressWarnings("unchecked")
    void updateExtraDataByName(int row, String name, Object data) {
        HashMap extraData = (HashMap) this.getValueAt(row, 4);
        extraData.put(name, data);
        this.setValueAt(extraData, row, 4);
    }

    String getValueFromIndex(int column, int row) {
        Object[] objects = getDataVector().toArray();
        if (row <= objects.length - 1) {
            Vector rowVector = ((Vector) objects[row]);
            return rowVector.elementAt(column).toString();
        } else {
            return "";
        }
    }

    List<PropTypeBean> data2PropList() {
        Vector vector = this.getDataVector();
        List<PropTypeBean> propTypeBeans = new ArrayList<>();
        for (Object a : vector) {
            if (a instanceof Vector) {
                Object[] o = ((Vector) a).toArray();
                if (o[0].toString().trim().equals("")) continue;
                String name = o[0].toString();
                String type = o[1].toString();
                boolean isRequired = o[2].toString().equals("true");
                String defaultValue = o[3] == null ? null : o[3].toString();
                HashMap extraData = (HashMap) o[4];
                Object shapePropsObj = extraData.get("shapeProps");
                PropTypeBean bean = new PropTypeBean(name, type, isRequired, defaultValue);
                if (type != null && (type.equals("shape") || type.equals("exact")) && shapePropsObj != null) {
                    List<BasePropType> shapePropList = ((List<?>) shapePropsObj).stream()
                            .map(e -> (BasePropType) e).collect(Collectors.toList());
                    bean.setShapePropTypeList(shapePropList);
                }
                String jsonData = (String) extraData.get("jsonData");
                if (type != null && jsonData != null && !jsonData.equals("")) {
                    bean.setJsonData(jsonData);
                }
                propTypeBeans.add(bean);
            }
        }
        // sort by name
        if (this.settingService.getState().isSortProps()) {
            return propTypeBeans.stream()
                    .filter(PropTypesHelper.distinctByKey(PropTypeBean::getName))
                    .sorted(Comparator.comparing(PropTypeBean::getName))
                    .collect(Collectors.toList());
        } else {
            return propTypeBeans.stream()
                    .filter(PropTypesHelper.distinctByKey(PropTypeBean::getName))
                    .collect(Collectors.toList());
        }

    }
}
