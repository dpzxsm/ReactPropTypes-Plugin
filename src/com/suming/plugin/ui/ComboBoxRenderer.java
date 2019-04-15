package com.suming.plugin.ui;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ComboBoxRenderer extends JComboBox<String> implements TableCellRenderer {

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
                "node",
                "element",
                "arrayOf",
                "objectOf",
                "oneOf",
                "instanceOf",
                "oneOfType"
        });
        if (showAllType) {
            this.insertItemAt("shape", 10);
            this.insertItemAt("exact", 11);
        }
        this.setMaximumRowCount(10);
        this.setEditable(false);
        this.setLightWeightPopupEnabled(false);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        this.setSelectedItem(value.toString());
        return this;
    }
}
