package com.suming.plugin.ui;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.ui.table.JBTable;
import com.suming.plugin.bean.*;
import com.suming.plugin.bean.Component;
import com.suming.plugin.persist.SettingService;
import com.suming.plugin.utils.PropTypesHelper;
import sun.swing.table.DefaultTableCellHeaderRenderer;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

public class PropTypesDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JScrollPane sp;
    private JComboBox esVersionBox;
    private JButton addPropBtn;
    private JComboBox importBox;
    private JCheckBox handleDefaultPropCheckBox;
    private JTable table;
    private onSubmitListener onSubmitListener;
    private SettingService settingService = ServiceManager.getService(SettingService.class);

    public void setOnSubmitListener(PropTypesDialog.onSubmitListener onSubmitListener) {
        this.onSubmitListener = onSubmitListener;
    }

    public PropTypesDialog(List<PropTypeBean> paramList ,Component component) {
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
        // init Table
        initTable(paramList);
        // init other widget
        initOtherWidgets(component);
    }

    private List<PropTypeBean> data2ParamList(){
        PropTypesModel model = (PropTypesModel) table.getModel();
        Vector vector = model.getDataVector();
        List<PropTypeBean> propTypeBeans = new ArrayList<>();
        for(Object a : vector){
            if(a instanceof Vector){
                Object[] o =  ((Vector) a).toArray();
                if(o[0].toString().trim().equals("")) continue;
                boolean isRequired = o[2].toString().equals("true");
                String defaultValue = o[3] == null ? null : o[3].toString();
                propTypeBeans.add(new PropTypeBean(o[0].toString(),o[1].toString(),isRequired , "", defaultValue));
            }
        }
        // sort by name
        return propTypeBeans.stream()
                .filter(PropTypesHelper.distinctByKey(PropTypeBean::getName))
                .sorted(Comparator.comparing(PropTypeBean::getName))
                .collect(Collectors.toList());
    }

    private void initTable(List<PropTypeBean> paramList){
        table = new JBTable();
        PropTypesModel model = new PropTypesModel();
        model.initData(paramList);
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
        TableColumn infoColumn = table.getColumn("defaultValue");
        TableColumn operationColumn = table.getColumn("ops");
        nameColumn.setCellRenderer(new NameTextRenderer(true, "Please input name !"));
        nameColumn.setCellEditor(new DefaultCellEditor(new NameTextRenderer(false, "Please input name !")));
        typeColumn.setCellEditor(new DefaultCellEditor(new ComboBoxRenderer()));
        typeColumn.setCellRenderer(new ComboBoxRenderer());
        typeColumn.setMaxWidth(150);
        isRequireColumn.setCellEditor(new DefaultCellEditor(new CheckBoxRenderer()));
        isRequireColumn.setCellRenderer(new CheckBoxRenderer());
        isRequireColumn.setMaxWidth(100);
        infoColumn.setCellRenderer(new NameTextRenderer(true, ""));
        infoColumn.setCellEditor(new DefaultCellEditor(new NameTextRenderer(false, "")));
        ButtonEditor buttonEditor = new ButtonEditor();
        operationColumn.setCellRenderer(new ButtonRenderer());
        operationColumn.setCellEditor(buttonEditor);
        operationColumn.setMinWidth(100);
        operationColumn.setMaxWidth(100);
        sp.setViewportView(table);
    }

    private void initOtherWidgets(Component component){
        Setting setting = settingService.getState();
        if(component.getEsVersion()!=null){
            this.esVersionBox.setSelectedItem(component.getEsVersion().toString());
        }else if(setting.getEsVersion()!=null){
            this.esVersionBox.setSelectedItem(setting.getEsVersion().toString());
        }
        // if the component is a stateless component, disable esVersionBox
        if(component.getComponentType() == ComponentType.STATELESS){
            this.esVersionBox.setEnabled(false);
        }
        if(setting.getImportMode()!=null){
            this.importBox.setSelectedItem(setting.getImportMode().getValue());
        }
        this.handleDefaultPropCheckBox.setSelected(setting.isNeedDefault());

        this.esVersionBox.addActionListener(e -> {
            // current hasn't propTypes
            if(component.getEsVersion() ==null){
                Object selectItem = esVersionBox.getSelectedItem();
                ESVersion esVersion = ESVersion.valueOf(selectItem!=null?selectItem.toString():"ES6");
                setting.setEsVersion(esVersion);
            }
        });
        this.importBox.addActionListener(e -> {
            Object selectImportItem  = importBox.getSelectedItem();
            ImportMode importMode = selectImportItem == null?ImportMode.Disabled:ImportMode.toEnum(selectImportItem.toString());
            setting.setImportMode(importMode);
        });
        // add button onClick event
        addPropBtn.addActionListener(e -> {
            PropTypesModel model = (PropTypesModel) table.getModel();
            model.addRow(new PropTypeBean("","any", false, "manual added"));
            int  rowCount = table.getRowCount();
            table.getSelectionModel().setSelectionInterval(rowCount-1 , rowCount- 1 );
            Rectangle rect = table.getCellRect(rowCount-1 ,  0 ,  true );
            table.scrollRectToVisible(rect);
        });
        handleDefaultPropCheckBox.addActionListener(e -> {
            setting.setNeedDefault(handleDefaultPropCheckBox.isSelected());
        });
    }

    private void onOK() {
        dispose();
        if(this.onSubmitListener!=null){
            Object selectItem = esVersionBox.getSelectedItem();
            ESVersion esVersion = selectItem==null?ESVersion.ES6:ESVersion.valueOf(selectItem.toString());
            ImportMode importMode = settingService.getState().getImportMode();
            this.onSubmitListener.onSubmit(data2ParamList() ,importMode ,esVersion , handleDefaultPropCheckBox.isSelected());
        }
    }

    private void onCancel() {
        dispose();
    }

    public interface onSubmitListener{
        void onSubmit(List<PropTypeBean> beans , ImportMode importMode ,ESVersion esVersion , boolean handleDefault);
    }
}
