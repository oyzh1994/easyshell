package cn.oyzh.easyshell.db.trigger;

import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBTrigger;

import java.util.List;

/**
 * 触发器设计目标（模式节点或数据库节点）
 * <p>
 * 各数据库只需在模式/数据库树节点上实现本接口，即可复用触发器树节点与触发器设计页面。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public interface ShellTriggerTarget {

    /**
     * 获取数据库方言
     *
     * @return 数据库方言
     */
    DBDialect dialect();

    /**
     * 获取连接名称
     *
     * @return 连接名称
     */
    String connectName();

    /**
     * 获取触发器所属范围（模式名称或数据库名称）
     *
     * @return 范围名称
     */
    String triggerScope();

    /**
     * 查询触发器列表（包含所属表名称）
     *
     * @return 触发器列表
     */
    List<? extends DBTrigger> selectTriggers();

    /**
     * 查询触发器定义
     *
     * @param triggerName 触发器名称
     * @return 触发器定义
     */
    String selectTriggerDefinition(String triggerName);

    /**
     * 执行触发器相关SQL
     *
     * @param sqlList SQL列表
     */
    void executeTriggerSql(List<String> sqlList);

    /**
     * 判断触发器是否存在
     *
     * @param triggerName 触发器名称
     * @return 是否存在
     */
    boolean existTrigger(String triggerName);

    /**
     * 删除触发器
     *
     * @param triggerName 触发器名称
     * @param tableName   所属表名称
     */
    void dropTrigger(String triggerName, String tableName);

    /**
     * 创建一个新的触发器对象
     *
     * @return 触发器对象
     */
    DBTrigger newTrigger();

    /**
     * 刷新触发器树节点
     */
    void reloadTriggers();
}
