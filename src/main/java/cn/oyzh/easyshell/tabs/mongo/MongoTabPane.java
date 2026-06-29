package cn.oyzh.easyshell.tabs.mongo;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.event.connection.ShellConnectionClosedEvent;
import cn.oyzh.easyshell.event.mongo.bucket.MongoBucketOpenEvent;
import cn.oyzh.easyshell.event.mongo.collection.MongoCollectionOpenEvent;
import cn.oyzh.easyshell.event.mongo.collection.MongoCollectionRenamedEvent;
import cn.oyzh.easyshell.event.mongo.database.MongoDatabaseClosedEvent;
import cn.oyzh.easyshell.event.mongo.function.ShellMongoFunctionDesignEvent;
import cn.oyzh.easyshell.event.mongo.function.ShellMongoFunctionDroppedEvent;
import cn.oyzh.easyshell.event.mongo.function.ShellMongoFunctionRenamedEvent;
import cn.oyzh.easyshell.event.mongo.query.MongoQueryAddEvent;
import cn.oyzh.easyshell.event.mongo.query.MongoQueryOpenEvent;
import cn.oyzh.easyshell.event.mongo.query.MongoQueryRenamedEvent;
import cn.oyzh.easyshell.event.mongo.terminal.MongoTerminalCloseEvent;
import cn.oyzh.easyshell.event.mongo.terminal.MongoTerminalOpenEvent;
import cn.oyzh.easyshell.event.mongo.window.ShellShowMessageEvent;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.tabs.mongo.bucket.MongoBucketRecordTab;
import cn.oyzh.easyshell.tabs.mongo.collection.MongoCollectionRecordTab;
import cn.oyzh.easyshell.tabs.mongo.function.ShellMongoFunctionDesignTab;
import cn.oyzh.easyshell.tabs.mongo.home.MongoHomeTab;
import cn.oyzh.easyshell.tabs.mongo.message.ShellMessageTab;
import cn.oyzh.easyshell.tabs.mongo.query.MongoQueryMainTab;
import cn.oyzh.easyshell.tabs.mongo.terminal.MongoTerminalTab;
import cn.oyzh.easyshell.trees.mongo.database.MongoDatabaseTreeItem;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.tabs.RichTabPane;
import cn.oyzh.fx.plus.event.FXEventListener;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Tab;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * db切换面板
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class MongoTabPane extends RichTabPane implements FXEventListener {

    private SimpleObjectProperty<ShellMongoClient> clientProperty;

    public void setClient(ShellMongoClient client) {
        this.clientProperty().set(client);
    }

    public ShellMongoClient getClient() {
        return this.clientProperty == null ? null : this.clientProperty.get();
    }

    public SimpleObjectProperty<ShellMongoClient> clientProperty() {
        if (this.clientProperty == null) {
            clientProperty = new SimpleObjectProperty<>();
        }
        return this.clientProperty;
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
    public MongoHomeTab getHomeTab() {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof MongoHomeTab homeTab) {
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
            super.addTab(new MongoHomeTab());
        }
    }

    /**
     * 关闭主页tab
     */
    public void closeHomeTab() {
        MongoHomeTab homeTab = this.getHomeTab();
        if (homeTab != null) {
            super.removeTab(homeTab);
        }
    }

    private MongoCollectionRecordTab getMongoCollectionRecordTab(MongoDatabaseTreeItem dbItem, String tableName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof MongoCollectionRecordTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(tableName, tab1.collectionName())) {
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
    private void onMongoCollectionOpen(MongoCollectionOpenEvent event) {
        MongoCollectionRecordTab tab = this.getMongoCollectionRecordTab(event.getDbItem(), event.collectionName());
        if (tab == null) {
            tab = new MongoCollectionRecordTab();
            super.addTab(tab);
        }
        // 选中节点
        this.select(tab);
        // 初始化节点
        tab.init(event.data());
    }

    private MongoBucketRecordTab getBucketRecordTab(MongoDatabaseTreeItem dbItem, String bucketName) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof MongoBucketRecordTab tab1 && tab1.dbItem() == dbItem && StringUtil.equals(bucketName, tab1.bucketName())) {
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
    private void onBucketOpen(MongoBucketOpenEvent event) {
        MongoBucketRecordTab tab = this.getBucketRecordTab(event.getDbItem(), event.bucketName());
        if (tab == null) {
            tab = new MongoBucketRecordTab();
            super.addTab(tab);
        }
        // 选中节点
        this.select(tab);
        // 初始化节点
        tab.init(event.data());
    }

    private MongoQueryMainTab getMongoQueryMainTab(String queryId) {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof MongoQueryMainTab tab1 && StringUtil.equals(tab1.queryId(), queryId)) {
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
    private void onMongoQueryAdd(MongoQueryAddEvent event) {
        try {
            MongoQueryMainTab tab = new MongoQueryMainTab();
            this.addTab(tab);
            this.select(tab);
            ShellQuery query = new ShellQuery();
            tab.init(query, event.data());
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
    private void onMongoQueryOpen(MongoQueryOpenEvent event) {
        try {
            MongoQueryMainTab tab = this.getMongoQueryMainTab(event.queryId());
            if (tab == null) {
                tab = new MongoQueryMainTab();
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
     * @param client redis客户端
     * @param dbName db索引
     * @return 终端tab
     */
    private MongoTerminalTab getTerminalTab(ShellMongoClient client, String dbName) {
        if (client != null) {
            for (Tab tab : this.getTabs()) {
                if (tab instanceof MongoTerminalTab tab1 && tab1.client() == client && Objects.equals(tab1.dbName(), dbName)) {
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
    private void terminalOpen(MongoTerminalOpenEvent event) {
        MongoTerminalTab terminalTab = this.getTerminalTab(event.data(), event.getDbName());
        if (terminalTab == null) {
            terminalTab = new MongoTerminalTab(event.data(), event.getDbName());
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
    private void terminalClose(MongoTerminalCloseEvent event) {
        try {
            // 寻找节点
            MongoTerminalTab terminalTab = this.getTerminalTab(event.data(), event.getDbName());
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
     * @param dbItem       db节点
     * @param functionName 函数名称
     * @return 结果
     */
    private ShellMongoFunctionDesignTab getFunctionDesignTab(MongoDatabaseTreeItem dbItem, String functionName) {
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
    private void onFunctionRenamed(ShellMongoFunctionRenamedEvent event) {
        try {
            ShellMongoFunctionDesignTab tab = this.getFunctionDesignTab(event.getDbItem(), event.functionName());
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
    private void onFunctionDesign(ShellMongoFunctionDesignEvent event) {
        try {
            ShellMongoFunctionDesignTab tab = this.getFunctionDesignTab(event.getDbItem(), event.functionName());
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
    private void onFunctionDropped(ShellMongoFunctionDroppedEvent event) {
        try {
            ShellMongoFunctionDesignTab tab1 = this.getFunctionDesignTab(event.getDbItem(), event.functionName());
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
    private void onCollectionRenamed(MongoCollectionRenamedEvent event) {
        try {
            MongoCollectionRecordTab tab1 = this.getMongoCollectionRecordTab(event.getDbItem(), event.getNewCollectionName());
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
    private List<MongoBaseTab> getBaseTabs(MongoDatabaseTreeItem dbItem) {
        List<MongoBaseTab> list = new ArrayList<>();
        for (Tab tab : this.getTabs()) {
            if (tab instanceof MongoBaseTab tab1 && tab1.dbItem() == dbItem) {
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
    private void onDatabaseClosed(MongoDatabaseClosedEvent event) {
        this.removeTab(this.getBaseTabs(event.data()));
    }

    /**
     * 获取tab列表
     *
     * @return tab列表
     */
    public List<MongoBaseTab> getMongoTabs() {
        List<MongoBaseTab> list = new ArrayList<>();
        for (Tab tab : this.getTabs()) {
            if (tab instanceof MongoBaseTab tab1) {
                list.add(tab1);
            }
        }
        return list;
    }

    /**
     * 连接关闭事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onConnectionClosed(ShellConnectionClosedEvent event) {
        this.removeTab(this.getMongoTabs());
    }

    /**
     * 查询重命名事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void onQueryRenamed(MongoQueryRenamedEvent event) {
        try {
            MongoQueryMainTab tab = this.getMongoQueryMainTab(event.data());
            if (tab != null) {
                tab.closeTab();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获取消息tab
     *
     * @return 结果
     */
    private ShellMessageTab getMessageTab() {
        for (Tab tab : this.getTabs()) {
            if (tab instanceof ShellMessageTab tab1) {
                return tab1;
            }
        }
        return null;
    }

    /**
     * 显示消息事件
     *
     * @param event 事件
     */
    @EventSubscribe
    public void showMessage(ShellShowMessageEvent event) {
        ShellMessageTab tab = this.getMessageTab();
        if (tab == null) {
            tab = new ShellMessageTab();
            super.addTab(tab);
        } else {
            tab.flushGraphic();
        }
        if (!tab.isSelected()) {
            this.select(tab);
        }
    }
}

