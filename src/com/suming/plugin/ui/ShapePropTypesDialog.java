package com.suming.plugin.ui;

import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.WindowManager;
import com.suming.plugin.bean.PropTypeBean;
import com.suming.plugin.bean.ShapePropType;

import javax.swing.*;
import java.awt.event.*;
import java.util.Collections;
import java.util.List;

public class ShapePropTypesDialog extends JDialog {
  private JPanel contentPane;
  private JButton buttonOK;
  private JButton buttonCancel;
  private JButton pasteWithJSONButton;
  private JScrollPane sp;

  public ShapePropTypesDialog(List<ShapePropType> paramList) {
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(buttonOK);

    buttonOK.addActionListener(e -> onOK());

    buttonCancel.addActionListener(e -> onCancel());

    // call onCancel() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        onCancel();
      }
    });

    // call onCancel() on ESCAPE
    contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);


    // init Table
    initTable(paramList);
    // init other widget
    initOtherWidgets();
  }

  private void initTable(List<ShapePropType> paramList) {

  }

  private void initOtherWidgets(){

  }

  private void onOK() {
    // add your code here
    dispose();
  }

  private void onCancel() {
    // add your code here if necessary
    dispose();
  }

  public static void main(String[] args) {
    ShapePropTypesDialog dialog = new ShapePropTypesDialog(Collections.emptyList());
    dialog.pack();
    dialog.setVisible(true);
    System.exit(0);
  }
}
