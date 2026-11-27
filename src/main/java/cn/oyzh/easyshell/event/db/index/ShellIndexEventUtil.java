package cn.oyzh.easyshell.event.db.index;

import cn.oyzh.easyshell.db.index.ShellIndexTarget;
import cn.oyzh.event.EventUtil;
import cn.oyzh.fx.db.DBIndex;

/**
 * 索引事件工具类
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellIndexEventUtil {

    /**
     * 索引设计事件
     *
     * @param target 索引设计目标
     * @param index  索引
     */
    public static void designIndex(ShellIndexTarget target, DBIndex index) {
        ShellIndexDesignEvent event = new ShellIndexDesignEvent();
        event.data(index);
        event.setTarget(target);
        EventUtil.post(event);
    }

    /**
     * 索引删除事件
     *
     * @param target 索引设计目标
     * @param index  索引
     */
    public static void dropIndex(ShellIndexTarget target, DBIndex index) {
        ShellIndexDroppedEvent event = new ShellIndexDroppedEvent();
        event.data(index);
        event.setTarget(target);
        EventUtil.postSync(event);
    }
}
