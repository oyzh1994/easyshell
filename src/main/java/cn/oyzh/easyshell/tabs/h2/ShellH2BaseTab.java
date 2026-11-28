package cn.oyzh.easyshell.tabs.h2;

import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.easyshell.trees.h2.root.ShellH2RootTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;

/**
 * H2 数据库操作标签页基类
 *
 * @author oyzh
 * @since 2026-11-02
 */
public abstract class ShellH2BaseTab extends RichTab {

    /**
     * 获取数据库树节点
     *
     * @return 数据库树节点
     */
    public abstract ShellH2RootTreeItem dbItem();

    /**
     * 获取H2客户端
     *
     * @return H2客户端
     */
    public ShellH2Client client() {
        return this.dbItem() == null ? null : this.dbItem().client();
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        return this.dbItem() == null ? null : this.dbItem().dbName();
    }

    /**
     * 获取连接名称
     *
     * @return 连接名称
     */
    public String connectName() {
        return this.dbItem() == null ? null : this.dbItem().infoName();
    }
}
