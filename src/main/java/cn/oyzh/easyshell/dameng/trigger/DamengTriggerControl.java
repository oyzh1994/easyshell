package cn.oyzh.easyshell.dameng.trigger;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.trigger.DamengTrigger;
import cn.oyzh.easyshell.fx.dameng.table.DamengTriggerPolicyComboBox;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.editor.incubator.EditorFormatType;
import cn.oyzh.fx.editor.incubator.control.EditorEnlargeTextFiled;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.tableview.TableViewUtil;
import cn.oyzh.i18n.I18nHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/09/14
 */
public class DamengTriggerControl extends DamengTrigger {

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

    public DamengTriggerPolicyComboBox getPolicyControl() {
        DamengTriggerPolicyComboBox comboBox = new DamengTriggerPolicyComboBox();
        comboBox.selectedItemChanged((observable, oldValue, newValue) -> {
            this.setPolicy(newValue);
        });
        comboBox.selectFirstIfNull(this.getPolicy());
        TableViewUtil.rowOnCtrlS(comboBox);
        TableViewUtil.selectRowOnMouseClicked(comboBox);
        return comboBox;
    }

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

    public static DamengTriggerControl of(DamengTrigger trigger) {
        DamengTriggerControl control = new DamengTriggerControl();
        control.copy(trigger);
        return control;
    }

    public static List<DamengTriggerControl> of(List<DamengTrigger> triggers) {
        List<DamengTriggerControl> controls = new ArrayList<>();
        for (DamengTrigger trigger : triggers) {
            controls.add(of(trigger));
        }
        return controls;
    }
}
