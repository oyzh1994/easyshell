package cn.oyzh.easyshell.event.zk;

import cn.oyzh.easyshell.util.zk.ShellZKClientActionArgument;
import cn.oyzh.event.EventUtil;

import java.util.List;

/**
 * @author oyzh
 * @since 2025-02-14
 */

public class ShellZKEventUtil {

    // /**
    //  * 节点acl修改事件
    //  *
    //  * @param zkConnect zk连接
    //  */
    // public static void zkNodeACLUpdated(ShellConnect zkConnect, String nodePath) {
    //     ShellZKNodeACLUpdatedEvent event = new ShellZKNodeACLUpdatedEvent();
    //     event.data(zkConnect);
    //     event.setNodePath(nodePath);
    //     EventUtil.post(event);
    // }

    /**
     * 客户端操作
     */
    public static void zkClientAction(String connectName, String action) {
        ShellZKClientActionEvent event = new ShellZKClientActionEvent();
        event.data(connectName);
        event.setAction(action);
        EventUtil.postAsync(event);
    }

    /**
     * 客户端操作
     */
    public static void zkClientAction(String connectName, String action, List<ShellZKClientActionArgument> arguments) {
        ShellZKClientActionEvent event = new ShellZKClientActionEvent();
        event.data(connectName);
        event.setAction(action);
        event.arguments(arguments);
        EventUtil.postAsync(event);
    }

}
