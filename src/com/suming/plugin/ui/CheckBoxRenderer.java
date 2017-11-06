package com.suming.plugin.ui;

import com.intellij.ui.components.JBCheckBox;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class CheckBoxRenderer extends JBCheckBox implements TableCellRenderer{

    CheckBoxRenderer() {
        this.setHorizontalAlignment(SwingConstants.CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        this.setSelected(value.toString().equals("true"));
        return this;
    }
}
