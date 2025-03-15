package cn.oyzh.easyshell.trees.connect;

import cn.oyzh.easyshell.domain.ShellConnect;
import lombok.NonNull;

import java.util.List;

/**
 * 连接管理
 *
 * @author oyzh
 * @since 2023/5/12
 */
public interface ShellConnectManager {

    /**
     * 添加连接
     *
     * @param shellConnect 连接信息
     */
    void addConnect(@NonNull ShellConnect shellConnect);

    /**
     * 添加连接节点
     *
     * @param item 连接节点
     */
    void addConnectItem(@NonNull ShellConnectTreeItem item);

    /**
     * 添加多个连接节点
     *
     * @param items 连接节点列表
     */
    void addConnectItems(@NonNull List<ShellConnectTreeItem> items);

    /**
     * 删除连接节点
     *
     * @param item 连接节点
     * @return 结果
     */
    boolean delConnectItem(@NonNull ShellConnectTreeItem item);

    /**
     * 获取连接节点
     *
     * @return 连接节点
     */
    List<ShellConnectTreeItem> getConnectItems();

    /**
     * 获取已连接的连接节点
     *
     * @return 已连接的连接节点
     */
    default List<ShellConnectTreeItem> getConnectedItems() {
        return this.getConnectItems();
    }
}
