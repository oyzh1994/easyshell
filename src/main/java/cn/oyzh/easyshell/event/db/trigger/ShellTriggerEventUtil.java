package cn.oyzh.easyshell.event.db.trigger;

import cn.oyzh.fx.db.DBTrigger;
import cn.oyzh.easyshell.db.trigger.ShellTriggerTarget;
import cn.oyzh.event.EventUtil;

/**
 * 触发器事件工具类
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellTriggerEventUtil {

    /**
     * 触发器设计事件
     *
     * @param target  触发器设计目标
     * @param trigger 触发器
     */
    public static void designTrigger(ShellTriggerTarget target, DBTrigger trigger) {
        ShellTriggerDesignEvent event = new ShellTriggerDesignEvent();
        event.data(trigger);
        event.setTarget(target);
        EventUtil.post(event);
    }

    /**
     * 触发器删除事件
     *
     * @param target  触发器设计目标
     * @param trigger 触发器
     */
    public static void dropTrigger(ShellTriggerTarget target, DBTrigger trigger) {
        ShellTriggerDroppedEvent event = new ShellTriggerDroppedEvent();
        event.data(trigger);
        event.setTarget(target);
        EventUtil.postSync(event);
    }
}
