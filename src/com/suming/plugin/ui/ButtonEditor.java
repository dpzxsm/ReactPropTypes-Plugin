package com.suming.plugin.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;

public class ButtonEditor extends DefaultCellEditor{

    private JButton editor;
    private Object value;
    private int row;
    private JTable table;

    ButtonEditor() {
        super(new JTextField());
        editor = new JButton();
        editor.addActionListener(e -> {
        //这里调用自定义的事件处理方法
            if (table != null && table.getModel() instanceof DefaultTableModel) {
                fireEditingStopped();
                ((DefaultTableModel)table.getModel()).removeRow(row);
            }
        });

    }

    @Override
    public boolean isCellEditable(EventObject e) {
        return true;
    }

    @Override
    public Object getCellEditorValue() {
        return value;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        this.table = table;
        this.row = row;
        this.value = value;
        editor.setText("delete");

//        if (isSelected) {
//            editor.setForeground(table.getSelectionForeground());
//            editor.setBackground(table.getSelectionBackground());
//        } else {
//            editor.setForeground(table.getForeground());
//            editor.setBackground(UIManager.getColor("Button.background"));
//        }
        return editor;
    }
}