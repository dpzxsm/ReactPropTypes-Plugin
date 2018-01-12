package com.suming.plugin.ui;

import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class NameTextRenderer extends JTextField implements TableCellRenderer{

    private String placeholder = "";

    NameTextRenderer(boolean isCellRenderer , String placeholder) {
        super();
        this.placeholder = placeholder;
        if(isCellRenderer){
            setBorder(null);
            setBackground(null);
        }
        this.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if(getText().equals(placeholder)){
                    setText("");
                }
                super.focusGained(e);
            }

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if(getText().equals("")){
                    setText(placeholder);
                }
            }
        });
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        String content = value == null ? "" : value.toString();
        if(content.trim().equals("")||content.equals(placeholder)){
            setText(placeholder);
            setForeground(JBColor.GRAY);
        }else {
            setText(content);
            setForeground(null);
        }
        return this;
    }


}
