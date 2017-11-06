package com.suming.plugin.ui;

import com.intellij.ui.components.JBLabel;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class TextRederer extends JBLabel implements TableCellRenderer{

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setText(value.toString());
        return this;
    }
}
