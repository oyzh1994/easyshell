package cn.oyzh.easyshell.mysql.trigger;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.easyshell.mysql.DBObjectStatus;

/**
 * db表触发器
 *
 * @author oyzh
 * @since 2024/07/10
 */
public class MysqlTrigger extends DBObjectStatus implements ObjectCopier<MysqlTrigger> {

    /**
     * 名称
     */
    private String name;

    /**
     * 策略
     */
    private String policy;

    /**
     * 定义
     */
    private String definition;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 原始名称
     *
     * @return 结果
     */
    public String originalName() {
        return (String) this.getOriginalData("name");
    }

    public void setName(String name) {
        this.name = name;
        super.putOriginalData("name", name);
    }

    // public ClearableTextField getNameControl() {
    //     ClearableTextField textField = new ClearableTextField();
    //     textField.setPromptText(I18nHelper.pleaseInputName());
    //     textField.addTextChangeListener((observable, oldValue, newValue) -> {
    //         this.setName(newValue);
    //     });
    //     if (this.name != null) {
    //         textField.setText(this.name);
    //     }
    //     TableViewUtil.rowOnCtrlS(textField);
    //     TableViewUtil.selectRowOnMouseClicked(textField);
    //     return textField;
    // }

    public void setPolicy(String policy) {
        this.policy = policy;
        super.putOriginalData("policy", policy);
    }
    //
    // public MysqlTriggerPolicyComboBox getPolicyControl() {
    //     MysqlTriggerPolicyComboBox comboBox = new MysqlTriggerPolicyComboBox();
    //     comboBox.selectedItemChanged((observable, oldValue, newValue) -> {
    //         this.setPolicy(newValue);
    //     });
    //     comboBox.selectFirstIfNull(this.policy);
    //     TableViewUtil.rowOnCtrlS(comboBox);
    //     TableViewUtil.selectRowOnMouseClicked(comboBox);
    //     return comboBox;
    // }

    public void setDefinition(String definition) {
        this.definition = definition;
        super.putOriginalData("definition", definition);
    }

    // public EnlargeTextFiled getDefinitionControl() {
    //     EnlargeTextFiled textField = new EnlargeTextFiled();
    //     textField.setPromptText(I18nHelper.pleaseInputContent());
    //     textField.addTextChangeListener((observable, oldValue, newValue) -> {
    //         // if (!StrUtil.equalsIgnoreCase(newValue, this.definition)) {
    //         //     this.definition = newValue;
    //         //     this.setChanged(true);
    //         // }
    //         this.setDefinition(newValue);
    //     });
    //     if (this.definition != null) {
    //         textField.setText(this.definition);
    //     }
    //     TableViewUtil.rowOnCtrlS(textField);
    //     TableViewUtil.selectRowOnMouseClicked(textField);
    //     return textField;
    // }

    public void setPolicy(String timing, String manipulation) {
        this.setPolicy(timing.toUpperCase() + " " + manipulation.toUpperCase());
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
        super.putOriginalData("tableName", tableName);
    }

    @Override
    public void copy(MysqlTrigger t1) {
        if (t1 != null) {
            this.name = t1.name;
            this.policy = t1.policy;
            this.definition = t1.definition;
        }
    }

    public boolean isInvalid() {
        return false;
    }

    public String getName() {
        return name;
    }

    public String getPolicy() {
        return policy;
    }

    public String getDefinition() {
        return definition;
    }

    public String getTableName() {
        return tableName;
    }
}
