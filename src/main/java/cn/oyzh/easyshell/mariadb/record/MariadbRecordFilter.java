package cn.oyzh.easyshell.mariadb.record;

import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.condition.MariadbCondition;
import cn.oyzh.easyshell.mariadb.condition.MariadbConditionUtil;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBRecordFilter;
import cn.oyzh.fx.db.condition.ui.DBConditionComboBox;
import cn.oyzh.fx.db.ui.DBColumnComboBox;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.flex.FlexUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Node;
import javafx.scene.control.TextField;

import java.util.List;

/**
 * 记录过滤条件
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbRecordFilter extends DBRecordFilter {

    /**
     * 条件
     */
    private MariadbCondition condition;

    @Override
    public Object value() throws Exception {
        if (this.valueBox == null || this.valueBox.isChildEmpty() || this.valueBox.isDisable()) {
            return this.condition == null ? this.value : this.condition.getValue();
        }
        return this.value = MariadbConditionUtil.getNodeVal(this.valueBox.getChildren());
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

   @Override
    protected void updateValueControl() {
       if (this.condition != null && !this.condition.isRequireCondition()) {
           NodeUtil.disable(this.valueBox);
           return;
       }
        if (this.valueBox == null) {
            this.valueBox = new FXHBox();
            FlexUtil.flexWidth(this.valueBox, "100%");
        }
        NodeUtil.enable(this.valueBox);
        List<Node> nodes = MariadbConditionUtil.generateNode((MariadbColumn) this.column, this.condition);
        MariadbConditionUtil.setNodeVal(nodes, this.value);
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
            this.column = (MariadbColumn) newValue;
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
        DBConditionComboBox comboBox = new DBConditionComboBox(DBDialect.MARIADB);
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> {
            this.condition = (MariadbCondition) newValue;
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
     * @throws Exception 异常
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

    /**
     * 获取查询条件
     *
     * @return 查询条件
     */
    public MariadbCondition getCondition() {
        return condition;
    }

    /**
     * 设置查询条件
     *
     * @param condition 查询条件
     */
    public void setCondition(MariadbCondition condition) {
        this.condition = condition;
    }
}
