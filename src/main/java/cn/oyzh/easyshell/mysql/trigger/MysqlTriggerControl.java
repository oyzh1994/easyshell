package cn.oyzh.easyshell.mysql.trigger;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.mysql.table.ShellMysqlTriggerPolicyComboBox;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.fx.editor.incubator.control.EditorEnlargeTextFiled;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * MySQL触发器组件
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class MysqlTriggerControl extends MysqlTrigger {

    /**
     * 获取名称组件
     *
     * @return 名称组件
     */
    public ClearableTextField getNameControl() {
        ClearableTextField textField = new ClearableTextField();
        textField.setPromptText(I18nHelper.pleaseInputName());
        if (StringUtil.isEmpty(this.getName())) {
            this.setName(DBUtil.genTriggerName());
        }
        textField.addTextChangeListener((observable, oldValue, newValue) -> {
            this.setName(newValue);
        });
        textField.setText(this.getName());
        TableViewUtil.rowOnCtrlS(textField);
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    /**
     * 获取策略组件
     *
     * @return 策略组件
     */
    public ShellMysqlTriggerPolicyComboBox getPolicyControl() {
        ShellMysqlTriggerPolicyComboBox comboBox = new ShellMysqlTriggerPolicyComboBox();
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> {
            this.setPolicy(newValue);
        });
        comboBox.selectFirstIfNull(this.getPolicy());
        TableViewUtil.rowOnCtrlS(comboBox);
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        return comboBox;
    }

    /**
     * 获取定义组件
     *
     * @return 定义组件
     */
    public EditorEnlargeTextFiled getDefinitionControl() {
        EditorEnlargeTextFiled textField = new EditorEnlargeTextFiled();
        textField.setFormatType(EditorFormatType.SQL);
        textField.setPromptText(I18nHelper.pleaseInputContent());
        textField.addTextChangeListener((observable, oldValue, newValue) -> this.setDefinition(newValue));
        textField.setText(this.getDefinition());
        TableViewUtil.rowOnCtrlS(textField);
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    /**
     * 根据触发器构建组件
     *
     * @param trigger 触发器
     * @return 组件
     */
    public static MysqlTriggerControl of(MysqlTrigger trigger) {
        MysqlTriggerControl control = new MysqlTriggerControl();
        control.copy(trigger);
        return control;
    }

    /**
     * 根据触发器列表构建组件列表
     *
     * @param triggers 触发器列表
     * @return 组件列表
     */
    public static List<MysqlTriggerControl> of(List<MysqlTrigger> triggers) {
        List<MysqlTriggerControl> controls = new ArrayList<>();
        for (MysqlTrigger trigger : triggers) {
            controls.add(of(trigger));
        }
        return controls;
    }
}
