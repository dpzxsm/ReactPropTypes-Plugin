package com.suming.plugin.ui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.suming.plugin.bean.BasePropType;
import com.suming.plugin.utils.PropTypesHelper;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonInputDialog extends JDialog {
  private JPanel contentPane;
  private JButton buttonOK;
  private JButton buttonCancel;
  private JTextArea input;
  private JLabel hintText;
  private JsonInputDialog.onSubmitListener onSubmitListener;

  public void setOnSubmitListener(JsonInputDialog.onSubmitListener onSubmitListener) {
    this.onSubmitListener = onSubmitListener;
  }

  public JsonInputDialog() {
    setContentPane(contentPane);
    setTitle("paste a json or a js Object");
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
  }

  private void onOK() {
    // add your code here
    String jsonStr = input.getText();
    if( jsonStr != null && !jsonStr.trim().equals("")){
      try{
        List<BasePropType> basePropTypes = new ArrayList<>();
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(jsonStr);
        if(element instanceof JsonObject){
          for (Object o : ((JsonObject) element).entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            String name = entry.getKey().toString();
            String value = entry.getValue().toString();
            basePropTypes.add(new BasePropType(name, PropTypesHelper.getPropTypeByValue(value), false));
          }
          if(this.onSubmitListener != null){
            this.onSubmitListener.onSubmit(basePropTypes);
          }
          dispose();
        }else {
          hintText.setText("Not a Json Object !");
        }
      } catch (Exception e){
        System.out.println(e);
        hintText.setText("Incorrectly formatting !");
      }
    }else {
      hintText.setText("Can not be empty !");
    }
  }

  private void onCancel() {
    // add your code here if necessary
    dispose();
  }
  public interface onSubmitListener{
    void onSubmit(List<BasePropType> beans);
  }
  

  public static void main(String[] args) {
    JsonInputDialog dialog = new JsonInputDialog();
    dialog.pack();
    dialog.setVisible(true);
    System.exit(0);
  }
}
