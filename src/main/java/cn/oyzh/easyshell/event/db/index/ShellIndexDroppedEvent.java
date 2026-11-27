package cn.oyzh.easyshell.event.db.index;

import cn.oyzh.easyshell.db.index.ShellIndexTarget;
import cn.oyzh.event.Event;
import cn.oyzh.fx.db.DBDialect;
import cn.oyzh.fx.db.DBIndex;

/**
 * 索引已删除事件
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellIndexDroppedEvent extends Event<DBIndex> {

    /**
     * 索引设计目标
     */
    private ShellIndexTarget target;

    /**
     * 获取索引名称
     *
     * @return 索引名称
     */
    public String indexName() {
        return this.data() == null ? null : this.data().getName();
    }

    /**
     * 获取索引所属范围
     *
     * @return 范围名称
     */
    public String scope() {
        return this.target == null ? null : this.target.indexScope();
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
     * 获取索引设计目标
     *
     * @return 索引设计目标
     */
    public ShellIndexTarget getTarget() {
        return target;
    }

    /**
     * 设置索引设计目标
     *
     * @param target 索引设计目标
     */
    public void setTarget(ShellIndexTarget target) {
        this.target = target;
    }
}
