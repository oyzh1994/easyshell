package cn.oyzh.easyshell.mysql.record;

import cn.oyzh.easyshell.mysql.condition.MysqlCondition;
import cn.oyzh.easyshell.mysql.condition.MysqlConditionUtil;
import cn.oyzh.easyshell.fx.mysql.table.DBJoinSymbolComboBox;
import cn.oyzh.easyshell.fx.mysql.table.MysqlColumnComboBox;
import cn.oyzh.easyshell.fx.mysql.table.MysqlConditionComboBox;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.flex.FlexUtil;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Node;
import javafx.scene.control.TextField;

import java.util.List;

/**
 * 记录过滤条件
 *
 * @author oyzh
 * @since 2024/06/26
 */
public class MysqlRecordFilter {

    /**
     * 值
     */
    private Object value;

    /**
     * 是否已启用
     */
    private boolean enabled = true;

    /**
     * 连接符号
     */
    private String joinSymbol;

    /**
     * 条件
     */
    private MysqlCondition condition;

    /**
     * 字段
     */
    private MysqlColumn column;

    /**
     * 字段列表
     */
    private List<MysqlColumn> columns;

    /**
     * 值组件
     */
    private FXHBox valueBox;

    /**
     * 获取值
     *
     * @return 值
     */
    public Object value() throws Exception {
        if (this.valueBox == null || this.valueBox.isChildEmpty()) {
            return this.value;
        }
        return this.value = MysqlConditionUtil.getNodeVal(this.valueBox.getChildren());
    }

    /**
     * 获取值组件
     *
     * @return 值组件
     */
    public Node getValueControl() {
        this.updateValueControl();
        return this.valueBox;
    }

    /**
     * 更新值组件
     */
    private void updateValueControl() {
        if (this.valueBox == null) {
            this.valueBox = new FXHBox();
            FlexUtil.flexWidth(this.valueBox, "100%");
        }
        List<Node> nodes = MysqlConditionUtil.generateNode(this.column, this.condition);
        MysqlConditionUtil.setNodeVal(nodes, this.value);
        if (nodes.size() == 1) {
            FlexUtil.flexWidth(nodes.getFirst(), "100% - 10");
            FlexUtil.flexHeight(nodes.getFirst(), "100%");
        } else if (nodes.size() == 2) {
            FlexUtil.flexWidth(nodes.get(0), "50% - 10");
            FlexUtil.flexHeight(nodes.get(0), "100%");
            FlexUtil.flexWidth(nodes.get(1), "50% - 10");
            FlexUtil.flexHeight(nodes.get(1), "100%");
        }
        for (Node node : nodes) {
            if (node instanceof TextField textField) {
                textField.setPromptText(I18nHelper.pleaseInputContent());
            }
            TableViewUtil.selectRowOnMouseClicked(node);
        }
        this.valueBox.setChild(nodes);
    }

    /**
     * 获取字段组件
     *
     * @return 字段组件
     */
    public MysqlColumnComboBox getColumnControl() {
        MysqlColumnComboBox comboBox = new MysqlColumnComboBox(this.columns);
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> {
            this.column = newValue;
            this.updateValueControl();
        });
        comboBox.selectFirstIfNull(this.column);
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        return comboBox;
    }

    /**
     * 获取条件组件
     *
     * @return 条件组件
     */
    public MysqlConditionComboBox getConditionControl() {
        MysqlConditionComboBox comboBox = new MysqlConditionComboBox();
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> {
            this.condition = newValue;
            this.updateValueControl();
        });
        comboBox.selectFirstIfNull(this.condition);
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        return comboBox;
    }

    /**
     * 获取启用组件
     *
     * @return 启用组件
     */
    public FXCheckBox getEnabledControl() {
        FXCheckBox checkBox = new FXCheckBox();
        checkBox.setSelected(this.enabled);
        checkBox.selectedChanged((observable, oldValue, newValue) -> this.enabled = newValue);
        TableViewUtil.selectRowOnMouseClicked(checkBox);
        return checkBox;
    }

    /**
     * 获取连接符组件
     *
     * @return 连接符组件
     */
    public DBJoinSymbolComboBox getJoinSymbolControl() {
        DBJoinSymbolComboBox comboBox = new DBJoinSymbolComboBox();
        comboBox.selectFirstIfNull(this.joinSymbol);
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> this.joinSymbol = newValue);
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        this.setJoinSymbol(comboBox.getSelectedItem());
        return comboBox;
    }

    /**
     * 获取字段名
     *
     * @return 字段名
     */
    public String column() {
        return this.column.getName();
    }

    /**
     * 获取条件
     *
     * @return 条件
     */
    public String condition() throws Exception {
        return this.condition.wrapCondition(this.value());
    }

    /**
     * 是否需要条件
     *
     * @return 结果
     */
    public boolean isRequireCondition() {
        return this.condition.isRequireCondition();
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getJoinSymbol() {
        return joinSymbol;
    }

    public void setJoinSymbol(String joinSymbol) {
        this.joinSymbol = joinSymbol;
    }

    public MysqlCondition getCondition() {
        return condition;
    }

    public void setCondition(MysqlCondition condition) {
        this.condition = condition;
    }

    public MysqlColumn getColumn() {
        return column;
    }

    public void setColumn(MysqlColumn column) {
        this.column = column;
    }

    public List<MysqlColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<MysqlColumn> columns) {
        this.columns = columns;
    }
}
