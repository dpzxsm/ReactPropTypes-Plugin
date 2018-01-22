package com.suming.plugin.ui;

import com.intellij.ui.table.JBTable;
import com.suming.plugin.bean.BasePropType;
import sun.swing.table.DefaultTableCellHeaderRenderer;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

public class ShapePropTypesDialog extends JDialog {
  private JPanel contentPane;
  private JButton buttonOK;
  private JButton buttonCancel;
  private JButton pasteWithJSONButton;
  private JScrollPane sp;
  private JButton addPropBtn;
  private JTable table;
  private ShapePropTypesModel model;
  private ShapePropTypesDialog.onSubmitListener onSubmitListener;

  public void setOnSubmitListener(ShapePropTypesDialog.onSubmitListener onSubmitListener) {
    this.onSubmitListener = onSubmitListener;
  }

  public ShapePropTypesDialog(List<BasePropType> propTypeList, String title) {
    setContentPane(contentPane);
    setTitle((title !=null && !title.equals("")? title : "unnamed"));
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
    initTable(propTypeList);
    // init other widget
    initOtherWidgets();
  }

  private void initTable(List<BasePropType> propTypeList) {
    table = new JBTable();
    model = new ShapePropTypesModel();
    model.initData(propTypeList);
    table.setModel(model);
    final DefaultListSelectionModel defaultListSelectionModel = new DefaultListSelectionModel();
    defaultListSelectionModel.setSelectionMode(SINGLE_SELECTION);
    table.setSelectionModel(defaultListSelectionModel);
    // form header center
    DefaultTableCellHeaderRenderer thr = new DefaultTableCellHeaderRenderer();
    thr.setHorizontalAlignment(JLabel.CENTER);
    table.getTableHeader().setDefaultRenderer(thr);
    //render special column
    TableColumn nameColumn = table.getColumn("name");
    TableColumn typeColumn = table.getColumn("type");
    TableColumn isRequireColumn = table.getColumn("isRequired");
    TableColumn operationColumn = table.getColumn("ops");
    nameColumn.setCellRenderer(new NameTextRenderer(true, "Please input name !"));
    nameColumn.setCellEditor(new DefaultCellEditor(new NameTextRenderer(false, "Please input name !")));
    nameColumn.setPreferredWidth(200);
    typeColumn.setCellEditor(new DefaultCellEditor(new ComboBoxRenderer(false)));
    typeColumn.setCellRenderer(new ComboBoxRenderer(false));
    typeColumn.setPreferredWidth(100);
    isRequireColumn.setCellEditor(new DefaultCellEditor(new CheckBoxRenderer()));
    isRequireColumn.setCellRenderer(new CheckBoxRenderer());
    isRequireColumn.setWidth(50);
    ButtonEditor buttonEditor = new ButtonEditor();
    operationColumn.setCellRenderer(new ButtonRenderer());
    operationColumn.setCellEditor(buttonEditor);
    operationColumn.setPreferredWidth(80);
    sp.setViewportView(table);
  }

  private void initOtherWidgets(){
    // add button onClick event
    addPropBtn.addActionListener(e -> {
      model.addRow(new BasePropType("","any", false ));
      int  rowCount = table.getRowCount();
      table.getSelectionModel().setSelectionInterval(rowCount-1 , rowCount- 1 );
      Rectangle rect = table.getCellRect(rowCount-1 ,  0 ,  true );
      table.scrollRectToVisible(rect);
    });
    pasteWithJSONButton.addActionListener(e -> {
      JsonInputDialog dialog = new JsonInputDialog();
      dialog.pack();
      dialog.setLocationRelativeTo(this);
      dialog.setOnSubmitListener(beans -> model.reInitData(beans));
      dialog.setVisible(true);
    });
  }

  private void onOK() {
    // add your code here
    dispose();
    if(this.onSubmitListener!=null){
      this.onSubmitListener.onSubmit(model.data2PropList());
    }
  }

  private void onCancel() {
    dispose();
  }

  public interface onSubmitListener{
    void onSubmit(List<BasePropType> beans);
  }

  public static void main(String[] args) {
    List<BasePropType> list = new ArrayList<>();
    list.add(new BasePropType("test", "string", true));
    ShapePropTypesDialog dialog = new ShapePropTypesDialog(list, "测试弹框");
    dialog.pack();
    dialog.setVisible(true);
    System.exit(0);
  }
}
