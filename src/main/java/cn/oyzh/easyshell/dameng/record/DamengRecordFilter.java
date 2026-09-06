package cn.oyzh.easyshell.dameng.record;

import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.condition.DamengCondition;
import cn.oyzh.easyshell.dameng.condition.DamengConditionUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBRecordFilter;
import cn.oyzh.fx.db.condition.ui.DBConditionComboBox;
import cn.oyzh.fx.db.ui.DBColumnComboBox;
import cn.oyzh.fx.plus.controls.box.FXHBox;
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
public class DamengRecordFilter extends DBRecordFilter {

    /**
     * 条件
     */
    private DamengCondition condition;

    /**
     * 字段
     */
    private DamengColumn column;

    /**
     * 字段列表
     */
    private List<DamengColumn> columns;

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
        return this.value = DamengConditionUtil.getNodeVal(this.valueBox.getChildren());
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
        List<Node> nodes = DamengConditionUtil.generateNode(this.column, this.condition);
        DamengConditionUtil.setNodeVal(nodes, this.value);
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
    public DBColumnComboBox getColumnControl() {
        DBColumnComboBox comboBox = new DBColumnComboBox(this.columns);
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> {
            this.column = (DamengColumn) newValue;
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
    public DBConditionComboBox getConditionControl() {
        DBConditionComboBox comboBox = new DBConditionComboBox(DBDialect.DAMENG);
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> {
            this.condition = (DamengCondition) newValue;
            this.updateValueControl();
        });
        comboBox.selectFirstIfNull(this.condition);
        TableViewUtil.selectRowOnMouseClicked(comboBox);
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
        return this.condition.wrapCondition(this.column(), this.value());
    }

    /**
     * 是否需要条件
     *
     * @return 结果
     */
    public boolean isRequireCondition() {
        return this.condition.isRequireCondition();
    }

    public DamengCondition getCondition() {
        return condition;
    }

    public void setCondition(DamengCondition condition) {
        this.condition = condition;
    }

    public DamengColumn getColumn() {
        return column;
    }

    public void setColumn(DamengColumn column) {
        this.column = column;
    }

    public List<DamengColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<DamengColumn> columns) {
        this.columns = columns;
    }
}
