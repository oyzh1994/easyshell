package cn.oyzh.easyshell.dameng.trigger;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.fx.db.DBObject;
import cn.oyzh.fx.db.DBTrigger;

/**
 * 达梦触发器
 *
 * @author oyzh
 * @since 2024/07/10
 */
public class DamengTrigger extends DBObject implements DBTrigger, ObjectCopier<DamengTrigger> {

    /**
     * 名称
     */
    private String name;

    /**
     * 模式
     */
    private String schema;

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
     * 创建定义
     */
    private String createDefinition;

    /**
     * 获取原始名称
     *
     * @return 原始名称
     */
    public String originalName() {
        return (String) this.getOriginalData("name");
    }

    @Override
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

    /**
     * 设置策略
     *
     * @param policy 策略
     */
    public void setPolicy(String policy) {
        this.policy = policy;
        super.putOriginalData("policy", policy);
    }
    //
    // public DamengTriggerPolicyComboBox getPolicyControl() {
    //     DamengTriggerPolicyComboBox comboBox = new DamengTriggerPolicyComboBox();
    //     comboBox.selectedItemChanged((observable, oldValue, newValue) -> {
    //         this.setPolicy(newValue);
    //     });
    //     comboBox.selectFirstIfNull(this.policy);
    //     TableViewUtil.rowOnCtrlS(comboBox);
    //     TableViewUtil.selectRowOnMouseClicked(comboBox);
    //     return comboBox;
    // }

    /**
     * 设置定义
     *
     * @param definition 定义
     */
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

    /**
     * 根据触发时机与操作设置策略
     *
     * @param timing       触发时机
     * @param manipulation 触发操作
     */
    public void setPolicy(String timing, String manipulation) {
        this.setPolicy(timing.toUpperCase() + " " + manipulation.toUpperCase());
    }

    /**
     * 设置表名
     *
     * @param tableName 表名
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
        super.putOriginalData("tableName", tableName);
    }

    @Override
    public void copy(DamengTrigger t1) {
        if (t1 != null) {
            this.name = t1.name;
            this.policy = t1.policy;
            this.definition = t1.definition;
            this.createDefinition = t1.createDefinition;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * 获取模式
     *
     * @return 模式
     */
    public String getSchema() {
        return schema;
    }

    /**
     * 设置模式
     *
     * @param schema 模式
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * 获取策略
     *
     * @return 策略
     */
    public String getPolicy() {
        return policy;
    }

    /**
     * 获取定义
     *
     * @return 定义
     */
    public String getDefinition() {
        return definition;
    }

    /**
     * 获取表名
     *
     * @return 表名
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * 获取创建定义
     *
     * @return 创建定义
     */
    public String getCreateDefinition() {
        return createDefinition;
    }

    /**
     * 设置创建定义
     *
     * @param createDefinition 创建定义
     */
    public void setCreateDefinition(String createDefinition) {
        this.createDefinition = createDefinition;
    }
}
