package cn.oyzh.easyshell.event.db.type;

import cn.oyzh.easyshell.db.type.ShellTypeTarget;
import cn.oyzh.event.Event;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBTypeDefinition;

/**
 * 类型已删除事件
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellTypeDroppedEvent extends Event<DBTypeDefinition> {

    /**
     * 类型设计目标
     */
    private ShellTypeTarget target;

    /**
     * 获取类型名称
     *
     * @return 类型名称
     */
    public String typeName() {
        return this.data() == null ? null : this.data().getName();
    }

    /**
     * 获取类型所属范围
     *
     * @return 范围名称
     */
    public String scope() {
        return this.target == null ? null : this.target.typeScope();
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
     * 获取类型设计目标
     *
     * @return 类型设计目标
     */
    public ShellTypeTarget getTarget() {
        return target;
    }

    /**
     * 设置类型设计目标
     *
     * @param target 类型设计目标
     */
    public void setTarget(ShellTypeTarget target) {
        this.target = target;
    }
}
