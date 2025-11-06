package cn.oyzh.easyshell.mysql.trigger;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.mysql.table.MysqlTriggerPolicyComboBox;
import cn.oyzh.easyshell.mysql.trigger.MysqlTrigger;
import cn.oyzh.easyshell.util.mysql.DBUtil;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.gui.text.field.EnlargeTextFiled;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/14
 */
public class MysqlTriggerControl extends MysqlTrigger {

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

    public MysqlTriggerPolicyComboBox getPolicyControl() {
        MysqlTriggerPolicyComboBox comboBox = new MysqlTriggerPolicyComboBox();
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> {
            this.setPolicy(newValue);
        });
        comboBox.selectFirstIfNull(this.getPolicy());
        TableViewUtil.rowOnCtrlS(comboBox);
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        return comboBox;
    }

    public EnlargeTextFiled getDefinitionControl() {
        EnlargeTextFiled textField = new EnlargeTextFiled();
        textField.setPromptText(I18nHelper.pleaseInputContent());
        textField.addTextChangeListener((observable, oldValue, newValue) -> this.setDefinition(newValue));
        textField.setText(this.getDefinition());
        TableViewUtil.rowOnCtrlS(textField);
        TableViewUtil.selectRowOnMouseClicked(textField);
        return textField;
    }

    public static MysqlTriggerControl of(MysqlTrigger trigger) {
        MysqlTriggerControl control = new MysqlTriggerControl();
        control.copy(trigger);
        return control;
    }

    public static List<MysqlTriggerControl> of(List<MysqlTrigger> triggers) {
        List<MysqlTriggerControl> controls = new ArrayList<>();
        for (MysqlTrigger trigger : triggers) {
            controls.add(of(trigger));
        }
        return controls;
    }
}
