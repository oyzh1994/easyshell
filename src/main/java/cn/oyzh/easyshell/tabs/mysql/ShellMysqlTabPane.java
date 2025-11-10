package cn.oyzh.easyshell.tabs.mysql;

import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.tabs.mysql.home.ShellMysqlHomeTab;
import cn.oyzh.fx.gui.tabs.RichTabPane;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Tab;

/**
 *
 * @author oyzh
 * @since 2025-11-10
 */
public class ShellMysqlTabPane extends RichTabPane {

    private SimpleObjectProperty<ShellMysqlClient> clientProperty;

    public void setClient(ShellMysqlClient client) {
        this.clientProperty().set(client);
    }

    public ShellMysqlClient getClient() {
        return this.clientProperty == null ? null : this.clientProperty.get();
    }

    public SimpleObjectProperty<ShellMysqlClient> clientProperty() {
        if (this.clientProperty == null) {
            clientProperty = new SimpleObjectProperty<>();
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
    public ShellMysqlHomeTab getHomeTab() {
        return super.getTab(ShellMysqlHomeTab.class);
    }

    /**
     * 初始化主页tab
     */
    public void initHomeTab() {
        if (this.getHomeTab() == null) {
            super.addTab(new ShellMysqlHomeTab());
        }
    }

    /**
     * 关闭主页tab
     */
    public void closeHomeTab() {
        super.closeTab(ShellMysqlHomeTab.class);
    }
}
