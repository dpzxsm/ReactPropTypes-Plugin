package com.suming.plugin.ui;

import com.intellij.ui.table.JBTable;
import com.suming.plugin.bean.PropTypeBean;
import sun.swing.table.DefaultTableCellHeaderRenderer;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

public class PropTypesDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JScrollPane sp;
    private JCheckBox isUseNewPropTypes;
    private JTable table;
    private List<String> paramList ;
    private onSubmitListener onSubmitListener;

    public void setOnSubmitListener(PropTypesDialog.onSubmitListener onSubmitListener) {
        this.onSubmitListener = onSubmitListener;
    }

    public PropTypesDialog(List<String> paramList) {
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
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        //赋值
        this.paramList = paramList;
        initParamList();
    }

    private void initParamList(){
        table = new JBTable();
        PropTypesModel model = new PropTypesModel();
        String[] columnNames = {
                "name",
                "type",
                "isRequired"};
        Object[][] data = new Object[this.paramList.size()][3];
        for (int i = 0; i < this.paramList.size(); i++) {
            data[i][0] = paramList.get(i);
            data[i][1] = "any";
            data[i][2] = false;
        }
        model.setDataVector(data,columnNames);
        table.setModel(model);
        final DefaultListSelectionModel defaultListSelectionModel = new DefaultListSelectionModel();
        defaultListSelectionModel.setSelectionMode(SINGLE_SELECTION);
        table.setSelectionModel(defaultListSelectionModel);
        //表头居中
        DefaultTableCellHeaderRenderer thr = new DefaultTableCellHeaderRenderer();
        thr.setHorizontalAlignment(JLabel.CENTER);
        table.getTableHeader().setDefaultRenderer(thr);
        //修改特殊项
        TableColumn typeColumn = table.getColumn("type");
        TableColumn isRequireColumn = table.getColumn("isRequired");
        typeColumn.setCellEditor(new DefaultCellEditor(new ComboBoxRenderer()));
        typeColumn.setCellRenderer(new ComboBoxRenderer());
        isRequireColumn.setCellEditor(new DefaultCellEditor(new CheckBoxRenderer()));
        isRequireColumn.setCellRenderer(new CheckBoxRenderer());
        isRequireColumn.setMaxWidth(100);
        sp.setViewportView(table);
    }

    private void onOK() {
        // add your code here
        dispose();
        PropTypesModel model = (PropTypesModel) table.getModel();
        Vector vector = model.getDataVector();
        List<PropTypeBean> propTypeBeans = new ArrayList<>();
        for(Object a : vector){
            if(a instanceof Vector){
                Object[] o =  ((Vector) a).toArray();
                propTypeBeans.add(new PropTypeBean(o[0],o[1],o[2]));
            }
        }
        if(this.onSubmitListener!=null){
            this.onSubmitListener.onSubmit(propTypeBeans , isUseNewPropTypes.isSelected());
        }
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("name");
        list.add("title");
        list.add("onChange");
        list.add("key");
        list.add("style");
        PropTypesDialog dialog = new PropTypesDialog(list);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    public static interface onSubmitListener{
        void onSubmit(List<PropTypeBean> beans , boolean isNew);
    }
}
