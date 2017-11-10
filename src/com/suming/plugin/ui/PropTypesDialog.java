package com.suming.plugin.ui;

import com.intellij.ui.table.JBTable;
import com.suming.plugin.bean.ESVersion;
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
    private JComboBox esVersionBox;
    private JTable table;
    private onSubmitListener onSubmitListener;

    public void setOnSubmitListener(PropTypesDialog.onSubmitListener onSubmitListener) {
        this.onSubmitListener = onSubmitListener;
    }

    public PropTypesDialog(List<PropTypeBean> paramList ,ESVersion esVersion) {
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

        // init Views
        initParamList(paramList);
        this.esVersionBox.setSelectedItem(esVersion.toString());
    }

    private void initParamList(List<PropTypeBean> paramList){
        table = new JBTable();
        PropTypesModel model = new PropTypesModel();
        String[] columnNames = {
                "name",
                "type",
                "isRequired"};
        Object[][] data = new Object[paramList.size()][3];
        for (int i = 0; i < paramList.size(); i++) {
            data[i][0] = paramList.get(i).name;
            data[i][1] = paramList.get(i).type;
            data[i][2] = paramList.get(i).isRequired;
        }
        model.setDataVector(data,columnNames);
        table.setModel(model);
        final DefaultListSelectionModel defaultListSelectionModel = new DefaultListSelectionModel();
        defaultListSelectionModel.setSelectionMode(SINGLE_SELECTION);
        table.setSelectionModel(defaultListSelectionModel);
        // form header center
        DefaultTableCellHeaderRenderer thr = new DefaultTableCellHeaderRenderer();
        thr.setHorizontalAlignment(JLabel.CENTER);
        table.getTableHeader().setDefaultRenderer(thr);
        //render special column
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
            Object selectItem = esVersionBox.getSelectedItem();
            this.onSubmitListener.onSubmit(propTypeBeans , isUseNewPropTypes.isSelected() ,
                    ESVersion.valueOf(selectItem!=null?selectItem.toString():"ES6"));
        }
    }

    private void onCancel() {
        dispose();
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
    }

    public interface onSubmitListener{
        void onSubmit(List<PropTypeBean> beans , boolean isNew ,ESVersion esVersion);
    }
}
