package cn.oyzh.easyshell.mysql.foreignKey;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.mysql.ShellMysqlDatabaseComboBox;
import cn.oyzh.easyshell.fx.mysql.table.ShellMysqlFieldTextFiled;
import cn.oyzh.easyshell.fx.mysql.table.ShellMysqlForeignKeyPolicyComboBox;
import cn.oyzh.easyshell.fx.mysql.table.ShellMysqlTableComboBox;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlSelectColumnParam;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.easyshell.util.mysql.ShellMysqlUtil;
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

    private String dbName;

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    private ShellMysqlClient dbClient;

    public void setDbClient(ShellMysqlClient dbClient) {
        this.dbClient = dbClient;
    }

    private List<MysqlColumn> columnList;

    public void setColumnList(List<MysqlColumn> columnList) {
        this.columnList = columnList;
    }

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

    public ShellMysqlFieldTextFiled getColumnControl() {
        try {
            //List<MysqlColumn> columnList = CacheHelper.get("mysql:columnList");
            if (this.columnList == null) {
                this.columnList = new ArrayList<>();
            }
            ShellMysqlFieldTextFiled textField = new ShellMysqlFieldTextFiled(this.columnList, this.getColumns());
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

    public ShellMysqlDatabaseComboBox getPrimaryKeyDatabaseControl() {
        try {
            ShellMysqlDatabaseComboBox comboBox = new ShellMysqlDatabaseComboBox();
            comboBox.init(this.dbClient);
            //comboBox.init(CacheHelper.get("mysql:dbClient"));
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

    public ShellMysqlTableComboBox getPrimaryKeyTableControl() {
        try {
            ShellMysqlTableComboBox comboBox = new ShellMysqlTableComboBox();
            //ShellMysqlClient dbClient = CacheHelper.get("mysql:dbClient");
            comboBox.init(this.getPrimaryKeyDatabase(), this.dbClient);
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

    public ShellMysqlForeignKeyPolicyComboBox getDeletePolicyControl() {
        try {
            ShellMysqlForeignKeyPolicyComboBox comboBox = new ShellMysqlForeignKeyPolicyComboBox();
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

    public ShellMysqlFieldTextFiled getPrimaryKeyColumnControl() {
        try {
            ShellMysqlFieldTextFiled textField = new ShellMysqlFieldTextFiled();
            textField.addTextChangeListener((observable, oldValue, newValue) -> this.setPrimaryKeyColumns(textField.getSelectedColumns()));
            textField.setFlexWidth("100% - 12");
            Runnable func = () -> {
                textField.clear();
                String dbName = this.getPrimaryKeyDatabase();
                String tableName = this.getPrimaryKeyTable();
                //ShellMysqlClient client = CacheHelper.get("mysql:dbClient");
                textField.setColumns(this.dbClient.selectColumns(new MysqlSelectColumnParam(dbName, tableName)));
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

    public ShellMysqlForeignKeyPolicyComboBox getUpdatePolicyControl() {
        try {
            ShellMysqlForeignKeyPolicyComboBox comboBox = new ShellMysqlForeignKeyPolicyComboBox();
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

    @Override
    public String getPrimaryKeyDatabase() {
        return super.getPrimaryKeyDatabase() == null ? this.dbName : super.getPrimaryKeyDatabase();
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
