package cn.oyzh.easyshell.postgresql.foreignKey;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.postgresql.ShellPostgresqlClient;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.postgresql.column.PostgresqlSelectColumnParam;
import cn.oyzh.easyshell.fx.postgresql.ShellPostgresqlSchemaComboBox;
import cn.oyzh.easyshell.fx.postgresql.table.ShellPostgresqlFieldTextFiled;
import cn.oyzh.easyshell.fx.postgresql.table.ShellPostgresqlForeignKeyPolicyComboBox;
import cn.oyzh.easyshell.fx.postgresql.table.ShellPostgresqlTableComboBox;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQL外键组件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class PostgresqlForeignKeyControl extends PostgresqlForeignKey {

    /**
     * 模式名称
     */
    private String schema;

    /**
     * 设置模式名称
     *
     * @param schema 模式名称
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * 数据库客户端
     */
    private ShellPostgresqlClient dbClient;

    /**
     * 设置数据库客户端
     *
     * @param dbClient 数据库客户端
     */
    public void setDbClient(ShellPostgresqlClient dbClient) {
        this.dbClient = dbClient;
    }

    /**
     * 字段列表
     */
    private List<PostgresqlColumn> columnList;

    /**
     * 设置字段列表
     *
     * @param columnList 字段列表
     */
    public void setColumnList(List<PostgresqlColumn> columnList) {
        this.columnList = columnList;
    }

    /**
     * 获取名称组件
     *
     * @return 名称组件
     */
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
            MessageBox.exception(ex, StageManager.getFrontWindow());
        }
        return null;
    }

    /**
     * 获取外键字段组件
     *
     * @return 外键字段组件
     */
    public ShellPostgresqlFieldTextFiled getColumnControl() {
        try {
            //List<PostgresqlColumn> columnList = CacheHelper.get("postgresql:columnList");
            if (this.columnList == null) {
                this.columnList = new ArrayList<>();
            }
            ShellPostgresqlFieldTextFiled textField = new ShellPostgresqlFieldTextFiled(columnList, this.getColumns());
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

    /**
     * 获取引用模式组件
     *
     * @return 引用模式组件
     */
    public ShellPostgresqlSchemaComboBox getReferencedDatabaseControl() {
        try {
            ShellPostgresqlSchemaComboBox comboBox = new ShellPostgresqlSchemaComboBox();
            //comboBox.init(CacheHelper.get("postgresql:dbClient"));
            comboBox.init(this.dbClient);
            comboBox.selectedItemChanged((observable, oldValue, newValue) -> this.setReferencedDatabase(newValue));
            comboBox.selectFirstIfNull(this.getReferencedDatabase());
            TableViewUtil.rowOnCtrlS(comboBox);
            TableViewUtil.selectRowOnMouseClicked(comboBox);
            return comboBox;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 获取引用表组件
     *
     * @return 引用表组件
     */
    public ShellPostgresqlTableComboBox getReferencedTableControl() {
        try {
            ShellPostgresqlTableComboBox comboBox = new ShellPostgresqlTableComboBox();
            //ShellPostgresqlClient dbClient = CacheHelper.get("postgresql:dbClient");
            comboBox.init(this.getReferencedDatabase(), this.dbClient);
            comboBox.selectedItemChanged((observable, oldValue, newValue) -> this.setReferencedTable(newValue));
            comboBox.selectFirstIfNull(this.getReferencedTable());
            this.referencedDatabaseProperty().addListener((observable, oldValue, newValue) -> {
                comboBox.init(this.getReferencedDatabase(), dbClient);
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

    /**
     * 获取删除策略组件
     *
     * @return 删除策略组件
     */
    public ShellPostgresqlForeignKeyPolicyComboBox getDeletePolicyControl() {
        try {
            ShellPostgresqlForeignKeyPolicyComboBox comboBox = new ShellPostgresqlForeignKeyPolicyComboBox();
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

    /**
     * 获取引用字段组件
     *
     * @return 引用字段组件
     */
    public ShellPostgresqlFieldTextFiled getReferencedColumnControl() {
        try {
            ShellPostgresqlFieldTextFiled textField = new ShellPostgresqlFieldTextFiled();
            textField.addTextChangeListener((observable, oldValue, newValue) -> this.setReferencedColumns(textField.getSelectedColumns()));
            textField.setFlexWidth("100% - 12");
            Runnable func = () -> {
                textField.clear();
                String dbName = this.getReferencedDatabase();
                String tableName = this.getReferencedTable();
                //ShellPostgresqlClient client = CacheHelper.get("postgresql:dbClient");
                textField.setColumns(this.dbClient.selectColumns(new PostgresqlSelectColumnParam(dbName, tableName)));
                textField.setSelectedColumns(this.getReferencedColumns());
            };
            this.referencedTableProperty().addListener((observable, oldValue, newValue) -> func.run());
            func.run();
            TableViewUtil.rowOnCtrlS(textField);
            TableViewUtil.selectRowOnMouseClicked(textField);
            return textField;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 获取更新策略组件
     *
     * @return 更新策略组件
     */
    public ShellPostgresqlForeignKeyPolicyComboBox getUpdatePolicyControl() {
        try {
            ShellPostgresqlForeignKeyPolicyComboBox comboBox = new ShellPostgresqlForeignKeyPolicyComboBox();
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

    /**
     * 根据外键构建组件
     *
     * @param foreignKey 外键
     * @return 组件
     */
    public static PostgresqlForeignKeyControl of(PostgresqlForeignKey foreignKey) {
        PostgresqlForeignKeyControl control = new PostgresqlForeignKeyControl();
        control.copy(foreignKey);
        return control;
    }

    /**
     * 根据外键列表构建组件列表
     *
     * @param foreignKeys 外键列表
     * @return 组件列表
     */
    public static List<PostgresqlForeignKeyControl> of(List<PostgresqlForeignKey> foreignKeys) {
        List<PostgresqlForeignKeyControl> controls = new ArrayList<>();
        for (PostgresqlForeignKey foreignKey : foreignKeys) {
            controls.add(of(foreignKey));
        }
        return controls;
    }

    @Override
    public String getReferencedDatabase() {
        return super.getReferencedDatabase() == null ? this.schema : super.getReferencedDatabase();
    }
}
