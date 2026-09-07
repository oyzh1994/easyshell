package cn.oyzh.easyshell.tabs.mongo;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.mongo.bucket.ShellMongoBucketOpenEvent;
import cn.oyzh.easyshell.event.mongo.collection.ShellMongoCollectionOpenEvent;
import cn.oyzh.easyshell.event.mongo.collection.ShellMongoCollectionRenamedEvent;
import cn.oyzh.easyshell.event.mongo.database.ShellMongoDatabaseClosedEvent;
import cn.oyzh.easyshell.event.mongo.function.ShellMongoFunctionDesignEvent;
import cn.oyzh.easyshell.event.mongo.function.ShellMongoFunctionDroppedEvent;
import cn.oyzh.easyshell.event.mongo.function.ShellMongoFunctionRenamedEvent;
import cn.oyzh.easyshell.event.mongo.query.ShellMongoQueryAddEvent;
import cn.oyzh.easyshell.event.mongo.query.ShellMongoQueryOpenEvent;
import cn.oyzh.easyshell.event.mongo.query.ShellMongoQueryRenamedEvent;
import cn.oyzh.easyshell.event.mongo.terminal.ShellMongoTerminalCloseEvent;
import cn.oyzh.easyshell.event.mongo.terminal.ShellMongoTerminalOpenEvent;
import cn.oyzh.easyshell.event.mongo.user.ShellMongoUserDeletedEvent;
import cn.oyzh.easyshell.event.mongo.user.ShellMongoUserViewEvent;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mongo.user.MongoUser;
import cn.oyzh.easyshell.tabs.mongo.bucket.ShellMongoBucketRecordTab;
import cn.oyzh.easyshell.tabs.mongo.collection.ShellMongoCollectionRecordTab;
import cn.oyzh.easyshell.tabs.mongo.function.ShellMongoFunctionDesignTab;
import cn.oyzh.easyshell.tabs.mongo.home.ShellMongoHomeTab;
import cn.oyzh.easyshell.tabs.mongo.query.ShellMongoQueryMainTab;
import cn.oyzh.easyshell.tabs.mongo.terminal.ShellMongoTerminalTab;
import cn.oyzh.easyshell.tabs.mongo.user.ShellMongoUserViewTab;
import cn.oyzh.easyshell.trees.mongo.database.ShellMongoDatabaseTreeItem;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.tabs.RichTabPane;
import cn.oyzh.fx.plus.event.FXEventListener;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Tab;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * mongodb切换面板
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellMongoTabPane extends RichTabPane implements FXEventListener {

    private SimpleObjectProperty<ShellMongoClient> clientProperty;

    public void setClient(ShellMongoClient client) {
        this.clientProperty().set(client);
    }

    public ShellMongoClient getClient() {
        return this.clientProperty == null ? null : this.clientProperty.get();
    }

    public SimpleObjectProperty<ShellMongoClient> clientProperty() {
        if (this.clientProperty == null) {
            this.clientProperty = new SimpleObjectProperty<>();
        }
        return this.clientProperty;
    }

    @Override
    public void initNode() {
        super.initNode();
        this.initHomeTab();
        // 监听tab
        this.getTabs().addListener((ListChangeListener<? super Tab>) (c) -> {
            while (c.next()) {
                if (c.wasAdded() || c.wasRemoved()) {
                    TaskManager.startDelay(this::flushHomeTab, 100);
                }
            }
        });
    }

    /**
     * 刷新主页标签
     */
    private void flushHomeTab() {
        if (this.tabsEmpty()) {
            this.initHomeTab();
        } else if (this.tabsSize() > 1) {
            this.closeHomeTab();
        }
    }

    /**
     * 获取主页tab
     *
     * @return 主页tab
     */
    public ShellMongoHomeTab getHomeTab() {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMongoHomeTab homeTab) {
                return homeTab;
            }
        }
        return null;
    }

    /**
     * 初始化主页tab
     */
    public void initHomeTab() {
        if (this.getHomeTab() == null) {
            super.addTab(new ShellMongoHomeTab());
        }
    }

    /**
     * 关闭主页tab
     */
    public void closeHomeTab() {
        ShellMongoHomeTab homeTab = this.getHomeTab();
        if (homeTab != null) {
            super.removeTab(homeTab);
        }
    }

    private ShellMongoCollectionRecordTab getMongoCollectionRecordTab(ShellMongoDatabaseTreeItem dbItem, String tableName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMongoCollectionRecordTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(tableName, tab1.collectionName())) {
                return tab1;
            }
        }
        return null;
    }

    /**
     * 表打开事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onMongoCollectionOpen(ShellMongoCollectionOpenEvent event) {
        ShellMongoCollectionRecordTab tab = this.getMongoCollectionRecordTab(event.getDbItem(), event.collectionName());
        if (tab == null) {
            tab = new ShellMongoCollectionRecordTab();
            // 选中节点
            tab.init(event.data());
            super.addTab(tab);
        }
        // 初始化节点
        this.select(tab);
    }

    private ShellMongoBucketRecordTab getMongoBucketRecordTab(ShellMongoDatabaseTreeItem dbItem, String bucketName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMongoBucketRecordTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(bucketName, tab1.bucketName())) {
                return tab1;
            }
        }
        return null;
    }

    /**
     * 桶打开事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onMongoBucketOpen(ShellMongoBucketOpenEvent event) {
        ShellMongoBucketRecordTab tab = this.getMongoBucketRecordTab(event.getDbItem(), event.bucketName());
        if (tab == null) {
            tab = new ShellMongoBucketRecordTab();
            // 选中节点
            tab.init(event.data());
            super.addTab(tab);
        }
        // 初始化节点
        this.select(tab);
    }

    private ShellMongoQueryMainTab getMongoQueryMainTab(String queryId) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMongoQueryMainTab tab1 && StringUtil.equals(tab1.queryId(), queryId)) {
                return tab1;
            }
        }
        return null;
    }

    /**
     * 查询新增事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onMongoQueryAdd(ShellMongoQueryAddEvent event) {
        try {
            ShellMongoQueryMainTab tab = new ShellMongoQueryMainTab();
            ShellQuery query = new ShellQuery();
            tab.init(query, event.data());
            this.addTab(tab);
            this.select(tab);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 查询打开事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onMongoQueryOpen(ShellMongoQueryOpenEvent event) {
        try {
            ShellMongoQueryMainTab tab = this.getMongoQueryMainTab(event.queryId());
            if (tab == null) {
                tab = new ShellMongoQueryMainTab();
                tab.init(event.data(), event.getDbItem());
                this.addTab(tab);
            }
            this.select(tab);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获取终端tab
     *
     * @param client mongodb客户端
     * @param dbName 数据库名称
     * @return 终端tab
     */
    private ShellMongoTerminalTab getTerminalTab(ShellMongoClient client, String dbName) {
        if (client != null) {
            for (Tab tab : this.getTabs()) {
                if (tab instanceof ShellMongoTerminalTab tab1 && tab1.client() == client && Objects.equals(tab1.dbName(), dbName)) {
                    return tab1;
                }
            }
        }
        return null;
    }

    /**
     * 终端打开事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onMongoTerminalOpen(ShellMongoTerminalOpenEvent event) {
        ShellMongoTerminalTab terminalTab = this.getTerminalTab(event.data(), event.getDbName());
        if (terminalTab == null) {
            terminalTab = new ShellMongoTerminalTab();
            terminalTab.init(event.data(), event.getDbName());
            super.addTab(terminalTab);
        } else {
            terminalTab.flushGraphic();
        }
        if (!terminalTab.isSelected()) {
            this.select(terminalTab);
        }
    }

    /**
     * 终端关闭事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onMongoTerminalClose(ShellMongoTerminalCloseEvent event) {
        try {
            // 寻找节点
            ShellMongoTerminalTab terminalTab = this.getTerminalTab(event.data(), event.getDbName());
            // 移除节点
            if (terminalTab != null) {
                terminalTab.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获取函数tab
     *
     * @param dbItem       mongodb节点
     * @param functionName 函数名称
     * @return 结果
     */
    private ShellMongoFunctionDesignTab getMongoFunctionDesignTab(ShellMongoDatabaseTreeItem dbItem, String functionName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMongoFunctionDesignTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(functionName, tab1.functionName())) {
                return tab1;
            }
        }
        return null;
    }

    /**
     * 函数重命名事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onMongoFunctionRenamed(ShellMongoFunctionRenamedEvent event) {
        try {
            ShellMongoFunctionDesignTab tab = this.getMongoFunctionDesignTab(event.getDbItem(), event.functionName());
            if (tab != null) {
                tab.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 函数设计事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onMongoFunctionDesign(ShellMongoFunctionDesignEvent event) {
        try {
            ShellMongoFunctionDesignTab tab = this.getMongoFunctionDesignTab(event.getDbItem(), event.functionName());
            if (tab == null) {
                tab = new ShellMongoFunctionDesignTab();
                tab.init(event.data(), event.getDbItem());
                this.addTab(tab);
            }
            this.select(tab);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 函数删除事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onMongoFunctionDropped(ShellMongoFunctionDroppedEvent event) {
        try {
            ShellMongoFunctionDesignTab tab1 = this.getMongoFunctionDesignTab(event.getDbItem(), event.functionName());
            if (tab1 != null) {
                tab1.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 表重命名事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onMongoCollectionRenamed(ShellMongoCollectionRenamedEvent event) {
        try {
            ShellMongoCollectionRecordTab tab1 = this.getMongoCollectionRecordTab(event.getDbItem(), event.getNewCollectionName());
            if (tab1 != null) {
                tab1.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获取tab列表
     *
     * @param dbItem 数据节点
     * @return tab列表
     */
    private List<ShellMongoBaseTab> getBaseTabs(ShellMongoDatabaseTreeItem dbItem) {
        List<ShellMongoBaseTab> list = new ArrayList<>();
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMongoBaseTab tab1 && tab1.dbItem() == dbItem) {
                list.add(tab1);
            }
        }
        return list;
    }

    /**
     * 数据库关闭事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onMongoDatabaseClosed(ShellMongoDatabaseClosedEvent event) {
        this.removeTab(this.getBaseTabs(event.data()));
    }

    /**
     * 查询重命名事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onMongoQueryRenamed(ShellMongoQueryRenamedEvent event) {
        try {
            ShellMongoQueryMainTab tab = this.getMongoQueryMainTab(event.data());
            if (tab != null) {
                tab.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private ShellMongoUserViewTab getMongoUserViewTab(MongoUser user) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMongoUserViewTab tab1 && StringUtil.equals(tab1.userName(), user.getUser())) {
                return tab1;
            }
        }
        return null;
    }

    /**
     * 用户查看事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onMongoUserView(ShellMongoUserViewEvent event) {
        try {
            ShellMongoUserViewTab tab = this.getMongoUserViewTab(event.data());
            if (tab == null) {
                tab = new ShellMongoUserViewTab();
                tab.init(event.data(), event.getDbItem());
                this.addTab(tab);
            }
            this.select(tab);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 用户删除事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onMongoUserDelete(ShellMongoUserDeletedEvent event) {
        try {
            ShellMongoUserViewTab tab = this.getMongoUserViewTab(event.user());
            if (tab != null) {
                tab.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

