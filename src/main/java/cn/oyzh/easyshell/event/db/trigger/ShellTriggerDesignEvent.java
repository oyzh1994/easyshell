package cn.oyzh.easyshell.event.db.trigger;

import cn.oyzh.fx.db.DBTrigger;
import cn.oyzh.easyshell.db.trigger.ShellTriggerTarget;
import cn.oyzh.event.Event;
import cn.oyzh.fx.db.DBDialect;

/**
 * 触发器设计事件
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellTriggerDesignEvent extends Event<DBTrigger> {

    /**
     * 触发器设计目标
     */
    private ShellTriggerTarget target;

    /**
     * 获取触发器名称
     *
     * @return 触发器名称
     */
    public String triggerName() {
        return this.data() == null ? null : this.data().getName();
    }

    /**
     * 获取触发器所属范围
     *
     * @return 范围名称
     */
    public String scope() {
        return this.target == null ? null : this.target.triggerScope();
    }

    /**
     * 获取连接名称
     *
     * @return 连接名称
     */
    public String connectName() {
        return this.target == null ? null : this.target.connectName();
    }

    /**
     * 获取数据库方言
     *
     * @return 数据库方言
     */
    public DBDialect dialect() {
        return this.target == null ? null : this.target.dialect();
    }

    /**
     * 获取触发器设计目标
     *
     * @return 触发器设计目标
     */
    public ShellTriggerTarget getTarget() {
        return target;
    }

    /**
     * 设置触发器设计目标
     *
     * @param target 触发器设计目标
     */
    public void setTarget(ShellTriggerTarget target) {
        this.target = target;
    }
}
