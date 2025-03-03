package cn.oyzh.easyssh.trees.connect;

import cn.oyzh.easyssh.domain.SSHConnect;
import lombok.NonNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 连接管理
 *
 * @author oyzh
 * @since 2023/5/12
 */
public interface SSHConnectManager {

    /**
     * 添加连接
     *
     * @param sshConnect 连接信息
     */
    void addConnect(@NonNull SSHConnect sshConnect);

    /**
     * 添加连接节点
     *
     * @param item 连接节点
     */
    void addConnectItem(@NonNull SSHConnectTreeItem item);

    /**
     * 添加多个连接节点
     *
     * @param items 连接节点列表
     */
    void addConnectItems(@NonNull List<SSHConnectTreeItem> items);

    /**
     * 删除连接节点
     *
     * @param item 连接节点
     * @return 结果
     */
    boolean delConnectItem(@NonNull SSHConnectTreeItem item);

    /**
     * 获取连接节点
     *
     * @return 连接节点
     */
    List<SSHConnectTreeItem> getConnectItems();

    /**
     * 获取已连接的连接节点
     *
     * @return 已连接的连接节点
     */
    default List<SSHConnectTreeItem> getConnectedItems() {
        return this.getConnectItems().parallelStream().filter(SSHConnectTreeItem::isConnected).collect(Collectors.toList());
    }
}
