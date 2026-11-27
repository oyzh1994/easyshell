package cn.oyzh.easyshell.event.db.type;

import cn.oyzh.easyshell.db.type.ShellTypeTarget;
import cn.oyzh.event.EventUtil;
import cn.oyzh.fx.db.DBTypeDefinition;

/**
 * 类型事件工具类
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellTypeEventUtil {

    /**
     * 类型设计事件
     *
     * @param target 类型设计目标
     * @param type   类型
     */
    public static void designType(ShellTypeTarget target, DBTypeDefinition type) {
        ShellTypeDesignEvent event = new ShellTypeDesignEvent();
        event.data(type);
        event.setTarget(target);
        EventUtil.post(event);
    }

    /**
     * 类型删除事件
     *
     * @param target 类型设计目标
     * @param type   类型
     */
    public static void dropType(ShellTypeTarget target, DBTypeDefinition type) {
        ShellTypeDroppedEvent event = new ShellTypeDroppedEvent();
        event.data(type);
        event.setTarget(target);
        EventUtil.postSync(event);
    }
}
