package cn.oyzh.easyshell.mysql.foreignKey;

import cn.oyzh.common.cache.CacheHelper;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.mysql.DBDatabaseComboBox;
import cn.oyzh.easyshell.fx.mysql.table.MysqlFieldTextFiled;
import cn.oyzh.easyshell.fx.mysql.table.MysqlForeignKeyPolicyComboBox;
import cn.oyzh.easyshell.fx.mysql.table.MysqlTableComboBox;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlSelectColumnParam;
import cn.oyzh.easyshell.util.mysql.DBUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author oyzh
 * @since 2024/01/25
 */
public class MysqlForeignKeyControl extends MysqlForeignKey {

    public FXTextField getNameControl() {
        try {
            ClearableTextField textField = new ClearableTextField();
            textField.setPromptText(I18nHelper.pleaseInputName());
            if (StringUtil.isEmpty(this.getName())) {
                this.setName(DBUtil.genForeignKeyName());
            }
            textField.addTextChangeListener((observable, oldValue, newValue) -> this.setName(newValue));
            textField.setText(this.getName());
            TableViewUtil.rowOnCtrlS(textField);
            TableViewUtil.selectRowOnMouseClicked(textField);
            return textField;
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
        return null;
    }

    public MysqlFieldTextFiled getColumnControl() {
        try {
            List<MysqlColumn> columnList = CacheHelper.get("columnList");
            if (columnList == null) {
                columnList = new ArrayList<>();
            }
            MysqlFieldTextFiled textField = new MysqlFieldTextFiled(columnList, this.getColumns());
            textField.addTextChangeListener((observable, oldValue, newValue) -> this.setColumns(textField.getSelectedColumns()));
            textField.setFlexWidth("100% - 12");
            TableViewUtil.rowOnCtrlS(textField);
            TableViewUtil.selectRowOnMouseClicked(textField);
            return textField;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public DBDatabaseComboBox getPrimaryKeyDatabaseControl() {
        try {
            DBDatabaseComboBox comboBox = new DBDatabaseComboBox();
            comboBox.init(CacheHelper.get("dbClient"));
            comboBox.selectedItemChanged((observable, oldValue, newValue) -> this.setPrimaryKeyDatabase(newValue));
            comboBox.selectFirstIfNull(this.getPrimaryKeyDatabase());
            TableViewUtil.rowOnCtrlS(comboBox);
            TableViewUtil.selectRowOnMouseClicked(comboBox);
            return comboBox;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public MysqlTableComboBox getPrimaryKeyTableControl() {
        try {
            MysqlTableComboBox comboBox = new MysqlTableComboBox();
            ShellMysqlClient dbClient = CacheHelper.get("dbClient");
            comboBox.init(this.getPrimaryKeyDatabase(), dbClient);
            comboBox.selectedItemChanged((observable, oldValue, newValue) -> this.setPrimaryKeyTable(newValue));
            comboBox.selectFirstIfNull(this.getPrimaryKeyTable());
            this.primaryKeyDatabaseProperty().addListener((observable, oldValue, newValue) -> {
                comboBox.init(this.getPrimaryKeyDatabase(), dbClient);
                comboBox.selectFirst();
            });
            TableViewUtil.rowOnCtrlS(comboBox);
            TableViewUtil.selectRowOnMouseClicked(comboBox);
            return comboBox;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public MysqlForeignKeyPolicyComboBox getDeletePolicyControl() {
        try {
            MysqlForeignKeyPolicyComboBox comboBox = new MysqlForeignKeyPolicyComboBox();
            comboBox.selectedItemChanged((observable, oldValue, newValue) -> this.setDeletePolicy(newValue));
            comboBox.selectFirstIfNull(this.getDeletePolicy());
            TableViewUtil.rowOnCtrlS(comboBox);
            TableViewUtil.selectRowOnMouseClicked(comboBox);
            return comboBox;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public MysqlFieldTextFiled getPrimaryKeyColumnControl() {
        try {
            MysqlFieldTextFiled textField = new MysqlFieldTextFiled();
            textField.addTextChangeListener((observable, oldValue, newValue) -> this.setPrimaryKeyColumns(textField.getSelectedColumns()));
            textField.setFlexWidth("100% - 12");
            Runnable func = () -> {
                textField.clear();
                String dbName = this.getPrimaryKeyDatabase();
                String tableName = this.getPrimaryKeyTable();
                ShellMysqlClient client = CacheHelper.get("dbClient");
                textField.setColumns(client.selectColumns(new MysqlSelectColumnParam(dbName, tableName)));
                textField.setSelectedColumns(this.getPrimaryKeyColumns());
            };
            this.primaryKeyTableProperty().addListener((observable, oldValue, newValue) -> func.run());
            func.run();
            TableViewUtil.rowOnCtrlS(textField);
            TableViewUtil.selectRowOnMouseClicked(textField);
            return textField;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public MysqlForeignKeyPolicyComboBox getUpdatePolicyControl() {
        try {
            MysqlForeignKeyPolicyComboBox comboBox = new MysqlForeignKeyPolicyComboBox();
            comboBox.selectedItemChanged((observable, oldValue, newValue) -> this.setUpdatePolicy(newValue));
            comboBox.selectFirstIfNull(this.getUpdatePolicy());
            TableViewUtil.rowOnCtrlS(comboBox);
            TableViewUtil.selectRowOnMouseClicked(comboBox);
            return comboBox;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static MysqlForeignKeyControl of(MysqlForeignKey foreignKey) {
        MysqlForeignKeyControl control = new MysqlForeignKeyControl();
        control.copy(foreignKey);
        return control;
    }

    public static List<MysqlForeignKeyControl> of(List<MysqlForeignKey> foreignKeys) {
        List<MysqlForeignKeyControl> controls = new ArrayList<>();
        for (MysqlForeignKey foreignKey : foreignKeys) {
            controls.add(of(foreignKey));
        }
        return controls;
    }


}
