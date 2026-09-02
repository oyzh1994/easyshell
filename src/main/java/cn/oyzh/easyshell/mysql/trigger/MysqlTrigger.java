package cn.oyzh.easyshell.mysql.trigger;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.fx.db.DBObjectStatus;
import cn.oyzh.fx.db.DBTrigger;

/**
 * db表触发器
 *
 * @author oyzh
 * @since 2024/07/10
 */
public class MysqlTrigger extends DBObjectStatus implements DBTrigger, ObjectCopier<MysqlTrigger> {

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

    @Override
    public void setName(String name) {
        this.name = name;
        super.putOriginalData("name", name);
    }

    public void setPolicy(String policy) {
        this.policy = policy;
        super.putOriginalData("policy", policy);
    }

    public void setDefinition(String definition) {
        this.definition = definition;
        super.putOriginalData("definition", definition);
    }

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

    @Override
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
