package com.suming.plugin.ui;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ComboBoxRenderer extends JComboBox<String> implements TableCellRenderer{

    ComboBoxRenderer(boolean showAllType) {
        super(new String[]{
                "any",
                "string",
                "object",
                "bool",
                "func",
                "number",
                "array",
                "symbol",
                "arrayOf",
                "element",
                "instanceOf",
                "node",
                "objectOf",
                "oneOf",
                "oneOfType",
                "exact"
        });
        if(showAllType){
            this.addItem("shape");
        }
        this.setEditable(false);
        this.setLightWeightPopupEnabled(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        this.setSelectedItem(value.toString());
        return this;
    }
}
