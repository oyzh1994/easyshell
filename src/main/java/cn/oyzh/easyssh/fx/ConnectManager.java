package cn.oyzh.easyssh.fx;

import cn.hutool.core.collection.CollUtil;
import cn.oyzh.easyssh.domain.SSHInfo;
import lombok.NonNull;

import java.util.List;

/**
 * 连接管理
 *
 * @author oyzh
 * @since 2023/5/12
 */
public interface ConnectManager {

    /**
     * 添加连接
     *
     * @param sshInfo 连接信息
     */
    void addConnect(@NonNull SSHInfo sshInfo);

    /**
     * 删除多个连接
     *
     * @param sshInfos 连接列表
     */
    default void addConnects(List<SSHInfo> sshInfos) {
        if (CollUtil.isNotEmpty(sshInfos)) {
            for (SSHInfo sshInfo : sshInfos) {
                this.addConnect(sshInfo);
            }
        }
    }

    /**
     * 添加连接键
     *
     * @param item 连接键
     */
    void addConnectItem(@NonNull SSHConnectTreeItem item);

    /**
     * 添加多个连接键
     *
     * @param items 连接键列表
     */
    void addConnectItems(@NonNull List<SSHConnectTreeItem> items);

    /**
     * 删除连接键
     *
     * @param item 连接键
     * @return 结果
     */
    boolean delConnectItem(@NonNull SSHConnectTreeItem item);

    /**
     * 获取连接键
     *
     * @return 连接键
     */
    List<SSHConnectTreeItem> getConnectItems();

    /**
     * 获取已连接的连接键
     *
     * @return 已连接的连接键
     */
    List<SSHConnectTreeItem> getConnectedItems();

}
