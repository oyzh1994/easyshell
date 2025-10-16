package cn.oyzh.easyshell.trees.connect;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellGroup;

import java.util.List;

/**
 * 连接管理
 *
 * @author oyzh
 * @since 2025/03/12
 */
public interface ShellConnectManager {

    /**
     * 分组连接
     *
     * @param group 分组信息
     */
    void addGroup(ShellGroup group);

    /**
     * 添加分组节点
     *
     * @param item 分组节点
     */
    void addGroupItem(ShellConnectGroupTreeItem item);

    /**
     * 获取分组节点
     *
     * @return 分组节点
     */
    List<ShellConnectGroupTreeItem> getGroupItems();

    /**
     * 获取所有分组节点
     *
     * @return 分组节点
     */
    List<ShellConnectGroupTreeItem> getAllGroupItems();

    /**
     * 添加连接
     *
     * @param shellConnect 连接信息
     */
    void addConnect(ShellConnect shellConnect);

    /**
     * 添加连接节点
     *
     * @param item 连接节点
     */
    void addConnectItem(ShellConnectTreeItem item);

    /**
     * 添加多个连接节点
     *
     * @param items 连接节点列表
     */
    void addConnectItems(List<ShellConnectTreeItem> items);

    /**
     * 删除连接节点
     *
     * @param item 连接节点
     * @return 结果
     */
    boolean delConnectItem(ShellConnectTreeItem item);

    /**
     * 获取连接节点
     *
     * @return 连接节点
     */
    List<ShellConnectTreeItem> getConnectItems();

    /**
     * 获取所有连接节点
     *
     * @return 连接节点
     */
    List<ShellConnectTreeItem> getAllConnectItems();

    /**
     * 获取已连接的连接节点
     *
     * @return 已连接的连接节点
     */
    default List<ShellConnectTreeItem> getConnectedItems() {
        return this.getConnectItems();
    }
}
