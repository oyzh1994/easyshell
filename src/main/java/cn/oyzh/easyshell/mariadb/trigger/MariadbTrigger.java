package cn.oyzh.easyshell.mariadb.trigger;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.fx.db.DBObject;
import cn.oyzh.fx.db.DBTrigger;

/**
 * MariaDB触发器
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class MariadbTrigger extends DBObject implements DBTrigger, ObjectCopier<MariadbTrigger> {

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

    /**
     * 设置策略
     *
     * @param policy 策略
     */
    public void setPolicy(String policy) {
        this.policy = policy;
        super.putOriginalData("policy", policy);
    }

    /**
     * 设置定义
     *
     * @param definition 定义
     */
    public void setDefinition(String definition) {
        this.definition = definition;
        super.putOriginalData("definition", definition);
    }

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
    public void copy(MariadbTrigger t1) {
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
