package cn.oyzh.easyshell.dameng.foreignKey;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.column.DamengSelectColumnParam;
import cn.oyzh.easyshell.fx.dameng.ShellDamengSchemaComboBox;
import cn.oyzh.easyshell.fx.dameng.table.DamengFieldTextFiled;
import cn.oyzh.easyshell.fx.dameng.table.DamengForeignKeyPolicyComboBox;
import cn.oyzh.easyshell.fx.dameng.table.DamengTableComboBox;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.text.field.FXTextField;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 达梦外键组件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class DamengForeignKeyControl extends DamengForeignKey {

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
    private ShellDamengClient dbClient;

    /**
     * 设置数据库客户端
     *
     * @param dbClient 数据库客户端
     */
    public void setDbClient(ShellDamengClient dbClient) {
        this.dbClient = dbClient;
    }

    /**
     * 字段列表
     */
    private List<DamengColumn> columnList;

    /**
     * 设置字段列表
     *
     * @param columnList 字段列表
     */
    public void setColumnList(List<DamengColumn> columnList) {
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
            MessageBox.exception(ex);
        }
        return null;
    }

    /**
     * 获取外键字段组件
     *
     * @return 外键字段组件
     */
    public DamengFieldTextFiled getColumnControl() {
        try {
            //List<DamengColumn> columnList = CacheHelper.get("dameng:columnList");
            if (this.columnList == null) {
                this.columnList = new ArrayList<>();
            }
            DamengFieldTextFiled textField = new DamengFieldTextFiled(columnList, this.getColumns());
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
    public ShellDamengSchemaComboBox getPrimaryKeyDatabaseControl() {
        try {
            ShellDamengSchemaComboBox comboBox = new ShellDamengSchemaComboBox();
            //comboBox.init(CacheHelper.get("dameng:dbClient"));
            comboBox.init(this.dbClient);
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

    /**
     * 获取引用表组件
     *
     * @return 引用表组件
     */
    public DamengTableComboBox getPrimaryKeyTableControl() {
        try {
            DamengTableComboBox comboBox = new DamengTableComboBox();
            //ShellDamengClient dbClient = CacheHelper.get("dameng:dbClient");
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

    /**
     * 获取删除策略组件
     *
     * @return 删除策略组件
     */
    public DamengForeignKeyPolicyComboBox getDeletePolicyControl() {
        try {
            DamengForeignKeyPolicyComboBox comboBox = new DamengForeignKeyPolicyComboBox();
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
    public DamengFieldTextFiled getPrimaryKeyColumnControl() {
        try {
            DamengFieldTextFiled textField = new DamengFieldTextFiled();
            textField.addTextChangeListener((observable, oldValue, newValue) -> this.setPrimaryKeyColumns(textField.getSelectedColumns()));
            textField.setFlexWidth("100% - 12");
            Runnable func = () -> {
                textField.clear();
                String dbName = this.getPrimaryKeyDatabase();
                String tableName = this.getPrimaryKeyTable();
                //ShellDamengClient client = CacheHelper.get("dameng:dbClient");
                textField.setColumns(this.dbClient.selectColumns(new DamengSelectColumnParam(dbName, tableName)));
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

    /**
     * 获取更新策略组件
     *
     * @return 更新策略组件
     */
    public DamengForeignKeyPolicyComboBox getUpdatePolicyControl() {
        try {
            DamengForeignKeyPolicyComboBox comboBox = new DamengForeignKeyPolicyComboBox();
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
    public static DamengForeignKeyControl of(DamengForeignKey foreignKey) {
        DamengForeignKeyControl control = new DamengForeignKeyControl();
        control.copy(foreignKey);
        return control;
    }

    /**
     * 根据外键列表构建组件列表
     *
     * @param foreignKeys 外键列表
     * @return 组件列表
     */
    public static List<DamengForeignKeyControl> of(List<DamengForeignKey> foreignKeys) {
        List<DamengForeignKeyControl> controls = new ArrayList<>();
        for (DamengForeignKey foreignKey : foreignKeys) {
            controls.add(of(foreignKey));
        }
        return controls;
    }

    @Override
    public String getPrimaryKeyDatabase() {
        return super.getPrimaryKeyDatabase() == null ? this.schema : super.getPrimaryKeyDatabase();
    }
}
