package cn.oyzh.easyshell.tabs.mongo;

import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.fx.gui.tabs.RichTab;

/**
 * MongoDB 数据库基础标签页，提供数据库树节点等公共访问方法
 *
 * @author oyzh
 * @since 2026-06-29
 */
public abstract class ShellMongoBaseTab extends RichTab {

    /**
     * 获取数据库树节点
     *
     * @return 数据库树节点
     */
    public abstract ShellMongoDatabaseTreeItem dbItem() ;
}
