package com.suming.plugin.ui;

import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class TextRenderer extends JTextField implements TableCellRenderer{
    public static String defaultValue = "Please input name !";

    TextRenderer(boolean isCellRenderer) {
        super();
        if(isCellRenderer){
            setBorder(null);
            setBackground(null);
        }
        this.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if(getText().equals(defaultValue)){
                    setText("");
                }
                super.focusGained(e);
            }

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if(getText().equals("")){
                    setText(defaultValue);
                }
            }
        });
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        String content = value.toString();
        if(content.trim().equals("")||content.equals(defaultValue)){
            setText(defaultValue);
            setForeground(JBColor.GRAY);
        }else {
            setText(content);
            setForeground(null);
        }
        return this;
    }


}
